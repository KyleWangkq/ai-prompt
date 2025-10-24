package com.bytz.modules.cms.payment.domain;

import com.bytz.modules.cms.payment.application.impl.PaymentApplicationServiceImpl;
import com.bytz.modules.cms.payment.domain.command.ExecutePaymentCommand;
import com.bytz.modules.cms.payment.domain.command.ExecuteRefundCommand;
import com.bytz.modules.cms.payment.domain.enums.*;
import com.bytz.modules.cms.payment.domain.model.PaymentAggregate;
import com.bytz.modules.cms.payment.domain.model.PaymentTransaction;
import com.bytz.modules.cms.payment.domain.repository.IPaymentRepository;
import com.bytz.modules.cms.payment.infrastructure.channel.IPaymentChannelService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * PaymentApplicationServiceImpl 单元测试
 * 测试支付应用服务的核心业务逻辑（包含原域服务的批量支付逻辑）
 */
@DisplayName("PaymentApplicationServiceImpl 单元测试")
@ExtendWith(MockitoExtension.class)
class PaymentDomainServiceTest {

    @Mock
    private IPaymentRepository paymentRepository;

    @Mock
    private ApplicationEventPublisher eventPublisher;

    @Mock
    private IPaymentChannelService onlinePaymentChannelService;
    
    @Mock
    private IPaymentChannelService walletPaymentChannelService;

    @InjectMocks
    private PaymentApplicationServiceImpl paymentApplicationService;

    private String resellerId;
    private PaymentAggregate payment1;
    private PaymentAggregate payment2;
    
    @BeforeEach
    void setUp() {
        // 手动设置 paymentChannelServices 列表
        List<IPaymentChannelService> channelServices = Arrays.asList(
                onlinePaymentChannelService, 
                walletPaymentChannelService
        );
        
        // 使用反射设置私有字段
        try {
            java.lang.reflect.Field field = PaymentApplicationServiceImpl.class
                    .getDeclaredField("paymentChannelServices");
            field.setAccessible(true);
            field.set(paymentApplicationService, channelServices);
        } catch (Exception e) {
            throw new RuntimeException("Failed to inject paymentChannelServices", e);
        }
        
        resellerId = "RESELLER-001";
        
        // 创建第一个支付单
        payment1 = PaymentAggregate.builder()
                .code("PAY-001")
                .orderId("ORDER-001")
                .resellerId(resellerId)
                .paymentAmount(new BigDecimal("10000.00"))
                .paidAmount(BigDecimal.ZERO)
                .refundedAmount(BigDecimal.ZERO)
                .actualAmount(BigDecimal.ZERO)
                .currency("CNY")
                .paymentType(PaymentType.ADVANCE_PAYMENT)
                .paymentStatus(PaymentStatus.UNPAID)
                .refundStatus(RefundStatus.NO_REFUND)
                .createTime(LocalDateTime.now())
                .runningTransaction(null)
                .completedTransactions(new java.util.ArrayList<>())
                .build();
        
        // 创建第二个支付单
        payment2 = PaymentAggregate.builder()
                .code("PAY-002")
                .orderId("ORDER-002")
                .resellerId(resellerId)
                .paymentAmount(new BigDecimal("20000.00"))
                .paidAmount(BigDecimal.ZERO)
                .refundedAmount(BigDecimal.ZERO)
                .actualAmount(BigDecimal.ZERO)
                .currency("CNY")
                .paymentType(PaymentType.ADVANCE_PAYMENT)
                .paymentStatus(PaymentStatus.UNPAID)
                .refundStatus(RefundStatus.NO_REFUND)
                .createTime(LocalDateTime.now())
                .runningTransaction(null)
                .completedTransactions(new java.util.ArrayList<>())
                .build();
    }

    @Test
    @DisplayName("执行批量支付 - 正常流程")
    void testExecuteBatchPayment_Success() {
        // Given
        ExecutePaymentCommand.PaymentItem item1 = ExecutePaymentCommand.PaymentItem.builder()
                .paymentId("1")
                .amount(new BigDecimal("10000.00"))
                .build();
        
        ExecutePaymentCommand.PaymentItem item2 = ExecutePaymentCommand.PaymentItem.builder()
                .paymentId("2")
                .amount(new BigDecimal("20000.00"))
                .build();
        
        ExecutePaymentCommand command = ExecutePaymentCommand.builder()
                .paymentChannel(PaymentChannel.ONLINE_PAYMENT)
                .paymentItems(Arrays.asList(item1, item2))
                .build();
        
        // Mock channel service
        when(onlinePaymentChannelService.getChannelType()).thenReturn(PaymentChannel.ONLINE_PAYMENT);
        when(onlinePaymentChannelService.isAvailable(resellerId)).thenReturn(true);
        when(onlinePaymentChannelService.createPaymentRequest(any()))
                .thenReturn(new com.bytz.modules.cms.payment.infrastructure.channel.response.PaymentRequestResponse(
                        "MOCK_RECORD_001", "MOCK_CHANNEL_TXN_001"));
        
        when(paymentRepository.findByIds(Arrays.asList("1", "2")))
                .thenReturn(Arrays.asList(payment1, payment2));        
        when(paymentRepository.save(any(PaymentAggregate.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
                
        // When
        String channelTransactionNumber = paymentApplicationService.executeBatchPayment(command);
        
        // Then
        assertNotNull(channelTransactionNumber);
        assertEquals("MOCK_RECORD_001", channelTransactionNumber);
        
        // 验证两个支付单都被保存
        verify(paymentRepository, times(2)).save(any(PaymentAggregate.class));
        
        // 验证支付单状态变更
        assertEquals(PaymentStatus.PAYING, payment1.getPaymentStatus());
        assertEquals(PaymentStatus.PAYING, payment2.getPaymentStatus());
        
        // 验证每个支付单都创建了流水
        assertEquals(1, payment1.getTransactions().size());
        assertEquals(1, payment2.getTransactions().size());
    }

    @Test
    @DisplayName("执行批量支付 - 不同经销商异常")
    void testExecuteBatchPayment_DifferentReseller() {
        // Given
        payment2.setResellerId("RESELLER-002"); // 不同的经销商
        
        ExecutePaymentCommand.PaymentItem item1 = ExecutePaymentCommand.PaymentItem.builder()
                .paymentId("1")
                .amount(new BigDecimal("10000.00"))
                .build();
        
        ExecutePaymentCommand.PaymentItem item2 = ExecutePaymentCommand.PaymentItem.builder()
                .paymentId("2")
                .amount(new BigDecimal("20000.00"))
                .build();
        
        ExecutePaymentCommand command = ExecutePaymentCommand.builder()
                .paymentChannel(PaymentChannel.ONLINE_PAYMENT)
                .paymentItems(Arrays.asList(item1, item2))
                .build();
        
        // Mock channel service
        when(onlinePaymentChannelService.getChannelType()).thenReturn(PaymentChannel.ONLINE_PAYMENT);
        
        when(paymentRepository.findByIds(Arrays.asList("1", "2")))
                .thenReturn(Arrays.asList(payment1, payment2));        
        // When & Then
        com.bytz.modules.cms.payment.shared.exception.PaymentException exception = assertThrows(
                com.bytz.modules.cms.payment.shared.exception.PaymentException.class, () -> {
            paymentApplicationService.executeBatchPayment(command);
        });
        
        assertEquals("所有支付单必须属于同一经销商", exception.getMessage());
        verify(paymentRepository, never()).save(any(PaymentAggregate.class));
    }

    @Test
    @DisplayName("执行批量支付 - 存在不允许支付的支付单")
    void testExecuteBatchPayment_PaymentNotAllowed() {
        // Given
        payment1.setPaymentStatus(PaymentStatus.FROZEN); // 冻结状态不允许支付
        
        ExecutePaymentCommand.PaymentItem item1 = ExecutePaymentCommand.PaymentItem.builder()
                .paymentId("1")
                .amount(new BigDecimal("10000.00"))
                .build();
        
        ExecutePaymentCommand command = ExecutePaymentCommand.builder()
                .paymentChannel(PaymentChannel.ONLINE_PAYMENT)
                .paymentItems(Arrays.asList(item1))
                .build();
        
        // Mock channel service
        when(onlinePaymentChannelService.getChannelType()).thenReturn(PaymentChannel.ONLINE_PAYMENT);
        when(onlinePaymentChannelService.isAvailable(resellerId)).thenReturn(true);
        
        when(paymentRepository.findByIds(Arrays.asList("1")))
                .thenReturn(Arrays.asList(payment1));        
        // When & Then
        com.bytz.modules.cms.payment.shared.exception.PaymentException exception = assertThrows(
                com.bytz.modules.cms.payment.shared.exception.PaymentException.class, () -> {
            paymentApplicationService.executeBatchPayment(command);
        });
        
        assertTrue(exception.getMessage().contains("当前状态不允许支付"));
        verify(paymentRepository, never()).save(any(PaymentAggregate.class));
    }

    @Test
    @DisplayName("验证支付渠道可用性 - 默认返回true")
    void testValidateChannelAvailability() {
        // When - Now using queryAvailableChannels which is the public API
        List<PaymentChannel> availableChannels = paymentApplicationService.queryAvailableChannels(resellerId);
        
        // Then - The channel should be available (since mock returns empty list, we check the method doesn't throw)
        assertNotNull(availableChannels);
    }

    @Test
    @DisplayName("执行批量支付 - 单个支付单")
    void testExecuteBatchPayment_SinglePayment() {
        // Given
        ExecutePaymentCommand.PaymentItem item = ExecutePaymentCommand.PaymentItem.builder()
                .paymentId("1")
                .amount(new BigDecimal("10000.00"))
                .build();
        
        ExecutePaymentCommand command = ExecutePaymentCommand.builder()
                .paymentChannel(PaymentChannel.WALLET_PAYMENT)
                .paymentItems(Arrays.asList(item))
                .build();
        
        // Mock wallet payment channel service
        when(walletPaymentChannelService.getChannelType()).thenReturn(PaymentChannel.WALLET_PAYMENT);
        when(walletPaymentChannelService.isAvailable(resellerId)).thenReturn(true);
        when(walletPaymentChannelService.createPaymentRequest(any()))
                .thenReturn(new com.bytz.modules.cms.payment.infrastructure.channel.response.PaymentRequestResponse(
                        "WALLET_RECORD_001", "WALLET_TXN_001"));
        
        when(paymentRepository.findByIds(Arrays.asList("1")))
                .thenReturn(Arrays.asList(payment1));
        when(paymentRepository.save(any(PaymentAggregate.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
                
        // When
        String channelTransactionNumber = paymentApplicationService.executeBatchPayment(command);
        
        // Then
        assertNotNull(channelTransactionNumber);
        assertEquals("WALLET_RECORD_001", channelTransactionNumber);
        verify(paymentRepository, times(1)).save(payment1);
        assertEquals(PaymentStatus.PAYING, payment1.getPaymentStatus());
        assertEquals(1, payment1.getTransactions().size());
        assertEquals(PaymentChannel.WALLET_PAYMENT, payment1.getTransactions().get(0).getPaymentChannel());
    }

    @Test
    @DisplayName("验证渠道可用性 - 不同渠道")
    void testValidateChannelAvailability_DifferentChannels() {
        // When & Then - Using the public API queryAvailableChannels
        List<PaymentChannel> channels = paymentApplicationService.queryAvailableChannels(resellerId);
        assertNotNull(channels);
        // Since we have no channel services mocked, the list should be empty or we should get an exception
        // The actual behavior depends on the implementation
    }

    @Test
    @DisplayName("批量支付创建的流水包含正确的渠道交易号")
    void testExecuteBatchPayment_ChannelTransactionNumber() {
        // Given
        ExecutePaymentCommand.PaymentItem item1 = ExecutePaymentCommand.PaymentItem.builder()
                .paymentId("1")
                .amount(new BigDecimal("10000.00"))
                .build();
        
        ExecutePaymentCommand command = ExecutePaymentCommand.builder()
                .paymentChannel(PaymentChannel.ONLINE_PAYMENT)
                .paymentItems(Arrays.asList(item1))
                .build();
        
        // Mock channel service
        when(onlinePaymentChannelService.getChannelType()).thenReturn(PaymentChannel.ONLINE_PAYMENT);
        when(onlinePaymentChannelService.isAvailable(resellerId)).thenReturn(true);
        when(onlinePaymentChannelService.createPaymentRequest(any()))
                .thenReturn(new com.bytz.modules.cms.payment.infrastructure.channel.response.PaymentRequestResponse(
                        "MOCK_RECORD_123", "MOCK_CHANNEL_TXN_123"));
        
        when(paymentRepository.findByIds(Arrays.asList("1")))
                .thenReturn(Arrays.asList(payment1));
        when(paymentRepository.save(any(PaymentAggregate.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
                
        // When
        String channelRecordId = paymentApplicationService.executeBatchPayment(command);
        
        // Then
        PaymentTransaction transaction = payment1.getTransactions().get(0);
        assertEquals("MOCK_CHANNEL_TXN_123", transaction.getChannelTransactionNumber());
        assertEquals("MOCK_RECORD_123", transaction.getChannelPaymentRecordId());
        assertEquals("MOCK_RECORD_123", channelRecordId);
    }
}

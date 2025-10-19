package com.bytz.cms.payment.domain;

import com.bytz.cms.payment.domain.command.ExecutePaymentCommand;
import com.bytz.cms.payment.domain.command.ExecuteRefundCommand;
import com.bytz.cms.payment.domain.entity.PaymentTransactionEntity;
import com.bytz.cms.payment.domain.enums.*;
import com.bytz.cms.payment.domain.model.PaymentAggregate;
import com.bytz.cms.payment.domain.repository.IPaymentRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * PaymentDomainService 单元测试
 * 测试支付领域服务的核心业务逻辑
 */
@DisplayName("PaymentDomainService 单元测试")
@ExtendWith(MockitoExtension.class)
class PaymentDomainServiceTest {

    @Mock
    private IPaymentRepository paymentRepository;

    @InjectMocks
    private PaymentDomainService paymentDomainService;

    private String resellerId;
    private PaymentAggregate payment1;
    private PaymentAggregate payment2;
    
    @BeforeEach
    void setUp() {
        resellerId = "RESELLER-001";
        
        // 创建第一个支付单
        payment1 = PaymentAggregate.builder()
                .id("PAY-001")
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
                .transactions(new java.util.ArrayList<>())
                .build();
        
        // 创建第二个支付单
        payment2 = PaymentAggregate.builder()
                .id("PAY-002")
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
                .transactions(new java.util.ArrayList<>())
                .build();
    }

    @Test
    @DisplayName("执行批量支付 - 正常流程")
    void testExecuteBatchPayment_Success() {
        // Given
        ExecutePaymentCommand.PaymentItem item1 = ExecutePaymentCommand.PaymentItem.builder()
                .paymentId("PAY-001")
                .amount(new BigDecimal("10000.00"))
                .build();
        
        ExecutePaymentCommand.PaymentItem item2 = ExecutePaymentCommand.PaymentItem.builder()
                .paymentId("PAY-002")
                .amount(new BigDecimal("20000.00"))
                .build();
        
        ExecutePaymentCommand command = ExecutePaymentCommand.builder()
                .paymentChannel(PaymentChannel.ONLINE_PAYMENT)
                .paymentItems(Arrays.asList(item1, item2))
                .build();
        
        when(paymentRepository.findById("PAY-001")).thenReturn(payment1);
        when(paymentRepository.findById("PAY-002")).thenReturn(payment2);
        
        // When
        String channelTransactionNumber = paymentDomainService.executeBatchPayment(command);
        
        // Then
        assertNotNull(channelTransactionNumber);
        assertTrue(channelTransactionNumber.startsWith("MOCK_"));
        
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
                .paymentId("PAY-001")
                .amount(new BigDecimal("10000.00"))
                .build();
        
        ExecutePaymentCommand.PaymentItem item2 = ExecutePaymentCommand.PaymentItem.builder()
                .paymentId("PAY-002")
                .amount(new BigDecimal("20000.00"))
                .build();
        
        ExecutePaymentCommand command = ExecutePaymentCommand.builder()
                .paymentChannel(PaymentChannel.ONLINE_PAYMENT)
                .paymentItems(Arrays.asList(item1, item2))
                .build();
        
        when(paymentRepository.findById("PAY-001")).thenReturn(payment1);
        when(paymentRepository.findById("PAY-002")).thenReturn(payment2);
        
        // When & Then
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            paymentDomainService.executeBatchPayment(command);
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
                .paymentId("PAY-001")
                .amount(new BigDecimal("10000.00"))
                .build();
        
        ExecutePaymentCommand command = ExecutePaymentCommand.builder()
                .paymentChannel(PaymentChannel.ONLINE_PAYMENT)
                .paymentItems(Arrays.asList(item1))
                .build();
        
        when(paymentRepository.findById("PAY-001")).thenReturn(payment1);
        
        // When & Then
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            paymentDomainService.executeBatchPayment(command);
        });
        
        assertEquals("存在不允许支付的支付单", exception.getMessage());
        verify(paymentRepository, never()).save(any(PaymentAggregate.class));
    }

    @Test
    @DisplayName("验证支付渠道可用性 - 默认返回true")
    void testValidateChannelAvailability() {
        // When
        boolean isAvailable = paymentDomainService.validateChannelAvailability(
                PaymentChannel.ONLINE_PAYMENT, 
                resellerId
        );
        
        // Then
        assertTrue(isAvailable);
    }

    @Test
    @DisplayName("选择退款流水 - 指定原流水ID")
    void testSelectTransactionForRefund_SpecifiedTransaction() {
        // Given
        PaymentTransactionEntity transaction = PaymentTransactionEntity.builder()
                .id("TXN-001")
                .paymentId("PAY-001")
                .transactionType(TransactionType.PAYMENT)
                .transactionStatus(TransactionStatus.SUCCESS)
                .transactionAmount(new BigDecimal("10000.00"))
                .paymentChannel(PaymentChannel.ONLINE_PAYMENT)
                .createTime(LocalDateTime.now())
                .build();
        
        payment1.getTransactions().add(transaction);
        payment1.setPaidAmount(new BigDecimal("10000.00"));
        payment1.setPaymentStatus(PaymentStatus.PAID);
        
        ExecuteRefundCommand command = ExecuteRefundCommand.builder()
                .paymentId("PAY-001")
                .refundAmount(new BigDecimal("5000.00"))
                .originalTransactionId("TXN-001")
                .refundOrderId("REFUND-001")
                .refundReason("客户申请退款")
                .build();
        
        // When
        String selectedTransactionId = paymentDomainService.selectTransactionForRefund(payment1, command);
        
        // Then
        assertEquals("TXN-001", selectedTransactionId);
    }

    @Test
    @DisplayName("选择退款流水 - 未指定原流水ID，选择最新的成功流水")
    void testSelectTransactionForRefund_SelectLatest() {
        // Given
        PaymentTransactionEntity transaction1 = PaymentTransactionEntity.builder()
                .id("TXN-001")
                .paymentId("PAY-001")
                .transactionType(TransactionType.PAYMENT)
                .transactionStatus(TransactionStatus.SUCCESS)
                .transactionAmount(new BigDecimal("5000.00"))
                .paymentChannel(PaymentChannel.ONLINE_PAYMENT)
                .createTime(LocalDateTime.now().minusHours(2))
                .build();
        
        PaymentTransactionEntity transaction2 = PaymentTransactionEntity.builder()
                .id("TXN-002")
                .paymentId("PAY-001")
                .transactionType(TransactionType.PAYMENT)
                .transactionStatus(TransactionStatus.SUCCESS)
                .transactionAmount(new BigDecimal("5000.00"))
                .paymentChannel(PaymentChannel.WALLET_PAYMENT)
                .createTime(LocalDateTime.now().minusHours(1))
                .build();
        
        payment1.getTransactions().add(transaction1);
        payment1.getTransactions().add(transaction2);
        payment1.setPaidAmount(new BigDecimal("10000.00"));
        payment1.setPaymentStatus(PaymentStatus.PAID);
        
        ExecuteRefundCommand command = ExecuteRefundCommand.builder()
                .paymentId("PAY-001")
                .refundAmount(new BigDecimal("3000.00"))
                .originalTransactionId(null) // 未指定
                .refundOrderId("REFUND-001")
                .refundReason("客户申请退款")
                .build();
        
        // When
        String selectedTransactionId = paymentDomainService.selectTransactionForRefund(payment1, command);
        
        // Then
        assertEquals("TXN-002", selectedTransactionId); // 应该选择最新的
    }

    @Test
    @DisplayName("选择退款流水 - 无可用支付流水")
    void testSelectTransactionForRefund_NoAvailableTransaction() {
        // Given
        payment1.setPaymentStatus(PaymentStatus.UNPAID);
        
        ExecuteRefundCommand command = ExecuteRefundCommand.builder()
                .paymentId("PAY-001")
                .refundAmount(new BigDecimal("3000.00"))
                .originalTransactionId(null)
                .refundOrderId("REFUND-001")
                .refundReason("测试退款")
                .build();
        
        // When & Then
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            paymentDomainService.selectTransactionForRefund(payment1, command);
        });
        
        assertEquals("未找到可用的支付流水", exception.getMessage());
    }

    @Test
    @DisplayName("执行批量支付 - 单个支付单")
    void testExecuteBatchPayment_SinglePayment() {
        // Given
        ExecutePaymentCommand.PaymentItem item = ExecutePaymentCommand.PaymentItem.builder()
                .paymentId("PAY-001")
                .amount(new BigDecimal("10000.00"))
                .build();
        
        ExecutePaymentCommand command = ExecutePaymentCommand.builder()
                .paymentChannel(PaymentChannel.WALLET_PAYMENT)
                .paymentItems(Arrays.asList(item))
                .build();
        
        when(paymentRepository.findById("PAY-001")).thenReturn(payment1);
        
        // When
        String channelTransactionNumber = paymentDomainService.executeBatchPayment(command);
        
        // Then
        assertNotNull(channelTransactionNumber);
        verify(paymentRepository, times(1)).save(payment1);
        assertEquals(PaymentStatus.PAYING, payment1.getPaymentStatus());
        assertEquals(1, payment1.getTransactions().size());
        assertEquals(PaymentChannel.WALLET_PAYMENT, payment1.getTransactions().get(0).getPaymentChannel());
    }

    @Test
    @DisplayName("验证渠道可用性 - 不同渠道")
    void testValidateChannelAvailability_DifferentChannels() {
        // When & Then
        assertTrue(paymentDomainService.validateChannelAvailability(PaymentChannel.ONLINE_PAYMENT, resellerId));
        assertTrue(paymentDomainService.validateChannelAvailability(PaymentChannel.WALLET_PAYMENT, resellerId));
        assertTrue(paymentDomainService.validateChannelAvailability(PaymentChannel.WIRE_TRANSFER, resellerId));
        assertTrue(paymentDomainService.validateChannelAvailability(PaymentChannel.CREDIT_ACCOUNT, resellerId));
    }

    @Test
    @DisplayName("批量支付创建的流水包含正确的渠道交易号")
    void testExecuteBatchPayment_ChannelTransactionNumber() {
        // Given
        ExecutePaymentCommand.PaymentItem item1 = ExecutePaymentCommand.PaymentItem.builder()
                .paymentId("PAY-001")
                .amount(new BigDecimal("10000.00"))
                .build();
        
        ExecutePaymentCommand command = ExecutePaymentCommand.builder()
                .paymentChannel(PaymentChannel.ONLINE_PAYMENT)
                .paymentItems(Arrays.asList(item1))
                .build();
        
        when(paymentRepository.findById("PAY-001")).thenReturn(payment1);
        
        // When
        String channelTransactionNumber = paymentDomainService.executeBatchPayment(command);
        
        // Then
        PaymentTransactionEntity transaction = payment1.getTransactions().get(0);
        assertEquals(channelTransactionNumber, transaction.getChannelTransactionNumber());
        assertTrue(channelTransactionNumber.startsWith("MOCK_"));
    }
}

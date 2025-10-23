package com.bytz.modules.cms.payment.infrastructure.channel;

import com.bytz.modules.cms.payment.domain.enums.PaymentChannel;
import com.bytz.modules.cms.payment.infrastructure.channel.command.CreatePaymentRequestCommand;
import com.bytz.modules.cms.payment.infrastructure.channel.command.CreateRefundRequestCommand;
import com.bytz.modules.cms.payment.infrastructure.channel.command.QueryPaymentStatusCommand;
import com.bytz.modules.cms.payment.infrastructure.channel.command.QueryRefundStatusCommand;
import com.bytz.modules.cms.payment.infrastructure.channel.impl.CreditAccountChannelService;
import com.bytz.modules.cms.payment.infrastructure.channel.impl.OnlinePaymentChannelService;
import com.bytz.modules.cms.payment.infrastructure.channel.impl.WalletPaymentChannelService;
import com.bytz.modules.cms.payment.infrastructure.channel.impl.WireTransferChannelService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 支付渠道服务单元测试
 * 测试支付渠道接口的核心功能
 */
@DisplayName("支付渠道服务单元测试")
class PaymentChannelServiceTest {

    private WalletPaymentChannelService walletChannelService;
    private WireTransferChannelService wireTransferChannelService;
    private CreditAccountChannelService creditAccountChannelService;
    private OnlinePaymentChannelService onlinePaymentChannelService;

    private String resellerId;
    private String channelPaymentRecordId;
    private BigDecimal paymentAmount;

    @BeforeEach
    void setUp() {
        walletChannelService = new WalletPaymentChannelService();
        wireTransferChannelService = new WireTransferChannelService();
        creditAccountChannelService = new CreditAccountChannelService();
        onlinePaymentChannelService = new OnlinePaymentChannelService();

        resellerId = "RESELLER-001";
        channelPaymentRecordId = "CHANNEL_RECORD_001";
        paymentAmount = new BigDecimal("5000.00");
    }

    @Test
    @DisplayName("测试钱包支付渠道 - 创建支付请求使用命令对象")
    void testWalletChannel_CreatePaymentRequest_WithCommand() {
        // Given
        Map<String, Object> channelParams = new HashMap<>();
        channelParams.put("accountId", "ACC-001");
        
        CreatePaymentRequestCommand command = CreatePaymentRequestCommand.builder()
                .totalAmount(paymentAmount)
                .channelPaymentRecordId(channelPaymentRecordId)
                .resellerId(resellerId)
                .channelParams(channelParams)
                .build();

        // When
        String channelTransactionNumber = walletChannelService.createPaymentRequest(command);

        // Then
        assertNotNull(channelTransactionNumber);
        assertTrue(channelTransactionNumber.startsWith("WALLET_"));
    }

    @Test
    @DisplayName("测试钱包支付渠道 - 查询支付状态使用命令对象")
    void testWalletChannel_QueryPaymentStatus_WithCommand() {
        // Given
        QueryPaymentStatusCommand command = QueryPaymentStatusCommand.builder()
                .channelTransactionNumber("WALLET_12345")
                .channelPaymentRecordId(channelPaymentRecordId)
                .resellerId(resellerId)
                .build();

        // When
        String status = walletChannelService.queryPaymentStatus(command);

        // Then
        assertNotNull(status);
        assertEquals("SUCCESS", status);
    }

    @Test
    @DisplayName("测试钱包支付渠道 - 创建退款请求使用命令对象")
    void testWalletChannel_CreateRefundRequest_WithCommand() {
        // Given
        CreateRefundRequestCommand command = CreateRefundRequestCommand.builder()
                .channelTransactionNumber("WALLET_12345")
                .refundAmount(new BigDecimal("1000.00"))
                .refundReason("客户取消订单")
                .channelPaymentRecordId(channelPaymentRecordId)
                .resellerId(resellerId)
                .build();

        // When
        String refundTransactionNumber = walletChannelService.createRefundRequest(command);

        // Then
        assertNotNull(refundTransactionNumber);
        assertTrue(refundTransactionNumber.startsWith("REFUND_"));
    }

    @Test
    @DisplayName("测试钱包支付渠道 - 查询退款状态使用命令对象")
    void testWalletChannel_QueryRefundStatus_WithCommand() {
        // Given
        QueryRefundStatusCommand command = QueryRefundStatusCommand.builder()
                .refundTransactionNumber("REFUND_12345")
                .channelPaymentRecordId(channelPaymentRecordId)
                .resellerId(resellerId)
                .build();

        // When
        String status = walletChannelService.queryRefundStatus(command);

        // Then
        assertNotNull(status);
        assertEquals("SUCCESS", status);
    }

    @Test
    @DisplayName("测试钱包支付渠道 - 检查可用性需要经销商ID")
    void testWalletChannel_IsAvailable_WithResellerId() {
        // When
        boolean available = walletChannelService.isAvailable(resellerId);

        // Then
        assertTrue(available);
    }

    @Test
    @DisplayName("测试所有渠道类型匹配")
    void testAllChannels_GetChannelType() {
        // When & Then
        assertEquals(PaymentChannel.WALLET_PAYMENT, walletChannelService.getChannelType());
        assertEquals(PaymentChannel.WIRE_TRANSFER, wireTransferChannelService.getChannelType());
        assertEquals(PaymentChannel.CREDIT_ACCOUNT, creditAccountChannelService.getChannelType());
        assertEquals(PaymentChannel.ONLINE_PAYMENT, onlinePaymentChannelService.getChannelType());
    }

    @Test
    @DisplayName("测试电汇渠道 - 创建支付请求")
    void testWireTransferChannel_CreatePaymentRequest() {
        // Given
        CreatePaymentRequestCommand command = CreatePaymentRequestCommand.builder()
                .totalAmount(paymentAmount)
                .channelPaymentRecordId(channelPaymentRecordId)
                .resellerId(resellerId)
                .channelParams(new HashMap<>())
                .build();

        // When
        String channelTransactionNumber = wireTransferChannelService.createPaymentRequest(command);

        // Then
        assertNotNull(channelTransactionNumber);
        assertTrue(channelTransactionNumber.startsWith("WIRE_"));
    }

    @Test
    @DisplayName("测试信用账户渠道 - 创建支付请求")
    void testCreditAccountChannel_CreatePaymentRequest() {
        // Given
        CreatePaymentRequestCommand command = CreatePaymentRequestCommand.builder()
                .totalAmount(paymentAmount)
                .channelPaymentRecordId(channelPaymentRecordId)
                .resellerId(resellerId)
                .channelParams(new HashMap<>())
                .build();

        // When
        String channelTransactionNumber = creditAccountChannelService.createPaymentRequest(command);

        // Then
        assertNotNull(channelTransactionNumber);
        assertTrue(channelTransactionNumber.startsWith("CREDIT_"));
    }

    @Test
    @DisplayName("测试线上支付渠道 - 创建支付请求")
    void testOnlinePaymentChannel_CreatePaymentRequest() {
        // Given
        CreatePaymentRequestCommand command = CreatePaymentRequestCommand.builder()
                .totalAmount(paymentAmount)
                .channelPaymentRecordId(channelPaymentRecordId)
                .resellerId(resellerId)
                .channelParams(new HashMap<>())
                .build();

        // When
        String channelTransactionNumber = onlinePaymentChannelService.createPaymentRequest(command);

        // Then
        assertNotNull(channelTransactionNumber);
        assertTrue(channelTransactionNumber.startsWith("ONLINE_"));
    }

    @Test
    @DisplayName("测试所有渠道 - 检查可用性接受经销商ID")
    void testAllChannels_IsAvailable_WithResellerId() {
        // When & Then
        assertTrue(walletChannelService.isAvailable(resellerId));
        assertTrue(wireTransferChannelService.isAvailable(resellerId));
        assertTrue(creditAccountChannelService.isAvailable(resellerId));
        assertTrue(onlinePaymentChannelService.isAvailable(resellerId));
    }

    @Test
    @DisplayName("测试命令对象 - 创建支付请求命令包含渠道支付记录ID")
    void testCreatePaymentRequestCommand_ContainsChannelPaymentRecordId() {
        // Given
        CreatePaymentRequestCommand command = CreatePaymentRequestCommand.builder()
                .totalAmount(paymentAmount)
                .channelPaymentRecordId(channelPaymentRecordId)
                .resellerId(resellerId)
                .channelParams(new HashMap<>())
                .build();

        // Then
        assertEquals(paymentAmount, command.getTotalAmount());
        assertEquals(channelPaymentRecordId, command.getChannelPaymentRecordId());
        assertEquals(resellerId, command.getResellerId());
        assertNotNull(command.getChannelParams());
    }

    @Test
    @DisplayName("测试命令对象 - 查询支付状态命令包含渠道支付记录ID")
    void testQueryPaymentStatusCommand_ContainsChannelPaymentRecordId() {
        // Given
        QueryPaymentStatusCommand command = QueryPaymentStatusCommand.builder()
                .channelTransactionNumber("WALLET_12345")
                .channelPaymentRecordId(channelPaymentRecordId)
                .resellerId(resellerId)
                .build();

        // Then
        assertEquals("WALLET_12345", command.getChannelTransactionNumber());
        assertEquals(channelPaymentRecordId, command.getChannelPaymentRecordId());
        assertEquals(resellerId, command.getResellerId());
    }

    @Test
    @DisplayName("测试命令对象 - 创建退款请求命令包含渠道支付记录ID")
    void testCreateRefundRequestCommand_ContainsChannelPaymentRecordId() {
        // Given
        CreateRefundRequestCommand command = CreateRefundRequestCommand.builder()
                .channelTransactionNumber("WALLET_12345")
                .refundAmount(new BigDecimal("1000.00"))
                .refundReason("客户取消订单")
                .channelPaymentRecordId(channelPaymentRecordId)
                .resellerId(resellerId)
                .build();

        // Then
        assertEquals("WALLET_12345", command.getChannelTransactionNumber());
        assertEquals(new BigDecimal("1000.00"), command.getRefundAmount());
        assertEquals("客户取消订单", command.getRefundReason());
        assertEquals(channelPaymentRecordId, command.getChannelPaymentRecordId());
        assertEquals(resellerId, command.getResellerId());
    }

    @Test
    @DisplayName("测试命令对象 - 查询退款状态命令包含渠道支付记录ID")
    void testQueryRefundStatusCommand_ContainsChannelPaymentRecordId() {
        // Given
        QueryRefundStatusCommand command = QueryRefundStatusCommand.builder()
                .refundTransactionNumber("REFUND_12345")
                .channelPaymentRecordId(channelPaymentRecordId)
                .resellerId(resellerId)
                .build();

        // Then
        assertEquals("REFUND_12345", command.getRefundTransactionNumber());
        assertEquals(channelPaymentRecordId, command.getChannelPaymentRecordId());
        assertEquals(resellerId, command.getResellerId());
    }
}

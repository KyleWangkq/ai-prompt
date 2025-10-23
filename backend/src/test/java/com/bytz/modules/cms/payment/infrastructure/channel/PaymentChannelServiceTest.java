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
import com.bytz.modules.cms.payment.infrastructure.channel.response.PaymentRequestResponse;
import com.bytz.modules.cms.payment.infrastructure.channel.response.RefundRequestResponse;
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
    @DisplayName("测试钱包支付渠道 - 创建支付请求返回渠道支付记录ID和交易号")
    void testWalletChannel_CreatePaymentRequest_ReturnsResponse() {
        // Given
        Map<String, Object> channelParams = new HashMap<>();
        channelParams.put("accountId", "ACC-001");
        
        CreatePaymentRequestCommand command = CreatePaymentRequestCommand.builder()
                .totalAmount(paymentAmount)
                .resellerId(resellerId)
                .channelParams(channelParams)
                .build();

        // When
        PaymentRequestResponse response = walletChannelService.createPaymentRequest(command);

        // Then
        assertNotNull(response);
        assertNotNull(response.getChannelPaymentRecordId());
        assertNotNull(response.getChannelTransactionNumber());
        assertTrue(response.getChannelPaymentRecordId().startsWith("WALLET_RECORD_"));
        assertTrue(response.getChannelTransactionNumber().startsWith("WALLET_TXN_"));
    }

    @Test
    @DisplayName("测试钱包支付渠道 - 查询支付状态使用渠道支付记录ID")
    void testWalletChannel_QueryPaymentStatus_WithChannelPaymentRecordId() {
        // Given
        QueryPaymentStatusCommand command = QueryPaymentStatusCommand.builder()
                .channelTransactionNumber("WALLET_TXN_12345")
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
    @DisplayName("测试钱包支付渠道 - 创建退款请求返回渠道支付记录ID和退款流水号")
    void testWalletChannel_CreateRefundRequest_ReturnsResponse() {
        // Given
        CreateRefundRequestCommand command = CreateRefundRequestCommand.builder()
                .channelTransactionNumber("WALLET_TXN_12345")
                .originalChannelPaymentRecordId(channelPaymentRecordId)
                .refundAmount(new BigDecimal("1000.00"))
                .refundReason("客户取消订单")
                .resellerId(resellerId)
                .build();

        // When
        RefundRequestResponse response = walletChannelService.createRefundRequest(command);

        // Then
        assertNotNull(response);
        assertNotNull(response.getChannelPaymentRecordId());
        assertNotNull(response.getRefundTransactionNumber());
        assertTrue(response.getChannelPaymentRecordId().startsWith("WALLET_REFUND_RECORD_"));
        assertTrue(response.getRefundTransactionNumber().startsWith("WALLET_REFUND_TXN_"));
    }

    @Test
    @DisplayName("测试钱包支付渠道 - 查询退款状态使用渠道支付记录ID")
    void testWalletChannel_QueryRefundStatus_WithChannelPaymentRecordId() {
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
    @DisplayName("测试电汇渠道 - 创建支付请求返回响应对象")
    void testWireTransferChannel_CreatePaymentRequest() {
        // Given
        CreatePaymentRequestCommand command = CreatePaymentRequestCommand.builder()
                .totalAmount(paymentAmount)
                .resellerId(resellerId)
                .channelParams(new HashMap<>())
                .build();

        // When
        PaymentRequestResponse response = wireTransferChannelService.createPaymentRequest(command);

        // Then
        assertNotNull(response);
        assertNotNull(response.getChannelPaymentRecordId());
        assertNotNull(response.getChannelTransactionNumber());
        assertTrue(response.getChannelPaymentRecordId().startsWith("WIRE_RECORD_"));
    }

    @Test
    @DisplayName("测试信用账户渠道 - 创建支付请求返回响应对象")
    void testCreditAccountChannel_CreatePaymentRequest() {
        // Given
        CreatePaymentRequestCommand command = CreatePaymentRequestCommand.builder()
                .totalAmount(paymentAmount)
                .resellerId(resellerId)
                .channelParams(new HashMap<>())
                .build();

        // When
        PaymentRequestResponse response = creditAccountChannelService.createPaymentRequest(command);

        // Then
        assertNotNull(response);
        assertNotNull(response.getChannelPaymentRecordId());
        assertNotNull(response.getChannelTransactionNumber());
        assertTrue(response.getChannelPaymentRecordId().startsWith("CREDIT_RECORD_"));
    }

    @Test
    @DisplayName("测试线上支付渠道 - 创建支付请求返回响应对象")
    void testOnlinePaymentChannel_CreatePaymentRequest() {
        // Given
        CreatePaymentRequestCommand command = CreatePaymentRequestCommand.builder()
                .totalAmount(paymentAmount)
                .resellerId(resellerId)
                .channelParams(new HashMap<>())
                .build();

        // When
        PaymentRequestResponse response = onlinePaymentChannelService.createPaymentRequest(command);

        // Then
        assertNotNull(response);
        assertNotNull(response.getChannelPaymentRecordId());
        assertNotNull(response.getChannelTransactionNumber());
        assertTrue(response.getChannelPaymentRecordId().startsWith("ONLINE_RECORD_"));
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
    @DisplayName("测试命令对象 - 创建支付请求命令不包含渠道支付记录ID")
    void testCreatePaymentRequestCommand_NoChannelPaymentRecordId() {
        // Given
        CreatePaymentRequestCommand command = CreatePaymentRequestCommand.builder()
                .totalAmount(paymentAmount)
                .resellerId(resellerId)
                .channelParams(new HashMap<>())
                .build();

        // Then - 渠道支付记录ID由渠道返回，不在请求中
        assertEquals(paymentAmount, command.getTotalAmount());
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

        // Then - 查询时需要渠道支付记录ID
        assertEquals("WALLET_12345", command.getChannelTransactionNumber());
        assertEquals(channelPaymentRecordId, command.getChannelPaymentRecordId());
        assertEquals(resellerId, command.getResellerId());
    }

    @Test
    @DisplayName("测试命令对象 - 创建退款请求命令使用原支付记录ID")
    void testCreateRefundRequestCommand_UsesOriginalChannelPaymentRecordId() {
        // Given
        CreateRefundRequestCommand command = CreateRefundRequestCommand.builder()
                .channelTransactionNumber("WALLET_12345")
                .originalChannelPaymentRecordId(channelPaymentRecordId)
                .refundAmount(new BigDecimal("1000.00"))
                .refundReason("客户取消订单")
                .resellerId(resellerId)
                .build();

        // Then - 退款请求包含原支付的渠道支付记录ID
        assertEquals("WALLET_12345", command.getChannelTransactionNumber());
        assertEquals(channelPaymentRecordId, command.getOriginalChannelPaymentRecordId());
        assertEquals(new BigDecimal("1000.00"), command.getRefundAmount());
        assertEquals("客户取消订单", command.getRefundReason());
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

        // Then - 查询退款时需要渠道支付记录ID
        assertEquals("REFUND_12345", command.getRefundTransactionNumber());
        assertEquals(channelPaymentRecordId, command.getChannelPaymentRecordId());
        assertEquals(resellerId, command.getResellerId());
    }

    @Test
    @DisplayName("测试响应对象 - 支付请求响应包含两个ID")
    void testPaymentRequestResponse_ContainsBothIds() {
        // Given
        CreatePaymentRequestCommand command = CreatePaymentRequestCommand.builder()
                .totalAmount(paymentAmount)
                .resellerId(resellerId)
                .channelParams(new HashMap<>())
                .build();

        // When
        PaymentRequestResponse response = walletChannelService.createPaymentRequest(command);

        // Then - 响应包含渠道支付记录ID和渠道交易号
        assertNotNull(response.getChannelPaymentRecordId(), "渠道支付记录ID应该在开始支付时即返回");
        assertNotNull(response.getChannelTransactionNumber(), "渠道交易号可能在创建时或回调时返回");
    }

    @Test
    @DisplayName("测试响应对象 - 退款请求响应包含两个ID")
    void testRefundRequestResponse_ContainsBothIds() {
        // Given
        CreateRefundRequestCommand command = CreateRefundRequestCommand.builder()
                .channelTransactionNumber("WALLET_12345")
                .originalChannelPaymentRecordId(channelPaymentRecordId)
                .refundAmount(new BigDecimal("1000.00"))
                .refundReason("客户取消订单")
                .resellerId(resellerId)
                .build();

        // When
        RefundRequestResponse response = walletChannelService.createRefundRequest(command);

        // Then - 响应包含渠道支付记录ID和退款流水号
        assertNotNull(response.getChannelPaymentRecordId(), "渠道支付记录ID应该在开始退款时即返回");
        assertNotNull(response.getRefundTransactionNumber(), "退款流水号可能在创建时或回调时返回");
    }
}

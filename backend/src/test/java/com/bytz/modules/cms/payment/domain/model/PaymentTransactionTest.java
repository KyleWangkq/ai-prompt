package com.bytz.modules.cms.payment.domain.model;

import com.bytz.modules.cms.payment.domain.enums.PaymentChannel;
import com.bytz.modules.cms.payment.domain.enums.TransactionStatus;
import com.bytz.modules.cms.payment.domain.enums.TransactionType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

/**
 * PaymentTransaction 单元测试
 * 测试支付流水的状态转换和判断方法
 */
@DisplayName("PaymentTransaction 单元测试")
class PaymentTransactionTest {

    private PaymentTransaction paymentTransaction;
    private PaymentTransaction refundTransaction;

    @BeforeEach
    void setUp() {
        // 创建支付流水
        paymentTransaction = PaymentTransaction.builder()
                .code("TXN-PAY-001")
                .paymentCode("PAY-001")
                .transactionType(TransactionType.PAYMENT)
                .transactionStatus(TransactionStatus.PROCESSING)
                .transactionAmount(new BigDecimal("10000.00"))
                .paymentChannel(PaymentChannel.ONLINE_PAYMENT)
                .channelTransactionNumber("ALIPAY-TXN-001")
                .createTime(LocalDateTime.now())
                .build();

        // 创建退款流水
        refundTransaction = PaymentTransaction.builder()
                .code("TXN-REFUND-001")
                .paymentCode("PAY-001")
                .transactionType(TransactionType.REFUND)
                .transactionStatus(TransactionStatus.PROCESSING)
                .transactionAmount(new BigDecimal("3000.00"))
                .paymentChannel(PaymentChannel.ONLINE_PAYMENT)
                .originalTransactionCode("TXN-PAY-001")
                .businessOrderId("REFUND-ORDER-001")
                .createTime(LocalDateTime.now())
                .build();
    }

    @Test
    @DisplayName("判断是否为支付流水 - 支付类型")
    void testIsPaymentTransaction_PaymentType() {
        // When
        boolean isPayment = paymentTransaction.isPaymentTransaction();

        // Then
        assertTrue(isPayment);
    }

    @Test
    @DisplayName("判断是否为支付流水 - 退款类型")
    void testIsPaymentTransaction_RefundType() {
        // When
        boolean isPayment = refundTransaction.isPaymentTransaction();

        // Then
        assertFalse(isPayment);
    }

    @Test
    @DisplayName("判断是否为退款流水 - 退款类型")
    void testIsRefundTransaction_RefundType() {
        // When
        boolean isRefund = refundTransaction.isRefundTransaction();

        // Then
        assertTrue(isRefund);
    }

    @Test
    @DisplayName("判断是否为退款流水 - 支付类型")
    void testIsRefundTransaction_PaymentType() {
        // When
        boolean isRefund = paymentTransaction.isRefundTransaction();

        // Then
        assertFalse(isRefund);
    }

    @Test
    @DisplayName("判断流水是否成功 - 成功状态")
    void testIsSuccess_SuccessStatus() {
        // Given
        paymentTransaction.setTransactionStatus(TransactionStatus.SUCCESS);

        // When
        boolean isSuccess = paymentTransaction.isSuccess();

        // Then
        assertTrue(isSuccess);
    }

    @Test
    @DisplayName("判断流水是否成功 - 处理中状态")
    void testIsSuccess_ProcessingStatus() {
        // When
        boolean isSuccess = paymentTransaction.isSuccess();

        // Then
        assertFalse(isSuccess);
    }

    @Test
    @DisplayName("判断流水是否失败 - 失败状态")
    void testIsFailed_FailedStatus() {
        // Given
        paymentTransaction.setTransactionStatus(TransactionStatus.FAILED);

        // When
        boolean isFailed = paymentTransaction.isFailed();

        // Then
        assertTrue(isFailed);
    }

    @Test
    @DisplayName("判断流水是否失败 - 成功状态")
    void testIsFailed_SuccessStatus() {
        // Given
        paymentTransaction.setTransactionStatus(TransactionStatus.SUCCESS);

        // When
        boolean isFailed = paymentTransaction.isFailed();

        // Then
        assertFalse(isFailed);
    }

    @Test
    @DisplayName("判断流水是否处理中 - 处理中状态")
    void testIsProcessing_ProcessingStatus() {
        // When
        boolean isProcessing = paymentTransaction.isProcessing();

        // Then
        assertTrue(isProcessing);
    }

    @Test
    @DisplayName("判断流水是否处理中 - 成功状态")
    void testIsProcessing_SuccessStatus() {
        // Given
        paymentTransaction.setTransactionStatus(TransactionStatus.SUCCESS);

        // When
        boolean isProcessing = paymentTransaction.isProcessing();

        // Then
        assertFalse(isProcessing);
    }

    @Test
    @DisplayName("更新流水状态为成功")
    void testMarkAsSuccess() {
        // Given
        LocalDateTime completeTime = LocalDateTime.now();
        assertEquals(TransactionStatus.PROCESSING, paymentTransaction.getTransactionStatus());
        assertNull(paymentTransaction.getCompleteDateTime());

        // When
        paymentTransaction.markAsSuccess(completeTime);

        // Then
        assertEquals(TransactionStatus.SUCCESS, paymentTransaction.getTransactionStatus());
        assertEquals(completeTime, paymentTransaction.getCompleteDateTime());
        assertNotNull(paymentTransaction.getUpdateTime());
    }

    @Test
    @DisplayName("更新流水状态为失败")
    void testMarkAsFailed() {
        // Given
        String failureReason = "余额不足";
        assertEquals(TransactionStatus.PROCESSING, paymentTransaction.getTransactionStatus());
        assertNull(paymentTransaction.getBusinessRemark());

        // When
        paymentTransaction.markAsFailed(failureReason);

        // Then
        assertEquals(TransactionStatus.FAILED, paymentTransaction.getTransactionStatus());
        assertEquals(failureReason, paymentTransaction.getBusinessRemark());
        assertNotNull(paymentTransaction.getUpdateTime());
    }

    @Test
    @DisplayName("状态转换链 - 处理中到成功")
    void testStatusTransition_ProcessingToSuccess() {
        // Given
        assertTrue(paymentTransaction.isProcessing());

        // When
        paymentTransaction.markAsSuccess(LocalDateTime.now());

        // Then
        assertTrue(paymentTransaction.isSuccess());
        assertFalse(paymentTransaction.isProcessing());
        assertFalse(paymentTransaction.isFailed());
    }

    @Test
    @DisplayName("状态转换链 - 处理中到失败")
    void testStatusTransition_ProcessingToFailed() {
        // Given
        assertTrue(paymentTransaction.isProcessing());

        // When
        paymentTransaction.markAsFailed("支付失败");

        // Then
        assertTrue(paymentTransaction.isFailed());
        assertFalse(paymentTransaction.isProcessing());
        assertFalse(paymentTransaction.isSuccess());
    }

    @Test
    @DisplayName("退款流水包含原流水信息")
    void testRefundTransaction_ContainsOriginalTransactionInfo() {
        // Then
        assertNotNull(refundTransaction.getOriginalTransactionCode());
        assertEquals("TXN-PAY-001", refundTransaction.getOriginalTransactionCode());
        assertEquals("REFUND-ORDER-001", refundTransaction.getBusinessOrderId());
        assertTrue(refundTransaction.isRefundTransaction());
    }

    @Test
    @DisplayName("Builder模式创建流水实体")
    void testBuilder_CreateTransaction() {
        // When
        PaymentTransaction transaction = PaymentTransaction.builder()
                .code("TXN-TEST-001")
                .paymentCode("PAY-TEST-001")
                .transactionType(TransactionType.PAYMENT)
                .transactionStatus(TransactionStatus.PROCESSING)
                .transactionAmount(new BigDecimal("5000.00"))
                .paymentChannel(PaymentChannel.WALLET_PAYMENT)
                .channelTransactionNumber("WECHAT-TXN-001")
                .paymentWay("微信支付")
                .createTime(LocalDateTime.now())
                .createBy("USER-001")
                .createByName("测试用户")
                .build();

        // Then
        assertNotNull(transaction);
        assertEquals("TXN-TEST-001", transaction.getCode());
        assertEquals("PAY-TEST-001", transaction.getPaymentCode());
        assertEquals(TransactionType.PAYMENT, transaction.getTransactionType());
        assertEquals(TransactionStatus.PROCESSING, transaction.getTransactionStatus());
        assertEquals(new BigDecimal("5000.00"), transaction.getTransactionAmount());
        assertEquals(PaymentChannel.WALLET_PAYMENT, transaction.getPaymentChannel());
        assertEquals("WECHAT-TXN-001", transaction.getChannelTransactionNumber());
        assertEquals("微信支付", transaction.getPaymentWay());
        assertEquals("USER-001", transaction.getCreateBy());
        assertEquals("测试用户", transaction.getCreateByName());
    }

    @Test
    @DisplayName("完成时间在成功时被设置")
    void testCompleteDateTime_SetOnSuccess() {
        // Given
        assertNull(paymentTransaction.getCompleteDateTime());
        LocalDateTime beforeComplete = LocalDateTime.now();

        // When
        paymentTransaction.markAsSuccess(beforeComplete);

        // Then
        assertNotNull(paymentTransaction.getCompleteDateTime());
        assertEquals(beforeComplete, paymentTransaction.getCompleteDateTime());
    }

    @Test
    @DisplayName("失败原因在失败时被记录")
    void testBusinessRemark_SetOnFailure() {
        // Given
        assertNull(paymentTransaction.getBusinessRemark());
        String expectedReason = "网络超时";

        // When
        paymentTransaction.markAsFailed(expectedReason);

        // Then
        assertNotNull(paymentTransaction.getBusinessRemark());
        assertEquals(expectedReason, paymentTransaction.getBusinessRemark());
    }
    
    @Test
    @DisplayName("更新渠道交易号")
    void testUpdateChannelTransactionNumber() {
        // Given
        String originalNumber = paymentTransaction.getChannelTransactionNumber();
        String newChannelNumber = "CHANNEL-TXN-NEW-001";
        
        // When
        paymentTransaction.updateChannelTransactionNumber(newChannelNumber);
        
        // Then
        assertEquals(newChannelNumber, paymentTransaction.getChannelTransactionNumber());
        assertNotEquals(originalNumber, paymentTransaction.getChannelTransactionNumber());
        assertNotNull(paymentTransaction.getUpdateTime());
    }
}

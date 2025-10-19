package com.bytz.cms.payment.domain.model;

import com.bytz.cms.payment.domain.entity.PaymentTransactionEntity;
import com.bytz.cms.payment.domain.enums.PaymentChannel;
import com.bytz.cms.payment.domain.enums.TransactionStatus;
import com.bytz.cms.payment.domain.enums.TransactionType;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 支付流水工厂类测试
 * 
 * @author DDD设计团队
 * @version 1.0.0
 * @since 2025-01-15
 */
class PaymentTransactionFactoryTest {

    @Test
    void testCreateTransaction() {
        // Arrange
        String paymentId = "PAY123456789";
        TransactionType transactionType = TransactionType.PAYMENT;
        BigDecimal amount = new BigDecimal("100.00");
        PaymentChannel channel = PaymentChannel.ONLINE_PAYMENT;
        String paymentWay = "bank_transfer";
        String channelTransactionNumber = "CHANNEL_123";
        LocalDateTime expirationTime = LocalDateTime.now().plusDays(1);

        // Act
        PaymentTransactionEntity transaction = PaymentTransactionFactory.createTransaction(
            paymentId,
            transactionType,
            amount,
            channel,
            paymentWay,
            channelTransactionNumber,
            expirationTime
        );

        // Assert
        assertNotNull(transaction);
        assertEquals(paymentId, transaction.getPaymentId());
        assertEquals(transactionType, transaction.getTransactionType());
        assertEquals(TransactionStatus.PROCESSING, transaction.getTransactionStatus());
        assertEquals(amount, transaction.getTransactionAmount());
        assertEquals(channel, transaction.getPaymentChannel());
        assertEquals(paymentWay, transaction.getPaymentWay());
        assertEquals(channelTransactionNumber, transaction.getChannelTransactionNumber());
        assertEquals(expirationTime, transaction.getExpirationTime());
        assertEquals(Integer.valueOf(0), transaction.getDelFlag());
        assertNotNull(transaction.getCreatedTime());
    }

    @Test
    void testCreateTransactionWithMinimalParameters() {
        // Arrange
        String paymentId = "PAY123456789";
        TransactionType transactionType = TransactionType.REFUND;
        BigDecimal amount = new BigDecimal("-50.00");
        PaymentChannel channel = PaymentChannel.WALLET_PAYMENT;

        // Act
        PaymentTransactionEntity transaction = PaymentTransactionFactory.createTransaction(
            paymentId,
            transactionType,
            amount,
            channel,
            null,
            null,
            null
        );

        // Assert
        assertNotNull(transaction);
        assertEquals(paymentId, transaction.getPaymentId());
        assertEquals(transactionType, transaction.getTransactionType());
        assertEquals(TransactionStatus.PROCESSING, transaction.getTransactionStatus());
        assertEquals(amount, transaction.getTransactionAmount());
        assertEquals(channel, transaction.getPaymentChannel());
        assertNull(transaction.getPaymentWay());
        assertNull(transaction.getChannelTransactionNumber());
        assertNull(transaction.getExpirationTime());
    }
}

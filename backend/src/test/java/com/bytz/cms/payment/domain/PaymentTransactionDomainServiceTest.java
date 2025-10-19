package com.bytz.cms.payment.domain;

import com.bytz.cms.payment.domain.entity.PaymentTransactionEntity;
import com.bytz.cms.payment.domain.enums.TransactionStatus;
import com.bytz.cms.payment.domain.enums.TransactionType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 支付流水领域服务测试
 * 
 * @author DDD设计团队
 * @version 1.0.0
 * @since 2025-01-15
 */
class PaymentTransactionDomainServiceTest {

    private PaymentTransactionDomainService domainService;
    private PaymentTransactionEntity transaction;

    @BeforeEach
    void setUp() {
        domainService = new PaymentTransactionDomainService();
        transaction = new PaymentTransactionEntity();
        transaction.setId("TXN123456789");
        transaction.setPaymentId("PAY123456789");
        transaction.setTransactionType(TransactionType.PAYMENT);
        transaction.setTransactionStatus(TransactionStatus.PROCESSING);
        transaction.setTransactionAmount(new BigDecimal("100.00"));
    }

    @Test
    void testMarkSuccess() {
        // Arrange
        LocalDateTime completedAt = LocalDateTime.now();

        // Act - Use entity's method directly
        transaction.success(completedAt);

        // Assert
        assertEquals(TransactionStatus.SUCCESS, transaction.getTransactionStatus());
        assertEquals(completedAt, transaction.getCompleteDatetime());
        assertNotNull(transaction.getUpdatedTime());
    }

    @Test
    void testMarkFailed() {
        // Arrange
        String reason = "支付渠道超时";

        // Act - Use entity's method directly
        transaction.fail(reason);

        // Assert
        assertEquals(TransactionStatus.FAILED, transaction.getTransactionStatus());
        assertTrue(transaction.getBusinessRemark().contains(reason));
        assertNotNull(transaction.getCompleteDatetime());
        assertNotNull(transaction.getUpdatedTime());
    }

    @Test
    void testValidateAmountTypeConsistency_PaymentWithPositiveAmount() {
        // Arrange
        transaction.setTransactionType(TransactionType.PAYMENT);
        transaction.setTransactionAmount(new BigDecimal("100.00"));

        // Act
        boolean result = domainService.validateAmountTypeConsistency(transaction);

        // Assert
        assertTrue(result);
    }

    @Test
    void testValidateAmountTypeConsistency_PaymentWithNegativeAmount() {
        // Arrange
        transaction.setTransactionType(TransactionType.PAYMENT);
        transaction.setTransactionAmount(new BigDecimal("-100.00"));

        // Act
        boolean result = domainService.validateAmountTypeConsistency(transaction);

        // Assert
        assertFalse(result);
    }

    @Test
    void testValidateAmountTypeConsistency_RefundWithNegativeAmount() {
        // Arrange
        transaction.setTransactionType(TransactionType.REFUND);
        transaction.setTransactionAmount(new BigDecimal("-50.00"));

        // Act
        boolean result = domainService.validateAmountTypeConsistency(transaction);

        // Assert
        assertTrue(result);
    }

    @Test
    void testValidateAmountTypeConsistency_RefundWithPositiveAmount() {
        // Arrange
        transaction.setTransactionType(TransactionType.REFUND);
        transaction.setTransactionAmount(new BigDecimal("50.00"));

        // Act
        boolean result = domainService.validateAmountTypeConsistency(transaction);

        // Assert
        assertFalse(result);
    }

    @Test
    void testValidateAmountTypeConsistency_WithZeroAmount() {
        // Arrange
        transaction.setTransactionType(TransactionType.PAYMENT);
        transaction.setTransactionAmount(BigDecimal.ZERO);

        // Act
        boolean result = domainService.validateAmountTypeConsistency(transaction);

        // Assert
        assertFalse(result);
    }
}

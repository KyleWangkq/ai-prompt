package com.bytz.modules.cms.payment.domain.model;

import com.bytz.modules.cms.payment.domain.enums.*;
import com.bytz.modules.cms.payment.domain.valueobject.CompletedPaymentTransactionValueObject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * PaymentAggregate 单元测试
 * 测试支付单聚合根的核心业务逻辑
 */
@DisplayName("PaymentAggregate 单元测试")
class PaymentAggregateTest {

    private String orderId;
    private String resellerId;
    private BigDecimal paymentAmount;
    private LocalDateTime paymentDeadline;
    
    @BeforeEach
    void setUp() {
        orderId = "ORDER-001";
        resellerId = "RESELLER-001";
        paymentAmount = new BigDecimal("10000.00");
        paymentDeadline = LocalDateTime.now().plusDays(7);
    }

    @Test
    @DisplayName("创建支付单 - 正常流程")
    void testCreate_Success() {
        // Given & When
        PaymentAggregate payment = PaymentAggregate.create(
                orderId,
                resellerId,
                paymentAmount,
                "CNY",
                PaymentType.ADVANCE_PAYMENT,
                "订单支付",
                paymentDeadline,
                "RELATED-001",
                RelatedBusinessType.ORDER,
                LocalDateTime.now().plusMonths(1)
        );

        // Then
        assertNotNull(payment);
        assertEquals(orderId, payment.getOrderId());
        assertEquals(resellerId, payment.getResellerId());
        assertEquals(paymentAmount, payment.getPaymentAmount());
        assertEquals(BigDecimal.ZERO, payment.getPaidAmount());
        assertEquals(BigDecimal.ZERO, payment.getRefundedAmount());
        assertEquals(BigDecimal.ZERO, payment.getActualAmount());
        assertEquals("CNY", payment.getCurrency());
        assertEquals(PaymentType.ADVANCE_PAYMENT, payment.getPaymentType());
        assertEquals(PaymentStatus.UNPAID, payment.getPaymentStatus());
        assertEquals(RefundStatus.NO_REFUND, payment.getRefundStatus());
        assertNotNull(payment.getTransactions());
        assertTrue(payment.getTransactions().isEmpty());
    }

    @Test
    @DisplayName("创建支付单 - 币种默认为CNY")
    void testCreate_DefaultCurrency() {
        // Given & When
        PaymentAggregate payment = PaymentAggregate.create(
                orderId,
                resellerId,
                paymentAmount,
                null, // 币种传null
                PaymentType.ADVANCE_PAYMENT,
                "订单支付",
                paymentDeadline,
                "RELATED-001",
                RelatedBusinessType.ORDER,
                LocalDateTime.now().plusMonths(1)
        );

        // Then
        assertEquals("CNY", payment.getCurrency());
    }

    @Test
    @DisplayName("执行支付操作 - 正常流程")
    void testExecutePayment_Success() {
        // Given
        PaymentAggregate payment = createDefaultPayment();
        BigDecimal payAmount = new BigDecimal("5000.00");

        // When
        PaymentTransaction transaction = payment.executePayment(
                PaymentChannel.ONLINE_PAYMENT,
                payAmount,
                "CHANNEL-TXN-001",
                "CHANNEL-RECORD-001"
        );

        // Then
        assertNotNull(transaction);
        assertEquals(payment.getId(), transaction.getPaymentId());
        assertEquals(TransactionType.PAYMENT, transaction.getTransactionType());
        assertEquals(TransactionStatus.PROCESSING, transaction.getTransactionStatus());
        assertEquals(payAmount, transaction.getTransactionAmount());
        assertEquals(PaymentChannel.ONLINE_PAYMENT, transaction.getPaymentChannel());
        assertEquals("CHANNEL-TXN-001", transaction.getChannelTransactionNumber());
        assertEquals(PaymentStatus.PAYING, payment.getPaymentStatus());
        assertEquals(1, payment.getTransactions().size());
    }

    @Test
    @DisplayName("处理支付回调 - 支付成功，全额支付")
    void testHandlePaymentCallback_Success_FullPaid() {
        // Given
        PaymentAggregate payment = createDefaultPayment();
        payment.executePayment(PaymentChannel.ONLINE_PAYMENT, paymentAmount, "CHANNEL-001", "RECORD-001");
        PaymentTransaction transaction = payment.getTransactions().get(0);
        transaction.setId("1"); 
        transaction.setCode("TXN-001"); // 设置流水号
        String transactionId = transaction.getCode();

        // When
        payment.handlePaymentCallback(transactionId, true, LocalDateTime.now());

        // Then
        assertEquals(paymentAmount, payment.getPaidAmount());
        assertEquals(paymentAmount, payment.getActualAmount());
        assertEquals(PaymentStatus.PAID, payment.getPaymentStatus());
        // 支付流水仍在runningTransaction中（状态已变为SUCCESS）
        assertEquals(TransactionStatus.SUCCESS, payment.getRunningTransaction().getTransactionStatus());
    }

    @Test
    @DisplayName("处理支付回调 - 支付成功，部分支付")
    void testHandlePaymentCallback_Success_PartialPaid() {
        // Given
        PaymentAggregate payment = createDefaultPayment();
        BigDecimal partialAmount = new BigDecimal("5000.00");
        payment.executePayment(PaymentChannel.ONLINE_PAYMENT, partialAmount, "CHANNEL-001", "RECORD-001");
        PaymentTransaction transaction = payment.getTransactions().get(0);
        transaction.setId("1");
        transaction.setCode("TXN-001"); // 设置流水号
        String transactionId = transaction.getCode();

        // When
        payment.handlePaymentCallback(transactionId, true, LocalDateTime.now());

        // Then
        assertEquals(partialAmount, payment.getPaidAmount());
        assertEquals(partialAmount, payment.getActualAmount());
        assertEquals(PaymentStatus.PARTIAL_PAID, payment.getPaymentStatus());
    }

    @Test
    @DisplayName("处理支付回调 - 支付失败")
    void testHandlePaymentCallback_Failed() {
        // Given
        PaymentAggregate payment = createDefaultPayment();
        payment.executePayment(PaymentChannel.ONLINE_PAYMENT, paymentAmount, "CHANNEL-001", "RECORD-001");
        PaymentTransaction transaction = payment.getTransactions().get(0);
        transaction.setId("1");
        transaction.setCode("TXN-001"); // 设置流水号
        String transactionId = transaction.getCode();

        // When
        payment.handlePaymentCallback(transactionId, false, LocalDateTime.now());

        // Then
        assertEquals(BigDecimal.ZERO, payment.getPaidAmount());
        assertEquals(PaymentStatus.FAILED, payment.getPaymentStatus());
        // 支付流水仍在runningTransaction中（状态已变为FAILED）
        assertEquals(TransactionStatus.FAILED, payment.getRunningTransaction().getTransactionStatus());
    }

    @Test
    @DisplayName("处理支付回调 - 流水不存在")
    void testHandlePaymentCallback_TransactionNotFound() {
        // Given
        PaymentAggregate payment = createDefaultPayment();

        // When & Then
        assertThrows(IllegalArgumentException.class, () -> {
            payment.handlePaymentCallback("NON-EXISTENT-ID", true, LocalDateTime.now());
        });
    }

    @Test
    @DisplayName("执行退款操作 - 正常流程")
    void testExecuteRefund_Success() {
        // Given
        PaymentAggregate payment = createPaidPayment();
        BigDecimal refundAmount = new BigDecimal("3000.00");
        String originalTxnId = payment.getCompletedTransactions().get(0).getId();

        // When
        PaymentTransaction refundTransaction = payment.executeRefund(
                refundAmount,
                originalTxnId,
                "REFUND-ORDER-001",
                "客户申请退款"
        );

        // Then
        assertNotNull(refundTransaction);
        assertEquals(TransactionType.REFUND, refundTransaction.getTransactionType());
        assertEquals(TransactionStatus.PROCESSING, refundTransaction.getTransactionStatus());
        assertEquals(refundAmount, refundTransaction.getTransactionAmount());
        assertEquals(originalTxnId, refundTransaction.getOriginalTransactionId());
        assertEquals("REFUND-ORDER-001", refundTransaction.getBusinessOrderId());
        assertEquals(RefundStatus.REFUNDING, payment.getRefundStatus());
    }

    @Test
    @DisplayName("执行退款操作 - 原流水不存在")
    void testExecuteRefund_OriginalTransactionNotFound() {
        // Given
        PaymentAggregate payment = createPaidPayment();

        // When & Then
        assertThrows(IllegalArgumentException.class, () -> {
            payment.executeRefund(
                    new BigDecimal("1000.00"),
                    "999",
                    "REFUND-001",
                    "测试退款"
            );
        });
    }

    @Test
    @DisplayName("处理退款回调 - 退款成功，部分退款")
    void testHandleRefundCallback_Success_PartialRefund() {
        // Given
        PaymentAggregate payment = createPaidPayment();
        BigDecimal refundAmount = new BigDecimal("3000.00");
        // 获取已完成的支付流水ID（现在在completedTransactions中）
        String originalTxnId = payment.getCompletedTransactions().get(0).getId();
        payment.executeRefund(refundAmount, originalTxnId, "REFUND-001", "测试退款");
        // 获取运行期的退款流水（刚创建的）
        PaymentTransaction refundTransaction = payment.getRunningTransaction();
        refundTransaction.setId("1");
        refundTransaction.setCode("TXN-REFUND-1"); // 设置退款流水号
        String refundTxnId = refundTransaction.getCode();

        // When
        payment.handleRefundCallback(refundTxnId, true, LocalDateTime.now());

        // Then
        assertEquals(refundAmount, payment.getRefundedAmount());
        assertEquals(paymentAmount.subtract(refundAmount), payment.getActualAmount());
        assertEquals(RefundStatus.PARTIAL_REFUNDED, payment.getRefundStatus());
        // 退款流水仍在runningTransaction中（状态已变为SUCCESS）
        assertEquals(TransactionStatus.SUCCESS, payment.getRunningTransaction().getTransactionStatus());
        assertEquals(TransactionType.REFUND, payment.getRunningTransaction().getTransactionType());
    }

    @Test
    @DisplayName("处理退款回调 - 退款成功，全额退款")
    void testHandleRefundCallback_Success_FullRefund() {
        // Given
        PaymentAggregate payment = createPaidPayment();
        String originalTxnId = payment.getCompletedTransactions().get(0).getId();
        payment.executeRefund(paymentAmount, originalTxnId, "REFUND-001", "全额退款");
        PaymentTransaction refundTransaction = payment.getRunningTransaction();
        refundTransaction.setId("1");
        refundTransaction.setCode("TXN-REFUND-1"); // 设置退款流水号
        String refundTxnId = refundTransaction.getCode();

        // When
        payment.handleRefundCallback(refundTxnId, true, LocalDateTime.now());

        // Then
        assertEquals(paymentAmount, payment.getRefundedAmount());
        assertEquals(0, payment.getActualAmount().compareTo(BigDecimal.ZERO));
        assertEquals(RefundStatus.FULL_REFUNDED, payment.getRefundStatus());
    }

    @Test
    @DisplayName("处理退款回调 - 退款失败")
    void testHandleRefundCallback_Failed() {
        // Given
        PaymentAggregate payment = createPaidPayment();
        String originalTxnId = payment.getCompletedTransactions().get(0).getId();
        payment.executeRefund(new BigDecimal("1000.00"), originalTxnId, "REFUND-001", "测试");
        PaymentTransaction refundTransaction = payment.getRunningTransaction();
        refundTransaction.setId("1");
        refundTransaction.setCode("TXN-REFUND-1"); // 设置退款流水号
        String refundTxnId = refundTransaction.getCode();

        // When
        payment.handleRefundCallback(refundTxnId, false, LocalDateTime.now());

        // Then
        assertEquals(BigDecimal.ZERO, payment.getRefundedAmount());
        assertEquals(RefundStatus.REFUND_FAILED, payment.getRefundStatus());
        // 退款流水仍在runningTransaction中（状态已变为FAILED），通过getTransactions()可以访问
        assertEquals(TransactionStatus.FAILED, payment.getRunningTransaction().getTransactionStatus());
    }

    @Test
    @DisplayName("计算待支付金额")
    void testGetPendingAmount() {
        // Given
        PaymentAggregate payment = createDefaultPayment();
        payment.setPaidAmount(new BigDecimal("3000.00"));

        // When
        BigDecimal pendingAmount = payment.getPendingAmount();

        // Then
        assertEquals(new BigDecimal("7000.00"), pendingAmount);
    }

    @Test
    @DisplayName("判断是否允许支付 - 未支付状态")
    void testCanPay_UnpaidStatus() {
        // Given
        PaymentAggregate payment = createDefaultPayment();

        // When
        boolean canPay = payment.canPay();

        // Then
        assertTrue(canPay);
    }

    @Test
    @DisplayName("判断是否允许支付 - 部分支付状态")
    void testCanPay_PartialPaidStatus() {
        // Given
        PaymentAggregate payment = createDefaultPayment();
        payment.setPaymentStatus(PaymentStatus.PARTIAL_PAID);
        payment.setPaidAmount(new BigDecimal("5000.00"));

        // When
        boolean canPay = payment.canPay();

        // Then
        assertTrue(canPay);
    }

    @Test
    @DisplayName("判断是否允许支付 - 已支付状态")
    void testCanPay_PaidStatus() {
        // Given
        PaymentAggregate payment = createDefaultPayment();
        payment.setPaymentStatus(PaymentStatus.PAID);
        payment.setPaidAmount(paymentAmount);

        // When
        boolean canPay = payment.canPay();

        // Then
        assertFalse(canPay);
    }

    @Test
    @DisplayName("判断是否允许退款 - 已支付状态")
    void testCanRefund_HasPaidAmount() {
        // Given
        PaymentAggregate payment = createPaidPayment();

        // When
        boolean canRefund = payment.canRefund();

        // Then
        assertTrue(canRefund);
    }

    @Test
    @DisplayName("判断是否允许退款 - 未支付状态")
    void testCanRefund_NoPaidAmount() {
        // Given
        PaymentAggregate payment = createDefaultPayment();

        // When
        boolean canRefund = payment.canRefund();

        // Then
        assertFalse(canRefund);
    }

    @Test
    @DisplayName("判断是否允许退款 - 已全额退款")
    void testCanRefund_FullyRefunded() {
        // Given
        PaymentAggregate payment = createPaidPayment();
        payment.setRefundedAmount(paymentAmount);

        // When
        boolean canRefund = payment.canRefund();

        // Then
        assertFalse(canRefund);
    }

    @Test
    @DisplayName("判断是否为信用还款类型 - 是")
    void testIsCreditRepayment_True() {
        // Given
        PaymentAggregate payment = createDefaultPayment();
        payment.setPaymentType(PaymentType.CREDIT_REPAYMENT);

        // When
        boolean isCreditRepayment = payment.isCreditRepayment();

        // Then
        assertTrue(isCreditRepayment);
    }

    @Test
    @DisplayName("判断是否为信用还款类型 - 否")
    void testIsCreditRepayment_False() {
        // Given
        PaymentAggregate payment = createDefaultPayment();

        // When
        boolean isCreditRepayment = payment.isCreditRepayment();

        // Then
        assertFalse(isCreditRepayment);
    }

    @Test
    @DisplayName("停止支付单")
    void testStop() {
        // Given
        PaymentAggregate payment = createDefaultPayment();
        String stopReason = "订单已取消";

        // When
        payment.stop(stopReason);

        // Then
        assertEquals(PaymentStatus.STOPPED, payment.getPaymentStatus());
        assertTrue(payment.getBusinessDesc().contains(stopReason));
    }

    @Test
    @DisplayName("冻结支付单")
    void testFreeze() {
        // Given
        PaymentAggregate payment = createDefaultPayment();
        String freezeReason = "风控审查";

        // When
        payment.freeze(freezeReason);

        // Then
        assertEquals(PaymentStatus.FROZEN, payment.getPaymentStatus());
        assertTrue(payment.getBusinessDesc().contains(freezeReason));
    }

    @Test
    @DisplayName("多次支付累计金额计算")
    void testMultiplePayments_AccumulatedAmount() {
        // Given
        PaymentAggregate payment = createDefaultPayment();
        
        // When - 第一次支付3000
        payment.executePayment(PaymentChannel.ONLINE_PAYMENT, new BigDecimal("3000.00"), "TXN-001", "RECORD-001");
        PaymentTransaction txn1 = payment.getRunningTransaction();
        txn1.setId("1");
        txn1.setCode("TXN-001"); // 设置第一笔流水号
        String txn1Id = txn1.getCode();
        payment.handlePaymentCallback(txn1Id, true, LocalDateTime.now());
        
        // 模拟持久化后重新加载
        simulateRepositoryReload(payment);
        
        // Then - 检查第一次支付后的状态
        assertEquals(new BigDecimal("3000.00"), payment.getPaidAmount());
        assertEquals(PaymentStatus.PARTIAL_PAID, payment.getPaymentStatus());
        
        // When - 第二次支付7000
        payment.executePayment(PaymentChannel.WALLET_PAYMENT, new BigDecimal("7000.00"), "TXN-002", "RECORD-002");
        PaymentTransaction txn2 = payment.getRunningTransaction();  // 第一笔已完成，所以第二笔现在是运行期流水
        txn2.setId("2");
        txn2.setCode("TXN-002"); // 设置第二笔流水号
        String txn2Id = txn2.getCode();
        payment.handlePaymentCallback(txn2Id, true, LocalDateTime.now());
        
        // Then - 检查第二次支付后的状态
        assertEquals(new BigDecimal("10000.00"), payment.getPaidAmount());
        assertEquals(PaymentStatus.PAID, payment.getPaymentStatus());
    }

    @Test
    @DisplayName("支付后退款，实际金额正确计算")
    void testPaymentThenRefund_ActualAmountCalculation() {
        // Given
        PaymentAggregate payment = createPaidPayment();
        assertEquals(paymentAmount, payment.getActualAmount());
        
        // When - 退款3000
        String originalTxnId = payment.getCompletedTransactions().get(0).getId();
        payment.executeRefund(new BigDecimal("3000.00"), originalTxnId, "REFUND-001", "部分退款");
        PaymentTransaction refundTransaction = payment.getRunningTransaction();
        refundTransaction.setId("1");
        refundTransaction.setCode("TXN-REFUND-1"); // 设置退款流水号
        String refundTxnId = refundTransaction.getCode();
        payment.handleRefundCallback(refundTxnId, true, LocalDateTime.now());
        
        // Then
        assertEquals(paymentAmount, payment.getPaidAmount()); // 已支付金额不变
        assertEquals(new BigDecimal("3000.00"), payment.getRefundedAmount());
        assertEquals(new BigDecimal("7000.00"), payment.getActualAmount()); // 实际收款 = 已支付 - 已退款
    }

    // ========== 辅助方法 ==========

    /**
     * 创建一个默认的未支付状态的支付单
     */
    private PaymentAggregate createDefaultPayment() {
        PaymentAggregate payment = PaymentAggregate.create(
                orderId,
                resellerId,
                paymentAmount,
                "CNY",
                PaymentType.ADVANCE_PAYMENT,
                "测试支付单",
                paymentDeadline,
                "RELATED-001",
                RelatedBusinessType.ORDER,
                LocalDateTime.now().plusMonths(1)
        );
        // 模拟设置ID，因为实际使用中会由repository生成
        payment.setCode("PAY-" + System.currentTimeMillis());
        return payment;
    }

    /**
     * 创建一个已支付状态的支付单
     */
    private PaymentAggregate createPaidPayment() {
        PaymentAggregate payment = createDefaultPayment();
        
        // 执行支付并回调成功
        payment.executePayment(PaymentChannel.ONLINE_PAYMENT, paymentAmount, "CHANNEL-001", "RECORD-001");
        PaymentTransaction transaction = payment.getTransactions().get(0);
        transaction.setId("TXN-" + System.currentTimeMillis());
        transaction.setCode("TXN-" + System.currentTimeMillis());
        payment.handlePaymentCallback(transaction.getCode(), true, LocalDateTime.now());
        
        // 模拟持久化后重新加载：将完成的流水转换为值对象
        // 这模拟了 PaymentRepositoryImpl.toPaymentAggregate() 的行为
        simulateRepositoryReload(payment);
        
        return payment;
    }
    
    /**
     * 模拟仓储层在加载聚合根时的流水分离和转换逻辑
     * 这模拟了 PaymentRepositoryImpl.toPaymentAggregate() 方法的行为
     */
    private void simulateRepositoryReload(PaymentAggregate payment) {
        // 将所有已完成的流水（SUCCESS或FAILED状态）转换为值对象
        List<PaymentTransaction> allTransactions = new ArrayList<>(payment.getTransactions());
        
        PaymentTransaction runningTxn = null;
        List<CompletedPaymentTransactionValueObject> completedTxns = new ArrayList<>();
        
        for (PaymentTransaction txn : allTransactions) {
            if (TransactionStatus.PROCESSING.equals(txn.getTransactionStatus())) {
                runningTxn = txn;
            } else {
                // 已完成的流水（SUCCESS或FAILED），转换为值对象
                completedTxns.add(
                    CompletedPaymentTransactionValueObject.builder()
                        .id(txn.getId())
                        .code(txn.getCode())
                        .paymentId(txn.getPaymentId())
                        .transactionType(txn.getTransactionType())
                        .transactionStatus(txn.getTransactionStatus())
                        .transactionAmount(txn.getTransactionAmount())
                        .paymentChannel(txn.getPaymentChannel())
                        .channelTransactionNumber(txn.getChannelTransactionNumber())
                        .channelPaymentRecordId(txn.getChannelPaymentRecordId())
                        .paymentWay(txn.getPaymentWay())
                        .originalTransactionId(txn.getOriginalTransactionId())
                        .businessOrderId(txn.getBusinessOrderId())
                        .createTime(txn.getCreateTime())
                        .completeDateTime(txn.getCompleteDateTime())
                        .expirationTime(txn.getExpirationTime())
                        .businessRemark(txn.getBusinessRemark())
                        .createBy(txn.getCreateBy())
                        .createByName(txn.getCreateByName())
                        .updateBy(txn.getUpdateBy())
                        .updateByName(txn.getUpdateByName())
                        .updateTime(txn.getUpdateTime())
                        .build()
                );
            }
        }
        
        payment.setRunningTransaction(runningTxn);
        payment.setCompletedTransactions(completedTxns);
    }
    
    @Test
    @DisplayName("同时只能有一条支付明细为运行期 - 支付流水")
    void testOnlyOneRunningTransaction_Payment() {
        // Given
        PaymentAggregate payment = createDefaultPayment();
        
        // When - 创建第一条运行期支付流水
        payment.executePayment(PaymentChannel.ONLINE_PAYMENT, new BigDecimal("3000.00"), null, null);
        
        // Then - 尝试创建第二条运行期支付流水应该失败
        IllegalStateException exception = assertThrows(IllegalStateException.class, () -> {
            payment.executePayment(PaymentChannel.WALLET_PAYMENT, new BigDecimal("2000.00"), null, null);
        });
        
        assertEquals("已存在运行期的支付流水，同时只能有一条支付明细为运行期状态", exception.getMessage());
        assertNotNull(payment.getRunningTransaction());
    }
    
    @Test
    @DisplayName("同时只能有一条支付明细为运行期 - 退款流水")
    void testOnlyOneRunningTransaction_Refund() {
        // Given - 创建一个已支付的支付单
        PaymentAggregate payment = createPaidPayment();
        String originalTxnId = payment.getCompletedTransactions().get(0).getId();
        
        // When - 创建第一条运行期退款流水
        payment.executeRefund(new BigDecimal("1000.00"), originalTxnId, "REFUND-001", "测试退款");
        
        // Then - 尝试创建第二条运行期退款流水应该失败
        IllegalStateException exception = assertThrows(IllegalStateException.class, () -> {
            payment.executeRefund(new BigDecimal("500.00"), originalTxnId, "REFUND-002", "测试退款2");
        });
        
        assertEquals("已存在运行期的支付流水，同时只能有一条支付明细为运行期状态", exception.getMessage());
        assertNotNull(payment.getRunningTransaction());
    }
    
    @Test
    @DisplayName("运行期流水完成后可以创建新的运行期流水")
    void testNewRunningTransactionAfterCompletion() {
        // Given
        PaymentAggregate payment = createDefaultPayment();
        
        // When - 创建第一条支付流水并完成
        payment.executePayment(PaymentChannel.ONLINE_PAYMENT, new BigDecimal("3000.00"), "TXN-001", "RECORD-001");
        PaymentTransaction txn1 = payment.getRunningTransaction();
        txn1.setId("1");
        txn1.setCode("TXN-001");
        payment.handlePaymentCallback("TXN-001", true, LocalDateTime.now());
        
        // 模拟持久化后重新加载
        simulateRepositoryReload(payment);
        
        // Then - 第一条流水已完成，应该可以创建第二条运行期流水
        assertDoesNotThrow(() -> {
            payment.executePayment(PaymentChannel.WALLET_PAYMENT, new BigDecimal("2000.00"), null, null);
        });
        
        assertNotNull(payment.getRunningTransaction()); // 第二条已创建在运行期
        assertEquals(1, payment.getCompletedTransactions().size()); // 第一条已完成
    }
}

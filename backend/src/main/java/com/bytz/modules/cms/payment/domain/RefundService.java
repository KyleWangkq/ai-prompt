package com.bytz.modules.cms.payment.domain;

import com.bytz.modules.cms.payment.domain.model.PaymentAggregate;
import com.bytz.modules.cms.payment.domain.model.PaymentTransaction;
import com.bytz.modules.cms.payment.domain.repository.IPaymentRepository;
import com.bytz.modules.cms.payment.shared.exception.PaymentException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;

/**
 * 退款服务
 * Refund Domain Service
 * <p>
 * 领域服务类型：编排服务（原名RefundExecutionService，简化为RefundService）
 * 职责：退款执行编排，支持最新流水退款/指定流水退款/多流水分摊
 * <p>
 * 术语来源：Glossary.md - 领域服务术语"退款执行服务"
 * 需求来源：需求文档4.8节退款管理
 * 相关用例：UC-PM-006
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class RefundService {

    private final IPaymentRepository paymentRepository;
    private final PaymentValidationService validationService;

    /**
     * 按策略选择流水并下发渠道退款（UC-PM-006）
     * <p>
     * 处理流程：
     * 1. 接收退款指令
     * 2. 验证退款前置条件
     * 3. 选择退款流水（最新流水/指定流水/多流水分摊）
     * 4. 创建退款流水
     * 5. 更新支付单状态
     * <p>
     * 流水选择策略：
     * - 指定流水退款：按指定的原支付流水ID退款
     * - 最新流水退款：选择最新成功的支付流水退款
     * - 多流水分摊退款：按比例分摊到多个支付流水（未实现）
     * <p>
     * 用例来源：UC-PM-006步骤1-8
     *
     * @param paymentId             支付单ID
     * @param refundAmount          退款金额
     * @param originalTransactionId 原支付流水ID（指定流水退款时必填）
     * @param businessOrderId       业务单号（退款单号）
     * @param refundReason          退款原因
     * @return 退款流水
     * @throws PaymentException 如果退款失败
     */
    public PaymentTransaction executeRefund(
            String paymentId,
            BigDecimal refundAmount,
            String originalTransactionId,
            String businessOrderId,
            String refundReason) {

        log.info("开始执行退款，支付单ID: {}, 退款金额: {}, 原流水ID: {}",
                paymentId, refundAmount, originalTransactionId);

        // ========== 步骤1: 查询支付单 ==========
        PaymentAggregate payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new PaymentException("支付单不存在: " + paymentId));

        // ========== 步骤2: 验证退款前置条件 ==========
        validationService.validateRefund(payment, refundAmount);

        // ========== 步骤3: 选择退款流水 ==========
        String selectedTransactionId = selectRefundTransaction(
                payment, originalTransactionId);

        log.info("选择退款流水: {}", selectedTransactionId);

        // ========== 步骤4: 创建退款流水 ==========
        PaymentTransaction refundTransaction = payment.executeRefund(
                refundAmount,
                selectedTransactionId,
                businessOrderId,
                refundReason
        );

        // ========== 步骤5: 持久化 ==========
        paymentRepository.save(payment);

        log.info("退款执行成功，退款流水号: {}", refundTransaction.getId());
        return refundTransaction;
    }

    /**
     * 选择退款流水
     * <p>
     * 策略：
     * 1. 如果指定了原流水ID，使用指定的流水
     * 2. 如果未指定，选择最新成功的支付流水
     * 3. TODO: 支持多流水分摊策略
     *
     * @param payment               支付单
     * @param originalTransactionId 原支付流水ID（可为空）
     * @return 选中的流水ID
     * @throws PaymentException 如果找不到合适的流水
     */
    private String selectRefundTransaction(
            PaymentAggregate payment,
            String originalTransactionId) {

        // 策略1: 指定流水退款
        if (originalTransactionId != null && !originalTransactionId.trim().isEmpty()) {
            log.debug("使用指定流水退款策略");
            return originalTransactionId;
        }

        // 策略2: 最新流水退款
        log.debug("使用最新流水退款策略");
        Optional<PaymentTransaction> latestTransaction = findLatestSuccessfulPaymentTransaction(payment);

        if (!latestTransaction.isPresent()) {
            throw new PaymentException("未找到可退款的支付流水");
        }

        return latestTransaction.get().getId();
    }

    /**
     * 查找最新成功的支付流水
     *
     * @param payment 支付单
     * @return 最新成功的支付流水
     */
    private Optional<PaymentTransaction> findLatestSuccessfulPaymentTransaction(
            PaymentAggregate payment) {

        return payment.getTransactions().stream()
                .filter(PaymentTransaction::isPaymentTransaction)
                .filter(PaymentTransaction::isSuccess)
                .findFirst(); // 流水已按时间倒序排列
    }

    /**
     * 处理退款回调（由PaymentCallbackService调用）
     * <p>
     * 处理流程：
     * 1. 查找退款流水
     * 2. 更新流水状态
     * 3. 调用支付单handleRefundCallback
     * 4. 持久化
     *
     * @param paymentId       支付单ID
     * @param transactionCode 退款流水号
     * @param success         是否成功
     * @param completeTime    完成时间
     */
    public void handleRefundCallback(
            String paymentId,
            String transactionCode,
            boolean success,
            LocalDateTime completeTime) {

        log.info("处理退款回调，支付单ID: {}, 退款流水号: {}, 成功: {}",
                paymentId, transactionCode, success);

        // 查询支付单
        PaymentAggregate payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new PaymentException("支付单不存在: " + paymentId));

        // 调用聚合根方法处理退款回调
        payment.handleRefundCallback(transactionCode, success, completeTime);

        // 持久化
        paymentRepository.save(payment);

        log.info("退款回调处理完成");
    }
}

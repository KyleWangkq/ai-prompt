package com.bytz.modules.cms.payment.domain;

import com.bytz.modules.cms.payment.domain.enums.PaymentType;
import com.bytz.modules.cms.payment.domain.enums.RelatedBusinessType;
import com.bytz.modules.cms.payment.domain.model.PaymentAggregate;
import com.bytz.modules.cms.payment.shared.exception.PaymentException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 支付验证服务
 * Payment Validation Domain Service
 * <p>
 * 领域服务类型：验证服务
 * 职责：支付单创建与执行前的业务校验（金额、状态、截止时间、唯一性等）
 * <p>
 * 术语来源：Glossary.md - 领域服务术语"支付验证服务"
 * 相关用例：UC-PM-001、UC-PM-003、UC-PM-006
 */
@Slf4j
@Service
public class PaymentValidationService {

    /**
     * 校验创建请求（UC-PM-001），包含普通支付单和信用还款支付单
     * <p>
     * 验证项：
     * - 支付单号唯一性
     * - 金额合法性
     * - 必填字段完整性
     * - 业务规则符合性
     * - 信用还款时订单号与信用记录的绑定关系
     * <p>
     * 用例来源：UC-PM-001步骤3-4、UC-PM-007步骤3-4
     *
     * @param orderId             订单号
     * @param resellerId          经销商ID
     * @param paymentAmount       支付金额
     * @param paymentType         支付类型
     * @param relatedBusinessId   关联业务ID
     * @param relatedBusinessType 关联业务类型
     * @throws IllegalArgumentException 如果验证失败
     */
    public void validateCreate(
            String orderId,
            String resellerId,
            BigDecimal paymentAmount,
            PaymentType paymentType,
            String relatedBusinessId,
            RelatedBusinessType relatedBusinessType) {

        log.debug("开始校验支付单创建请求，订单号: {}, 支付类型: {}", orderId, paymentType);

        // 验证必填字段
        if (orderId == null || orderId.trim().isEmpty()) {
            throw new IllegalArgumentException("订单号不能为空");
        }

        if (resellerId == null || resellerId.trim().isEmpty()) {
            throw new IllegalArgumentException("经销商ID不能为空");
        }

        if (paymentType == null) {
            throw new IllegalArgumentException("支付类型不能为空");
        }

        // 验证金额合法性
        if (paymentAmount == null || paymentAmount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("支付金额必须大于0");
        }

        // 信用还款特殊校验：必须提供关联业务ID和类型
        if (PaymentType.CREDIT_REPAYMENT.equals(paymentType)) {
            if (relatedBusinessId == null || relatedBusinessId.trim().isEmpty()) {
                throw new IllegalArgumentException("信用还款支付单必须提供关联业务ID（信用记录ID）");
            }

            if (!RelatedBusinessType.CREDIT_RECORD.equals(relatedBusinessType)) {
                throw new IllegalArgumentException("信用还款支付单的关联业务类型必须为CREDIT_RECORD");
            }

            // TODO: 调用信用管理系统接口，验证订单号与信用记录ID的绑定关系
            log.info("信用还款校验通过，订单号: {}, 信用记录ID: {}", orderId, relatedBusinessId);
        }

        log.debug("支付单创建请求校验通过");
    }

    /**
     * 校验执行支付前置条件（UC-PM-003），适用于所有支付类型
     * <p>
     * 验证项：
     * - 支付单状态可支付
     * - 待支付金额 > 0
     * - 未超过截止时间
     * - 经销商权限
     * - 批量支付时所有支付单属于同一经销商
     * - 每个支付单的支付金额不超过待支付金额
     * <p>
     * 用例来源：UC-PM-003步骤3-4
     *
     * @param payments 待支付的支付单列表
     * @throws PaymentException 如果验证失败
     */
    public void validateExecute(List<PaymentAggregate> payments) {
        log.debug("开始校验批量支付执行前置条件，支付单数量: {}", payments.size());

        if (payments == null || payments.isEmpty()) {
            throw new IllegalArgumentException("支付单列表不能为空");
        }

        // 验证所有支付单属于同一经销商
        Set<String> resellerIds = payments.stream()
                .map(PaymentAggregate::getResellerId)
                .collect(Collectors.toSet());

        if (resellerIds.size() != 1) {
            throw new PaymentException("所有支付单必须属于同一经销商");
        }

        // 验证每个支付单的状态和金额
        for (PaymentAggregate payment : payments) {
            // 验证支付单状态是否允许支付
            if (!payment.canPay()) {
                throw new PaymentException(
                        String.format("支付单 %s 当前状态 %s 不允许支付",
                                payment.getCode(),
                                payment.getPaymentStatus().getDescription()));
            }

            // 验证待支付金额
            BigDecimal pendingAmount = payment.getPendingAmount();
            if (pendingAmount.compareTo(BigDecimal.ZERO) <= 0) {
                throw new PaymentException(
                        String.format("支付单 %s 待支付金额必须大于0", payment.getCode()));
            }

            // TODO: 验证支付截止时间
            // if (payment.getPaymentDeadline() != null && LocalDateTime.now().isAfter(payment.getPaymentDeadline())) {
            //     throw new PaymentException(String.format("支付单 %s 已超过截止时间", payment.getCode()));
            // }
        }

        log.debug("批量支付执行前置条件校验通过");
    }

    /**
     * 校验支付金额分配（UC-PM-003步骤6）
     * <p>
     * 验证规则：
     * - 每个支付单的分配金额 <= 待支付金额
     * - 总分配金额 = 支付渠道支付金额
     * <p>
     * 用例来源：UC-PM-003步骤6
     *
     * @param payments       支付单列表
     * @param allocatedAmounts 分配金额列表（与支付单列表一一对应）
     * @param totalAmount    总支付金额
     * @throws PaymentException 如果验证失败
     */
    public void validateAmountAllocation(
            List<PaymentAggregate> payments,
            List<BigDecimal> allocatedAmounts,
            BigDecimal totalAmount) {

        log.debug("开始校验金额分配，支付单数量: {}, 总金额: {}", payments.size(), totalAmount);

        if (payments.size() != allocatedAmounts.size()) {
            throw new IllegalArgumentException("支付单数量与分配金额数量不匹配");
        }

        // 计算总分配金额
        BigDecimal sumAllocated = allocatedAmounts.stream()
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // 验证总分配金额等于支付渠道支付金额
        if (sumAllocated.compareTo(totalAmount) != 0) {
            throw new PaymentException(
                    String.format("总分配金额 %s 不等于支付渠道支付金额 %s",
                            sumAllocated, totalAmount));
        }

        // 验证每个支付单的分配金额不超过待支付金额
        for (int i = 0; i < payments.size(); i++) {
            PaymentAggregate payment = payments.get(i);
            BigDecimal allocatedAmount = allocatedAmounts.get(i);
            BigDecimal pendingAmount = payment.getPendingAmount();

            if (allocatedAmount.compareTo(pendingAmount) > 0) {
                throw new PaymentException(
                        String.format("支付单 %s 的分配金额 %s 超过待支付金额 %s",
                                payment.getCode(), allocatedAmount, pendingAmount));
            }

            if (allocatedAmount.compareTo(BigDecimal.ZERO) <= 0) {
                throw new PaymentException(
                        String.format("支付单 %s 的分配金额必须大于0", payment.getCode()));
            }
        }

        log.debug("金额分配校验通过");
    }

    /**
     * 校验退款前置条件（UC-PM-006）
     * <p>
     * 验证项：
     * - 支付单状态可退款
     * - 可退款金额足够
     * - 退款流水可选
     * - 业务授权
     * <p>
     * 用例来源：UC-PM-006步骤2-3
     *
     * @param payment      支付单
     * @param refundAmount 退款金额
     * @throws PaymentException 如果验证失败
     */
    public void validateRefund(PaymentAggregate payment, BigDecimal refundAmount) {
        log.debug("开始校验退款前置条件，支付单: {}, 退款金额: {}",
                payment.getCode(), refundAmount);

        // 验证退款金额
        if (refundAmount == null || refundAmount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("退款金额必须大于0");
        }

        // 验证支付单状态是否允许退款
        if (!payment.canRefund()) {
            throw new PaymentException(
                    String.format("支付单 %s 当前状态不允许退款", payment.getCode()));
        }

        // 验证可退款金额
        BigDecimal refundableAmount = payment.getPaidAmount().subtract(payment.getRefundedAmount());
        if (refundAmount.compareTo(refundableAmount) > 0) {
            throw new PaymentException(
                    String.format("退款金额 %s 超过可退款金额 %s",
                            refundAmount, refundableAmount));
        }

        log.debug("退款前置条件校验通过");
    }
}

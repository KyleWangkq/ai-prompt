package com.bytz.cms.payment.domain.model;

import com.bytz.cms.payment.domain.entity.PaymentTransactionEntity;
import com.bytz.cms.payment.domain.enums.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * 支付单聚合根
 * 
 * <p>管理支付金额、状态与退款等业务，涵盖普通支付与信用还款两类</p>
 * <p>作为核心聚合根，负责支付单的完整生命周期管理，包括创建、支付执行、退款处理、状态管理</p>
 * 
 * <p>术语来源：Glossary.md - 核心业务术语"支付单(Payment)"</p>
 * <p>需求来源：需求文档4.1节支付单管理、4.4节支付单数据模型</p>
 * 
 * <p>相关用例：UC-PM-001至UC-PM-009</p>
 * 
 * @author DDD设计团队
 * @version 1.0.0
 * @since 2025-01-15
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PaymentAggregate {
    
    /**
     * 支付单号，唯一标识（32位字符，全局唯一）
     */
    private String id;
    
    /**
     * 关联订单号（32位字符，可选，信用还款支付单可能无订单号）
     */
    private String orderId;
    
    /**
     * 经销商ID（买方标识，32位字符，必填）
     */
    private String resellerId;
    
    /**
     * 目标支付金额（6位小数，必须为正数）
     */
    private BigDecimal paymentAmount;
    
    /**
     * 已支付金额（6位小数，累计成功支付金额，初始值0）
     */
    private BigDecimal paidAmount;
    
    /**
     * 已退款金额（6位小数，累计成功退款金额，初始值0）
     */
    private BigDecimal refundedAmount;
    
    /**
     * 实际收款金额（6位小数，计算公式：paidAmount - refundedAmount）
     */
    private BigDecimal actualAmount;
    
    /**
     * 币种（固定为CNY人民币，3位字符）
     */
    private String currency;
    
    /**
     * 支付类型枚举：ADVANCE_PAYMENT(预付款)/FINAL_PAYMENT(尾款)/OTHER_PAYMENT(其他费用)/CREDIT_REPAYMENT(信用还款)
     */
    private PaymentType paymentType;
    
    /**
     * 支付状态枚举：UNPAID(未支付)/PAYING(支付中)/PARTIAL_PAID(部分支付)/PAID(已支付)/FAILED(支付失败)/STOPPED(已停止)/FROZEN(已冻结)
     */
    private PaymentStatus paymentStatus;
    
    /**
     * 退款状态枚举：NO_REFUND(未退款)/REFUNDING(退款中)/PARTIAL_REFUNDED(部分退款)/FULL_REFUNDED(全额退款)/REFUND_FAILED(退款失败)
     */
    private RefundStatus refundStatus;
    
    /**
     * 支付截止时间（可选，建议的支付完成时间）
     */
    private LocalDateTime paymentDeadline;
    
    /**
     * 优先级（可选，1-高，2-中，3-低）
     */
    private Integer priorityLevel;
    
    /**
     * 关联业务ID（32位字符，可选，如信用记录ID、提货单ID等）
     */
    private String relatedBusinessId;
    
    /**
     * 关联业务类型枚举（可选，如CREDIT_RECORD/DELIVERY_ORDER/ORDER等）
     */
    private RelatedBusinessType relatedBusinessType;
    
    /**
     * 关联业务到期日（可选，如信用还款到期日、提货到期日等）
     */
    private LocalDateTime businessExpireDate;
    
    /**
     * 业务描述（500位字符，可选，支付用途说明）
     */
    private String businessDesc;
    
    /**
     * 业务标签（JSON字符串，可选，相关业务标记）
     */
    private String businessTags;
    
    /**
     * 删除状态（0-正常，1-删除）
     */
    private Integer delFlag;
    
    /**
     * 创建人ID（32位字符）
     */
    private String createBy;
    
    /**
     * 创建人姓名（32位字符）
     */
    private String createByName;
    
    /**
     * 创建时间（必填）
     */
    private LocalDateTime createdTime;
    
    /**
     * 更新人ID（32位字符）
     */
    private String updateBy;
    
    /**
     * 更新人姓名（32位字符）
     */
    private String updateByName;
    
    /**
     * 更新时间（必填）
     */
    private LocalDateTime updatedTime;

    /**
     * 支付流水列表（聚合内实体集合）
     */
    private List<PaymentTransactionEntity> transactions = new ArrayList<>();

    /**
     * 接收并创建支付单（UC-PM-001 / UC-PM-007），初始化为UNPAID并完成基础校验
     * 
     * <p>用例来源：UC-PM-001接收支付单创建请求（普通支付）、UC-PM-007创建信用还款支付单</p>
     * <p>业务规则：支付单号必须唯一，支付金额必须为正数</p>
     * <p>信用还款说明：信用还款支付单通过设置paymentType=CREDIT_REPAYMENT和relatedBusinessType=CREDIT_RECORD来标识，创建流程与普通支付单一致</p>
     * 
     * @param id 支付单号
     * @param orderId 订单号
     * @param resellerId 经销商ID
     * @param paymentAmount 支付金额
     * @param paymentType 支付类型
     * @param paymentDeadline 支付截止时间
     * @param priorityLevel 优先级
     * @param relatedBusinessId 关联业务ID
     * @param relatedBusinessType 关联业务类型
     * @param businessExpireDate 业务到期日
     * @param businessDesc 业务描述
     * @param businessTags 业务标签
     * @return 新创建的支付单聚合根
     */
    public static PaymentAggregate create(
            String id,
            String orderId,
            String resellerId,
            BigDecimal paymentAmount,
            PaymentType paymentType,
            LocalDateTime paymentDeadline,
            Integer priorityLevel,
            String relatedBusinessId,
            RelatedBusinessType relatedBusinessType,
            LocalDateTime businessExpireDate,
            String businessDesc,
            String businessTags) {
        
        // 验证必填字段
        if (id == null || id.trim().isEmpty()) {
            throw new IllegalArgumentException("支付单号不能为空");
        }
        if (resellerId == null || resellerId.trim().isEmpty()) {
            throw new IllegalArgumentException("经销商ID不能为空");
        }
        if (paymentAmount == null || paymentAmount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("支付金额必须大于0");
        }
        if (paymentType == null) {
            throw new IllegalArgumentException("支付类型不能为空");
        }

        PaymentAggregate payment = new PaymentAggregate();
        payment.setId(id);
        payment.setOrderId(orderId);
        payment.setResellerId(resellerId);
        payment.setPaymentAmount(paymentAmount);
        payment.setPaidAmount(BigDecimal.ZERO);
        payment.setRefundedAmount(BigDecimal.ZERO);
        payment.setActualAmount(BigDecimal.ZERO);
        payment.setCurrency("CNY");
        payment.setPaymentType(paymentType);
        payment.setPaymentStatus(PaymentStatus.UNPAID);
        payment.setRefundStatus(RefundStatus.NO_REFUND);
        payment.setPaymentDeadline(paymentDeadline);
        payment.setPriorityLevel(priorityLevel);
        payment.setRelatedBusinessId(relatedBusinessId);
        payment.setRelatedBusinessType(relatedBusinessType);
        payment.setBusinessExpireDate(businessExpireDate);
        payment.setBusinessDesc(businessDesc);
        payment.setBusinessTags(businessTags);
        payment.setDelFlag(0);
        payment.setCreatedTime(LocalDateTime.now());
        payment.setUpdatedTime(LocalDateTime.now());
        
        return payment;
    }

    /**
     * 将支付单状态置为PAYING（提交支付后，合并支付场景保持一致）
     * 
     * <p>用例来源：UC-PM-003执行支付操作、UC-PM-008执行信用还款支付</p>
     * <p>状态转换：UNPAID → PAYING</p>
     */
    public void markPaying() {
        if (!paymentStatus.isPayable()) {
            throw new IllegalStateException("当前支付单状态不允许支付：" + paymentStatus);
        }
        this.paymentStatus = PaymentStatus.PAYING;
        this.updatedTime = LocalDateTime.now();
    }

    /**
     * 应用一次支付成功的回调，增加已支付金额、重算实际收款并更新状态（UC-PM-004）
     * 
     * <p>用例来源：UC-PM-004处理支付回调</p>
     * <p>金额计算：paidAmount += amount, actualAmount = paidAmount - refundedAmount</p>
     * <p>状态更新：根据待支付金额是否为0自动更新为PARTIAL_PAID或PAID</p>
     * 
     * @param amount 本次支付金额
     * @param channel 支付渠道
     * @param channelTransactionNumber 渠道交易号
     * @param finishedAt 支付完成时间
     */
    public void applyPayment(
            BigDecimal amount,
            PaymentChannel channel,
            String channelTransactionNumber,
            LocalDateTime finishedAt) {
        
        // 增加已支付金额
        this.paidAmount = this.paidAmount.add(amount);
        
        // 重新计算实际收款金额
        this.actualAmount = this.paidAmount.subtract(this.refundedAmount);
        
        // 根据金额自动更新状态
        updateStatusByAmounts();
        
        this.updatedTime = LocalDateTime.now();
    }

    /**
     * 应用一次退款成功记录，增加已退款金额、重算实际收款并更新退款状态（UC-PM-006）
     * 
     * <p>用例来源：UC-PM-006接收退款执行指令</p>
     * <p>金额计算：refundedAmount += amount, actualAmount = paidAmount - refundedAmount</p>
     * <p>状态更新：根据退款比例更新为PARTIAL_REFUNDED或FULL_REFUNDED</p>
     * 
     * @param amount 退款金额（正数）
     * @param originalTransactionId 原支付流水ID
     * @param businessOrderId 业务单号（如退款单号）
     * @param reason 退款原因
     */
    public void applyRefund(
            BigDecimal amount,
            String originalTransactionId,
            String businessOrderId,
            String reason) {
        
        // 验证可退款金额
        BigDecimal refundableAmount = this.paidAmount.subtract(this.refundedAmount);
        if (amount.compareTo(refundableAmount) > 0) {
            throw new IllegalArgumentException("退款金额超过可退款金额");
        }
        
        // 增加已退款金额
        this.refundedAmount = this.refundedAmount.add(amount);
        
        // 重新计算实际收款金额
        this.actualAmount = this.paidAmount.subtract(this.refundedAmount);
        
        // 更新退款状态
        if (this.refundedAmount.compareTo(this.paidAmount) == 0) {
            this.refundStatus = RefundStatus.FULL_REFUNDED;
        } else if (this.refundedAmount.compareTo(BigDecimal.ZERO) > 0) {
            this.refundStatus = RefundStatus.PARTIAL_REFUNDED;
        }
        
        this.updatedTime = LocalDateTime.now();
    }

    /**
     * 根据待支付金额是否为0自动计算并更新支付状态（UNPAID/PARTIAL_PAID/PAID）与退款状态
     * 
     * <p>业务规则：待支付金额 = paymentAmount - paidAmount</p>
     * <p>支付状态：待支付金额 = 0 → PAID，待支付金额 > 0 且 paidAmount > 0 → PARTIAL_PAID</p>
     * <p>退款状态：refundedAmount = paidAmount → FULL_REFUNDED，0 < refundedAmount < paidAmount → PARTIAL_REFUNDED</p>
     */
    public void updateStatusByAmounts() {
        BigDecimal pendingAmount = this.paymentAmount.subtract(this.paidAmount);
        
        // 更新支付状态
        if (pendingAmount.compareTo(BigDecimal.ZERO) == 0) {
            this.paymentStatus = PaymentStatus.PAID;
        } else if (this.paidAmount.compareTo(BigDecimal.ZERO) > 0) {
            this.paymentStatus = PaymentStatus.PARTIAL_PAID;
        } else {
            this.paymentStatus = PaymentStatus.UNPAID;
        }
        
        // 更新退款状态
        if (this.refundedAmount.compareTo(this.paidAmount) == 0 && this.refundedAmount.compareTo(BigDecimal.ZERO) > 0) {
            this.refundStatus = RefundStatus.FULL_REFUNDED;
        } else if (this.refundedAmount.compareTo(BigDecimal.ZERO) > 0) {
            this.refundStatus = RefundStatus.PARTIAL_REFUNDED;
        } else {
            this.refundStatus = RefundStatus.NO_REFUND;
        }
        
        this.updatedTime = LocalDateTime.now();
    }

    /**
     * 校验当前支付单是否允许支付（状态、金额、截止时间等）
     * 
     * <p>用例来源：UC-PM-003执行支付操作前置校验</p>
     * <p>验证规则：状态必须为UNPAID或PARTIAL_PAID，待支付金额必须>0，未超过截止时间</p>
     * 
     * @throws IllegalStateException 如果不允许支付
     */
    public void validatePayable() {
        if (!paymentStatus.isPayable()) {
            throw new IllegalStateException("当前支付单状态不允许支付：" + paymentStatus);
        }
        
        BigDecimal pendingAmount = this.paymentAmount.subtract(this.paidAmount);
        if (pendingAmount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalStateException("支付单已无待支付金额");
        }
        
        if (paymentDeadline != null && LocalDateTime.now().isAfter(paymentDeadline)) {
            throw new IllegalStateException("支付单已超过截止时间");
        }
    }

    /**
     * 校验当前支付单是否允许退款（状态、可退款金额等）
     * 
     * <p>用例来源：UC-PM-006退款执行前置校验</p>
     * <p>验证规则：支付状态必须为PAID或PARTIAL_PAID，可退款金额 = paidAmount - refundedAmount >= refundAmount</p>
     * 
     * @param refundAmount 退款金额
     * @throws IllegalStateException 如果不允许退款
     */
    public void validateRefundable(BigDecimal refundAmount) {
        if (!paymentStatus.isRefundable()) {
            throw new IllegalStateException("当前支付单状态不允许退款：" + paymentStatus);
        }
        
        BigDecimal refundableAmount = this.paidAmount.subtract(this.refundedAmount);
        if (refundAmount.compareTo(refundableAmount) > 0) {
            throw new IllegalStateException("退款金额超过可退款金额");
        }
    }

    /**
     * 冻结支付单，状态置为FROZEN（异常/风控处理）
     * 
     * <p>状态转换：任何可操作状态 → FROZEN</p>
     * 
     * @param reason 冻结原因
     */
    public void freeze(String reason) {
        this.paymentStatus = PaymentStatus.FROZEN;
        this.businessDesc = (this.businessDesc != null ? this.businessDesc + "; " : "") + "冻结原因: " + reason;
        this.updatedTime = LocalDateTime.now();
    }

    /**
     * 解冻支付单，恢复冻结前可执行状态
     * 
     * <p>状态转换：FROZEN → 冻结前的状态</p>
     * 
     * TODO: 实现解冻逻辑，需要记录冻结前的状态以便恢复
     */
    public void unfreeze() {
        if (this.paymentStatus != PaymentStatus.FROZEN) {
            throw new IllegalStateException("支付单未冻结，无需解冻");
        }
        // TODO: 根据业务需求恢复到合适的状态
        this.updatedTime = LocalDateTime.now();
    }

    /**
     * 终止支付单，状态置为STOPPED（业务强制停止）
     * 
     * <p>状态转换：任何状态 → STOPPED（不可逆）</p>
     * 
     * @param reason 停止原因
     */
    public void stop(String reason) {
        this.paymentStatus = PaymentStatus.STOPPED;
        this.businessDesc = (this.businessDesc != null ? this.businessDesc + "; " : "") + "停止原因: " + reason;
        this.updatedTime = LocalDateTime.now();
    }

    /**
     * 计算待支付金额
     * 
     * @return 待支付金额
     */
    public BigDecimal getPendingAmount() {
        return this.paymentAmount.subtract(this.paidAmount);
    }

    /**
     * 计算可退款金额
     * 
     * @return 可退款金额
     */
    public BigDecimal getRefundableAmount() {
        return this.paidAmount.subtract(this.refundedAmount);
    }

    /**
     * 添加支付流水
     * 
     * @param transaction 支付流水实体
     */
    public void addTransaction(PaymentTransactionEntity transaction) {
        if (this.transactions == null) {
            this.transactions = new ArrayList<>();
        }
        this.transactions.add(transaction);
    }
}

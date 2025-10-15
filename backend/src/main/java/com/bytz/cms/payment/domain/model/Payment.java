package com.bytz.cms.payment.domain.model;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Payment Aggregate Root
 * 支付单聚合根，管理支付单的完整生命周期
 */
@Data
@Slf4j
public class Payment {
    
    // 支付单号，唯一标识
    private String id;
    
    // 关联订单号（信用还款可能无订单）
    private String orderId;
    
    // 经销商ID（买方标识）
    private String resellerId;
    
    // 目标支付金额
    private BigDecimal paymentAmount;
    
    // 已支付金额（累计成功支付）
    private BigDecimal paidAmount;
    
    // 已退款金额（累计成功退款）
    private BigDecimal refundedAmount;
    
    // 实际收款金额 = paidAmount - refundedAmount
    private BigDecimal actualAmount;
    
    // 支付类型：ADVANCE_PAYMENT/FINAL_PAYMENT/OTHER_FEE/CREDIT_REPAYMENT
    private PaymentType paymentType;
    
    // 支付状态
    private PaymentStatus paymentStatus;
    
    // 退款状态
    private RefundStatus refundStatus;
    
    // 支付截止时间（可选）
    private LocalDateTime paymentDeadline;
    
    // 优先级：1-高，2-中，3-低
    private Integer priorityLevel;
    
    // 关联业务信息
    private RelatedBusinessInfo relatedBusinessInfo;
    
    // 业务描述
    private String businessDesc;
    
    // 业务标签，JSON字符串
    private String businessTags;
    
    // 创建时间
    private LocalDateTime createdTime;
    
    // 更新时间
    private LocalDateTime updatedTime;
    
    // 创建人信息
    private String createBy;
    private String createByName;
    
    // 更新人信息
    private String updateBy;
    private String updateByName;
    
    // 删除标识
    private Integer delFlag;
    
    // 支付流水集合
    private List<PaymentTransaction> transactions = new ArrayList<>();
    
    // 私有构造函数，强制使用工厂方法
    private Payment() {
        this.paidAmount = BigDecimal.ZERO;
        this.refundedAmount = BigDecimal.ZERO;
        this.actualAmount = BigDecimal.ZERO;
        this.paymentStatus = PaymentStatus.UNPAID;
        this.refundStatus = RefundStatus.NO_REFUND;
        this.delFlag = 0;
    }
    
    /**
     * 创建支付单（工厂方法）
     * UC-PM-001 / UC-PM-007
     */
    public static Payment create(String id, String orderId, String resellerId, 
                                 BigDecimal paymentAmount, PaymentType paymentType,
                                 LocalDateTime paymentDeadline, Integer priorityLevel,
                                 RelatedBusinessInfo relatedBusinessInfo,
                                 String businessDesc, String businessTags) {
        // 业务验证
        if (paymentAmount == null || paymentAmount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("支付金额必须大于0");
        }
        if (resellerId == null || resellerId.trim().isEmpty()) {
            throw new IllegalArgumentException("经销商ID不能为空");
        }
        if (paymentType == null) {
            throw new IllegalArgumentException("支付类型不能为空");
        }
        
        Payment payment = new Payment();
        payment.id = id;
        payment.orderId = orderId;
        payment.resellerId = resellerId;
        payment.paymentAmount = paymentAmount;
        payment.paymentType = paymentType;
        payment.paymentDeadline = paymentDeadline;
        payment.priorityLevel = priorityLevel != null ? priorityLevel : 2; // 默认中优先级
        payment.relatedBusinessInfo = relatedBusinessInfo;
        payment.businessDesc = businessDesc;
        payment.businessTags = businessTags;
        payment.createdTime = LocalDateTime.now();
        payment.updatedTime = LocalDateTime.now();
        
        log.info("创建支付单: id={}, paymentAmount={}, paymentType={}", id, paymentAmount, paymentType);
        return payment;
    }
    
    /**
     * 标记为支付中状态
     * UC-PM-003
     */
    public void markPaying() {
        validatePayable();
        this.paymentStatus = PaymentStatus.PAYING;
        this.updatedTime = LocalDateTime.now();
        log.info("支付单{}标记为支付中", this.id);
    }
    
    /**
     * 应用支付成功回调
     * UC-PM-004
     */
    public void applyPayment(BigDecimal amount, String channel, 
                           String channelTransactionNumber, LocalDateTime finishedAt) {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("支付金额必须大于0");
        }
        
        // 更新已支付金额
        this.paidAmount = this.paidAmount.add(amount);
        
        // 重新计算实际收款金额
        this.actualAmount = this.paidAmount.subtract(this.refundedAmount);
        
        // 更新支付状态
        updateStatusByAmounts();
        
        this.updatedTime = LocalDateTime.now();
        
        log.info("支付单{}应用支付成功: amount={}, paidAmount={}, actualAmount={}", 
                this.id, amount, this.paidAmount, this.actualAmount);
    }
    
    /**
     * 应用退款成功记录
     * UC-PM-006
     */
    public void applyRefund(BigDecimal amount, String originalTransactionId, 
                          String businessOrderId, String reason) {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("退款金额必须大于0");
        }
        
        // 验证可退款金额
        BigDecimal refundableAmount = this.paidAmount.subtract(this.refundedAmount);
        if (amount.compareTo(refundableAmount) > 0) {
            throw new IllegalArgumentException(
                String.format("退款金额(%s)超过可退款金额(%s)", amount, refundableAmount));
        }
        
        // 更新已退款金额
        this.refundedAmount = this.refundedAmount.add(amount);
        
        // 重新计算实际收款金额
        this.actualAmount = this.paidAmount.subtract(this.refundedAmount);
        
        // 更新退款状态
        updateRefundStatus();
        
        this.updatedTime = LocalDateTime.now();
        
        log.info("支付单{}应用退款成功: amount={}, refundedAmount={}, actualAmount={}", 
                this.id, amount, this.refundedAmount, this.actualAmount);
    }
    
    /**
     * 根据金额自动更新状态
     */
    public void updateStatusByAmounts() {
        BigDecimal pendingAmount = this.paymentAmount.subtract(this.paidAmount);
        
        if (pendingAmount.compareTo(BigDecimal.ZERO) <= 0) {
            this.paymentStatus = PaymentStatus.PAID;
        } else if (this.paidAmount.compareTo(BigDecimal.ZERO) > 0) {
            this.paymentStatus = PaymentStatus.PARTIAL_PAID;
        } else {
            this.paymentStatus = PaymentStatus.UNPAID;
        }
        
        updateRefundStatus();
        this.updatedTime = LocalDateTime.now();
    }
    
    /**
     * 更新退款状态
     */
    private void updateRefundStatus() {
        if (this.refundedAmount.compareTo(BigDecimal.ZERO) == 0) {
            this.refundStatus = RefundStatus.NO_REFUND;
        } else if (this.refundedAmount.compareTo(this.paidAmount) >= 0) {
            this.refundStatus = RefundStatus.FULL_REFUNDED;
        } else {
            this.refundStatus = RefundStatus.PARTIAL_REFUNDED;
        }
    }
    
    /**
     * 校验是否可支付
     */
    public void validatePayable() {
        if (this.paymentStatus == PaymentStatus.PAID) {
            throw new IllegalStateException("支付单已完成支付，不能再次支付");
        }
        if (this.paymentStatus == PaymentStatus.STOPPED) {
            throw new IllegalStateException("支付单已停止，不能支付");
        }
        if (this.paymentStatus == PaymentStatus.FROZEN) {
            throw new IllegalStateException("支付单已冻结，不能支付");
        }
        
        BigDecimal pendingAmount = this.paymentAmount.subtract(this.paidAmount);
        if (pendingAmount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalStateException("支付单已无待支付金额");
        }
        
        // 检查是否超过截止时间
        if (this.paymentDeadline != null && LocalDateTime.now().isAfter(this.paymentDeadline)) {
            log.warn("支付单{}已超过截止时间: {}", this.id, this.paymentDeadline);
        }
    }
    
    /**
     * 校验是否可退款
     */
    public void validateRefundable(BigDecimal refundAmount) {
        if (this.paidAmount.compareTo(BigDecimal.ZERO) == 0) {
            throw new IllegalStateException("支付单未支付，不能退款");
        }
        
        BigDecimal refundableAmount = this.paidAmount.subtract(this.refundedAmount);
        if (refundAmount.compareTo(refundableAmount) > 0) {
            throw new IllegalArgumentException(
                String.format("退款金额(%s)超过可退款金额(%s)", refundAmount, refundableAmount));
        }
    }
    
    /**
     * 冻结支付单
     */
    public void freeze(String reason) {
        this.paymentStatus = PaymentStatus.FROZEN;
        this.updatedTime = LocalDateTime.now();
        log.info("支付单{}已冻结，原因: {}", this.id, reason);
    }
    
    /**
     * 解冻支付单
     */
    public void unfreeze() {
        if (this.paymentStatus != PaymentStatus.FROZEN) {
            throw new IllegalStateException("支付单当前状态不是冻结状态");
        }
        
        // 恢复到冻结前的状态
        updateStatusByAmounts();
        log.info("支付单{}已解冻", this.id);
    }
    
    /**
     * 停止支付单
     */
    public void stop(String reason) {
        this.paymentStatus = PaymentStatus.STOPPED;
        this.updatedTime = LocalDateTime.now();
        log.info("支付单{}已停止，原因: {}", this.id, reason);
    }
    
    /**
     * 获取待支付金额
     */
    public BigDecimal getPendingAmount() {
        return this.paymentAmount.subtract(this.paidAmount);
    }
    
    /**
     * 获取可退款金额
     */
    public BigDecimal getRefundableAmount() {
        return this.paidAmount.subtract(this.refundedAmount);
    }
    
    /**
     * 添加支付流水
     */
    public void addTransaction(PaymentTransaction transaction) {
        this.transactions.add(transaction);
    }
    
    /**
     * 获取支付流水（只读）
     */
    public List<PaymentTransaction> getTransactions() {
        return Collections.unmodifiableList(this.transactions);
    }
}

package com.bytz.cms.payment.domain.model;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * PaymentTransaction Entity
 * 支付流水实体，记录支付/退款的每次渠道交易与状态
 */
@Data
@Slf4j
public class PaymentTransaction {
    
    /**
     * 流水号，唯一标识
     */
    private String id;
    
    /**
     * 关联支付单号
     */
    private String paymentId;
    
    /**
     * 流水类型：PAYMENT/REFUND
     */
    private TransactionType transactionType;
    
    /**
     * 流水状态：PROCESSING/SUCCESS/FAILED
     */
    private TransactionStatus transactionStatus;
    
    /**
     * 交易金额（支付为正、退款为负）
     */
    private BigDecimal transactionAmount;
    
    /**
     * 支付渠道
     */
    private PaymentChannel paymentChannel;
    
    /**
     * 渠道交易号（可选，合并支付下用于关联多支付单）
     */
    private String channelTransactionNumber;
    
    /**
     * 支付方式（渠道下的具体方式，可选）
     */
    private String paymentWay;
    
    /**
     * 原交易流水号（退款回溯支付时使用）
     */
    private String originalTransactionId;
    
    /**
     * 业务单号（如退款单号）
     */
    private String businessOrderId;
    
    /**
     * 创建时间
     */
    private LocalDateTime createdTime;
    
    /**
     * 交易完成时间（可选）
     */
    private LocalDateTime completeDatetime;
    
    /**
     * 支付过期时间（可选）
     */
    private LocalDateTime expirationTime;
    
    /**
     * 业务备注
     */
    private String remark;
    
    // 私有构造函数
    private PaymentTransaction() {
        this.transactionStatus = TransactionStatus.PROCESSING;
        this.createdTime = LocalDateTime.now();
    }
    
    /**
     * 创建并启动流水（工厂方法）
     * UC-PM-003-11/12
     */
    public static PaymentTransaction start(String id, String paymentId,
                                          TransactionType transactionType,
                                          BigDecimal transactionAmount,
                                          PaymentChannel paymentChannel,
                                          String paymentWay,
                                          String channelTransactionNumber,
                                          LocalDateTime expirationTime) {
        // 业务验证
        if (transactionAmount == null || transactionAmount.compareTo(BigDecimal.ZERO) == 0) {
            throw new IllegalArgumentException("交易金额不能为0");
        }
        
        // 验证交易金额与类型一致性
        if (transactionType == TransactionType.PAYMENT && transactionAmount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("支付类型的交易金额必须大于0");
        }
        if (transactionType == TransactionType.REFUND && transactionAmount.compareTo(BigDecimal.ZERO) >= 0) {
            throw new IllegalArgumentException("退款类型的交易金额必须小于0");
        }
        
        PaymentTransaction transaction = new PaymentTransaction();
        transaction.id = id;
        transaction.paymentId = paymentId;
        transaction.transactionType = transactionType;
        transaction.transactionAmount = transactionAmount;
        transaction.paymentChannel = paymentChannel;
        transaction.paymentWay = paymentWay;
        transaction.channelTransactionNumber = channelTransactionNumber;
        transaction.expirationTime = expirationTime;
        
        log.info("创建支付流水: id={}, paymentId={}, type={}, amount={}, channel={}", 
                id, paymentId, transactionType, transactionAmount, paymentChannel);
        
        return transaction;
    }
    
    /**
     * 标记流水成功
     * UC-PM-004
     */
    public void success(LocalDateTime completedAt) {
        if (this.transactionStatus != TransactionStatus.PROCESSING) {
            throw new IllegalStateException("只有处理中的流水才能标记为成功");
        }
        
        this.transactionStatus = TransactionStatus.SUCCESS;
        this.completeDatetime = completedAt != null ? completedAt : LocalDateTime.now();
        
        log.info("支付流水{}标记为成功，完成时间: {}", this.id, this.completeDatetime);
    }
    
    /**
     * 标记流水失败
     */
    public void fail(String reason) {
        if (this.transactionStatus != TransactionStatus.PROCESSING) {
            throw new IllegalStateException("只有处理中的流水才能标记为失败");
        }
        
        this.transactionStatus = TransactionStatus.FAILED;
        this.remark = reason;
        
        log.warn("支付流水{}标记为失败，原因: {}", this.id, reason);
    }
    
    /**
     * 检查流水是否已过期
     */
    public boolean isExpired() {
        return expirationTime != null && LocalDateTime.now().isAfter(expirationTime);
    }
}

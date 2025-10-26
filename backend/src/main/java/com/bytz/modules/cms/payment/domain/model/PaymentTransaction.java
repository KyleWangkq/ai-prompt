package com.bytz.modules.cms.payment.domain.model;

import com.bytz.modules.cms.payment.domain.enums.PaymentChannel;
import com.bytz.modules.cms.payment.domain.enums.TransactionStatus;
import com.bytz.modules.cms.payment.domain.enums.TransactionType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 支付流水
 * Payment Transaction
 * 
 * 记录支付单的每一笔支付或退款操作流水
 * 注意：这是聚合内的领域对象，支付记录是流水表，除了交易状态，不应有其他数据修改
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaymentTransaction implements Comparable<PaymentTransaction> {

    /**
     * 数据库主键ID（使用雪花算法生成）
     */
    private String id;
    
    /**
     * 流水号（业务编码）
     */
    private String code;
    
    /**
     * 支付单ID（外键关联）
     */
    private String paymentId;
    
    /**
     * 流水类型（支付/退款）
     */
    private TransactionType transactionType;
    
    /**
     * 流水状态
     */
    private TransactionStatus transactionStatus;
    
    /**
     * 交易金额
     */
    private BigDecimal transactionAmount;
    
    /**
     * 支付渠道
     */
    private PaymentChannel paymentChannel;
    
    /**
     * 渠道交易号
     */
    private String channelTransactionNumber;
    
    /**
     * 渠道支付记录ID（渠道系统的支付记录唯一标识，用于数据关联）
     */
    private String channelPaymentRecordId;
    
    /**
     * 支付方式
     */
    private String paymentWay;
    
    /**
     * 原流水ID（退款时使用）
     */
    private String originalTransactionId;
    
    /**
     * 业务单号（如退款单号）
     */
    private String businessOrderId;
    
    /**
     * 创建时间
     */
    private LocalDateTime createTime;
    
    /**
     * 完成时间
     */
    private LocalDateTime completeDateTime;
    
    /**
     * 过期时间
     */
    private LocalDateTime expirationTime;
    
    /**
     * 业务备注
     */
    private String businessRemark;
    
    /**
     * 创建人
     */
    private String createBy;
    
    /**
     * 创建人姓名
     */
    private String createByName;
    
    /**
     * 更新人
     */
    private String updateBy;
    
    /**
     * 更新人姓名
     */
    private String updateByName;
    
    /**
     * 更新时间
     */
    private LocalDateTime updateTime;
    
    /**
     * 判断是否为支付流水
     * 
     * @return true如果是支付流水，否则false
     */
    public boolean isPaymentTransaction() {
        return TransactionType.PAYMENT.equals(this.transactionType);
    }
    
    /**
     * 判断是否为退款流水
     * 
     * @return true如果是退款流水，否则false
     */
    public boolean isRefundTransaction() {
        return TransactionType.REFUND.equals(this.transactionType);
    }
    
    /**
     * 判断流水是否成功
     * 
     * @return true如果流水成功，否则false
     */
    public boolean isSuccess() {
        return TransactionStatus.SUCCESS.equals(this.transactionStatus);
    }
    
    /**
     * 判断流水是否失败
     * 
     * @return true如果流水失败，否则false
     */
    public boolean isFailed() {
        return TransactionStatus.FAILED.equals(this.transactionStatus);
    }
    
    /**
     * 判断流水是否处理中
     * 
     * @return true如果流水处理中，否则false
     */
    public boolean isProcessing() {
        return TransactionStatus.PROCESSING.equals(this.transactionStatus);
    }
    
    /**
     * 更新流水状态为成功
     * 支付记录是流水表，除了交易状态，不应有其他数据修改
     * 
     * @param completeTime 完成时间
     */
    public void markAsSuccess(LocalDateTime completeTime) {
        this.transactionStatus = TransactionStatus.SUCCESS;
        this.completeDateTime = completeTime;
    }
    
    /**
     * 更新流水状态为失败
     * 支付记录是流水表，除了交易状态，不应有其他数据修改
     * 
     * @param failureReason 失败原因
     */
    public void markAsFailed(String failureReason) {
        this.transactionStatus = TransactionStatus.FAILED;
        this.businessRemark = failureReason;
    }
    
    /**
     * 更新渠道交易号
     * 支付记录是流水表，除了交易状态和渠道交易号，不应有其他数据修改
     * 
     * @param channelTransactionNumber 渠道交易号
     */
    public void updateChannelTransactionNumber(String channelTransactionNumber) {
        this.channelTransactionNumber = channelTransactionNumber;
    }

    @Override
    public int compareTo(PaymentTransaction other) {
        return this.createTime.compareTo(other.createTime);
    }
}
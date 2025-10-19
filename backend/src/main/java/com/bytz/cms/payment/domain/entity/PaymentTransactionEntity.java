package com.bytz.cms.payment.domain.entity;

import com.bytz.cms.payment.domain.enums.PaymentChannel;
import com.bytz.cms.payment.domain.enums.TransactionStatus;
import com.bytz.cms.payment.domain.enums.TransactionType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 支付流水实体
 * 
 * <p>记录支付单的每一笔支付或退款操作流水</p>
 * <p>作为Payment聚合内的核心实体，统一管理支付和退款流水，通过transactionType区分操作类型</p>
 * 
 * <p>术语来源：Glossary.md - 实体术语"支付流水实体(PaymentTransaction Entity)"</p>
 * <p>需求来源：需求文档4.4.2节支付流水表设计</p>
 * 
 * <p>相关用例：UC-PM-003、UC-PM-004、UC-PM-006</p>
 * 
 * @author DDD设计团队
 * @version 1.0.0
 * @since 2025-01-15
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PaymentTransactionEntity {
    
    /**
     * 流水号，唯一标识（32位字符，全局唯一）
     */
    private String id;
    
    /**
     * 关联支付单号（32位字符，外键关联Payment.id）
     */
    private String paymentId;
    
    /**
     * 流水类型枚举：PAYMENT(支付)/REFUND(退款)
     */
    private TransactionType transactionType;
    
    /**
     * 流水状态枚举：PROCESSING(处理中)/SUCCESS(成功)/FAILED(失败)
     */
    private TransactionStatus transactionStatus;
    
    /**
     * 交易金额（6位小数，支付为正数、退款为负数）
     */
    private BigDecimal transactionAmount;
    
    /**
     * 支付渠道枚举：ONLINE_PAYMENT(线上支付)/WALLET_PAYMENT(钱包支付)/WIRE_TRANSFER(电汇支付)/CREDIT_ACCOUNT(信用账户)
     */
    private PaymentChannel paymentChannel;
    
    /**
     * 渠道交易号（64位字符，可选，合并支付下用于关联多支付单，同一渠道内应唯一）
     */
    private String channelTransactionNumber;
    
    /**
     * 支付方式（20位字符，可选，渠道下的具体方式）
     */
    private String paymentWay;
    
    /**
     * 原交易流水号（32位字符，可选，退款回溯支付时使用，关联到被退款的支付流水）
     */
    private String originalTransactionId;
    
    /**
     * 业务单号（32位字符，可选，如退款单号）
     */
    private String businessOrderId;
    
    /**
     * 业务备注（500位字符，可选，交易备注信息）
     */
    private String businessRemark;
    
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
     * 交易完成时间（可选，成功或失败时记录）
     */
    private LocalDateTime completeDatetime;
    
    /**
     * 支付过期时间（可选，用于支付超时处理）
     */
    private LocalDateTime expirationTime;
    
    /**
     * 更新人ID（32位字符）
     */
    private String updateBy;
    
    /**
     * 更新人姓名（32位字符）
     */
    private String updateByName;
    
    /**
     * 更新时间（可选）
     */
    private LocalDateTime updatedTime;

    /**
     * 创建流水并置为PROCESSING（UC-PM-003-11/12）
     * 
     * <p>用例来源：UC-PM-003执行支付操作、UC-PM-006接收退款执行指令</p>
     * <p>初始状态：PROCESSING</p>
     * 
     * @param paymentId 支付单ID
     * @param transactionType 流水类型
     * @param transactionAmount 交易金额
     * @param paymentChannel 支付渠道
     * @param paymentWay 支付方式
     * @param channelTransactionNumber 渠道交易号
     * @param expirationTime 过期时间
     * @return 新创建的支付流水实体
     */
    public static PaymentTransactionEntity start(
            String paymentId,
            TransactionType transactionType,
            BigDecimal transactionAmount,
            PaymentChannel paymentChannel,
            String paymentWay,
            String channelTransactionNumber,
            LocalDateTime expirationTime) {
        
        PaymentTransactionEntity transaction = new PaymentTransactionEntity();
        transaction.setPaymentId(paymentId);
        transaction.setTransactionType(transactionType);
        transaction.setTransactionStatus(TransactionStatus.PROCESSING);
        transaction.setTransactionAmount(transactionAmount);
        transaction.setPaymentChannel(paymentChannel);
        transaction.setPaymentWay(paymentWay);
        transaction.setChannelTransactionNumber(channelTransactionNumber);
        transaction.setExpirationTime(expirationTime);
        transaction.setCreatedTime(LocalDateTime.now());
        transaction.setDelFlag(0);
        
        return transaction;
    }

    /**
     * 标记流水成功并记录完成时间（对应渠道回调成功，UC-PM-004）
     * 
     * <p>用例来源：UC-PM-004处理支付回调</p>
     * <p>状态转换：PROCESSING → SUCCESS</p>
     * 
     * @param completedAt 完成时间
     */
    public void success(LocalDateTime completedAt) {
        this.transactionStatus = TransactionStatus.SUCCESS;
        this.completeDatetime = completedAt;
        this.updatedTime = LocalDateTime.now();
    }

    /**
     * 标记流水失败（渠道回调失败或主动失败）
     * 
     * <p>状态转换：PROCESSING → FAILED</p>
     * 
     * @param reason 失败原因
     */
    public void fail(String reason) {
        this.transactionStatus = TransactionStatus.FAILED;
        this.businessRemark = reason;
        this.completeDatetime = LocalDateTime.now();
        this.updatedTime = LocalDateTime.now();
    }

    /**
     * 校验金额类型一致性
     * 
     * <p>业务规则：PAYMENT时transactionAmount>0，REFUND时transactionAmount<0</p>
     * <p>来源：需求文档4.4.2节支付流水表设计</p>
     * 
     * @return true表示一致，false表示不一致
     */
    public boolean validateAmountTypeConsistency() {
        if (transactionType == TransactionType.PAYMENT) {
            return transactionAmount.compareTo(BigDecimal.ZERO) > 0;
        } else if (transactionType == TransactionType.REFUND) {
            return transactionAmount.compareTo(BigDecimal.ZERO) < 0;
        }
        return false;
    }
}

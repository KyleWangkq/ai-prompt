package com.bytz.cms.payment.shared.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 支付已执行事件
 * 
 * <p>触发时机：支付回调处理完成并更新支付单状态后</p>
 * <p>订阅者：订单系统、财务系统、信用管理系统</p>
 * 
 * <p>术语来源：Glossary.md - 领域事件术语"支付已执行事件(PaymentExecuted)"</p>
 * <p>用例来源：UC-PM-004</p>
 * 
 * @author DDD设计团队
 * @version 1.0.0
 * @since 2025-01-15
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PaymentExecutedEvent {
    
    /**
     * 支付单号
     */
    private String paymentId;
    
    /**
     * 订单号（可选）
     */
    private String orderId;
    
    /**
     * 经销商ID
     */
    private String resellerId;
    
    /**
     * 本次支付金额
     */
    private BigDecimal paymentAmount;
    
    /**
     * 累计已支付金额
     */
    private BigDecimal paidAmount;
    
    /**
     * 总应付金额
     */
    private BigDecimal totalAmount;
    
    /**
     * 支付状态
     */
    private String paymentStatus;
    
    /**
     * 支付类型
     */
    private String paymentType;
    
    /**
     * 支付渠道
     */
    private String paymentChannel;
    
    /**
     * 渠道交易号
     */
    private String channelTransactionNumber;
    
    /**
     * 关联业务ID（可选，用于信用还款等场景）
     */
    private String relatedBusinessId;
    
    /**
     * 关联业务类型（可选）
     */
    private String relatedBusinessType;
    
    /**
     * 事件发生时间
     */
    private LocalDateTime eventTime;

    /**
     * 创建支付已执行事件
     * 
     * @param paymentId 支付单号
     * @param orderId 订单号
     * @param resellerId 经销商ID
     * @param paymentAmount 本次支付金额
     * @param paidAmount 累计已支付金额
     * @param totalAmount 总应付金额
     * @param paymentStatus 支付状态
     * @param paymentType 支付类型
     * @param paymentChannel 支付渠道
     * @param channelTransactionNumber 渠道交易号
     * @param relatedBusinessId 关联业务ID
     * @param relatedBusinessType 关联业务类型
     * @return 事件对象
     */
    public static PaymentExecutedEvent of(
            String paymentId,
            String orderId,
            String resellerId,
            BigDecimal paymentAmount,
            BigDecimal paidAmount,
            BigDecimal totalAmount,
            String paymentStatus,
            String paymentType,
            String paymentChannel,
            String channelTransactionNumber,
            String relatedBusinessId,
            String relatedBusinessType) {
        return new PaymentExecutedEvent(
                paymentId,
                orderId,
                resellerId,
                paymentAmount,
                paidAmount,
                totalAmount,
                paymentStatus,
                paymentType,
                paymentChannel,
                channelTransactionNumber,
                relatedBusinessId,
                relatedBusinessType,
                LocalDateTime.now()
        );
    }
}

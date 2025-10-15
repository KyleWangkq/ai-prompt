package com.bytz.cms.payment.shared.model;

import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 支付已执行事件
 * Payment Executed Event
 * 
 * 术语来源：Glossary.md - 领域事件术语"支付已执行事件(PaymentExecuted)"
 * 触发时机：支付回调处理完成并更新支付单状态后
 * 订阅者：订单系统、财务系统、信用管理系统
 * 用例来源：UC-PM-004
 */
@Data
@Builder
@EqualsAndHashCode(callSuper = false)
public class PaymentExecutedEvent extends DomainEvent {
    
    private static final long serialVersionUID = 1L;
    
    /**
     * 支付单号
     */
    private String paymentId;
    
    /**
     * 订单号
     */
    private String orderId;
    
    /**
     * 支付流水号
     */
    private String transactionId;
    
    /**
     * 支付金额
     */
    private BigDecimal paymentAmount;
    
    /**
     * 已支付金额
     */
    private BigDecimal paidAmount;
    
    /**
     * 实际收款金额
     */
    private BigDecimal actualAmount;
    
    /**
     * 支付状态
     */
    private String paymentStatus;
    
    /**
     * 支付渠道
     */
    private String paymentChannel;
    
    /**
     * 渠道交易号
     */
    private String channelTransactionNumber;
    
    /**
     * 完成时间
     */
    private LocalDateTime completedTime;
}

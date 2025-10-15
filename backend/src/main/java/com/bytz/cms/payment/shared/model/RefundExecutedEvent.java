package com.bytz.cms.payment.shared.model;

import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 退款已执行事件
 * Refund Executed Event
 * 
 * 术语来源：Glossary.md - 领域事件术语"退款已执行事件(RefundExecuted)"
 * 触发时机：退款完成并更新支付单状态后
 * 订阅者：订单系统、财务系统
 * 用例来源：UC-PM-006
 */
@Data
@Builder
@EqualsAndHashCode(callSuper = false)
public class RefundExecutedEvent extends DomainEvent {
    
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
     * 退款流水号
     */
    private String refundTransactionId;
    
    /**
     * 退款金额
     */
    private BigDecimal refundAmount;
    
    /**
     * 已退款金额
     */
    private BigDecimal totalRefundedAmount;
    
    /**
     * 实际收款金额
     */
    private BigDecimal actualAmount;
    
    /**
     * 退款状态
     */
    private String refundStatus;
    
    /**
     * 原支付流水号
     */
    private String originalTransactionId;
    
    /**
     * 业务单号
     */
    private String businessOrderId;
    
    /**
     * 完成时间
     */
    private LocalDateTime completedTime;
}

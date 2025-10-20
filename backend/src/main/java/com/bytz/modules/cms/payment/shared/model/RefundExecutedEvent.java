package com.bytz.modules.cms.payment.shared.model;

import com.bytz.modules.cms.payment.domain.enums.RefundStatus;
import lombok.Getter;
import org.springframework.context.ApplicationEvent;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 退款已执行事件
 * Refund Executed Event
 * 
 * 当退款完成并更新支付单状态后发布此事件
 */
@Getter
public class RefundExecutedEvent extends ApplicationEvent {
    
    /**
     * 事件ID
     */
    private final String eventId;
    
    /**
     * 支付单号
     */
    private final String paymentId;
    
    /**
     * 关联订单号
     */
    private final String orderId;
    
    /**
     * 经销商ID
     */
    private final String resellerId;
    
    /**
     * 退款流水号
     */
    private final String transactionId;
    
    /**
     * 本次退款金额
     */
    private final BigDecimal refundAmount;
    
    /**
     * 已退款总金额
     */
    private final BigDecimal totalRefundedAmount;
    
    /**
     * 退款状态
     */
    private final RefundStatus refundStatus;
    
    /**
     * 是否退款成功
     */
    private final Boolean success;
    
    /**
     * 退款单号（订单系统）
     */
    private final String refundOrderId;
    
    /**
     * 事件发生时间
     */
    private final LocalDateTime occurredOn;
    
    /**
     * 构造函数
     */
    public RefundExecutedEvent(Object source, String eventId, String paymentId, String orderId,
                              String resellerId, String transactionId, BigDecimal refundAmount,
                              BigDecimal totalRefundedAmount, RefundStatus refundStatus, Boolean success,
                              String refundOrderId, LocalDateTime occurredOn) {
        super(source);
        this.eventId = eventId;
        this.paymentId = paymentId;
        this.orderId = orderId;
        this.resellerId = resellerId;
        this.transactionId = transactionId;
        this.refundAmount = refundAmount;
        this.totalRefundedAmount = totalRefundedAmount;
        this.refundStatus = refundStatus;
        this.success = success;
        this.refundOrderId = refundOrderId;
        this.occurredOn = occurredOn;
    }
}

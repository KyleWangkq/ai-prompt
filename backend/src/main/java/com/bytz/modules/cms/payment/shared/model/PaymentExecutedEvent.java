package com.bytz.modules.cms.payment.shared.model;

import com.bytz.modules.cms.payment.domain.enums.PaymentStatus;
import lombok.Getter;
import org.springframework.context.ApplicationEvent;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 支付已执行事件
 * Payment Executed Event
 * 
 * 当支付回调处理完成并更新支付单状态后发布此事件
 */
@Getter
public class PaymentExecutedEvent extends ApplicationEvent {
    
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
     * 支付流水号
     */
    private final String transactionId;
    
    /**
     * 本次支付金额
     */
    private final BigDecimal transactionAmount;
    
    /**
     * 已支付金额
     */
    private final BigDecimal paidAmount;
    
    /**
     * 支付单总金额
     */
    private final BigDecimal totalAmount;
    
    /**
     * 支付状态
     */
    private final PaymentStatus paymentStatus;
    
    /**
     * 是否支付成功
     */
    private final Boolean success;
    
    /**
     * 关联业务ID（用于信用还款）
     */
    private final String relatedBusinessId;
    
    /**
     * 事件发生时间
     */
    private final LocalDateTime occurredOn;
    
    /**
     * 构造函数
     */
    public PaymentExecutedEvent(Object source, String eventId, String paymentId, String orderId, 
                               String resellerId, String transactionId, BigDecimal transactionAmount,
                               BigDecimal paidAmount, BigDecimal totalAmount, PaymentStatus paymentStatus,
                               Boolean success, String relatedBusinessId, LocalDateTime occurredOn) {
        super(source);
        this.eventId = eventId;
        this.paymentId = paymentId;
        this.orderId = orderId;
        this.resellerId = resellerId;
        this.transactionId = transactionId;
        this.transactionAmount = transactionAmount;
        this.paidAmount = paidAmount;
        this.totalAmount = totalAmount;
        this.paymentStatus = paymentStatus;
        this.success = success;
        this.relatedBusinessId = relatedBusinessId;
        this.occurredOn = occurredOn;
    }
}

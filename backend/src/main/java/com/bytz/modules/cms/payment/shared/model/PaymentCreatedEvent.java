package com.bytz.modules.cms.payment.shared.model;

import com.bytz.modules.cms.payment.domain.enums.PaymentType;
import lombok.Getter;
import org.springframework.context.ApplicationEvent;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 支付单已创建事件
 * Payment Created Event
 * 
 * 当支付单创建成功后发布此事件
 */
@Getter
public class PaymentCreatedEvent extends ApplicationEvent {
    
    /**
     * 事件ID
     */
    private final String eventId;
    
    /**
     * 支付单号
     */
    private final String paymentCode;
    
    /**
     * 关联订单号
     */
    private final String orderId;
    
    /**
     * 经销商ID
     */
    private final String resellerId;
    
    /**
     * 支付金额
     */
    private final BigDecimal paymentAmount;
    
    /**
     * 支付类型
     */
    private final PaymentType paymentType;
    
    /**
     * 关联业务ID
     */
    private final String relatedBusinessId;
    
    /**
     * 事件发生时间
     */
    private final LocalDateTime occurredOn;
    
    /**
     * 构造函数
     */
    public PaymentCreatedEvent(Object source, String eventId, String paymentCode, String orderId, 
                              String resellerId, BigDecimal paymentAmount, PaymentType paymentType, 
                              String relatedBusinessId, LocalDateTime occurredOn) {
        super(source);
        this.eventId = eventId;
        this.paymentCode = paymentCode;
        this.orderId = orderId;
        this.resellerId = resellerId;
        this.paymentAmount = paymentAmount;
        this.paymentType = paymentType;
        this.relatedBusinessId = relatedBusinessId;
        this.occurredOn = occurredOn;
    }
}

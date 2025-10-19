package com.bytz.cms.payment.shared.model;

import com.bytz.cms.payment.domain.enums.PaymentType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 支付单已创建事件
 * Payment Created Event
 * 
 * 当支付单创建成功后发布此事件
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaymentCreatedEvent {
    
    /**
     * 事件ID
     */
    private String eventId;
    
    /**
     * 支付单号
     */
    private String paymentId;
    
    /**
     * 关联订单号
     */
    private String orderId;
    
    /**
     * 经销商ID
     */
    private String resellerId;
    
    /**
     * 支付金额
     */
    private BigDecimal paymentAmount;
    
    /**
     * 支付类型
     */
    private PaymentType paymentType;
    
    /**
     * 关联业务ID
     */
    private String relatedBusinessId;
    
    /**
     * 事件发生时间
     */
    private LocalDateTime occurredOn;
}

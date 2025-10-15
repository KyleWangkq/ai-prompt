package com.bytz.cms.payment.shared.model;

import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 支付单已创建事件
 * Payment Created Event
 * 
 * 术语来源：Glossary.md - 领域事件术语"支付单已创建事件(PaymentCreated)"
 * 触发时机：支付单创建成功后
 * 用例来源：UC-PM-001, UC-PM-007
 */
@Data
@Builder
@EqualsAndHashCode(callSuper = false)
public class PaymentCreatedEvent extends DomainEvent {
    
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
    private String paymentType;
    
    /**
     * 关联业务ID
     */
    private String relatedBusinessId;
    
    /**
     * 关联业务类型
     */
    private String relatedBusinessType;
    
    /**
     * 创建时间
     */
    private LocalDateTime createdTime;
}

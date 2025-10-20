package com.bytz.modules.cms.payment.shared.model;

import com.bytz.modules.cms.payment.domain.enums.PaymentStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 支付已执行事件
 * Payment Executed Event
 * 
 * 当支付回调处理完成并更新支付单状态后发布此事件
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaymentExecutedEvent {
    
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
     * 支付流水号
     */
    private String transactionId;
    
    /**
     * 本次支付金额
     */
    private BigDecimal transactionAmount;
    
    /**
     * 已支付金额
     */
    private BigDecimal paidAmount;
    
    /**
     * 支付单总金额
     */
    private BigDecimal totalAmount;
    
    /**
     * 支付状态
     */
    private PaymentStatus paymentStatus;
    
    /**
     * 是否支付成功
     */
    private Boolean success;
    
    /**
     * 关联业务ID（用于信用还款）
     */
    private String relatedBusinessId;
    
    /**
     * 事件发生时间
     */
    private LocalDateTime occurredOn;
}

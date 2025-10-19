package com.bytz.cms.payment.shared.model;

import com.bytz.cms.payment.domain.enums.RefundStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 退款已执行事件
 * Refund Executed Event
 * 
 * 当退款完成并更新支付单状态后发布此事件
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RefundExecutedEvent {
    
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
     * 退款流水号
     */
    private String transactionId;
    
    /**
     * 本次退款金额
     */
    private BigDecimal refundAmount;
    
    /**
     * 已退款总金额
     */
    private BigDecimal totalRefundedAmount;
    
    /**
     * 退款状态
     */
    private RefundStatus refundStatus;
    
    /**
     * 是否退款成功
     */
    private Boolean success;
    
    /**
     * 退款单号（订单系统）
     */
    private String refundOrderId;
    
    /**
     * 事件发生时间
     */
    private LocalDateTime occurredOn;
}

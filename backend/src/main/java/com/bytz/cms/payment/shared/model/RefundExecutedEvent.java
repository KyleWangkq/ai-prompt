package com.bytz.cms.payment.shared.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 退款已执行事件
 * 
 * <p>触发时机：退款完成并更新支付单状态后</p>
 * <p>订阅者：订单系统、财务系统</p>
 * 
 * <p>术语来源：Glossary.md - 领域事件术语"退款已执行事件(RefundExecuted)"</p>
 * <p>用例来源：UC-PM-006</p>
 * 
 * @author DDD设计团队
 * @version 1.0.0
 * @since 2025-01-15
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class RefundExecutedEvent {
    
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
     * 本次退款金额
     */
    private BigDecimal refundAmount;
    
    /**
     * 累计已退款金额
     */
    private BigDecimal totalRefundedAmount;
    
    /**
     * 退款状态
     */
    private String refundStatus;
    
    /**
     * 退款单号
     */
    private String refundOrderId;
    
    /**
     * 原支付流水号
     */
    private String originalTransactionId;
    
    /**
     * 退款原因
     */
    private String refundReason;
    
    /**
     * 事件发生时间
     */
    private LocalDateTime eventTime;

    /**
     * 创建退款已执行事件
     * 
     * @param paymentId 支付单号
     * @param orderId 订单号
     * @param resellerId 经销商ID
     * @param refundAmount 本次退款金额
     * @param totalRefundedAmount 累计已退款金额
     * @param refundStatus 退款状态
     * @param refundOrderId 退款单号
     * @param originalTransactionId 原支付流水号
     * @param refundReason 退款原因
     * @return 事件对象
     */
    public static RefundExecutedEvent of(
            String paymentId,
            String orderId,
            String resellerId,
            BigDecimal refundAmount,
            BigDecimal totalRefundedAmount,
            String refundStatus,
            String refundOrderId,
            String originalTransactionId,
            String refundReason) {
        return new RefundExecutedEvent(
                paymentId,
                orderId,
                resellerId,
                refundAmount,
                totalRefundedAmount,
                refundStatus,
                refundOrderId,
                originalTransactionId,
                refundReason,
                LocalDateTime.now()
        );
    }
}

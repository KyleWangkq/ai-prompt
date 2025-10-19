package com.bytz.cms.payment.shared.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 支付单已创建事件
 * 
 * <p>触发时机：支付单创建成功后</p>
 * <p>订阅者：无</p>
 * 
 * <p>术语来源：Glossary.md - 领域事件术语"支付单已创建事件(PaymentCreated)"</p>
 * <p>用例来源：UC-PM-001、UC-PM-007</p>
 * 
 * @author DDD设计团队
 * @version 1.0.0
 * @since 2025-01-15
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PaymentCreatedEvent {
    
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
     * 支付金额
     */
    private BigDecimal paymentAmount;
    
    /**
     * 支付类型
     */
    private String paymentType;
    
    /**
     * 关联业务ID（可选）
     */
    private String relatedBusinessId;
    
    /**
     * 关联业务类型（可选）
     */
    private String relatedBusinessType;
    
    /**
     * 事件发生时间
     */
    private LocalDateTime eventTime;

    /**
     * 创建支付单已创建事件
     * 
     * @param paymentId 支付单号
     * @param orderId 订单号
     * @param resellerId 经销商ID
     * @param paymentAmount 支付金额
     * @param paymentType 支付类型
     * @param relatedBusinessId 关联业务ID
     * @param relatedBusinessType 关联业务类型
     * @return 事件对象
     */
    public static PaymentCreatedEvent of(
            String paymentId,
            String orderId,
            String resellerId,
            BigDecimal paymentAmount,
            String paymentType,
            String relatedBusinessId,
            String relatedBusinessType) {
        return new PaymentCreatedEvent(
                paymentId,
                orderId,
                resellerId,
                paymentAmount,
                paymentType,
                relatedBusinessId,
                relatedBusinessType,
                LocalDateTime.now()
        );
    }
}

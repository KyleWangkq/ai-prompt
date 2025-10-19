package com.bytz.cms.payment.interfaces.model;

import com.bytz.cms.payment.domain.enums.PaymentStatus;
import com.bytz.cms.payment.domain.enums.PaymentType;
import com.bytz.cms.payment.domain.enums.RefundStatus;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 支付单响应对象
 * 
 * <p>返回支付单基本信息</p>
 * <p>术语来源：Glossary.md - DTO术语"支付单响应(PaymentResponse)"</p>
 * <p>相关用例：UC-PM-001、UC-PM-002、UC-PM-005、UC-PM-007</p>
 * 
 * @author DDD设计团队
 * @version 1.0.0
 * @since 2025-01-15
 */
@Data
public class PaymentVO {
    
    /**
     * 支付单号
     */
    private String id;
    
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
     * 已支付金额
     */
    private BigDecimal paidAmount;
    
    /**
     * 已退款金额
     */
    private BigDecimal refundedAmount;
    
    /**
     * 实际收款金额
     */
    private BigDecimal actualAmount;
    
    /**
     * 待支付金额
     */
    private BigDecimal pendingAmount;
    
    /**
     * 币种
     */
    private String currency;
    
    /**
     * 支付类型
     */
    private PaymentType paymentType;
    
    /**
     * 支付状态
     */
    private PaymentStatus paymentStatus;
    
    /**
     * 退款状态
     */
    private RefundStatus refundStatus;
    
    /**
     * 支付截止时间
     */
    private LocalDateTime paymentDeadline;
    
    /**
     * 优先级
     */
    private Integer priorityLevel;
    
    /**
     * 业务描述
     */
    private String businessDesc;
    
    /**
     * 创建时间
     */
    private LocalDateTime createdTime;
    
    /**
     * 更新时间
     */
    private LocalDateTime updatedTime;
}

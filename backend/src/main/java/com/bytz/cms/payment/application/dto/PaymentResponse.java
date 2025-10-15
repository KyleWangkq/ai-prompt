package com.bytz.cms.payment.application.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Payment Response DTO
 * 支付单响应DTO
 */
@Data
public class PaymentResponse {
    
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
     * 支付类型
     */
    private String paymentType;
    
    /**
     * 支付状态
     */
    private String paymentStatus;
    
    /**
     * 退款状态
     */
    private String refundStatus;
    
    /**
     * 支付截止时间
     */
    private LocalDateTime paymentDeadline;
    
    /**
     * 优先级
     */
    private Integer priorityLevel;
    
    /**
     * 关联业务ID
     */
    private String relatedBusinessId;
    
    /**
     * 关联业务类型
     */
    private String relatedBusinessType;
    
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

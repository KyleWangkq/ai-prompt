package com.bytz.cms.payment.interfaces.model.vo;

import com.bytz.cms.payment.shared.enums.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 支付单响应对象
 * Payment Response Object
 * 
 * 术语来源：Glossary.md - DTO术语"支付单响应(PaymentResponse)"
 * 用例来源：UC-PM-001, UC-PM-002, UC-PM-005, UC-PM-007
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaymentVO {
    
    /**
     * 支付单号
     */
    private String id;
    
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
     * 待支付金额（计算值）
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
     * 关联业务ID
     */
    private String relatedBusinessId;
    
    /**
     * 关联业务类型
     */
    private RelatedBusinessType relatedBusinessType;
    
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

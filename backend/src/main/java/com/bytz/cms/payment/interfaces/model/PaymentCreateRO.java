package com.bytz.cms.payment.interfaces.model;

import com.bytz.cms.payment.domain.enums.PaymentType;
import com.bytz.cms.payment.domain.enums.RelatedBusinessType;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Positive;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 创建支付单请求对象
 * 
 * <p>用于接收订单系统或信用管理系统创建支付单的请求</p>
 * <p>术语来源：Glossary.md - DTO术语"创建支付单请求(CreatePaymentRequest)"</p>
 * <p>相关用例：UC-PM-001、UC-PM-007</p>
 * 
 * @author DDD设计团队
 * @version 1.0.0
 * @since 2025-01-15
 */
@Data
public class PaymentCreateRO {
    
    /**
     * 关联订单号（可选，信用还款支付单可能无订单号）
     */
    private String orderId;
    
    /**
     * 经销商ID（必填）
     */
    @NotBlank(message = "经销商ID不能为空")
    private String resellerId;
    
    /**
     * 支付金额（必填，必须为正数）
     */
    @NotNull(message = "支付金额不能为空")
    @Positive(message = "支付金额必须为正数")
    private BigDecimal paymentAmount;
    
    /**
     * 支付类型（必填）
     */
    @NotNull(message = "支付类型不能为空")
    private PaymentType paymentType;
    
    /**
     * 支付截止时间（可选）
     */
    private LocalDateTime paymentDeadline;
    
    /**
     * 优先级（可选，1-高，2-中，3-低）
     */
    private Integer priorityLevel;
    
    /**
     * 关联业务ID（可选）
     */
    private String relatedBusinessId;
    
    /**
     * 关联业务类型（可选）
     */
    private RelatedBusinessType relatedBusinessType;
    
    /**
     * 关联业务到期日（可选）
     */
    private LocalDateTime businessExpireDate;
    
    /**
     * 业务描述（可选）
     */
    private String businessDesc;
    
    /**
     * 业务标签（可选）
     */
    private String businessTags;
    
    /**
     * 创建人ID（必填）
     */
    @NotBlank(message = "创建人ID不能为空")
    private String createBy;
    
    /**
     * 创建人姓名（必填）
     */
    @NotBlank(message = "创建人姓名不能为空")
    private String createByName;
}

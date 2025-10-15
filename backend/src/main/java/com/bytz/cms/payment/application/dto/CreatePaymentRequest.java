package com.bytz.cms.payment.application.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Positive;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Create Payment Request DTO
 * 创建支付单请求DTO
 */
@Data
public class CreatePaymentRequest {
    
    /**
     * 关联订单号
     */
    private String orderId;
    
    /**
     * 经销商ID
     */
    @NotBlank(message = "经销商ID不能为空")
    private String resellerId;
    
    /**
     * 支付金额
     */
    @NotNull(message = "支付金额不能为空")
    @Positive(message = "支付金额必须大于0")
    private BigDecimal paymentAmount;
    
    /**
     * 支付类型
     */
    @NotBlank(message = "支付类型不能为空")
    private String paymentType;
    
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
     * 业务到期日
     */
    private LocalDateTime businessExpireDate;
    
    /**
     * 业务描述
     */
    private String businessDesc;
    
    /**
     * 业务标签
     */
    private String businessTags;
}

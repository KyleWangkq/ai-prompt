package com.bytz.cms.payment.interfaces.model;

import com.bytz.cms.payment.domain.enums.PaymentType;
import com.bytz.cms.payment.domain.enums.RelatedBusinessType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.DecimalMin;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 创建支付单请求对象
 * Create Payment Request Object
 * 
 * 用于接收订单系统或信用管理系统创建支付单的请求
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaymentCreateRO {
    
    /**
     * 关联订单号（必填）
     */
    @NotBlank(message = "订单号不能为空")
    private String orderId;
    
    /**
     * 经销商ID（必填）
     */
    @NotBlank(message = "经销商ID不能为空")
    private String resellerId;
    
    /**
     * 支付金额（必填）
     */
    @NotNull(message = "支付金额不能为空")
    @DecimalMin(value = "0.01", message = "支付金额必须大于0")
    private BigDecimal paymentAmount;
    
    /**
     * 币种（可选，默认CNY）
     */
    private String currency;
    
    /**
     * 支付类型（必填）
     */
    @NotNull(message = "支付类型不能为空")
    private PaymentType paymentType;
    
    /**
     * 业务描述（可选）
     */
    private String businessDesc;
    
    /**
     * 支付截止时间（可选）
     */
    private LocalDateTime paymentDeadline;
    
    /**
     * 优先级（可选，1-高，2-中，3-低）
     */
    private Integer priorityLevel;
    
    /**
     * 关联业务ID（可选，信用还款时必填）
     */
    private String relatedBusinessId;
    
    /**
     * 关联业务类型（可选，信用还款时必填）
     */
    private RelatedBusinessType relatedBusinessType;
    
    /**
     * 业务到期日（可选）
     */
    private LocalDateTime businessExpireDate;
    
    /**
     * 业务标签（可选，JSON格式）
     */
    private String businessTags;
}

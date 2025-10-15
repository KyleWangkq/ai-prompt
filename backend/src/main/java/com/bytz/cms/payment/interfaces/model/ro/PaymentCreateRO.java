package com.bytz.cms.payment.interfaces.model.ro;

import com.bytz.cms.payment.shared.enums.PaymentType;
import com.bytz.cms.payment.shared.enums.RelatedBusinessType;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Positive;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 创建支付单请求对象
 * Create Payment Request Object
 * 
 * 术语来源：Glossary.md - DTO术语"创建支付单请求(CreatePaymentRequest)"
 * 用例来源：UC-PM-001, UC-PM-007
 */
@Data
public class PaymentCreateRO {
    
    /**
     * 订单号（信用还款时可为空）
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
    @NotNull(message = "支付类型不能为空")
    private PaymentType paymentType;
    
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
     * 关联业务到期日
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

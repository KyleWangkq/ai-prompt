package com.bytz.cms.payment.interfaces.model.ro;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Positive;
import java.math.BigDecimal;

/**
 * 退款执行请求对象
 * Refund Execution Request Object
 * 
 * 术语来源：Glossary.md - DTO术语"退款执行请求(RefundExecutionRequest)"
 * 用例来源：UC-PM-006
 */
@Data
public class RefundExecutionRO {
    
    /**
     * 支付单号
     */
    @NotBlank(message = "支付单号不能为空")
    private String paymentId;
    
    /**
     * 退款金额
     */
    @NotNull(message = "退款金额不能为空")
    @Positive(message = "退款金额必须大于0")
    private BigDecimal refundAmount;
    
    /**
     * 原支付流水号（可选，指定退款流水）
     */
    private String originalTransactionId;
    
    /**
     * 业务单号（如退款单号）
     */
    private String businessOrderId;
    
    /**
     * 退款原因
     */
    @NotBlank(message = "退款原因不能为空")
    private String reason;
}

package com.bytz.cms.payment.interfaces.model.ro;

import com.bytz.cms.payment.shared.enums.PaymentChannel;
import lombok.Data;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Positive;
import java.math.BigDecimal;
import java.util.Map;

/**
 * 支付执行请求对象
 * Payment Execution Request Object
 * 
 * 术语来源：Glossary.md - DTO术语"支付执行请求(PaymentExecutionRequest)"
 * 用例来源：UC-PM-003, UC-PM-008
 */
@Data
public class PaymentExecutionRO {
    
    /**
     * 支付单号列表（单支付单或多支付单合并支付）
     */
    @NotEmpty(message = "支付单号列表不能为空")
    private java.util.List<String> paymentIds;
    
    /**
     * 金额分配（支付单号 -> 分配金额）
     * Key: 支付单号, Value: 分配给该支付单的金额
     */
    @NotEmpty(message = "金额分配不能为空")
    private Map<String, BigDecimal> amountAllocations;
    
    /**
     * 支付渠道
     */
    @NotNull(message = "支付渠道不能为空")
    private PaymentChannel paymentChannel;
    
    /**
     * 支付方式
     */
    private String paymentWay;
    
    /**
     * 总支付金额
     */
    @NotNull(message = "总支付金额不能为空")
    @Positive(message = "总支付金额必须大于0")
    private BigDecimal totalAmount;
}

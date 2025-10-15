package com.bytz.cms.payment.application.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.util.List;

/**
 * Execute Payment Request DTO
 * 执行支付请求DTO（支持单支付单和批量支付）
 */
@Data
public class ExecutePaymentRequest {
    
    /**
     * 支付单列表（支持单个或多个）
     */
    @NotEmpty(message = "支付单列表不能为空")
    private List<PaymentItem> paymentItems;
    
    /**
     * 支付渠道
     */
    @NotBlank(message = "支付渠道不能为空")
    private String paymentChannel;
    
    /**
     * 支付方式
     */
    private String paymentWay;
    
    /**
     * 支付单项
     */
    @Data
    public static class PaymentItem {
        
        /**
         * 支付单号
         */
        @NotBlank(message = "支付单号不能为空")
        private String paymentId;
        
        /**
         * 本次支付金额
         */
        @NotNull(message = "支付金额不能为空")
        private BigDecimal amount;
    }
}

package com.bytz.modules.cms.payment.interfaces.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;

/**
 * 取消支付单请求对象
 * Cancel Payment Request Object
 * 
 * 用于接收取消支付单的HTTP请求参数
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CancelPaymentRO {
    
    /**
     * 支付单ID（必填）
     */
    @NotBlank(message = "支付单ID不能为空")
    private String paymentId;
    
    /**
     * 取消原因（可选）
     */
    private String reason;
}

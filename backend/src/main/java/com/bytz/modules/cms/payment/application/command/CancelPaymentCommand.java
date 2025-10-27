package com.bytz.modules.cms.payment.application.command;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 取消支付单命令
 * Cancel Payment Command
 * 
 * 用于封装取消支付单的请求参数
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CancelPaymentCommand {
    
    /**
     * 支付单ID（必填）
     */
    private String paymentId;
    
    /**
     * 取消原因（可选）
     */
    private String reason;
    
    /**
     * 操作人ID（可选）
     */
    private String operatorId;
    
    /**
     * 操作人姓名（可选）
     */
    private String operatorName;
}

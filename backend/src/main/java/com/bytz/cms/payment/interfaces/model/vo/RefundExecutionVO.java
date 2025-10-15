package com.bytz.cms.payment.interfaces.model.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 退款执行响应对象
 * Refund Execution Response Object
 * 
 * 术语来源：Glossary.md - DTO术语"退款执行响应(RefundExecutionResponse)"
 * 用例来源：UC-PM-006
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RefundExecutionVO {
    
    /**
     * 退款流水号
     */
    private String refundTransactionId;
    
    /**
     * 支付单号
     */
    private String paymentId;
    
    /**
     * 执行结果（成功/失败）
     */
    private Boolean success;
    
    /**
     * 结果消息
     */
    private String message;
}

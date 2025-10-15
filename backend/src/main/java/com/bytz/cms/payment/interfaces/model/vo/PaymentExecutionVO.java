package com.bytz.cms.payment.interfaces.model.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 支付执行响应对象
 * Payment Execution Response Object
 * 
 * 术语来源：Glossary.md - DTO术语"支付执行响应(PaymentExecutionResponse)"
 * 用例来源：UC-PM-003, UC-PM-008
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaymentExecutionVO {
    
    /**
     * 渠道交易号
     */
    private String channelTransactionNumber;
    
    /**
     * 执行结果（成功/失败）
     */
    private Boolean success;
    
    /**
     * 结果消息
     */
    private String message;
    
    /**
     * 支付单号列表
     */
    private java.util.List<String> paymentIds;
}

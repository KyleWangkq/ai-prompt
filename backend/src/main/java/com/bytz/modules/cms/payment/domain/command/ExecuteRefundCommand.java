package com.bytz.modules.cms.payment.domain.command;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * 执行退款命令
 * Execute Refund Command
 * 
 * 封装退款执行操作的参数
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ExecuteRefundCommand {
    
    /**
     * 退款单号（订单系统的退款单号）
     */
    private String refundOrderId;
    
    /**
     * 支付单ID（主键）
     */
    private String paymentId;
    
    /**
     * 退款金额
     */
    private BigDecimal refundAmount;
    
    /**
     * 退款原因
     */
    private String refundReason;
    
    /**
     * 审批信息（JSON格式）
     */
    private String approvalInfo;
    
    /**
     * 退款类型（全额/部分）
     */
    private String refundType;
    
    /**
     * 原支付流水ID（必填，指定退款流水）
     */
    private String originalTransactionId;
}

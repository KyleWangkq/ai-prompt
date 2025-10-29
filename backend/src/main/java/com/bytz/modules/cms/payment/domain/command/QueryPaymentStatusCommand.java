package com.bytz.modules.cms.payment.domain.command;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 查询支付状态命令
 * Query Payment Status Command
 * 
 * 封装查询支付状态时需要传递给支付渠道的参数
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class QueryPaymentStatusCommand {
    
    /**
     * 渠道交易号
     */
    private String channelTransactionNumber;
    
    /**
     * 渠道支付记录ID
     * （渠道系统的支付记录唯一标识，用于验证和查询）
     */
    private String channelPaymentRecordId;
    
    /**
     * 经销商ID
     */
    private String resellerId;
}
package com.bytz.modules.cms.payment.infrastructure.channel.command;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * 创建退款请求命令
 * Create Refund Request Command
 * 
 * 封装创建退款请求时需要传递给支付渠道的参数
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateRefundRequestCommand {
    
    /**
     * 原支付渠道交易号
     */
    private String channelTransactionNumber;
    
    /**
     * 原支付的渠道支付记录ID
     * （用于定位原支付记录）
     */
    private String originalChannelPaymentRecordId;
    
    /**
     * 退款金额
     */
    private BigDecimal refundAmount;
    
    /**
     * 退款原因
     */
    private String refundReason;
    
    /**
     * 经销商ID
     */
    private String resellerId;
}

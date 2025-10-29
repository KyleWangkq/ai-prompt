package com.bytz.modules.cms.payment.domain.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 退款请求响应
 * Refund Request Response
 * 
 * 封装支付渠道创建退款请求后返回的数据
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RefundRequestResponse {
    
    /**
     * 渠道支付记录ID
     * （渠道系统的退款记录唯一标识，在开始退款时即返回）
     */
    private String channelPaymentRecordId;
    
    /**
     * 退款流水号/渠道交易号
     * （有些渠道在创建退款时即返回，有些渠道在退款结果回调时才返回）
     */
    private String refundTransactionNumber;
}
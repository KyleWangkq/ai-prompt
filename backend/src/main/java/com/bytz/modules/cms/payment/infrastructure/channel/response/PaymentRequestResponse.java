package com.bytz.modules.cms.payment.infrastructure.channel.response;

import com.bytz.modules.cms.payment.domain.enums.TransactionStatus;
import com.bytz.modules.cms.payment.domain.model.PaymentTransaction;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 支付请求响应
 * Payment Request Response
 * 
 * 封装支付渠道创建支付请求后返回的数据
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaymentRequestResponse {

    private TransactionStatus transactionStatus;
    
    /**
     * 渠道支付记录ID
     * （渠道系统的支付记录唯一标识，在开始支付时即返回）
     */
    private String channelPaymentRecordId;
    
    /**
     * 渠道交易号
     * （有些渠道在创建时即返回，有些渠道在支付结果回调时才返回）
     */
    private String channelTransactionNumber;
}
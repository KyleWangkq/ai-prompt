package com.bytz.modules.cms.payment.domain.command;

import com.bytz.modules.cms.payment.domain.model.PaymentTransaction;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * 创建支付请求命令
 * Create Payment Request Command
 * 
 * 封装创建支付请求时需要传递给支付渠道的参数
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StartPaymentCommand {
    
    /**
     * 支付总金额
     */
    private BigDecimal amount;
    
    /**
     * 经销商ID
     */
    private String resellerId;

    /**
     * 支付流水信息
     */
    private PaymentTransaction paymentTransaction;

    /**
     * 渠道特定参数
     */
    private Map<String, Object> channelParams;


}
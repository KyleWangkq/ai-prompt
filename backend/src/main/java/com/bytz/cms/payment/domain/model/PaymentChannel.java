package com.bytz.cms.payment.domain.model;

/**
 * 支付渠道枚举
 */
public enum PaymentChannel {
    
    /**
     * 线上支付
     */
    ONLINE_PAYMENT("线上支付"),
    
    /**
     * 钱包支付
     */
    WALLET_PAYMENT("钱包支付"),
    
    /**
     * 电汇支付
     */
    WIRE_TRANSFER("电汇支付"),
    
    /**
     * 信用账户
     */
    CREDIT_ACCOUNT("信用账户");
    
    private final String description;
    
    PaymentChannel(String description) {
        this.description = description;
    }
    
    public String getDescription() {
        return description;
    }
}

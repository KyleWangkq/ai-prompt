package com.bytz.cms.payment.shared.enums;

/**
 * 支付渠道枚举
 * Payment Channel Enumeration
 * 
 * 术语来源：Glossary.md - 状态和枚举术语"支付渠道类型(Payment Channel Type)"
 * 需求来源：需求文档3.1节、4.5节
 */
public enum PaymentChannel {
    /**
     * 线上支付（银联、网银等）- Online Payment
     */
    ONLINE_PAYMENT("线上支付"),
    
    /**
     * 钱包支付（企业寄存资金）- Wallet Payment
     */
    WALLET_PAYMENT("钱包支付"),
    
    /**
     * 电汇支付（银行转账）- Wire Transfer
     */
    WIRE_TRANSFER("电汇支付"),
    
    /**
     * 信用账户（企业信用额度）- Credit Account
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

package com.bytz.cms.payment.domain.enums;

/**
 * 支付渠道类型枚举
 * Payment Channel Type Enumeration
 * 
 * 定义支付渠道的类型，用于支付和退款操作
 */
public enum PaymentChannel {
    
    /**
     * 线上支付 - 银联、网银等第三方支付平台
     */
    ONLINE_PAYMENT("线上支付", "Online Payment"),
    
    /**
     * 钱包支付 - 企业寄存资金账户
     */
    WALLET_PAYMENT("钱包支付", "Wallet Payment"),
    
    /**
     * 电汇支付 - 银行转账方式
     */
    WIRE_TRANSFER("电汇支付", "Wire Transfer"),
    
    /**
     * 信用账户 - 基于信用的延期付款
     */
    CREDIT_ACCOUNT("信用账户", "Credit Account");
    
    private final String description;
    private final String englishName;
    
    PaymentChannel(String description, String englishName) {
        this.description = description;
        this.englishName = englishName;
    }
    
    public String getDescription() {
        return description;
    }
    
    public String getEnglishName() {
        return englishName;
    }
}

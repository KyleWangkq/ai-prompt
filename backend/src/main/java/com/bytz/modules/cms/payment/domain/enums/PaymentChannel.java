package com.bytz.modules.cms.payment.domain.enums;

import com.baomidou.mybatisplus.annotation.IEnum;

/**
 * 支付渠道类型枚举
 * Payment Channel Type Enumeration
 * 
 * 定义支付渠道的类型，用于支付和退款操作
 */
public enum PaymentChannel implements IEnum<Integer> {
    
    /**
     * 线上支付 - 银联、网银等第三方支付平台
     */
    ONLINE_PAYMENT(1, "线上支付", "Online Payment"),
    
    /**
     * 钱包支付 - 企业寄存资金账户
     */
    WALLET_PAYMENT(2, "钱包支付", "Wallet Payment"),
    
    /**
     * 电汇支付 - 银行转账方式
     */
    WIRE_TRANSFER(3, "电汇支付", "Wire Transfer"),
    
    /**
     * 信用账户 - 基于信用的延期付款
     */
    CREDIT_ACCOUNT(4, "信用账户", "Credit Account");
    
    private final Integer code;
    private final String description;
    private final String englishName;
    
    PaymentChannel(Integer code, String description, String englishName) {
        this.code = code;
        this.description = description;
        this.englishName = englishName;
    }
    
    @Override
    public Integer getValue() {
        return code;
    }
    
    public Integer getCode() {
        return code;
    }
    
    public String getDescription() {
        return description;
    }
    
    public String getEnglishName() {
        return englishName;
    }
}

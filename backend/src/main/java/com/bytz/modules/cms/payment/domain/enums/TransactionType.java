package com.bytz.modules.cms.payment.domain.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;

/**
 * 交易类型枚举
 * Transaction Type Enumeration
 * 
 * 区分支付流水的类型：正向支付或逆向退款
 */
public enum TransactionType {
    
    /**
     * 支付 - 正向资金流入
     */
    PAYMENT("PAYMENT", "支付", "Payment"),
    
    /**
     * 退款 - 逆向资金流出
     */
    REFUND("REFUND", "退款", "Refund");
    
    @EnumValue
    private final String code;
    private final String description;
    private final String englishName;
    
    TransactionType(String code, String description, String englishName) {
        this.code = code;
        this.description = description;
        this.englishName = englishName;
    }
    
    public String getCode() {
        return code;
    }
    
    public String getDescription() {
        return description;
    }
    
    public String getEnglishName() {
        return englishName;
    }
}

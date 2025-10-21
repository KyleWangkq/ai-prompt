package com.bytz.modules.cms.payment.domain.enums;

import com.baomidou.mybatisplus.annotation.IEnum;

/**
 * 交易类型枚举
 * Transaction Type Enumeration
 * 
 * 区分支付流水的类型：正向支付或逆向退款
 */
public enum TransactionType implements IEnum<Integer> {
    
    /**
     * 支付 - 正向资金流入
     */
    PAYMENT(1, "支付", "Payment"),
    
    /**
     * 退款 - 逆向资金流出
     */
    REFUND(2, "退款", "Refund");
    
    private final Integer code;
    private final String description;
    private final String englishName;
    
    TransactionType(Integer code, String description, String englishName) {
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

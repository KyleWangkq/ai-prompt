package com.bytz.modules.cms.payment.domain.enums;

import com.baomidou.mybatisplus.annotation.IEnum;

/**
 * 支付类型枚举
 * Payment Type Enumeration
 * 
 * 定义支付单的业务类型
 */
public enum PaymentType implements IEnum<Integer> {
    
    /**
     * 预付款 - 订单确认后的首期付款
     */
    ADVANCE_PAYMENT(1, "预付款", "Advance Payment"),
    
    /**
     * 尾款 - 发货或完工后的最终付款
     */
    FINAL_PAYMENT(2, "尾款", "Final Payment"),
    
    /**
     * 其他费用 - 其他类型的费用支付
     */
    OTHER_PAYMENT(3, "其他费用", "Other Payment"),
    
    /**
     * 信用还款 - 企业对信用额度的还款操作
     */
    CREDIT_REPAYMENT(4, "信用还款", "Credit Repayment");
    
    private final Integer code;
    private final String description;
    private final String englishName;
    
    PaymentType(Integer code, String description, String englishName) {
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

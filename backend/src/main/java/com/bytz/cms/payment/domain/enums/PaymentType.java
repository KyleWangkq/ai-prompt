package com.bytz.cms.payment.domain.enums;

/**
 * 支付类型枚举
 * Payment Type Enumeration
 * 
 * 定义支付单的业务类型
 */
public enum PaymentType {
    
    /**
     * 预付款 - 订单确认后的首期付款
     */
    ADVANCE_PAYMENT("预付款", "Advance Payment"),
    
    /**
     * 尾款 - 发货或完工后的最终付款
     */
    FINAL_PAYMENT("尾款", "Final Payment"),
    
    /**
     * 其他费用 - 其他类型的费用支付
     */
    OTHER_PAYMENT("其他费用", "Other Payment"),
    
    /**
     * 信用还款 - 企业对信用额度的还款操作
     */
    CREDIT_REPAYMENT("信用还款", "Credit Repayment");
    
    private final String description;
    private final String englishName;
    
    PaymentType(String description, String englishName) {
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

package com.bytz.cms.payment.domain.model;

/**
 * 支付类型枚举
 */
public enum PaymentType {
    
    /**
     * 预付款
     */
    ADVANCE_PAYMENT("预付款"),
    
    /**
     * 尾款
     */
    FINAL_PAYMENT("尾款"),
    
    /**
     * 其他费用
     */
    OTHER_FEE("其他费用"),
    
    /**
     * 信用还款
     */
    CREDIT_REPAYMENT("信用还款");
    
    private final String description;
    
    PaymentType(String description) {
        this.description = description;
    }
    
    public String getDescription() {
        return description;
    }
}

package com.bytz.cms.payment.shared.enums;

/**
 * 支付类型枚举
 * Payment Type Enumeration
 * 
 * 术语来源：Glossary.md - 状态和枚举术语"支付类型(Payment Type)"
 * 需求来源：需求文档3.2节、4.4.1节
 */
public enum PaymentType {
    /**
     * 预付款 - Advance Payment
     */
    ADVANCE_PAYMENT("预付款"),
    
    /**
     * 尾款 - Final Payment
     */
    FINAL_PAYMENT("尾款"),
    
    /**
     * 其他费用 - Other Payment
     */
    OTHER_PAYMENT("其他费用"),
    
    /**
     * 信用还款 - Credit Repayment
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

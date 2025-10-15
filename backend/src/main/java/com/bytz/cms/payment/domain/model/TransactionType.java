package com.bytz.cms.payment.domain.model;

/**
 * 流水类型枚举
 */
public enum TransactionType {
    
    /**
     * 支付
     */
    PAYMENT("支付"),
    
    /**
     * 退款
     */
    REFUND("退款");
    
    private final String description;
    
    TransactionType(String description) {
        this.description = description;
    }
    
    public String getDescription() {
        return description;
    }
}

package com.bytz.cms.payment.shared.enums;

/**
 * 流水类型枚举
 * Transaction Type Enumeration
 * 
 * 术语来源：Glossary.md - 状态和枚举术语"流水类型(Transaction Type)"
 * 需求来源：需求文档4.4.2节
 */
public enum TransactionType {
    /**
     * 支付（正向资金流动）- Payment
     */
    PAYMENT("支付"),
    
    /**
     * 退款（逆向资金流动）- Refund
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

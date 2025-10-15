package com.bytz.cms.payment.shared.enums;

/**
 * 关联业务类型枚举
 * Related Business Type Enumeration
 * 
 * 术语来源：Glossary.md - 状态和枚举术语"关联业务类型(Related Business Type)"
 * 需求来源：需求文档3.2节、4.4.1节
 */
public enum RelatedBusinessType {
    /**
     * 信用记录（信用还款场景）- Credit Record
     */
    CREDIT_RECORD("信用记录"),
    
    /**
     * 提货单（提货费用场景）- Delivery Order
     */
    DELIVERY_ORDER("提货单"),
    
    /**
     * 订单（普通支付场景）- Order
     */
    ORDER("订单");
    
    private final String description;
    
    RelatedBusinessType(String description) {
        this.description = description;
    }
    
    public String getDescription() {
        return description;
    }
}

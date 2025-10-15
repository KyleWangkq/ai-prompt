package com.bytz.cms.payment.domain.model;

/**
 * 关联业务类型枚举
 */
public enum RelatedBusinessType {
    
    /**
     * 信用记录
     */
    CREDIT_RECORD("信用记录"),
    
    /**
     * 提货单
     */
    DELIVERY_ORDER("提货单"),
    
    /**
     * 订单
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

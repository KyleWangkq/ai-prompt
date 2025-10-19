package com.bytz.cms.payment.domain.enums;

/**
 * 关联业务类型枚举
 * Related Business Type Enumeration
 * 
 * 标识支付单关联的业务类型
 */
public enum RelatedBusinessType {
    
    /**
     * 信用记录 - 关联信用管理系统的信用记录
     */
    CREDIT_RECORD("信用记录", "Credit Record"),
    
    /**
     * 提货单 - 关联提货单记录
     */
    DELIVERY_ORDER("提货单", "Delivery Order"),
    
    /**
     * 订单 - 关联订单系统的订单
     */
    ORDER("订单", "Order");
    
    private final String description;
    private final String englishName;
    
    RelatedBusinessType(String description, String englishName) {
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

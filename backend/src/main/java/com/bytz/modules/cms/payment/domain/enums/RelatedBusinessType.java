package com.bytz.modules.cms.payment.domain.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;

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
    CREDIT_RECORD("CREDIT_RECORD", "信用记录", "Credit Record"),
    
    /**
     * 提货单 - 关联提货单记录
     */
    DELIVERY_ORDER("DELIVERY_ORDER", "提货单", "Delivery Order"),
    
    /**
     * 订单 - 关联订单系统的订单
     */
    ORDER("ORDER", "订单", "Order");
    
    @EnumValue
    private final String code;
    private final String description;
    private final String englishName;
    
    RelatedBusinessType(String code, String description, String englishName) {
        this.code = code;
        this.description = description;
        this.englishName = englishName;
    }
    
    public String getCode() {
        return code;
    }
    
    public String getDescription() {
        return description;
    }
    
    public String getEnglishName() {
        return englishName;
    }
}

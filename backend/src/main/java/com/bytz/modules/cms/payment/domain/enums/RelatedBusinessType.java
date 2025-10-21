package com.bytz.modules.cms.payment.domain.enums;

import com.baomidou.mybatisplus.annotation.IEnum;

/**
 * 关联业务类型枚举
 * Related Business Type Enumeration
 * 
 * 标识支付单关联的业务类型
 */
public enum RelatedBusinessType implements IEnum<Integer> {
    
    /**
     * 信用记录 - 关联信用管理系统的信用记录
     */
    CREDIT_RECORD(1, "信用记录", "Credit Record"),
    
    /**
     * 提货单 - 关联提货单记录
     */
    DELIVERY_ORDER(2, "提货单", "Delivery Order"),
    
    /**
     * 订单 - 关联订单系统的订单
     */
    ORDER(3, "订单", "Order");
    
    private final Integer code;
    private final String description;
    private final String englishName;
    
    RelatedBusinessType(Integer code, String description, String englishName) {
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

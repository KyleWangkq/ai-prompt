package com.bytz.cms.payment.domain.model;

/**
 * 退款状态枚举
 */
public enum RefundStatus {
    
    /**
     * 未退款
     */
    NO_REFUND("未退款"),
    
    /**
     * 退款中
     */
    REFUNDING("退款中"),
    
    /**
     * 部分退款
     */
    PARTIAL_REFUNDED("部分退款"),
    
    /**
     * 全额退款
     */
    FULL_REFUNDED("全额退款"),
    
    /**
     * 退款失败
     */
    REFUND_FAILED("退款失败");
    
    private final String description;
    
    RefundStatus(String description) {
        this.description = description;
    }
    
    public String getDescription() {
        return description;
    }
}

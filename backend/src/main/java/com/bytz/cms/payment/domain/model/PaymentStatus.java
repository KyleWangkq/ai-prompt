package com.bytz.cms.payment.domain.model;

/**
 * 支付状态枚举
 */
public enum PaymentStatus {
    
    /**
     * 未支付
     */
    UNPAID("未支付"),
    
    /**
     * 支付中
     */
    PAYING("支付中"),
    
    /**
     * 部分支付
     */
    PARTIAL_PAID("部分支付"),
    
    /**
     * 已支付
     */
    PAID("已支付"),
    
    /**
     * 支付失败
     */
    FAILED("支付失败"),
    
    /**
     * 已停止
     */
    STOPPED("已停止"),
    
    /**
     * 已冻结
     */
    FROZEN("已冻结");
    
    private final String description;
    
    PaymentStatus(String description) {
        this.description = description;
    }
    
    public String getDescription() {
        return description;
    }
}

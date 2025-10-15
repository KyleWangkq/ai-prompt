package com.bytz.cms.payment.shared.enums;

/**
 * 退款状态枚举
 * Refund Status Enumeration
 * 
 * 术语来源：Glossary.md - 状态和枚举术语"退款状态(Refund Status)"
 * 需求来源：需求文档3.3.2节
 * 状态转换：NO_REFUND → REFUNDING → PARTIAL_REFUNDED/FULL_REFUNDED/REFUND_FAILED
 */
public enum RefundStatus {
    /**
     * 未退款 - No Refund
     */
    NO_REFUND("未退款"),
    
    /**
     * 退款中 - Refunding
     */
    REFUNDING("退款中"),
    
    /**
     * 部分退款 - Partial Refunded
     */
    PARTIAL_REFUNDED("部分退款"),
    
    /**
     * 全额退款 - Full Refunded
     */
    FULL_REFUNDED("全额退款"),
    
    /**
     * 退款失败 - Refund Failed
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

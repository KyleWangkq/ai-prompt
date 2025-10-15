package com.bytz.cms.payment.shared.enums;

/**
 * 支付状态枚举
 * Payment Status Enumeration
 * 
 * 术语来源：Glossary.md - 状态和枚举术语"支付状态(Payment Status)"
 * 需求来源：需求文档3.3.1节
 * 状态转换：UNPAID → PAYING → PARTIAL_PAID/PAID/FAILED，支持STOPPED和FROZEN状态
 */
public enum PaymentStatus {
    /**
     * 未支付 - Unpaid
     */
    UNPAID("未支付"),
    
    /**
     * 支付中 - Paying
     */
    PAYING("支付中"),
    
    /**
     * 部分支付 - Partial Paid
     */
    PARTIAL_PAID("部分支付"),
    
    /**
     * 已支付 - Paid
     */
    PAID("已支付"),
    
    /**
     * 支付失败 - Failed
     */
    FAILED("支付失败"),
    
    /**
     * 已停止 - Stopped
     */
    STOPPED("已停止"),
    
    /**
     * 已冻结 - Frozen
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

package com.bytz.cms.payment.shared.enums;

/**
 * 流水状态枚举
 * Transaction Status Enumeration
 * 
 * 术语来源：Glossary.md - 状态和枚举术语"流水状态(Transaction Status)"
 * 需求来源：需求文档4.4.2节
 * 状态转换：PROCESSING → SUCCESS/FAILED
 */
public enum TransactionStatus {
    /**
     * 处理中 - Processing
     */
    PROCESSING("处理中"),
    
    /**
     * 成功 - Success
     */
    SUCCESS("成功"),
    
    /**
     * 失败 - Failed
     */
    FAILED("失败");
    
    private final String description;
    
    TransactionStatus(String description) {
        this.description = description;
    }
    
    public String getDescription() {
        return description;
    }
}

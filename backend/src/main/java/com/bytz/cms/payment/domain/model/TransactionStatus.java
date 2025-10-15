package com.bytz.cms.payment.domain.model;

/**
 * 流水状态枚举
 */
public enum TransactionStatus {
    
    /**
     * 处理中
     */
    PROCESSING("处理中"),
    
    /**
     * 成功
     */
    SUCCESS("成功"),
    
    /**
     * 失败
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

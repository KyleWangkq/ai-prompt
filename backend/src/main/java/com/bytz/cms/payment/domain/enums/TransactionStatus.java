package com.bytz.cms.payment.domain.enums;

/**
 * 交易状态枚举
 * Transaction Status Enumeration
 * 
 * 定义支付流水的处理状态
 */
public enum TransactionStatus {
    
    /**
     * 处理中 - 交易请求已发起，等待处理结果
     */
    PROCESSING("处理中", "Processing"),
    
    /**
     * 成功 - 交易处理成功
     */
    SUCCESS("成功", "Success"),
    
    /**
     * 失败 - 交易处理失败
     */
    FAILED("失败", "Failed");
    
    private final String description;
    private final String englishName;
    
    TransactionStatus(String description, String englishName) {
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

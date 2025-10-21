package com.bytz.modules.cms.payment.domain.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;

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
    PROCESSING("PROCESSING", "处理中", "Processing"),
    
    /**
     * 成功 - 交易处理成功
     */
    SUCCESS("SUCCESS", "成功", "Success"),
    
    /**
     * 失败 - 交易处理失败
     */
    FAILED("FAILED", "失败", "Failed");
    
    @EnumValue
    private final String code;
    private final String description;
    private final String englishName;
    
    TransactionStatus(String code, String description, String englishName) {
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

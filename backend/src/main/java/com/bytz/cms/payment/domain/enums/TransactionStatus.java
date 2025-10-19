package com.bytz.cms.payment.domain.enums;

/**
 * 流水状态枚举
 * 
 * <p>标识单笔流水的处理状态</p>
 * 
 * <p>术语来源：Glossary.md - 状态和枚举术语"流水状态(Transaction Status)"</p>
 * <p>需求来源：需求文档4.4.2节</p>
 * 
 * <p>状态转换：PROCESSING → SUCCESS/FAILED</p>
 * 
 * @author DDD设计团队
 * @version 1.0.0
 * @since 2025-01-15
 */
public enum TransactionStatus {
    
    /**
     * 处理中
     * <p>交易请求已提交到渠道，等待渠道处理结果</p>
     */
    PROCESSING("处理中"),
    
    /**
     * 成功
     * <p>交易已成功完成</p>
     */
    SUCCESS("成功"),
    
    /**
     * 失败
     * <p>交易处理失败</p>
     */
    FAILED("失败");

    private final String description;

    TransactionStatus(String description) {
        this.description = description;
    }

    /**
     * 获取枚举描述
     * 
     * @return 描述文本
     */
    public String getDescription() {
        return description;
    }
}

package com.bytz.cms.payment.domain.enums;

/**
 * 流水类型枚举
 * 
 * <p>区分正向支付和逆向退款</p>
 * 
 * <p>术语来源：Glossary.md - 状态和枚举术语"流水类型(Transaction Type)"</p>
 * <p>需求来源：需求文档4.4.2节</p>
 * 
 * @author DDD设计团队
 * @version 1.0.0
 * @since 2025-01-15
 */
public enum TransactionType {
    
    /**
     * 支付
     * <p>正向资金流动，支付流水金额为正数</p>
     */
    PAYMENT("支付"),
    
    /**
     * 退款
     * <p>逆向资金流动，退款流水金额为负数</p>
     */
    REFUND("退款");

    private final String description;

    TransactionType(String description) {
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

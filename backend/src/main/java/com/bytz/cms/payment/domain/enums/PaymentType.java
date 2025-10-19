package com.bytz.cms.payment.domain.enums;

/**
 * 支付类型枚举
 * 
 * <p>区分支付单的业务类型，支持不同的支付场景</p>
 * 
 * <p>术语来源：Glossary.md - 状态和枚举术语"支付类型(Payment Type)"</p>
 * <p>需求来源：需求文档3.2节、4.4.1节</p>
 * 
 * @author DDD设计团队
 * @version 1.0.0
 * @since 2025-01-15
 */
public enum PaymentType {
    
    /**
     * 预付款
     * <p>订单确认后的首期付款</p>
     */
    ADVANCE_PAYMENT("预付款"),
    
    /**
     * 尾款
     * <p>发货或完工后的最终付款</p>
     */
    FINAL_PAYMENT("尾款"),
    
    /**
     * 其他费用
     * <p>订单相关的其他费用支付</p>
     */
    OTHER_PAYMENT("其他费用"),
    
    /**
     * 信用还款
     * <p>企业对信用额度的还款操作，按统一支付流程处理</p>
     */
    CREDIT_REPAYMENT("信用还款");

    private final String description;

    PaymentType(String description) {
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

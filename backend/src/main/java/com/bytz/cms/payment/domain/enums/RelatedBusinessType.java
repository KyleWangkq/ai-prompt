package com.bytz.cms.payment.domain.enums;

/**
 * 关联业务类型枚举
 * 
 * <p>标识支付单关联的业务场景</p>
 * 
 * <p>术语来源：Glossary.md - 状态和枚举术语"关联业务类型(Related Business Type)"</p>
 * <p>需求来源：需求文档3.2节、4.4.1节</p>
 * 
 * @author DDD设计团队
 * @version 1.0.0
 * @since 2025-01-15
 */
public enum RelatedBusinessType {
    
    /**
     * 信用记录
     * <p>关联信用还款场景，支付类型为CREDIT_REPAYMENT时使用</p>
     */
    CREDIT_RECORD("信用记录"),
    
    /**
     * 提货单
     * <p>关联提货费用场景</p>
     */
    DELIVERY_ORDER("提货单"),
    
    /**
     * 订单
     * <p>关联普通订单支付场景（预付款、尾款等）</p>
     */
    ORDER("订单");

    private final String description;

    RelatedBusinessType(String description) {
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

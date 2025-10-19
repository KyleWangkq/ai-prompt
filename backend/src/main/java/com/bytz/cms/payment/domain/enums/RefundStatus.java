package com.bytz.cms.payment.domain.enums;

/**
 * 退款状态枚举
 * 
 * <p>标识支付单的退款进度状态</p>
 * 
 * <p>术语来源：Glossary.md - 状态和枚举术语"退款状态(Refund Status)"</p>
 * <p>需求来源：需求文档3.3.2节</p>
 * 
 * <p>状态转换：NO_REFUND → REFUNDING → PARTIAL_REFUNDED/FULL_REFUNDED/REFUND_FAILED</p>
 * 
 * @author DDD设计团队
 * @version 1.0.0
 * @since 2025-01-15
 */
public enum RefundStatus {
    
    /**
     * 未退款
     * <p>支付单未发生任何退款</p>
     */
    NO_REFUND("未退款"),
    
    /**
     * 退款中
     * <p>退款请求已发起，等待退款渠道确认</p>
     */
    REFUNDING("退款中"),
    
    /**
     * 部分退款
     * <p>支付单已发生部分退款，仍有金额未退款</p>
     */
    PARTIAL_REFUNDED("部分退款"),
    
    /**
     * 全额退款
     * <p>支付单已全额退款</p>
     */
    FULL_REFUNDED("全额退款"),
    
    /**
     * 退款失败
     * <p>退款过程中出现错误，退款未成功</p>
     */
    REFUND_FAILED("退款失败");

    private final String description;

    RefundStatus(String description) {
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

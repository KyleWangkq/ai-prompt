package com.bytz.cms.payment.domain.enums;

/**
 * 支付状态枚举
 * 
 * <p>标识支付单当前的支付进度状态</p>
 * 
 * <p>术语来源：Glossary.md - 状态和枚举术语"支付状态(Payment Status)"</p>
 * <p>需求来源：需求文档3.3.1节</p>
 * 
 * <p>状态转换：UNPAID → PAYING → PARTIAL_PAID/PAID/FAILED</p>
 * <p>支持STOPPED和FROZEN状态用于异常处理</p>
 * 
 * @author DDD设计团队
 * @version 1.0.0
 * @since 2025-01-15
 */
public enum PaymentStatus {
    
    /**
     * 未支付
     * <p>支付单刚创建，尚未开始支付</p>
     */
    UNPAID("未支付"),
    
    /**
     * 支付中
     * <p>支付请求已发起，等待支付渠道确认</p>
     */
    PAYING("支付中"),
    
    /**
     * 部分支付
     * <p>支付单已支付部分金额，仍有余额待支付</p>
     */
    PARTIAL_PAID("部分支付"),
    
    /**
     * 已支付
     * <p>支付单全额支付完成</p>
     */
    PAID("已支付"),
    
    /**
     * 支付失败
     * <p>支付过程中出现错误，支付未成功</p>
     */
    FAILED("支付失败"),
    
    /**
     * 已停止
     * <p>因业务原因主动停止支付操作</p>
     */
    STOPPED("已停止"),
    
    /**
     * 已冻结
     * <p>支付单因特殊原因暂时无法操作</p>
     */
    FROZEN("已冻结");

    private final String description;

    PaymentStatus(String description) {
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

    /**
     * 判断当前状态是否允许继续支付
     * 
     * @return true表示可以支付，false表示不可支付
     */
    public boolean isPayable() {
        return this == UNPAID || this == PARTIAL_PAID;
    }

    /**
     * 判断当前状态是否允许退款
     * 
     * @return true表示可以退款，false表示不可退款
     */
    public boolean isRefundable() {
        return this == PAID || this == PARTIAL_PAID;
    }
}

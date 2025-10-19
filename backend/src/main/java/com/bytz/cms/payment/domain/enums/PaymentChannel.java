package com.bytz.cms.payment.domain.enums;

/**
 * 支付渠道枚举
 * 
 * <p>标识支付使用的渠道类型</p>
 * 
 * <p>术语来源：Glossary.md - 状态和枚举术语"支付渠道类型(Payment Channel Type)"</p>
 * <p>需求来源：需求文档3.1节、4.5节</p>
 * 
 * @author DDD设计团队
 * @version 1.0.0
 * @since 2025-01-15
 */
public enum PaymentChannel {
    
    /**
     * 线上支付
     * <p>对接银联、企业网银、支付宝企业版、微信企业付款等</p>
     * <p>适用场景：B2B大额支付，确认时限较长</p>
     */
    ONLINE_PAYMENT("线上支付"),
    
    /**
     * 钱包支付
     * <p>调用企业内部资金账户支付接口</p>
     * <p>适用场景：企业寄存资金管理</p>
     */
    WALLET_PAYMENT("钱包支付"),
    
    /**
     * 电汇支付
     * <p>生成银行转账信息和转账凭证上传接口</p>
     * <p>适用场景：传统转账方式，需要凭证确认</p>
     */
    WIRE_TRANSFER("电汇支付"),
    
    /**
     * 信用账户
     * <p>生成赊账记录和还款计划</p>
     * <p>适用场景：基于信用的延期付款</p>
     */
    CREDIT_ACCOUNT("信用账户");

    private final String description;

    PaymentChannel(String description) {
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

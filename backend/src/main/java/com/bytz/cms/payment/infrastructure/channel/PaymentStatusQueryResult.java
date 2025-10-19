package com.bytz.cms.payment.infrastructure.channel;

/**
 * 支付状态查询结果
 * 
 * <p>封装支付状态查询的响应结果</p>
 * 
 * @author DDD设计团队
 * @version 1.0.0
 * @since 2025-01-15
 */
public class PaymentStatusQueryResult {
    /**
     * 支付状态（SUCCESS/PROCESSING/FAILED）
     */
    private String status;
    
    /**
     * 渠道交易号
     */
    private String channelTransactionNumber;
    
    /**
     * 错误信息
     */
    private String errorMessage;

    public PaymentStatusQueryResult(String status, String channelTransactionNumber, String errorMessage) {
        this.status = status;
        this.channelTransactionNumber = channelTransactionNumber;
        this.errorMessage = errorMessage;
    }

    public String getStatus() {
        return status;
    }

    public String getChannelTransactionNumber() {
        return channelTransactionNumber;
    }

    public String getErrorMessage() {
        return errorMessage;
    }
}

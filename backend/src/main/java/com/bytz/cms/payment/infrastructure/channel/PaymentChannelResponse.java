package com.bytz.cms.payment.infrastructure.channel;

/**
 * 支付渠道响应结果
 * 
 * <p>封装支付渠道调用的响应结果</p>
 * 
 * @author DDD设计团队
 * @version 1.0.0
 * @since 2025-01-15
 */
public class PaymentChannelResponse {
    /**
     * 是否成功
     */
    private boolean success;
    
    /**
     * 渠道交易号
     */
    private String channelTransactionNumber;
    
    /**
     * 错误信息
     */
    private String errorMessage;

    public PaymentChannelResponse(boolean success, String channelTransactionNumber, String errorMessage) {
        this.success = success;
        this.channelTransactionNumber = channelTransactionNumber;
        this.errorMessage = errorMessage;
    }

    public boolean isSuccess() {
        return success;
    }

    public String getChannelTransactionNumber() {
        return channelTransactionNumber;
    }

    public String getErrorMessage() {
        return errorMessage;
    }
}

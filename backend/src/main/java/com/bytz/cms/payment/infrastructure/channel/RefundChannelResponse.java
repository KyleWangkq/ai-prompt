package com.bytz.cms.payment.infrastructure.channel;

/**
 * 退款渠道响应结果
 * 
 * <p>封装退款渠道调用的响应结果</p>
 * 
 * @author DDD设计团队
 * @version 1.0.0
 * @since 2025-01-15
 */
public class RefundChannelResponse {
    /**
     * 是否成功
     */
    private boolean success;
    
    /**
     * 退款流水号
     */
    private String refundTransactionNumber;
    
    /**
     * 错误信息
     */
    private String errorMessage;

    public RefundChannelResponse(boolean success, String refundTransactionNumber, String errorMessage) {
        this.success = success;
        this.refundTransactionNumber = refundTransactionNumber;
        this.errorMessage = errorMessage;
    }

    public boolean isSuccess() {
        return success;
    }

    public String getRefundTransactionNumber() {
        return refundTransactionNumber;
    }

    public String getErrorMessage() {
        return errorMessage;
    }
}

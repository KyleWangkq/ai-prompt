package com.bytz.cms.payment.shared.exception;

/**
 * 支付单状态异常
 * 
 * <p>当支付单状态不符合业务操作要求时，抛出此异常</p>
 * 
 * @author DDD设计团队
 * @version 1.0.0
 * @since 2025-01-15
 */
public class PaymentStatusException extends BusinessException {
    
    public PaymentStatusException(String message) {
        super("PAYMENT_STATUS_ERROR", message);
    }
}

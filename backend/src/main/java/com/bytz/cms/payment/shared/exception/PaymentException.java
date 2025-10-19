package com.bytz.cms.payment.shared.exception;

/**
 * 支付异常
 * Payment Exception
 * 
 * 支付相关的业务异常
 */
public class PaymentException extends BusinessException {
    
    public PaymentException(String message) {
        super("PAYMENT_ERROR", message);
    }
    
    public PaymentException(String message, Throwable cause) {
        super("PAYMENT_ERROR", message, cause);
    }
    
    public PaymentException(String errorCode, String message) {
        super(errorCode, message);
    }
}

package com.bytz.cms.payment.shared.exception;

/**
 * 支付业务异常
 * Payment Business Exception
 */
public class PaymentException extends BusinessException {
    
    public PaymentException(String message) {
        super(message);
    }
    
    public PaymentException(String errorCode, String message) {
        super(errorCode, message);
    }
    
    public PaymentException(String message, Throwable cause) {
        super(message, cause);
    }
    
    public PaymentException(String errorCode, String message, Throwable cause) {
        super(errorCode, message, cause);
    }
}

package com.bytz.cms.payment.shared.exception;

/**
 * Payment Domain Exception
 * 支付领域异常基类
 */
public class PaymentDomainException extends RuntimeException {
    
    public PaymentDomainException(String message) {
        super(message);
    }
    
    public PaymentDomainException(String message, Throwable cause) {
        super(message, cause);
    }
}

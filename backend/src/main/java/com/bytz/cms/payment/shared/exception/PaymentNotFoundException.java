package com.bytz.cms.payment.shared.exception;

/**
 * 支付单未找到异常
 * 
 * <p>当根据支付单号查询支付单时，如果支付单不存在，则抛出此异常</p>
 * 
 * @author DDD设计团队
 * @version 1.0.0
 * @since 2025-01-15
 */
public class PaymentNotFoundException extends BusinessException {
    
    public PaymentNotFoundException(String paymentId) {
        super("PAYMENT_NOT_FOUND", "支付单不存在：" + paymentId);
    }
}

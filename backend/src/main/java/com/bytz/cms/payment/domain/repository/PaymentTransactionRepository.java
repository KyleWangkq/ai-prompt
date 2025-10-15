package com.bytz.cms.payment.domain.repository;

import com.bytz.cms.payment.domain.model.PaymentTransaction;
import com.bytz.cms.payment.domain.model.TransactionType;

import java.util.List;
import java.util.Optional;

/**
 * PaymentTransaction Repository Interface (Domain Layer)
 * 支付流水仓储接口
 */
public interface PaymentTransactionRepository {
    
    /**
     * 保存支付流水
     */
    void save(PaymentTransaction transaction);
    
    /**
     * 根据ID查找流水
     */
    Optional<PaymentTransaction> findById(String id);
    
    /**
     * 根据支付单ID查找所有流水
     */
    List<PaymentTransaction> findByPaymentId(String paymentId);
    
    /**
     * 根据支付单ID和流水类型查找流水
     */
    List<PaymentTransaction> findByPaymentIdAndType(String paymentId, TransactionType transactionType);
    
    /**
     * 根据渠道交易号查找流水
     */
    List<PaymentTransaction> findByChannelTransactionNumber(String channelTransactionNumber);
    
    /**
     * 更新流水
     */
    void update(PaymentTransaction transaction);
}

package com.bytz.cms.payment.domain.repository;

import com.baomidou.mybatisplus.extension.service.IService;
import com.bytz.cms.payment.domain.entity.PaymentTransactionEntity;
import com.bytz.cms.payment.domain.model.PaymentTransactionAggregate;

import java.util.List;
import java.util.Optional;

/**
 * 支付流水仓储接口
 * Payment Transaction Repository Interface
 * 
 * 职责：支付流水的持久化和查询
 * 使用场景：支付执行、退款处理、流水查询
 */
public interface IPaymentTransactionRepository extends IService<PaymentTransactionEntity> {
    
    /**
     * 根据ID查找支付流水
     * 
     * @param id 流水号
     * @return 支付流水（Optional）
     */
    Optional<PaymentTransactionAggregate> findById(String id);
    
    /**
     * 保存支付流水
     * 
     * @param aggregate 支付流水聚合
     * @return 是否成功
     */
    boolean save(PaymentTransactionAggregate aggregate);
    
    /**
     * 更新支付流水
     * 
     * @param aggregate 支付流水聚合
     * @return 是否成功
     */
    boolean update(PaymentTransactionAggregate aggregate);
    
    /**
     * 根据支付单ID查找所有流水
     * 
     * @param paymentId 支付单号
     * @return 支付流水列表
     */
    List<PaymentTransactionAggregate> findByPaymentId(String paymentId);
    
    /**
     * 根据渠道交易号查找流水
     * 
     * @param channelTransactionNumber 渠道交易号
     * @return 支付流水列表
     */
    List<PaymentTransactionAggregate> findByChannelTransactionNumber(String channelTransactionNumber);
}

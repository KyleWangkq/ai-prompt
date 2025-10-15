package com.bytz.cms.payment.domain.repository;

import com.baomidou.mybatisplus.extension.service.IService;
import com.bytz.cms.payment.domain.entity.PaymentEntity;
import com.bytz.cms.payment.domain.model.PaymentAggregate;

import java.util.Optional;

/**
 * 支付单仓储接口
 * Payment Repository Interface
 * 
 * 术语来源：Glossary.md - Repository术语"支付单仓储(PaymentRepository)"
 * 职责：支付单聚合的持久化和查询
 * 使用场景：所有支付单操作
 */
public interface IPaymentRepository extends IService<PaymentEntity> {
    
    /**
     * 根据ID查找支付单聚合
     * 
     * @param id 支付单号
     * @return 支付单聚合（Optional）
     */
    Optional<PaymentAggregate> findById(String id);
    
    /**
     * 保存支付单聚合
     * 
     * @param aggregate 支付单聚合
     * @return 是否成功
     */
    boolean save(PaymentAggregate aggregate);
    
    /**
     * 更新支付单聚合
     * 
     * @param aggregate 支付单聚合
     * @return 是否成功
     */
    boolean update(PaymentAggregate aggregate);
    
    /**
     * 根据订单ID查找支付单聚合
     * 
     * @param orderId 订单号
     * @return 支付单聚合（Optional）
     */
    Optional<PaymentAggregate> findByOrderId(String orderId);
    
    /**
     * 检查支付单号是否已存在
     * 
     * @param id 支付单号
     * @return 是否存在
     */
    boolean existsById(String id);
}

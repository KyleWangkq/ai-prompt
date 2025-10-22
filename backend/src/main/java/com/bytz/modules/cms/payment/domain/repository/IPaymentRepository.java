package com.bytz.modules.cms.payment.domain.repository;

import com.bytz.modules.cms.payment.domain.model.PaymentAggregate;

import java.util.List;

/**
 * 支付单仓储接口
 * Payment Repository Interface
 * 
 * 定义支付单聚合根的持久化和查询接口
 * 接口定义在领域层，实现在基础设施层
 */
public interface IPaymentRepository {
    
    /**
     * 保存支付单聚合根
     * 
     * @param payment 支付单聚合根
     * @return 保存后的支付单聚合根
     * TODO: 实现支付单的完整保存逻辑，包括支付单本身和关联的支付流水
     */
    PaymentAggregate save(PaymentAggregate payment);
    
    /**
     * 根据业务编码查找支付单
     * 
     * @param code 支付单号
     * @return 支付单聚合根，如果未找到返回null
     * TODO: 实现支付单的查询逻辑，包括关联的支付流水
     */
    PaymentAggregate findByCode(String code);
    
    /**
     * 根据订单号查找支付单列表
     * 
     * @param orderId 订单号
     * @return 支付单列表
     * TODO: 实现按订单号查询支付单列表的逻辑
     */
    List<PaymentAggregate> findByOrderId(String orderId);
    
    /**
     * 根据经销商ID查找支付单列表
     * 
     * @param resellerId 经销商ID
     * @return 支付单列表
     * TODO: 实现按经销商ID查询支付单列表的逻辑
     */
    List<PaymentAggregate> findByResellerId(String resellerId);
    
    /**
     * 根据关联业务ID查找支付单列表
     * 
     * @param relatedBusinessId 关联业务ID
     * @return 支付单列表
     * TODO: 实现按关联业务ID查询支付单列表的逻辑，用于信用还款查询
     */
    List<PaymentAggregate> findByRelatedBusinessId(String relatedBusinessId);
    
    /**
     * 删除支付单（逻辑删除）
     * 
     * @param code 支付单号
     * @return 是否删除成功
     * TODO: 实现支付单的逻辑删除
     */
    boolean deleteByCode(String code);
    
    /**
     * 检查支付单是否存在
     * 
     * @param code 支付单号
     * @return 是否存在
     * TODO: 实现支付单存在性检查
     */
    boolean existsByCode(String code);
    
    /**
     * 生成唯一的支付单号
     * 
     * @return 唯一的支付单号
     * TODO: 实现支付单号生成策略，确保全局唯一
     */
    String generatePaymentCode();
    
    /**
     * 生成唯一的支付流水号
     * 
     * @return 唯一的支付流水号
     * TODO: 实现支付流水号生成策略，确保全局唯一
     */
    String generateTransactionCode();
}

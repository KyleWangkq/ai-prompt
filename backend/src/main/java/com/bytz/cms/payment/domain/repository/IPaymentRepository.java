package com.bytz.cms.payment.domain.repository;

import com.bytz.cms.payment.domain.model.PaymentAggregate;
import com.bytz.cms.payment.domain.enums.PaymentStatus;
import com.bytz.cms.payment.domain.enums.PaymentType;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * 支付单仓储接口
 * 
 * <p>管理Payment聚合根的持久化和查询</p>
 * 
 * <p>术语来源：Glossary.md - Repository术语"支付单仓储(PaymentRepository)"</p>
 * <p>需求来源：需求文档4.1节支付单管理、4.7节支付结果查询</p>
 * 
 * <p>命名规范：Repository接口使用I前缀</p>
 * 
 * @author DDD设计团队
 * @version 1.0.0
 * @since 2025-01-15
 */
public interface IPaymentRepository {
    
    /**
     * 保存支付单聚合根
     * 
     * <p>用于新建支付单或更新支付单状态</p>
     * 
     * @param payment 支付单聚合根
     * @return 保存成功的支付单聚合根
     */
    PaymentAggregate save(PaymentAggregate payment);
    
    /**
     * 根据支付单号查询支付单
     * 
     * @param paymentId 支付单号
     * @return 支付单聚合根（可能为空）
     */
    Optional<PaymentAggregate> findById(String paymentId);
    
    /**
     * 根据订单号查询支付单列表
     * 
     * @param orderId 订单号
     * @return 支付单列表
     */
    List<PaymentAggregate> findByOrderId(String orderId);
    
    /**
     * 根据经销商ID查询支付单列表
     * 
     * @param resellerId 经销商ID
     * @return 支付单列表
     */
    List<PaymentAggregate> findByResellerId(String resellerId);
    
    /**
     * 筛选支付单（支持多条件筛选）
     * 
     * <p>用例来源：UC-PM-002筛选支付单</p>
     * 
     * @param resellerId 经销商ID（必填）
     * @param paymentStatus 支付状态（可选）
     * @param paymentType 支付类型（可选）
     * @param startTime 创建开始时间（可选）
     * @param endTime 创建结束时间（可选）
     * @return 符合条件的支付单列表
     */
    List<PaymentAggregate> filter(
            String resellerId,
            PaymentStatus paymentStatus,
            PaymentType paymentType,
            LocalDateTime startTime,
            LocalDateTime endTime);
    
    /**
     * 根据关联业务查询支付单
     * 
     * <p>用于信用还款等场景，查询特定业务关联的支付单</p>
     * 
     * @param relatedBusinessId 关联业务ID
     * @return 支付单列表
     */
    List<PaymentAggregate> findByRelatedBusinessId(String relatedBusinessId);
    
    /**
     * 检查支付单是否存在
     * 
     * @param paymentId 支付单号
     * @return true表示存在，false表示不存在
     */
    boolean exists(String paymentId);
    
    /**
     * 根据支付单号加载完整聚合（包含支付流水）
     * 
     * <p>用于需要查看支付流水历史的场景</p>
     * 
     * @param paymentId 支付单号
     * @return 包含流水的支付单聚合根（可能为空）
     */
    Optional<PaymentAggregate> loadWithTransactions(String paymentId);
}

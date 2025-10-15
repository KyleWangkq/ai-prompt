package com.bytz.cms.payment.domain.repository;

import com.bytz.cms.payment.domain.model.Payment;
import com.bytz.cms.payment.domain.model.PaymentStatus;
import com.bytz.cms.payment.domain.model.PaymentType;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Payment Repository Interface (Domain Layer)
 * 支付单仓储接口
 */
public interface PaymentRepository {
    
    /**
     * 保存支付单
     */
    void save(Payment payment);
    
    /**
     * 根据ID查找支付单
     */
    Optional<Payment> findById(String id);
    
    /**
     * 根据订单ID查找支付单列表
     */
    List<Payment> findByOrderId(String orderId);
    
    /**
     * 根据经销商ID查找支付单列表
     */
    List<Payment> findByResellerId(String resellerId);
    
    /**
     * 筛选支付单（UC-PM-002）
     */
    List<Payment> filterPayments(String resellerId,
                                 List<PaymentStatus> statuses,
                                 List<PaymentType> types,
                                 LocalDateTime startTime,
                                 LocalDateTime endTime);
    
    /**
     * 检查支付单号是否存在
     */
    boolean existsById(String id);
    
    /**
     * 更新支付单
     */
    void update(Payment payment);
    
    /**
     * 根据关联业务ID查找支付单
     */
    Optional<Payment> findByRelatedBusinessId(String relatedBusinessId);
}

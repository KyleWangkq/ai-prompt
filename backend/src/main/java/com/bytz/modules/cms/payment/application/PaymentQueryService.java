package com.bytz.modules.cms.payment.application;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.IService;
import com.bytz.modules.cms.payment.infrastructure.entity.PaymentEntity;

import java.util.List;

/**
 * 支付单查询服务接口
 * Payment Query Service Interface
 * 
 * 实现CQRS模式，查询服务直通数据库层，使用MyBatis-Plus的IService
 */
public interface PaymentQueryService extends IService<PaymentEntity> {
    
    /**
     * 根据支付单号查询支付单
     * 
     * @param paymentId 支付单号
     * @return 支付单数据库实体
     */
    PaymentEntity getPaymentById(String paymentId);
    
    /**
     * 根据订单号查询支付单列表
     * 
     * @param orderId 订单号
     * @return 支付单列表
     */
    List<PaymentEntity> getPaymentsByOrderId(String orderId);
    
    /**
     * 根据经销商ID查询支付单列表
     * 
     * @param resellerId 经销商ID
     * @return 支付单列表
     */
    List<PaymentEntity> getPaymentsByResellerId(String resellerId);
    
    /**
     * 根据关联业务ID查询支付单列表（用于信用还款查询）
     * 
     * @param relatedBusinessId 关联业务ID
     * @return 支付单列表
     */
    List<PaymentEntity> getPaymentsByRelatedBusinessId(String relatedBusinessId);
}

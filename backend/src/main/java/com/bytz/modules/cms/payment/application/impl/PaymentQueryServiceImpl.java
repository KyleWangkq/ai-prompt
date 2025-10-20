package com.bytz.modules.cms.payment.application.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.bytz.modules.cms.payment.application.PaymentQueryService;
import com.bytz.modules.cms.payment.infrastructure.entity.PaymentEntity;
import com.bytz.modules.cms.payment.infrastructure.mapper.PaymentMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 支付单查询服务实现
 * Payment Query Service Implementation
 * 
 * 实现CQRS模式，查询服务直通数据库层，继承MyBatis-Plus的ServiceImpl
 */
@Slf4j
@Service
public class PaymentQueryServiceImpl extends ServiceImpl<PaymentMapper, PaymentEntity> implements PaymentQueryService {
    
    /**
     * 根据支付单号查询支付单
     * 
     * @param paymentId 支付单号
     * @return 支付单数据库实体
     */
    @Override
    public PaymentEntity getPaymentById(String paymentId) {
        log.info("查询支付单，支付单号: {}", paymentId);
        return getById(paymentId);
    }
    
    /**
     * 根据订单号查询支付单列表
     * 
     * @param orderId 订单号
     * @return 支付单列表
     */
    @Override
    public List<PaymentEntity> getPaymentsByOrderId(String orderId) {
        log.info("根据订单号查询支付单列表，订单号: {}", orderId);
        LambdaQueryWrapper<PaymentEntity> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(PaymentEntity::getOrderId, orderId);
        return list(wrapper);
    }
    
    /**
     * 根据经销商ID查询支付单列表
     * 
     * @param resellerId 经销商ID
     * @return 支付单列表
     */
    @Override
    public List<PaymentEntity> getPaymentsByResellerId(String resellerId) {
        log.info("根据经销商ID查询支付单列表，经销商ID: {}", resellerId);
        LambdaQueryWrapper<PaymentEntity> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(PaymentEntity::getResellerId, resellerId);
        return list(wrapper);
    }
    
    /**
     * 根据关联业务ID查询支付单列表（用于信用还款查询）
     * 
     * @param relatedBusinessId 关联业务ID
     * @return 支付单列表
     */
    @Override
    public List<PaymentEntity> getPaymentsByRelatedBusinessId(String relatedBusinessId) {
        log.info("根据关联业务ID查询支付单列表，关联业务ID: {}", relatedBusinessId);
        LambdaQueryWrapper<PaymentEntity> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(PaymentEntity::getRelatedBusinessId, relatedBusinessId);
        return list(wrapper);
    }
}

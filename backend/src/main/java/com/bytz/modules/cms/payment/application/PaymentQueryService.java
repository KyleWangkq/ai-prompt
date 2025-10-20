package com.bytz.modules.cms.payment.application;

import com.bytz.modules.cms.payment.domain.model.PaymentAggregate;
import com.bytz.modules.cms.payment.domain.repository.IPaymentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 支付单查询服务
 * Payment Query Service
 * 
 * 实现CQRS模式，查询服务直通repository层，不经过领域层
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentQueryService {
    
    private final IPaymentRepository paymentRepository;
    
    /**
     * 根据支付单号查询支付单
     * 
     * @param paymentId 支付单号
     * @return 支付单聚合根
     */
    public PaymentAggregate getPaymentById(String paymentId) {
        log.info("查询支付单，支付单号: {}", paymentId);
        return paymentRepository.findById(paymentId);
    }
    
    /**
     * 根据订单号查询支付单列表
     * 
     * @param orderId 订单号
     * @return 支付单列表
     */
    public List<PaymentAggregate> getPaymentsByOrderId(String orderId) {
        log.info("根据订单号查询支付单列表，订单号: {}", orderId);
        return paymentRepository.findByOrderId(orderId);
    }
    
    /**
     * 根据经销商ID查询支付单列表
     * 
     * @param resellerId 经销商ID
     * @return 支付单列表
     */
    public List<PaymentAggregate> getPaymentsByResellerId(String resellerId) {
        log.info("根据经销商ID查询支付单列表，经销商ID: {}", resellerId);
        return paymentRepository.findByResellerId(resellerId);
    }
    
    /**
     * 根据关联业务ID查询支付单列表（用于信用还款查询）
     * 
     * @param relatedBusinessId 关联业务ID
     * @return 支付单列表
     */
    public List<PaymentAggregate> getPaymentsByRelatedBusinessId(String relatedBusinessId) {
        log.info("根据关联业务ID查询支付单列表，关联业务ID: {}", relatedBusinessId);
        return paymentRepository.findByRelatedBusinessId(relatedBusinessId);
    }
}

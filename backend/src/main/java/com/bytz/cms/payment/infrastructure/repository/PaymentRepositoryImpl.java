package com.bytz.cms.payment.infrastructure.repository;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.bytz.cms.payment.domain.entity.PaymentTransactionEntity;
import com.bytz.cms.payment.domain.enums.*;
import com.bytz.cms.payment.domain.model.PaymentAggregate;
import com.bytz.cms.payment.domain.repository.IPaymentRepository;
import com.bytz.cms.payment.infrastructure.dataobject.PaymentDO;
import com.bytz.cms.payment.infrastructure.dataobject.PaymentTransactionDO;
import com.bytz.cms.payment.infrastructure.mapper.PaymentMapper;
import com.bytz.cms.payment.infrastructure.mapper.PaymentTransactionMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * 支付单仓储实现
 * 
 * <p>实现支付单聚合根的持久化和查询操作</p>
 * <p>负责领域模型与数据模型之间的转换</p>
 * 
 * <p>术语来源：Glossary.md - Repository术语"支付单仓储(PaymentRepository)"</p>
 * <p>需求来源：需求文档4.1节支付单管理、4.7节支付结果查询</p>
 * 
 * @author DDD设计团队
 * @version 1.0.0
 * @since 2025-01-15
 */
@Slf4j
@Repository
public class PaymentRepositoryImpl implements IPaymentRepository {
    
    @Resource
    private PaymentMapper paymentMapper;
    
    @Resource
    private PaymentTransactionMapper transactionMapper;

    @Override
    public PaymentAggregate save(PaymentAggregate payment) {
        PaymentDO paymentDO = toPaymentDO(payment);
        
        if (paymentMapper.selectById(paymentDO.getId()) == null) {
            // 新增
            paymentMapper.insert(paymentDO);
            log.info("创建支付单成功：{}", paymentDO.getId());
        } else {
            // 更新
            paymentMapper.updateById(paymentDO);
            log.info("更新支付单成功：{}", paymentDO.getId());
        }
        
        // 保存流水记录
        if (payment.getTransactions() != null && !payment.getTransactions().isEmpty()) {
            for (PaymentTransactionEntity transaction : payment.getTransactions()) {
                PaymentTransactionDO transactionDO = toTransactionDO(transaction);
                if (transactionMapper.selectById(transactionDO.getId()) == null) {
                    transactionMapper.insert(transactionDO);
                } else {
                    transactionMapper.updateById(transactionDO);
                }
            }
        }
        
        return payment;
    }

    @Override
    public Optional<PaymentAggregate> findById(String paymentId) {
        PaymentDO paymentDO = paymentMapper.selectById(paymentId);
        if (paymentDO == null) {
            return Optional.empty();
        }
        return Optional.of(toAggregate(paymentDO));
    }

    @Override
    public List<PaymentAggregate> findByOrderId(String orderId) {
        LambdaQueryWrapper<PaymentDO> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(PaymentDO::getOrderId, orderId);
        List<PaymentDO> paymentDOList = paymentMapper.selectList(wrapper);
        return paymentDOList.stream()
                .map(this::toAggregate)
                .collect(Collectors.toList());
    }

    @Override
    public List<PaymentAggregate> findByResellerId(String resellerId) {
        LambdaQueryWrapper<PaymentDO> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(PaymentDO::getResellerId, resellerId);
        List<PaymentDO> paymentDOList = paymentMapper.selectList(wrapper);
        return paymentDOList.stream()
                .map(this::toAggregate)
                .collect(Collectors.toList());
    }

    @Override
    public List<PaymentAggregate> filter(
            String resellerId,
            PaymentStatus paymentStatus,
            PaymentType paymentType,
            LocalDateTime startTime,
            LocalDateTime endTime) {
        
        LambdaQueryWrapper<PaymentDO> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(PaymentDO::getResellerId, resellerId);
        
        if (paymentStatus != null) {
            wrapper.eq(PaymentDO::getPaymentStatus, paymentStatus.name());
        }
        if (paymentType != null) {
            wrapper.eq(PaymentDO::getPaymentType, paymentType.name());
        }
        if (startTime != null) {
            wrapper.ge(PaymentDO::getCreateTime, startTime);
        }
        if (endTime != null) {
            wrapper.le(PaymentDO::getCreateTime, endTime);
        }
        
        List<PaymentDO> paymentDOList = paymentMapper.selectList(wrapper);
        return paymentDOList.stream()
                .map(this::toAggregate)
                .collect(Collectors.toList());
    }

    @Override
    public List<PaymentAggregate> findByRelatedBusinessId(String relatedBusinessId) {
        LambdaQueryWrapper<PaymentDO> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(PaymentDO::getRelatedBusinessId, relatedBusinessId);
        List<PaymentDO> paymentDOList = paymentMapper.selectList(wrapper);
        return paymentDOList.stream()
                .map(this::toAggregate)
                .collect(Collectors.toList());
    }

    @Override
    public boolean exists(String paymentId) {
        return paymentMapper.selectById(paymentId) != null;
    }

    @Override
    public Optional<PaymentAggregate> loadWithTransactions(String paymentId) {
        PaymentDO paymentDO = paymentMapper.selectById(paymentId);
        if (paymentDO == null) {
            return Optional.empty();
        }
        
        PaymentAggregate aggregate = toAggregate(paymentDO);
        
        // 加载流水记录
        List<PaymentTransactionDO> transactionDOList = transactionMapper.selectByPaymentId(paymentId);
        List<PaymentTransactionEntity> transactions = transactionDOList.stream()
                .map(this::toTransactionEntity)
                .collect(Collectors.toList());
        
        aggregate.setTransactions(transactions);
        
        return Optional.of(aggregate);
    }

    /**
     * 将聚合根转换为数据对象
     * 
     * @param aggregate 聚合根
     * @return 数据对象
     */
    private PaymentDO toPaymentDO(PaymentAggregate aggregate) {
        PaymentDO paymentDO = new PaymentDO();
        paymentDO.setId(aggregate.getId());
        paymentDO.setOrderId(aggregate.getOrderId());
        paymentDO.setResellerId(aggregate.getResellerId());
        paymentDO.setPaymentAmount(aggregate.getPaymentAmount());
        paymentDO.setPaidAmount(aggregate.getPaidAmount());
        paymentDO.setRefundedAmount(aggregate.getRefundedAmount());
        paymentDO.setActualAmount(aggregate.getActualAmount());
        paymentDO.setCurrency(aggregate.getCurrency());
        paymentDO.setPaymentType(aggregate.getPaymentType() != null ? aggregate.getPaymentType().name() : null);
        paymentDO.setPaymentStatus(aggregate.getPaymentStatus() != null ? aggregate.getPaymentStatus().name() : null);
        paymentDO.setRefundStatus(aggregate.getRefundStatus() != null ? aggregate.getRefundStatus().name() : null);
        paymentDO.setBusinessDesc(aggregate.getBusinessDesc());
        paymentDO.setPaymentDeadline(aggregate.getPaymentDeadline());
        paymentDO.setPriorityLevel(aggregate.getPriorityLevel());
        paymentDO.setRelatedBusinessId(aggregate.getRelatedBusinessId());
        paymentDO.setRelatedBusinessType(aggregate.getRelatedBusinessType() != null ? aggregate.getRelatedBusinessType().name() : null);
        paymentDO.setBusinessExpireDate(aggregate.getBusinessExpireDate());
        paymentDO.setBusinessTags(aggregate.getBusinessTags());
        paymentDO.setDelFlag(aggregate.getDelFlag());
        paymentDO.setCreateBy(aggregate.getCreateBy());
        paymentDO.setCreateByName(aggregate.getCreateByName());
        paymentDO.setCreateTime(aggregate.getCreatedTime());
        paymentDO.setUpdateBy(aggregate.getUpdateBy());
        paymentDO.setUpdateByName(aggregate.getUpdateByName());
        paymentDO.setUpdateTime(aggregate.getUpdatedTime());
        return paymentDO;
    }

    /**
     * 将数据对象转换为聚合根
     * 
     * @param paymentDO 数据对象
     * @return 聚合根
     */
    private PaymentAggregate toAggregate(PaymentDO paymentDO) {
        PaymentAggregate aggregate = new PaymentAggregate();
        aggregate.setId(paymentDO.getId());
        aggregate.setOrderId(paymentDO.getOrderId());
        aggregate.setResellerId(paymentDO.getResellerId());
        aggregate.setPaymentAmount(paymentDO.getPaymentAmount());
        aggregate.setPaidAmount(paymentDO.getPaidAmount());
        aggregate.setRefundedAmount(paymentDO.getRefundedAmount());
        aggregate.setActualAmount(paymentDO.getActualAmount());
        aggregate.setCurrency(paymentDO.getCurrency());
        aggregate.setPaymentType(paymentDO.getPaymentType() != null ? PaymentType.valueOf(paymentDO.getPaymentType()) : null);
        aggregate.setPaymentStatus(paymentDO.getPaymentStatus() != null ? PaymentStatus.valueOf(paymentDO.getPaymentStatus()) : null);
        aggregate.setRefundStatus(paymentDO.getRefundStatus() != null ? RefundStatus.valueOf(paymentDO.getRefundStatus()) : null);
        aggregate.setBusinessDesc(paymentDO.getBusinessDesc());
        aggregate.setPaymentDeadline(paymentDO.getPaymentDeadline());
        aggregate.setPriorityLevel(paymentDO.getPriorityLevel());
        aggregate.setRelatedBusinessId(paymentDO.getRelatedBusinessId());
        aggregate.setRelatedBusinessType(paymentDO.getRelatedBusinessType() != null ? RelatedBusinessType.valueOf(paymentDO.getRelatedBusinessType()) : null);
        aggregate.setBusinessExpireDate(paymentDO.getBusinessExpireDate());
        aggregate.setBusinessTags(paymentDO.getBusinessTags());
        aggregate.setDelFlag(paymentDO.getDelFlag());
        aggregate.setCreateBy(paymentDO.getCreateBy());
        aggregate.setCreateByName(paymentDO.getCreateByName());
        aggregate.setCreatedTime(paymentDO.getCreateTime());
        aggregate.setUpdateBy(paymentDO.getUpdateBy());
        aggregate.setUpdateByName(paymentDO.getUpdateByName());
        aggregate.setUpdatedTime(paymentDO.getUpdateTime());
        aggregate.setTransactions(new ArrayList<>());
        return aggregate;
    }

    /**
     * 将流水实体转换为数据对象
     * 
     * @param entity 流水实体
     * @return 数据对象
     */
    private PaymentTransactionDO toTransactionDO(PaymentTransactionEntity entity) {
        PaymentTransactionDO transactionDO = new PaymentTransactionDO();
        transactionDO.setId(entity.getId());
        transactionDO.setPaymentId(entity.getPaymentId());
        transactionDO.setTransactionType(entity.getTransactionType() != null ? entity.getTransactionType().name() : null);
        transactionDO.setTransactionStatus(entity.getTransactionStatus() != null ? entity.getTransactionStatus().name() : null);
        transactionDO.setTransactionAmount(entity.getTransactionAmount());
        transactionDO.setPaymentChannel(entity.getPaymentChannel() != null ? entity.getPaymentChannel().name() : null);
        transactionDO.setChannelTransactionNumber(entity.getChannelTransactionNumber());
        transactionDO.setPaymentWay(entity.getPaymentWay());
        transactionDO.setOriginalTransactionId(entity.getOriginalTransactionId());
        transactionDO.setBusinessOrderId(entity.getBusinessOrderId());
        transactionDO.setBusinessRemark(entity.getBusinessRemark());
        transactionDO.setDelFlag(entity.getDelFlag());
        transactionDO.setCreateBy(entity.getCreateBy());
        transactionDO.setCreateByName(entity.getCreateByName());
        transactionDO.setCreateTime(entity.getCreatedTime());
        transactionDO.setCompleteDatetime(entity.getCompleteDatetime());
        transactionDO.setExpirationTime(entity.getExpirationTime());
        transactionDO.setUpdateBy(entity.getUpdateBy());
        transactionDO.setUpdateByName(entity.getUpdateByName());
        transactionDO.setUpdateTime(entity.getUpdatedTime());
        return transactionDO;
    }

    /**
     * 将数据对象转换为流水实体
     * 
     * @param transactionDO 数据对象
     * @return 流水实体
     */
    private PaymentTransactionEntity toTransactionEntity(PaymentTransactionDO transactionDO) {
        PaymentTransactionEntity entity = new PaymentTransactionEntity();
        entity.setId(transactionDO.getId());
        entity.setPaymentId(transactionDO.getPaymentId());
        entity.setTransactionType(transactionDO.getTransactionType() != null ? TransactionType.valueOf(transactionDO.getTransactionType()) : null);
        entity.setTransactionStatus(transactionDO.getTransactionStatus() != null ? TransactionStatus.valueOf(transactionDO.getTransactionStatus()) : null);
        entity.setTransactionAmount(transactionDO.getTransactionAmount());
        entity.setPaymentChannel(transactionDO.getPaymentChannel() != null ? PaymentChannel.valueOf(transactionDO.getPaymentChannel()) : null);
        entity.setChannelTransactionNumber(transactionDO.getChannelTransactionNumber());
        entity.setPaymentWay(transactionDO.getPaymentWay());
        entity.setOriginalTransactionId(transactionDO.getOriginalTransactionId());
        entity.setBusinessOrderId(transactionDO.getBusinessOrderId());
        entity.setBusinessRemark(transactionDO.getBusinessRemark());
        entity.setDelFlag(transactionDO.getDelFlag());
        entity.setCreateBy(transactionDO.getCreateBy());
        entity.setCreateByName(transactionDO.getCreateByName());
        entity.setCreatedTime(transactionDO.getCreateTime());
        entity.setCompleteDatetime(transactionDO.getCompleteDatetime());
        entity.setExpirationTime(transactionDO.getExpirationTime());
        entity.setUpdateBy(transactionDO.getUpdateBy());
        entity.setUpdateByName(transactionDO.getUpdateByName());
        entity.setUpdatedTime(transactionDO.getUpdateTime());
        return entity;
    }
}

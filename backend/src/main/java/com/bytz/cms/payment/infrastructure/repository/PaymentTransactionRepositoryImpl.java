package com.bytz.cms.payment.infrastructure.repository;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.bytz.cms.payment.domain.model.*;
import com.bytz.cms.payment.domain.repository.PaymentTransactionRepository;
import com.bytz.cms.payment.infrastructure.mapper.PaymentTransactionMapper;
import com.bytz.cms.payment.infrastructure.persistence.PaymentTransactionEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * PaymentTransaction Repository Implementation
 * 支付流水仓储实现类
 */
@Repository
@RequiredArgsConstructor
public class PaymentTransactionRepositoryImpl implements PaymentTransactionRepository {
    
    private final PaymentTransactionMapper transactionMapper;
    
    @Override
    public void save(PaymentTransaction transaction) {
        PaymentTransactionEntity entity = toEntity(transaction);
        transactionMapper.insert(entity);
    }
    
    @Override
    public Optional<PaymentTransaction> findById(String id) {
        PaymentTransactionEntity entity = transactionMapper.selectById(id);
        return Optional.ofNullable(entity).map(this::toDomain);
    }
    
    @Override
    public List<PaymentTransaction> findByPaymentId(String paymentId) {
        LambdaQueryWrapper<PaymentTransactionEntity> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(PaymentTransactionEntity::getPaymentId, paymentId)
               .orderByDesc(PaymentTransactionEntity::getCreateTime);
        List<PaymentTransactionEntity> entities = transactionMapper.selectList(wrapper);
        return entities.stream().map(this::toDomain).collect(Collectors.toList());
    }
    
    @Override
    public List<PaymentTransaction> findByPaymentIdAndType(String paymentId, TransactionType transactionType) {
        LambdaQueryWrapper<PaymentTransactionEntity> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(PaymentTransactionEntity::getPaymentId, paymentId)
               .eq(PaymentTransactionEntity::getTransactionType, transactionType.name())
               .orderByDesc(PaymentTransactionEntity::getCreateTime);
        List<PaymentTransactionEntity> entities = transactionMapper.selectList(wrapper);
        return entities.stream().map(this::toDomain).collect(Collectors.toList());
    }
    
    @Override
    public List<PaymentTransaction> findByChannelTransactionNumber(String channelTransactionNumber) {
        LambdaQueryWrapper<PaymentTransactionEntity> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(PaymentTransactionEntity::getChannelTransactionNumber, channelTransactionNumber);
        List<PaymentTransactionEntity> entities = transactionMapper.selectList(wrapper);
        return entities.stream().map(this::toDomain).collect(Collectors.toList());
    }
    
    @Override
    public void update(PaymentTransaction transaction) {
        PaymentTransactionEntity entity = toEntity(transaction);
        transactionMapper.updateById(entity);
    }
    
    /**
     * 将领域模型转换为数据库实体
     */
    private PaymentTransactionEntity toEntity(PaymentTransaction transaction) {
        PaymentTransactionEntity entity = new PaymentTransactionEntity();
        entity.setId(transaction.getId());
        entity.setPaymentId(transaction.getPaymentId());
        entity.setTransactionType(transaction.getTransactionType().name());
        entity.setTransactionStatus(transaction.getTransactionStatus().name());
        entity.setTransactionAmount(transaction.getTransactionAmount());
        entity.setPaymentChannel(transaction.getPaymentChannel().name());
        entity.setChannelTransactionNumber(transaction.getChannelTransactionNumber());
        entity.setPaymentWay(transaction.getPaymentWay());
        entity.setOriginalTransactionId(transaction.getOriginalTransactionId());
        entity.setBusinessOrderId(transaction.getBusinessOrderId());
        entity.setCreateTime(transaction.getCreatedTime());
        entity.setCompleteDatetime(transaction.getCompleteDatetime());
        entity.setExpirationTime(transaction.getExpirationTime());
        entity.setRemark(transaction.getRemark());
        return entity;
    }
    
    /**
     * 将数据库实体转换为领域模型
     */
    private PaymentTransaction toDomain(PaymentTransactionEntity entity) {
        PaymentTransaction transaction = PaymentTransaction.start(
                entity.getId(),
                entity.getPaymentId(),
                TransactionType.valueOf(entity.getTransactionType()),
                entity.getTransactionAmount(),
                PaymentChannel.valueOf(entity.getPaymentChannel()),
                entity.getPaymentWay(),
                entity.getChannelTransactionNumber(),
                entity.getExpirationTime()
        );
        
        // 设置状态和其他信息
        transaction.setTransactionStatus(TransactionStatus.valueOf(entity.getTransactionStatus()));
        transaction.setOriginalTransactionId(entity.getOriginalTransactionId());
        transaction.setBusinessOrderId(entity.getBusinessOrderId());
        transaction.setCreatedTime(entity.getCreateTime());
        transaction.setCompleteDatetime(entity.getCompleteDatetime());
        transaction.setRemark(entity.getRemark());
        
        return transaction;
    }
}

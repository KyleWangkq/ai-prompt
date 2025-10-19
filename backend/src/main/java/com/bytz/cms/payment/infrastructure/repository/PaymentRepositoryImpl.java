package com.bytz.cms.payment.infrastructure.repository;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.bytz.cms.payment.domain.entity.PaymentTransactionEntity;
import com.bytz.cms.payment.domain.enums.*;
import com.bytz.cms.payment.domain.model.PaymentAggregate;
import com.bytz.cms.payment.domain.repository.IPaymentRepository;
import com.bytz.cms.payment.infrastructure.mapper.PaymentMapper;
import com.bytz.cms.payment.infrastructure.mapper.PaymentTransactionMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * 支付单仓储实现
 * Payment Repository Implementation
 * 
 * 实现IPaymentRepository接口，处理支付单聚合根的持久化
 * 负责领域对象和数据库实体之间的转换
 */
@Slf4j
@Repository
@RequiredArgsConstructor
public class PaymentRepositoryImpl implements IPaymentRepository {
    
    private final PaymentMapper paymentMapper;
    private final PaymentTransactionMapper transactionMapper;
    
    /**
     * 保存支付单聚合根
     * 
     * @param payment 支付单聚合根
     * @return 保存后的支付单聚合根
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public PaymentAggregate save(PaymentAggregate payment) {
        log.info("保存支付单聚合根，支付单号: {}", payment.getId());
        
        // 转换为数据库实体
        com.bytz.cms.payment.infrastructure.entity.PaymentEntity entity = toPaymentEntity(payment);
        
        // 判断是插入还是更新
        if (existsById(payment.getId())) {
            paymentMapper.updateById(entity);
        } else {
            paymentMapper.insert(entity);
        }
        
        // 保存支付流水
        for (PaymentTransactionEntity transaction : payment.getTransactions()) {
            com.bytz.cms.payment.infrastructure.entity.PaymentTransactionEntity transactionEntity = 
                    toTransactionEntity(transaction);
            
            if (transaction.getId() == null || transaction.getId().isEmpty()) {
                transaction.setId(generateTransactionId());
                transactionEntity.setId(transaction.getId());
                transactionMapper.insert(transactionEntity);
            } else if (existsTransactionById(transaction.getId())) {
                transactionMapper.updateById(transactionEntity);
            } else {
                transactionMapper.insert(transactionEntity);
            }
        }
        
        return payment;
    }
    
    /**
     * 根据ID查找支付单
     * 
     * @param id 支付单号
     * @return 支付单聚合根，如果未找到返回null
     */
    @Override
    public PaymentAggregate findById(String id) {
        log.info("查找支付单，支付单号: {}", id);
        
        com.bytz.cms.payment.infrastructure.entity.PaymentEntity entity = paymentMapper.selectById(id);
        if (entity == null) {
            return null;
        }
        
        // 查询支付流水
        LambdaQueryWrapper<com.bytz.cms.payment.infrastructure.entity.PaymentTransactionEntity> wrapper = 
                new LambdaQueryWrapper<>();
        wrapper.eq(com.bytz.cms.payment.infrastructure.entity.PaymentTransactionEntity::getPaymentId, id);
        List<com.bytz.cms.payment.infrastructure.entity.PaymentTransactionEntity> transactionEntities = 
                transactionMapper.selectList(wrapper);
        
        // 转换为领域对象
        return toPaymentAggregate(entity, transactionEntities);
    }
    
    /**
     * 根据订单号查找支付单列表
     * 
     * @param orderId 订单号
     * @return 支付单列表
     */
    @Override
    public List<PaymentAggregate> findByOrderId(String orderId) {
        log.info("根据订单号查找支付单列表，订单号: {}", orderId);
        
        LambdaQueryWrapper<com.bytz.cms.payment.infrastructure.entity.PaymentEntity> wrapper = 
                new LambdaQueryWrapper<>();
        wrapper.eq(com.bytz.cms.payment.infrastructure.entity.PaymentEntity::getOrderId, orderId);
        
        List<com.bytz.cms.payment.infrastructure.entity.PaymentEntity> entities = paymentMapper.selectList(wrapper);
        
        return entities.stream()
                .map(entity -> {
                    List<com.bytz.cms.payment.infrastructure.entity.PaymentTransactionEntity> transactions = 
                            findTransactionsByPaymentId(entity.getId());
                    return toPaymentAggregate(entity, transactions);
                })
                .collect(Collectors.toList());
    }
    
    /**
     * 根据经销商ID查找支付单列表
     * 
     * @param resellerId 经销商ID
     * @return 支付单列表
     */
    @Override
    public List<PaymentAggregate> findByResellerId(String resellerId) {
        log.info("根据经销商ID查找支付单列表，经销商ID: {}", resellerId);
        
        LambdaQueryWrapper<com.bytz.cms.payment.infrastructure.entity.PaymentEntity> wrapper = 
                new LambdaQueryWrapper<>();
        wrapper.eq(com.bytz.cms.payment.infrastructure.entity.PaymentEntity::getResellerId, resellerId);
        
        List<com.bytz.cms.payment.infrastructure.entity.PaymentEntity> entities = paymentMapper.selectList(wrapper);
        
        return entities.stream()
                .map(entity -> {
                    List<com.bytz.cms.payment.infrastructure.entity.PaymentTransactionEntity> transactions = 
                            findTransactionsByPaymentId(entity.getId());
                    return toPaymentAggregate(entity, transactions);
                })
                .collect(Collectors.toList());
    }
    
    /**
     * 根据关联业务ID查找支付单列表
     * 
     * @param relatedBusinessId 关联业务ID
     * @return 支付单列表
     */
    @Override
    public List<PaymentAggregate> findByRelatedBusinessId(String relatedBusinessId) {
        log.info("根据关联业务ID查找支付单列表，关联业务ID: {}", relatedBusinessId);
        
        LambdaQueryWrapper<com.bytz.cms.payment.infrastructure.entity.PaymentEntity> wrapper = 
                new LambdaQueryWrapper<>();
        wrapper.eq(com.bytz.cms.payment.infrastructure.entity.PaymentEntity::getRelatedBusinessId, relatedBusinessId);
        
        List<com.bytz.cms.payment.infrastructure.entity.PaymentEntity> entities = paymentMapper.selectList(wrapper);
        
        return entities.stream()
                .map(entity -> {
                    List<com.bytz.cms.payment.infrastructure.entity.PaymentTransactionEntity> transactions = 
                            findTransactionsByPaymentId(entity.getId());
                    return toPaymentAggregate(entity, transactions);
                })
                .collect(Collectors.toList());
    }
    
    /**
     * 删除支付单（逻辑删除）
     * 
     * @param id 支付单号
     * @return 是否删除成功
     */
    @Override
    public boolean deleteById(String id) {
        log.info("删除支付单，支付单号: {}", id);
        return paymentMapper.deleteById(id) > 0;
    }
    
    /**
     * 检查支付单是否存在
     * 
     * @param id 支付单号
     * @return 是否存在
     */
    @Override
    public boolean existsById(String id) {
        return paymentMapper.selectById(id) != null;
    }
    
    /**
     * 生成唯一的支付单号
     * 
     * @return 唯一的支付单号
     * TODO: 实现更优的支付单号生成策略
     */
    @Override
    public String generatePaymentId() {
        // 格式：PAY + 年月日 + 8位随机字符
        String dateStr = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        String randomStr = UUID.randomUUID().toString().replace("-", "").substring(0, 8).toUpperCase();
        return "PAY" + dateStr + randomStr;
    }
    
    /**
     * 生成唯一的支付流水号
     * 
     * @return 唯一的支付流水号
     * TODO: 实现更优的支付流水号生成策略
     */
    @Override
    public String generateTransactionId() {
        // 格式：TXN + 年月日时分秒 + 6位随机字符
        String dateTimeStr = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
        String randomStr = UUID.randomUUID().toString().replace("-", "").substring(0, 6).toUpperCase();
        return "TXN" + dateTimeStr + randomStr;
    }
    
    /**
     * 检查支付流水是否存在
     */
    private boolean existsTransactionById(String id) {
        return transactionMapper.selectById(id) != null;
    }
    
    /**
     * 根据支付单ID查找支付流水
     */
    private List<com.bytz.cms.payment.infrastructure.entity.PaymentTransactionEntity> findTransactionsByPaymentId(String paymentId) {
        LambdaQueryWrapper<com.bytz.cms.payment.infrastructure.entity.PaymentTransactionEntity> wrapper = 
                new LambdaQueryWrapper<>();
        wrapper.eq(com.bytz.cms.payment.infrastructure.entity.PaymentTransactionEntity::getPaymentId, paymentId);
        return transactionMapper.selectList(wrapper);
    }
    
    /**
     * 将领域对象转换为数据库实体
     */
    private com.bytz.cms.payment.infrastructure.entity.PaymentEntity toPaymentEntity(PaymentAggregate aggregate) {
        return com.bytz.cms.payment.infrastructure.entity.PaymentEntity.builder()
                .id(aggregate.getId())
                .orderId(aggregate.getOrderId())
                .resellerId(aggregate.getResellerId())
                .paymentAmount(aggregate.getPaymentAmount())
                .paidAmount(aggregate.getPaidAmount())
                .refundedAmount(aggregate.getRefundedAmount())
                .actualAmount(aggregate.getActualAmount())
                .currency(aggregate.getCurrency())
                .paymentType(aggregate.getPaymentType().name())
                .paymentStatus(aggregate.getPaymentStatus().name())
                .refundStatus(aggregate.getRefundStatus().name())
                .businessDesc(aggregate.getBusinessDesc())
                .paymentDeadline(aggregate.getPaymentDeadline())
                .relatedBusinessId(aggregate.getRelatedBusinessId())
                .relatedBusinessType(aggregate.getRelatedBusinessType() != null ? 
                        aggregate.getRelatedBusinessType().name() : null)
                .businessExpireDate(aggregate.getBusinessExpireDate())
                .businessTags(aggregate.getBusinessTags())
                .createBy(aggregate.getCreateBy())
                .createByName(aggregate.getCreateByName())
                .createTime(aggregate.getCreateTime())
                .updateBy(aggregate.getUpdateBy())
                .updateByName(aggregate.getUpdateByName())
                .updateTime(aggregate.getUpdateTime())
                .build();
    }
    
    /**
     * 将领域流水实体转换为数据库实体
     */
    private com.bytz.cms.payment.infrastructure.entity.PaymentTransactionEntity toTransactionEntity(
            PaymentTransactionEntity domainEntity) {
        return com.bytz.cms.payment.infrastructure.entity.PaymentTransactionEntity.builder()
                .id(domainEntity.getId())
                .paymentId(domainEntity.getPaymentId())
                .transactionType(domainEntity.getTransactionType().name())
                .transactionStatus(domainEntity.getTransactionStatus().name())
                .transactionAmount(domainEntity.getTransactionAmount())
                .paymentChannel(domainEntity.getPaymentChannel().name())
                .channelTransactionNumber(domainEntity.getChannelTransactionNumber())
                .paymentWay(domainEntity.getPaymentWay())
                .originalTransactionId(domainEntity.getOriginalTransactionId())
                .businessOrderId(domainEntity.getBusinessOrderId())
                .createTime(domainEntity.getCreateTime())
                .completeDateTime(domainEntity.getCompleteDateTime())
                .expirationTime(domainEntity.getExpirationTime())
                .businessRemark(domainEntity.getBusinessRemark())
                .createBy(domainEntity.getCreateBy())
                .createByName(domainEntity.getCreateByName())
                .updateBy(domainEntity.getUpdateBy())
                .updateByName(domainEntity.getUpdateByName())
                .updateTime(domainEntity.getUpdateTime())
                .build();
    }
    
    /**
     * 将数据库实体转换为领域对象
     */
    private PaymentAggregate toPaymentAggregate(
            com.bytz.cms.payment.infrastructure.entity.PaymentEntity entity,
            List<com.bytz.cms.payment.infrastructure.entity.PaymentTransactionEntity> transactionEntities) {
        
        List<PaymentTransactionEntity> transactions = transactionEntities.stream()
                .map(this::toDomainTransaction)
                .collect(Collectors.toList());
        
        return PaymentAggregate.builder()
                .id(entity.getId())
                .orderId(entity.getOrderId())
                .resellerId(entity.getResellerId())
                .paymentAmount(entity.getPaymentAmount() != null ? entity.getPaymentAmount() : BigDecimal.ZERO)
                .paidAmount(entity.getPaidAmount() != null ? entity.getPaidAmount() : BigDecimal.ZERO)
                .refundedAmount(entity.getRefundedAmount() != null ? entity.getRefundedAmount() : BigDecimal.ZERO)
                .actualAmount(entity.getActualAmount() != null ? entity.getActualAmount() : BigDecimal.ZERO)
                .currency(entity.getCurrency() != null ? entity.getCurrency() : "CNY")
                .paymentType(PaymentType.valueOf(entity.getPaymentType()))
                .paymentStatus(PaymentStatus.valueOf(entity.getPaymentStatus()))
                .refundStatus(RefundStatus.valueOf(entity.getRefundStatus()))
                .businessDesc(entity.getBusinessDesc())
                .paymentDeadline(entity.getPaymentDeadline())
                .relatedBusinessId(entity.getRelatedBusinessId())
                .relatedBusinessType(entity.getRelatedBusinessType() != null ? 
                        RelatedBusinessType.valueOf(entity.getRelatedBusinessType()) : null)
                .businessExpireDate(entity.getBusinessExpireDate())
                .businessTags(entity.getBusinessTags())
                .createBy(entity.getCreateBy())
                .createByName(entity.getCreateByName())
                .createTime(entity.getCreateTime())
                .updateBy(entity.getUpdateBy())
                .updateByName(entity.getUpdateByName())
                .updateTime(entity.getUpdateTime())
                .transactions(transactions)
                .build();
    }
    
    /**
     * 将数据库流水实体转换为领域流水实体
     */
    private PaymentTransactionEntity toDomainTransaction(
            com.bytz.cms.payment.infrastructure.entity.PaymentTransactionEntity entity) {
        return PaymentTransactionEntity.builder()
                .id(entity.getId())
                .paymentId(entity.getPaymentId())
                .transactionType(TransactionType.valueOf(entity.getTransactionType()))
                .transactionStatus(TransactionStatus.valueOf(entity.getTransactionStatus()))
                .transactionAmount(entity.getTransactionAmount() != null ? entity.getTransactionAmount() : BigDecimal.ZERO)
                .paymentChannel(PaymentChannel.valueOf(entity.getPaymentChannel()))
                .channelTransactionNumber(entity.getChannelTransactionNumber())
                .paymentWay(entity.getPaymentWay())
                .originalTransactionId(entity.getOriginalTransactionId())
                .businessOrderId(entity.getBusinessOrderId())
                .createTime(entity.getCreateTime())
                .completeDateTime(entity.getCompleteDateTime())
                .expirationTime(entity.getExpirationTime())
                .businessRemark(entity.getBusinessRemark())
                .createBy(entity.getCreateBy())
                .createByName(entity.getCreateByName())
                .updateBy(entity.getUpdateBy())
                .updateByName(entity.getUpdateByName())
                .updateTime(entity.getUpdateTime())
                .build();
    }
}

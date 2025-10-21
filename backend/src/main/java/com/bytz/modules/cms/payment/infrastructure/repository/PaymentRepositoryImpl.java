package com.bytz.modules.cms.payment.infrastructure.repository;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.bytz.modules.cms.payment.domain.model.PaymentAggregate;
import com.bytz.modules.cms.payment.domain.model.PaymentTransaction;
import com.bytz.modules.cms.payment.domain.repository.IPaymentRepository;
import com.bytz.modules.cms.payment.infrastructure.assembler.InfrastructureAssembler;
import com.bytz.modules.cms.payment.infrastructure.config.CustomIdGenerator;
import com.bytz.modules.cms.payment.infrastructure.entity.PaymentEntity;
import com.bytz.modules.cms.payment.infrastructure.entity.PaymentTransactionEntity;
import com.bytz.modules.cms.payment.infrastructure.mapper.PaymentMapper;
import com.bytz.modules.cms.payment.infrastructure.mapper.PaymentTransactionMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 支付单仓储实现
 * Payment Repository Implementation
 * 
 * 实现IPaymentRepository接口，处理支付单聚合根的持久化
 * 负责领域对象和数据库实体之间的转换（使用MapStruct）
 */
@Slf4j
@Repository
@RequiredArgsConstructor
public class PaymentRepositoryImpl implements IPaymentRepository {
    
    private final PaymentMapper paymentMapper;
    private final PaymentTransactionMapper transactionMapper;
    private final InfrastructureAssembler infrastructureAssembler;
    private final CustomIdGenerator customIdGenerator;
    
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
        
        // 使用MapStruct转换为数据库实体
        PaymentEntity entity = infrastructureAssembler.toPaymentEntity(payment);
        
        // 判断是插入还是更新
        if (existsById(payment.getId())) {
            paymentMapper.updateById(entity);
        } else {
            paymentMapper.insert(entity);
        }
        
        // 保存支付流水
        for (PaymentTransaction transaction : payment.getTransactions()) {
            // 使用MapStruct转换流水实体
            PaymentTransactionEntity transactionEntity = 
                    infrastructureAssembler.toTransactionEntity(transaction);
            
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
        
        PaymentEntity entity = paymentMapper.selectById(id);
        if (entity == null) {
            return null;
        }
        
        // 查询支付流水
        LambdaQueryWrapper<PaymentTransactionEntity> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(PaymentTransactionEntity::getPaymentId, id);
        List<PaymentTransactionEntity> transactionEntities = transactionMapper.selectList(wrapper);
        
        // 使用MapStruct转换为领域对象
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
        
        LambdaQueryWrapper<PaymentEntity> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(PaymentEntity::getOrderId, orderId);
        
        List<PaymentEntity> entities = paymentMapper.selectList(wrapper);
        
        // 批量查询所有支付流水，避免在循环中调用数据库
        List<String> paymentIds = entities.stream()
                .map(PaymentEntity::getId)
                .collect(Collectors.toList());
        Map<String, List<PaymentTransactionEntity>> transactionsMap = 
                findTransactionsByPaymentIds(paymentIds);
        
        return entities.stream()
                .map(entity -> {
                    List<PaymentTransactionEntity> transactions = 
                            transactionsMap.getOrDefault(entity.getId(), new ArrayList<>());
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
        
        LambdaQueryWrapper<PaymentEntity> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(PaymentEntity::getResellerId, resellerId);
        
        List<PaymentEntity> entities = paymentMapper.selectList(wrapper);
        
        // 批量查询所有支付流水，避免在循环中调用数据库
        List<String> paymentIds = entities.stream()
                .map(PaymentEntity::getId)
                .collect(Collectors.toList());
        Map<String, List<PaymentTransactionEntity>> transactionsMap = 
                findTransactionsByPaymentIds(paymentIds);
        
        return entities.stream()
                .map(entity -> {
                    List<PaymentTransactionEntity> transactions = 
                            transactionsMap.getOrDefault(entity.getId(), new ArrayList<>());
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
        
        LambdaQueryWrapper<PaymentEntity> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(PaymentEntity::getRelatedBusinessId, relatedBusinessId);
        
        List<PaymentEntity> entities = paymentMapper.selectList(wrapper);
        
        // 批量查询所有支付流水，避免在循环中调用数据库
        List<String> paymentIds = entities.stream()
                .map(PaymentEntity::getId)
                .collect(Collectors.toList());
        Map<String, List<PaymentTransactionEntity>> transactionsMap = 
                findTransactionsByPaymentIds(paymentIds);
        
        return entities.stream()
                .map(entity -> {
                    List<PaymentTransactionEntity> transactions = 
                            transactionsMap.getOrDefault(entity.getId(), new ArrayList<>());
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
     * 使用自定义ID生成器（基于MyBatis-Plus的IdentifierGenerator接口）
     * 格式：PAY + 年月日时分秒毫秒(17位) + 随机数(8位) = 28个字符
     * 特性：时间有序、毫秒精度、支持分布式
     * 
     * @return 唯一的支付单号
     */
    @Override
    public String generatePaymentId() {
        return customIdGenerator.generatePaymentId();
    }
    
    /**
     * 生成唯一的支付流水号
     * 
     * 使用自定义ID生成器（基于MyBatis-Plus的IdentifierGenerator接口）
     * 格式：TXN + 年月日时分秒毫秒(17位) + 随机数(8位) = 28个字符
     * 特性：时间有序、毫秒精度、支持分布式
     * 
     * @return 唯一的支付流水号
     */
    @Override
    public String generateTransactionId() {
        return customIdGenerator.generateTransactionId();
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
    private List<PaymentTransactionEntity> findTransactionsByPaymentId(String paymentId) {
        LambdaQueryWrapper<PaymentTransactionEntity> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(PaymentTransactionEntity::getPaymentId, paymentId);
        return transactionMapper.selectList(wrapper);
    }
    
    /**
     * 批量查询支付流水
     * 根据多个支付单号批量查询对应的支付流水，避免在循环中调用数据库
     * 
     * @param paymentIds 支付单号列表
     * @return 支付单号到支付流水列表的映射
     */
    private Map<String, List<PaymentTransactionEntity>> findTransactionsByPaymentIds(List<String> paymentIds) {
        if (paymentIds == null || paymentIds.isEmpty()) {
            return new HashMap<>();
        }
        
        LambdaQueryWrapper<PaymentTransactionEntity> wrapper = new LambdaQueryWrapper<>();
        wrapper.in(PaymentTransactionEntity::getPaymentId, paymentIds);
        List<PaymentTransactionEntity> allTransactions = transactionMapper.selectList(wrapper);
        
        // 按支付单号分组
        return allTransactions.stream()
                .collect(Collectors.groupingBy(PaymentTransactionEntity::getPaymentId));
    }
    
    /**
     * 将数据库实体转换为领域对象（使用MapStruct）
     */
    private PaymentAggregate toPaymentAggregate(
            PaymentEntity entity,
            List<PaymentTransactionEntity> transactionEntities) {
        
        // 使用MapStruct转换支付单
        PaymentAggregate aggregate = infrastructureAssembler.toPaymentAggregate(entity);
        
        // 使用MapStruct转换流水列表
        List<PaymentTransaction> transactions = 
                infrastructureAssembler.toDomainTransactions(transactionEntities);
        
        // 设置流水列表
        aggregate.setTransactions(transactions);
        
        return aggregate;
    }
}

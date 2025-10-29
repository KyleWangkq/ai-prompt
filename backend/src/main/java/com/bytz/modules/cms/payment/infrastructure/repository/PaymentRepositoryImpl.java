package com.bytz.modules.cms.payment.infrastructure.repository;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.bytz.modules.cms.payment.domain.enums.TransactionStatus;
import com.bytz.modules.cms.payment.domain.model.PaymentAggregate;
import com.bytz.modules.cms.payment.domain.model.PaymentTransaction;
import com.bytz.modules.cms.payment.domain.repository.IPaymentRepository;
import com.bytz.modules.cms.payment.infrastructure.assembler.InfrastructureAssembler;
import com.bytz.modules.cms.payment.infrastructure.entity.PaymentEntity;
import com.bytz.modules.cms.payment.infrastructure.entity.PaymentTransactionEntity;
import com.bytz.modules.cms.payment.infrastructure.mapper.PaymentMapper;
import com.bytz.modules.cms.payment.infrastructure.mapper.PaymentTransactionMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 支付单仓储实现
 * Payment Repository Implementation
 * <p>
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

    /**
     * 保存支付单聚合根
     *
     * @param payment 支付单聚合根
     * @return 保存后的支付单聚合根
     */
    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW, rollbackFor = Exception.class)
    public PaymentAggregate save(PaymentAggregate payment) {
        log.info("保存支付单聚合根，支付单号: {}", payment.getCode());

        // 使用MapStruct转换为数据库实体
        PaymentEntity entity = infrastructureAssembler.toPaymentEntity(payment);

        if (payment.getId() != null) {
            paymentMapper.updateById(entity);
        } else {
            paymentMapper.insert(entity);
            // 回填生成的ID
            payment.setId(entity.getId());
        }
        PaymentTransaction runningTransaction = payment.getRunningTransaction();
        if (runningTransaction != null) {

            PaymentTransactionEntity transactionEntity = infrastructureAssembler.toTransactionEntity(runningTransaction);
            if (transactionEntity.getId() != null) {
                transactionMapper.updateById(transactionEntity);
            } else {
                transactionMapper.insert(transactionEntity);
                runningTransaction.setId(transactionEntity.getId());
            }
        }
        payment.updateAggregateAfterPersistence();
        return payment;
    }

    /**
     * 根据主键ID查找支付单
     *
     * @param id 主键ID
     * @return 支付单聚合根的Optional包装，如果未找到返回Optional.empty()
     */
    @Override
    public Optional<PaymentAggregate> findById(String id) {
        log.info("根据ID查找支付单，ID: {}", id);

        PaymentEntity entity = paymentMapper.selectById(id);
        if (entity == null) {
            return Optional.empty();
        }

        // 查询支付流水 - 使用payment_id关联
        LambdaQueryWrapper<PaymentTransactionEntity> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(PaymentTransactionEntity::getPaymentId, id);
        List<PaymentTransactionEntity> transactionEntities = transactionMapper.selectList(wrapper);

        // 使用MapStruct转换为领域对象
        return Optional.of(toPaymentAggregate(entity, transactionEntities));
    }

    /**
     * 根据业务编码查找支付单
     *
     * @param code 支付单号
     * @return 支付单聚合根的Optional包装，如果未找到返回Optional.empty()
     */
    @Override
    public Optional<PaymentAggregate> findByCode(String code) {
        log.info("根据业务编码查找支付单，支付单号: {}", code);

        PaymentEntity entity = findEntityByCode(code);
        if (entity == null) {
            return Optional.empty();
        }

        // 查询支付流水 - 使用payment_id关联
        LambdaQueryWrapper<PaymentTransactionEntity> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(PaymentTransactionEntity::getPaymentId, entity.getId());
        List<PaymentTransactionEntity> transactionEntities = transactionMapper.selectList(wrapper);

        // 使用MapStruct转换为领域对象
        return Optional.of(toPaymentAggregate(entity, transactionEntities));
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

        // 批量查询所有支付流水，避免在循环中调用数据库 - 使用ID关联
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

        // 批量查询所有支付流水，避免在循环中调用数据库 - 使用ID关联
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

        // 批量查询所有支付流水，避免在循环中调用数据库 - 使用ID关联
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
     * 根据主键ID列表批量查找支付单
     * 一次性查询所有支付单及其子聚合（支付流水），避免循环调用数据库
     *
     * @param ids 主键ID列表
     * @return 支付单聚合根列表
     */
    @Override
    public List<PaymentAggregate> findByIds(List<String> ids) {
        log.info("根据ID列表批量查找支付单，数量: {}", ids == null ? 0 : ids.size());

        if (ids == null || ids.isEmpty()) {
            return new ArrayList<>();
        }

        // 批量查询支付单
        List<PaymentEntity> entities = paymentMapper.selectBatchIds(ids);

        if (entities.isEmpty()) {
            return new ArrayList<>();
        }

        // 批量查询所有支付流水，避免在循环中调用数据库 - 使用ID关联
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
     * 删除支付单（逻辑删除）- 通过主键ID
     *
     * @param id 主键ID
     * @return 是否删除成功
     */
    @Override
    public boolean deleteById(String id) {
        log.info("删除支付单，ID: {}", id);
        return paymentMapper.deleteById(id) > 0;
    }

    /**
     * 删除支付单（逻辑删除）- 通过业务编码
     *
     * @param code 支付单号
     * @return 是否删除成功
     */
    @Override
    public boolean deleteByCode(String code) {
        log.info("删除支付单，支付单号: {}", code);
        PaymentEntity entity = findEntityByCode(code);
        if (entity != null) {
            return paymentMapper.deleteById(entity.getId()) > 0;
        }
        return false;
    }


    /**
     * 根据业务编码查找支付单实体
     */
    private PaymentEntity findEntityByCode(String code) {
        LambdaQueryWrapper<PaymentEntity> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(PaymentEntity::getCode, code);
        return paymentMapper.selectOne(wrapper);
    }

    /**
     * 根据业务编码查找支付流水实体
     */
    private PaymentTransactionEntity findTransactionEntityByCode(String code) {
        LambdaQueryWrapper<PaymentTransactionEntity> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(PaymentTransactionEntity::getCode, code);
        return transactionMapper.selectOne(wrapper);
    }

    /**
     * 批量查询支付流水
     * 根据多个支付单ID批量查询对应的支付流水，避免在循环中调用数据库
     *
     * @param paymentIds 支付单ID列表
     * @return 支付单ID到支付流水列表的映射
     */
    private Map<String, List<PaymentTransactionEntity>> findTransactionsByPaymentIds(List<String> paymentIds) {
        if (paymentIds == null || paymentIds.isEmpty()) {
            return new HashMap<>();
        }

        LambdaQueryWrapper<PaymentTransactionEntity> wrapper = new LambdaQueryWrapper<>();
        wrapper.in(PaymentTransactionEntity::getPaymentId, paymentIds);
        List<PaymentTransactionEntity> allTransactions = transactionMapper.selectList(wrapper);

        // 按支付单ID分组
        return allTransactions.stream()
                .collect(Collectors.groupingBy(PaymentTransactionEntity::getPaymentId));
    }

    /**
     * 将数据库实体转换为领域对象（使用MapStruct）
     */
    private PaymentAggregate toPaymentAggregate(
            PaymentEntity entity,
            List<PaymentTransactionEntity> transactionEntities) {


        Map<Boolean, List<PaymentTransactionEntity>> map = transactionEntities.stream().collect(Collectors.partitioningBy(
                tra -> tra.getTransactionStatus() == TransactionStatus.PROCESSING
        ));
        List<PaymentTransactionEntity> processing = map.getOrDefault(true, Collections.emptyList());
        PaymentTransaction runningTransaction = null;
        if (!processing.isEmpty()) {
            PaymentTransactionEntity paymentTransactionEntity = processing.get(0);
            runningTransaction = infrastructureAssembler.toDomainTransaction(paymentTransactionEntity);

        }
        List<PaymentTransactionEntity> end = map.getOrDefault(false, Collections.emptyList());
        List<PaymentTransaction> endTransactions = infrastructureAssembler.toDomainTransactions(end);

        // 使用MapStruct转换支付单
        PaymentAggregate aggregate = infrastructureAssembler.toPaymentAggregate(entity);
        return aggregate.toBuilder().runningTransaction(runningTransaction)
                .completedTransactions(endTransactions).build();
    }


}
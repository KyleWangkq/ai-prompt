package com.bytz.cms.payment.infrastructure.repository;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.bytz.cms.payment.domain.model.*;
import com.bytz.cms.payment.domain.repository.PaymentRepository;
import com.bytz.cms.payment.infrastructure.mapper.PaymentMapper;
import com.bytz.cms.payment.infrastructure.persistence.PaymentEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import org.springframework.util.CollectionUtils;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Payment Repository Implementation
 * 支付单仓储实现类
 */
@Repository
@RequiredArgsConstructor
public class PaymentRepositoryImpl implements PaymentRepository {
    
    private final PaymentMapper paymentMapper;
    
    @Override
    public void save(Payment payment) {
        PaymentEntity entity = toEntity(payment);
        paymentMapper.insert(entity);
    }
    
    @Override
    public Optional<Payment> findById(String id) {
        PaymentEntity entity = paymentMapper.selectById(id);
        return Optional.ofNullable(entity).map(this::toDomain);
    }
    
    @Override
    public List<Payment> findByOrderId(String orderId) {
        LambdaQueryWrapper<PaymentEntity> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(PaymentEntity::getOrderId, orderId);
        List<PaymentEntity> entities = paymentMapper.selectList(wrapper);
        return entities.stream().map(this::toDomain).collect(Collectors.toList());
    }
    
    @Override
    public List<Payment> findByResellerId(String resellerId) {
        LambdaQueryWrapper<PaymentEntity> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(PaymentEntity::getResellerId, resellerId);
        List<PaymentEntity> entities = paymentMapper.selectList(wrapper);
        return entities.stream().map(this::toDomain).collect(Collectors.toList());
    }
    
    @Override
    public List<Payment> filterPayments(String resellerId,
                                       List<PaymentStatus> statuses,
                                       List<PaymentType> types,
                                       LocalDateTime startTime,
                                       LocalDateTime endTime) {
        LambdaQueryWrapper<PaymentEntity> wrapper = new LambdaQueryWrapper<>();
        
        if (resellerId != null) {
            wrapper.eq(PaymentEntity::getResellerId, resellerId);
        }
        
        if (!CollectionUtils.isEmpty(statuses)) {
            List<String> statusStrings = statuses.stream()
                    .map(Enum::name)
                    .collect(Collectors.toList());
            wrapper.in(PaymentEntity::getPaymentStatus, statusStrings);
        }
        
        if (!CollectionUtils.isEmpty(types)) {
            List<String> typeStrings = types.stream()
                    .map(Enum::name)
                    .collect(Collectors.toList());
            wrapper.in(PaymentEntity::getPaymentType, typeStrings);
        }
        
        if (startTime != null) {
            wrapper.ge(PaymentEntity::getCreateTime, startTime);
        }
        
        if (endTime != null) {
            wrapper.le(PaymentEntity::getCreateTime, endTime);
        }
        
        List<PaymentEntity> entities = paymentMapper.selectList(wrapper);
        return entities.stream().map(this::toDomain).collect(Collectors.toList());
    }
    
    @Override
    public boolean existsById(String id) {
        return paymentMapper.selectById(id) != null;
    }
    
    @Override
    public void update(Payment payment) {
        PaymentEntity entity = toEntity(payment);
        paymentMapper.updateById(entity);
    }
    
    @Override
    public Optional<Payment> findByRelatedBusinessId(String relatedBusinessId) {
        LambdaQueryWrapper<PaymentEntity> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(PaymentEntity::getRelatedBusinessId, relatedBusinessId);
        PaymentEntity entity = paymentMapper.selectOne(wrapper);
        return Optional.ofNullable(entity).map(this::toDomain);
    }
    
    /**
     * 将领域模型转换为数据库实体
     */
    private PaymentEntity toEntity(Payment payment) {
        PaymentEntity entity = new PaymentEntity();
        entity.setId(payment.getId());
        entity.setOrderId(payment.getOrderId());
        entity.setResellerId(payment.getResellerId());
        entity.setPaymentAmount(payment.getPaymentAmount());
        entity.setPaidAmount(payment.getPaidAmount());
        entity.setRefundedAmount(payment.getRefundedAmount());
        entity.setActualAmount(payment.getActualAmount());
        entity.setCurrency("CNY");
        entity.setPaymentType(payment.getPaymentType().name());
        entity.setPaymentStatus(payment.getPaymentStatus().name());
        entity.setRefundStatus(payment.getRefundStatus().name());
        entity.setBusinessDesc(payment.getBusinessDesc());
        entity.setPaymentDeadline(payment.getPaymentDeadline());
        entity.setPriorityLevel(payment.getPriorityLevel());
        entity.setBusinessTags(payment.getBusinessTags());
        entity.setCreateTime(payment.getCreatedTime());
        entity.setUpdateTime(payment.getUpdatedTime());
        entity.setCreateBy(payment.getCreateBy());
        entity.setCreateByName(payment.getCreateByName());
        entity.setUpdateBy(payment.getUpdateBy());
        entity.setUpdateByName(payment.getUpdateByName());
        entity.setDelFlag(payment.getDelFlag());
        
        // 处理关联业务信息
        if (payment.getRelatedBusinessInfo() != null) {
            entity.setRelatedBusinessId(payment.getRelatedBusinessInfo().getRelatedBusinessId());
            entity.setRelatedBusinessType(payment.getRelatedBusinessInfo().getRelatedBusinessType().name());
            entity.setBusinessExpireDate(payment.getRelatedBusinessInfo().getBusinessExpireDate());
        }
        
        return entity;
    }
    
    /**
     * 将数据库实体转换为领域模型
     */
    private Payment toDomain(PaymentEntity entity) {
        RelatedBusinessInfo relatedBusinessInfo = null;
        if (entity.getRelatedBusinessId() != null) {
            relatedBusinessInfo = RelatedBusinessInfo.of(
                    entity.getRelatedBusinessId(),
                    RelatedBusinessType.valueOf(entity.getRelatedBusinessType()),
                    entity.getBusinessExpireDate()
            );
        }
        
        Payment payment = Payment.create(
                entity.getId(),
                entity.getOrderId(),
                entity.getResellerId(),
                entity.getPaymentAmount(),
                PaymentType.valueOf(entity.getPaymentType()),
                entity.getPaymentDeadline(),
                entity.getPriorityLevel(),
                relatedBusinessInfo,
                entity.getBusinessDesc(),
                entity.getBusinessTags()
        );
        
        // 设置已支付和已退款金额（这些值来自数据库）
        payment.setPaidAmount(entity.getPaidAmount());
        payment.setRefundedAmount(entity.getRefundedAmount());
        payment.setActualAmount(entity.getActualAmount());
        payment.setPaymentStatus(PaymentStatus.valueOf(entity.getPaymentStatus()));
        payment.setRefundStatus(RefundStatus.valueOf(entity.getRefundStatus()));
        payment.setCreatedTime(entity.getCreateTime());
        payment.setUpdatedTime(entity.getUpdateTime());
        payment.setCreateBy(entity.getCreateBy());
        payment.setCreateByName(entity.getCreateByName());
        payment.setUpdateBy(entity.getUpdateBy());
        payment.setUpdateByName(entity.getUpdateByName());
        payment.setDelFlag(entity.getDelFlag());
        
        return payment;
    }
}

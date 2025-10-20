package com.bytz.cms.payment.application;

import com.bytz.cms.payment.domain.command.CreatePaymentCommand;
import com.bytz.cms.payment.domain.enums.PaymentType;
import com.bytz.cms.payment.domain.enums.RelatedBusinessType;
import com.bytz.cms.payment.domain.model.PaymentAggregate;
import com.bytz.cms.payment.domain.repository.IPaymentRepository;
import com.bytz.cms.payment.shared.model.PaymentCreatedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

/**
 * 支付单应用服务
 * Payment Application Service
 * 
 * 协调支付单的创建、查询、筛选等用例
 * 应用层不包含业务逻辑，仅负责用例协调
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentApplicationService {
    
    private final IPaymentRepository paymentRepository;
    
    /**
     * 创建支付单
     * 
     * 此方法应该由系统内部调用（如订单系统、信用管理系统等），不应该通过外部REST接口调用
     * 
     * @param command 创建支付单命令
     * @return 支付单聚合根
     * TODO: 实现支付单创建完整流程
     * 需求：
     * 1. 验证命令参数完整性和合法性
     * 2. 如果是信用还款类型，验证订单号与信用记录的绑定关系
     * 3. 生成唯一支付单号
     * 4. 创建支付单聚合根
     * 5. 持久化支付单
     * 6. 发布支付单已创建事件
     */
    @Transactional(rollbackFor = Exception.class)
    public PaymentAggregate createPayment(CreatePaymentCommand command) {
        log.info("创建支付单，订单号: {}, 支付类型: {}", command.getOrderId(), command.getPaymentType());
        
        // TODO: 参数验证
        validateCreateCommand(command);
        
        // TODO: 如果是信用还款类型，验证订单号与信用记录的绑定关系
        if (PaymentType.CREDIT_REPAYMENT.equals(command.getPaymentType())) {
            validateCreditRepayment(command);
        }
        
        // 生成支付单号
        String paymentId = paymentRepository.generatePaymentId();
        
        // 创建支付单聚合根
        PaymentAggregate payment = PaymentAggregate.create(
                command.getOrderId(),
                command.getResellerId(),
                command.getPaymentAmount(),
                command.getCurrency() != null ? command.getCurrency() : "CNY",
                command.getPaymentType(),
                command.getBusinessDesc(),
                command.getPaymentDeadline(),
                command.getRelatedBusinessId(),
                command.getRelatedBusinessType(),
                command.getBusinessExpireDate()
        );
        
        // 设置支付单号和审计信息
        payment.setId(paymentId);
        payment.setCreateBy(command.getCreateBy());
        payment.setCreateByName(command.getCreateByName());
        payment.setBusinessTags(command.getBusinessTags());
        
        // 持久化
        payment = paymentRepository.save(payment);
        
        // TODO: 发布支付单已创建事件
        publishPaymentCreatedEvent(payment);
        
        log.info("支付单创建成功，支付单号: {}", payment.getId());
        return payment;
    }
    
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
    
    /**
     * 验证创建命令
     * 
     * TODO: 实现完整的参数验证逻辑
     */
    private void validateCreateCommand(CreatePaymentCommand command) {
        if (command.getOrderId() == null || command.getOrderId().trim().isEmpty()) {
            throw new IllegalArgumentException("订单号不能为空");
        }
        if (command.getResellerId() == null || command.getResellerId().trim().isEmpty()) {
            throw new IllegalArgumentException("经销商ID不能为空");
        }
        if (command.getPaymentAmount() == null || command.getPaymentAmount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("支付金额必须大于0");
        }
        if (command.getPaymentType() == null) {
            throw new IllegalArgumentException("支付类型不能为空");
        }
    }
    
    /**
     * 验证信用还款
     * 
     * TODO: 实现信用还款的验证逻辑
     * 需求：
     * 1. 验证关联业务ID和类型不为空
     * 2. 验证关联业务类型为CREDIT_RECORD
     * 3. 验证订单号与信用记录的绑定关系
     */
    private void validateCreditRepayment(CreatePaymentCommand command) {
        if (command.getRelatedBusinessId() == null || command.getRelatedBusinessId().trim().isEmpty()) {
            throw new IllegalArgumentException("信用还款必须提供关联业务ID");
        }
        if (!RelatedBusinessType.CREDIT_RECORD.equals(command.getRelatedBusinessType())) {
            throw new IllegalArgumentException("信用还款的关联业务类型必须为CREDIT_RECORD");
        }
        
        // TODO: 调用信用管理系统验证订单号与信用记录的绑定关系
    }
    
    /**
     * 发布支付单已创建事件
     * 
     * TODO: 实现事件发布逻辑
     */
    private void publishPaymentCreatedEvent(PaymentAggregate payment) {
        PaymentCreatedEvent event = PaymentCreatedEvent.builder()
                .eventId(UUID.randomUUID().toString())
                .paymentId(payment.getId())
                .orderId(payment.getOrderId())
                .resellerId(payment.getResellerId())
                .paymentAmount(payment.getPaymentAmount())
                .paymentType(payment.getPaymentType())
                .relatedBusinessId(payment.getRelatedBusinessId())
                .occurredOn(payment.getCreateTime())
                .build();
        
        // TODO: 使用Spring Events或消息队列发布事件
        log.info("支付单已创建事件: {}", event);
    }
}

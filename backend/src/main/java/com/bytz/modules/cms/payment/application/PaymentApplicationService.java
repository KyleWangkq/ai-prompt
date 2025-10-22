package com.bytz.modules.cms.payment.application;

import com.bytz.modules.cms.payment.domain.command.CreatePaymentCommand;
import com.bytz.modules.cms.payment.domain.model.PaymentAggregate;
import com.bytz.modules.cms.payment.domain.repository.IPaymentRepository;
import com.bytz.modules.cms.payment.shared.model.PaymentCreatedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
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
    private final ApplicationEventPublisher eventPublisher;
    
    /**
     * 创建支付单
     * 
     * 此方法应该由系统内部调用（如订单系统、信用管理系统等），不应该通过外部REST接口调用
     * 
     * @param command 创建支付单命令
     * @return 支付单聚合根
     */
    @Transactional(rollbackFor = Exception.class)
    public PaymentAggregate createPayment(CreatePaymentCommand command) {
        log.info("创建支付单，订单号: {}, 支付类型: {}", command.getOrderId(), command.getPaymentType());
        
        // 参数验证（仅验证支付模块自己的业务数据）
        validateCreateCommand(command);
        
        // 生成支付单号
        String paymentCode = paymentRepository.generatePaymentCode();
        
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
        payment.setCode(paymentCode);
        payment.setCreateBy(command.getCreateBy());
        payment.setCreateByName(command.getCreateByName());
        payment.setBusinessTags(command.getBusinessTags());
        
        // 持久化
        payment = paymentRepository.save(payment);
        
        // 发布支付单已创建事件
        publishPaymentCreatedEvent(payment);
        
        log.info("支付单创建成功，支付单号: {}", payment.getCode());
        return payment;
    }
    

    
    /**
     * 验证创建命令（仅验证支付模块自己的业务数据）
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
     * 发布支付单已创建事件
     */
    private void publishPaymentCreatedEvent(PaymentAggregate payment) {
        PaymentCreatedEvent event = new PaymentCreatedEvent(
                this,
                UUID.randomUUID().toString(),
                payment.getCode(),
                payment.getOrderId(),
                payment.getResellerId(),
                payment.getPaymentAmount(),
                payment.getPaymentType(),
                payment.getRelatedBusinessId(),
                payment.getCreateTime()
        );
        
        eventPublisher.publishEvent(event);
        log.info("已发布支付单创建事件，支付单号: {}", payment.getCode());
    }
}

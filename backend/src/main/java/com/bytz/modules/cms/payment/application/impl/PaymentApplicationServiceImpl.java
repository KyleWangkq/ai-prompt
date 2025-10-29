package com.bytz.modules.cms.payment.application.impl;

import com.bytz.modules.cms.payment.application.IPaymentApplicationService;
import com.bytz.modules.cms.payment.application.command.CancelPaymentCommand;
import com.bytz.modules.cms.payment.application.command.CreatePaymentCommand;
import com.bytz.modules.cms.payment.application.command.ExecutePaymentCommand;
import com.bytz.modules.cms.payment.application.command.ExecuteRefundCommand;
import com.bytz.modules.cms.payment.domain.PaymentDomainService;
import com.bytz.modules.cms.payment.domain.enums.PaymentChannel;
import com.bytz.modules.cms.payment.domain.model.PaymentAggregate;
import com.bytz.modules.cms.payment.domain.model.PaymentTransaction;
import com.bytz.modules.cms.payment.domain.repository.IPaymentRepository;
import com.bytz.modules.cms.payment.domain.PaymentCodeGenerator;
import com.bytz.modules.cms.payment.domain.repository.IPaymentChannelService;
import com.bytz.modules.cms.payment.shared.model.PaymentCreatedEvent;
import com.bytz.modules.cms.payment.shared.model.RefundExecutedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * 支付单应用服务实现（统一应用服务）
 * Payment Application Service Implementation
 * <p>
 * 协调支付单的创建、查询、筛选、批量支付、渠道查询等用例
 * 应用层不包含业务逻辑，仅负责用例协调
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentApplicationServiceImpl implements IPaymentApplicationService {

    private final IPaymentRepository paymentRepository;
    private final ApplicationEventPublisher eventPublisher;
    private final List<IPaymentChannelService> paymentChannelServices;
    
    // 统一的支付领域服务
    private final PaymentDomainService domainService;

    private final PaymentCodeGenerator paymentCodeGenerator;

    /**
     * 创建支付单（实现内部接口）
     * <p>
     * 此方法应该由系统内部调用（如订单系统、信用管理系统等），不应该通过外部REST接口调用
     *
     * @param command 创建支付单命令
     * @return 支付单聚合根
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public PaymentAggregate createPayment(@Validated  CreatePaymentCommand command) {
        log.info("创建支付单，订单号: {}, 支付类型: {}", command.getOrderId(), command.getPaymentType());


        // 创建支付单聚合根
        PaymentAggregate payment = PaymentAggregate.create(
                command.getOrderId(),
                command.getResellerId(),
                command.getPaymentAmount(),
                "CNY",
                command.getPaymentType(),
                command.getBusinessDesc(),
                command.getPaymentDeadline(),
                command.getRelatedBusinessId(),
                command.getRelatedBusinessType(),
                command.getBusinessExpireDate()
        );

        // 设置支付单号和审计信息
        payment.setCode(paymentCodeGenerator.generatePaymentCode());

        // 持久化
        payment = paymentRepository.save(payment);

        // 发布支付单已创建事件
        publishPaymentCreatedEvent(payment);

        log.info("支付单创建成功，支付单号: {}", payment.getCode());
        return payment;
    }


    /**
     * 执行退款（实现内部接口）
     * 此方法供订单系统等内部模块调用
     *
     * @param command 执行退款命令
     * @return 退款流水号
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public String executeRefund(ExecuteRefundCommand command) {
        log.info("开始执行退款，支付单ID: {}, 退款金额: {}", command.getPaymentId(), command.getRefundAmount());

        // 委托给领域服务处理
        PaymentTransaction refundTransaction = domainService.executeRefund(
                command.getPaymentId(),
                command.getRefundAmount(),
                command.getOriginalTransactionId(),
                command.getRefundOrderId(),
                command.getRefundReason()
        );

        // 查询支付单用于发布事件
        PaymentAggregate payment = paymentRepository.findById(command.getPaymentId())
                .orElseThrow(() -> new IllegalArgumentException("支付单不存在"));

        // 发布退款已执行事件
        publishRefundExecutedEvent(payment, refundTransaction, command);

        log.info("退款执行成功，退款流水号: {}", refundTransaction.getId());
        return refundTransaction.getId();
    }

    /**
     * 取消支付单（实现内部接口）
     * 此方法供订单系统等内部模块调用，也可由经销商主动调用
     *
     * @param command 取消支付单命令
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void cancelPayment(CancelPaymentCommand command) {
        log.info("开始取消支付单，支付单ID: {}, 取消原因: {}", command.getPaymentId(), command.getReason());

        // 参数验证
        if (command.getPaymentId() == null || command.getPaymentId().trim().isEmpty()) {
            throw new IllegalArgumentException("支付单ID不能为空");
        }

        // 查询支付单
        PaymentAggregate payment = paymentRepository.findById(command.getPaymentId())
                .orElseThrow(() -> new IllegalArgumentException("支付单不存在: " + command.getPaymentId()));

        // 执行取消（业务规则在聚合根内部）
        payment.cancel(command.getReason() != null ? command.getReason() : "用户主动取消");

        // 设置操作人信息
        if (command.getOperatorId() != null) {
            payment.setUpdateBy(command.getOperatorId());
        }
        if (command.getOperatorName() != null) {
            payment.setUpdateByName(command.getOperatorName());
        }

        // 持久化
        paymentRepository.save(payment);

        log.info("支付单取消成功，支付单号: {}", payment.getCode());
    }



    /**
     * 执行批量支付（重构版 - 委托给领域服务）
     * <p>
     * 应用服务职责：
     * 1. 查询支付单
     * 2. 准备数据
     * 3. 委托给领域服务执行
     * <p>
     * 业务逻辑已移至PaymentDomainService领域服务
     *
     * @param command 执行支付命令
     * @return 渠道交易id
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public String executeBatchPayment(ExecutePaymentCommand command) {
        log.info("开始执行批量支付，支付单数量: {}, 支付渠道: {}",
                command.getPaymentItems().size(), command.getPaymentChannel());

        // 查询所有支付单
        List<String> paymentIds = command.getPaymentItems().stream()
                .map(ExecutePaymentCommand.PaymentItem::getPaymentId)
                .collect(Collectors.toList());

        List<PaymentAggregate> payments = paymentRepository.findByIds(paymentIds);

        // 验证所有支付单都存在
        if (payments.size() != paymentIds.size()) {
            throw new IllegalArgumentException("部分支付单不存在");
        }

        // 准备分配金额列表
        List<BigDecimal> allocatedAmounts = command.getPaymentItems().stream()
                .map(ExecutePaymentCommand.PaymentItem::getAmount)
                .collect(Collectors.toList());

        // 委托给领域服务执行
        String channelRecordId = domainService.executeUnifiedPayment(
                payments,
                allocatedAmounts,
                command.getPaymentChannel()
        );

        log.info("批量支付执行完成，渠道交易id: {}", channelRecordId);
        return channelRecordId;
    }

    /**
     * 查询经销商可用的支付渠道列表
     *
     * @param resellerId 经销商ID
     * @return 支付渠道列表（枚举）
     */
    @Override
    public List<PaymentChannel> queryAvailableChannels(String resellerId) {
        log.info("查询经销商可用支付渠道，经销商ID: {}", resellerId);

        if (resellerId == null || resellerId.trim().isEmpty()) {
            throw new IllegalArgumentException("经销商ID不能为空");
        }
        
        return paymentChannelServices.stream()
                .filter(channel -> channel.isAvailable(resellerId))
                .map(IPaymentChannelService::getChannelType)
                .collect(Collectors.toList());
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

    /**
     * 发布退款已执行事件
     */
    private void publishRefundExecutedEvent(PaymentAggregate payment, PaymentTransaction refundTransaction, ExecuteRefundCommand command) {
        RefundExecutedEvent event = new RefundExecutedEvent(
                this,
                UUID.randomUUID().toString(),
                payment.getCode(),
                payment.getOrderId(),
                payment.getResellerId(),
                refundTransaction.getId(),
                command.getRefundAmount(),
                payment.getRefundedAmount(),
                payment.getRefundStatus(),
                true,  // 退款流水已创建，视为成功提交，最终结果需要等待回调
                command.getRefundOrderId(),
                LocalDateTime.now()
        );

        eventPublisher.publishEvent(event);
        log.info("已发布退款执行事件，支付单号: {}, 退款流水号: {}", payment.getCode(), refundTransaction.getId());
    }
}
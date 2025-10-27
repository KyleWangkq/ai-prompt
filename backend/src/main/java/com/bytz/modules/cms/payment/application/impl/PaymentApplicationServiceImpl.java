package com.bytz.modules.cms.payment.application.impl;

import com.bytz.modules.cms.payment.application.IPaymentApplicationService;
import com.bytz.modules.cms.payment.application.command.CancelPaymentCommand;
import com.bytz.modules.cms.payment.application.command.CreatePaymentCommand;
import com.bytz.modules.cms.payment.application.command.ExecutePaymentCommand;
import com.bytz.modules.cms.payment.application.command.ExecuteRefundCommand;
import com.bytz.modules.cms.payment.domain.enums.PaymentChannel;
import com.bytz.modules.cms.payment.domain.model.PaymentAggregate;
import com.bytz.modules.cms.payment.domain.model.PaymentTransaction;
import com.bytz.modules.cms.payment.domain.repository.IPaymentRepository;
import com.bytz.modules.cms.payment.infrastructure.channel.IPaymentChannelService;
import com.bytz.modules.cms.payment.infrastructure.channel.command.CreatePaymentRequestCommand;
import com.bytz.modules.cms.payment.infrastructure.channel.response.PaymentRequestResponse;
import com.bytz.modules.cms.payment.shared.exception.PaymentException;
import com.bytz.modules.cms.payment.shared.model.PaymentCreatedEvent;
import com.bytz.modules.cms.payment.shared.model.RefundExecutedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
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
                "CNY",
                command.getPaymentType(),
                command.getBusinessDesc(),
                command.getPaymentDeadline(),
                command.getRelatedBusinessId(),
                command.getRelatedBusinessType(),
                command.getBusinessExpireDate()
        );

        // 设置支付单号和审计信息
        payment.setCode(paymentCode);

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

        // 参数验证
        validateRefundCommand(command);

        // 查询支付单
        PaymentAggregate payment = paymentRepository.findById(command.getPaymentId())
                .orElseThrow(() -> new PaymentException("支付单不存在: " + command.getPaymentId()));

        // 验证退款前置条件（业务规则在聚合根内部）
        payment.validateRefundable(command.getRefundAmount());

        // 执行退款（创建退款流水）
        PaymentTransaction refundTransaction = payment.executeRefund(
                command.getRefundAmount(),
                command.getOriginalTransactionId(),
                command.getRefundOrderId(),
                command.getRefundReason()
        );

        // 持久化
        payment = paymentRepository.save(payment);

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
        validateCancelCommand(command);

        // 查询支付单
        PaymentAggregate payment = paymentRepository.findById(command.getPaymentId())
                .orElseThrow(() -> new PaymentException("支付单不存在: " + command.getPaymentId()));

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
     * 验证退款命令
     */
    private void validateRefundCommand(ExecuteRefundCommand command) {
        if (command.getPaymentId() == null || command.getPaymentId().trim().isEmpty()) {
            throw new IllegalArgumentException("支付单ID不能为空");
        }
        if (command.getRefundAmount() == null || command.getRefundAmount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("退款金额必须大于0");
        }
        if (command.getRefundOrderId() == null || command.getRefundOrderId().trim().isEmpty()) {
            throw new IllegalArgumentException("退款单号不能为空");
        }
    }

    /**
     * 验证取消命令
     */
    private void validateCancelCommand(CancelPaymentCommand command) {
        if (command.getPaymentId() == null || command.getPaymentId().trim().isEmpty()) {
            throw new IllegalArgumentException("支付单ID不能为空");
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

    /**
     * 执行批量支付（重构版 - 严格按照9步支付流程）
     * <p>
     * 支付流程：
     * 1. 获取支付渠道
     * 2. 验证支付渠道可用
     * 3. 计算支付总金额
     * 4. 验证该经销商该金额可支付
     * 5. 创建支付明细
     * 6. 创建渠道支付请求
     * 7. 发起渠道支付
     * 8. 获得支付请求结果，并回写到所有支付明细中
     * 9. 持久化支付明细
     *
     * @param command 执行支付命令
     * @return 渠道交易id
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public String executeBatchPayment(ExecutePaymentCommand command) {
        log.info("开始执行批量支付，支付单数量: {}, 支付渠道: {}",
                command.getPaymentItems().size(), command.getPaymentChannel());

        // ========== 步骤1: 获取支付渠道 ==========
        IPaymentChannelService channelService = findChannelService(command.getPaymentChannel());

        // ========== 步骤2: 验证支付渠道可用 ==========
        // 批量查询所有支付单，验证所有支付单属于同一经销商
        List<String> paymentIds = command.getPaymentItems().stream()
                .map(ExecutePaymentCommand.PaymentItem::getPaymentId)
                .collect(Collectors.toList());

        List<PaymentAggregate> payments = paymentRepository.findByIds(paymentIds);

        // 验证所有支付单都存在
        if (payments.size() != paymentIds.size()) {
            throw new PaymentException("部分支付单不存在");
        }

        // 验证所有支付单属于同一经销商
        Set<String> resellerIds = payments.stream()
                .map(PaymentAggregate::getResellerId)
                .collect(Collectors.toSet());
        if (resellerIds.size() != 1) {
            throw new PaymentException("所有支付单必须属于同一经销商");
        }
        String resellerId = resellerIds.iterator().next();

        // 验证渠道对该经销商是否可用
        if (!channelService.isAvailable(resellerId)) {
            throw new PaymentException("支付渠道对当前经销商不可用");
        }

        // ========== 步骤3: 计算支付总金额 ==========
        BigDecimal totalAmount = command.getPaymentItems().stream()
                .map(ExecutePaymentCommand.PaymentItem::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        log.info("总支付金额: {}", totalAmount);

        // ========== 步骤4: 验证该经销商该金额可支付 ==========
        channelService.supportsAmountForReseller(resellerId, totalAmount);

        // 验证每个支付单的状态是否允许支付
        for (PaymentAggregate payment : payments) {
            if (!payment.canPay()) {
                throw new PaymentException("支付单 " + payment.getCode() + " 当前状态不允许支付");
            }
        }

        // 验证每个支付单的金额不超过待支付金额
        for (int i = 0; i < payments.size(); i++) {
            PaymentAggregate payment = payments.get(i);
            ExecutePaymentCommand.PaymentItem item = command.getPaymentItems().get(i);

            BigDecimal pendingAmount = payment.getPendingAmount();
            if (item.getAmount().compareTo(pendingAmount) > 0) {
                throw new PaymentException("支付单 " + payment.getCode() +
                                           " 的支付金额超过待支付金额");
            }
        }

        // ========== 步骤5: 创建支付明细 ==========
        // 注意：此处仅在内存中创建流水对象，尚未持久化
        // 每个支付单创建一个对应的支付流水
        for (int i = 0; i < payments.size(); i++) {
            PaymentAggregate payment = payments.get(i);
            ExecutePaymentCommand.PaymentItem item = command.getPaymentItems().get(i);

            // 在聚合根中创建支付流水（流水状态为PROCESSING，尚未提交到渠道）
            payment.executePayment(
                    command.getPaymentChannel(),
                    item.getAmount()
            );
            log.info("为支付单 {} 创建支付流水，金额: {}", payment.getCode(), item.getAmount());
        }

        // ========== 步骤6: 创建渠道支付请求 ==========

        CreatePaymentRequestCommand createPaymentRequestCommand = CreatePaymentRequestCommand.builder()
                .totalAmount(totalAmount)
                .resellerId(resellerId)
                .build();

        // ========== 步骤7: 发起渠道支付 ==========
        PaymentRequestResponse paymentResponse = channelService.createPaymentRequest(createPaymentRequestCommand);
        log.info("渠道支付请求创建成功，渠道交易号: {}, 渠道支付记录ID: {}, 支付结果：{}",
                paymentResponse.getChannelTransactionNumber(),
                paymentResponse.getChannelPaymentRecordId(),
                paymentResponse.getTransactionStatus().getDescription()
        );

        // ========== 步骤8: 获得支付请求结果，并回写到所有支付明细中 ==========
        for (PaymentAggregate payment : payments) {
            // 获取该支付单最新创建的支付流水（即刚在步骤5创建的）
            PaymentTransaction runningTransaction = payment.getRunningTransaction();

            runningTransaction.setChannelTransactionNumber(paymentResponse.getChannelTransactionNumber());
            runningTransaction.setChannelPaymentRecordId(paymentResponse.getChannelPaymentRecordId());
            log.info("支付单 {} 的流水 {} 已回写渠道信息", payment.getCode(), runningTransaction.getCode());

        }

        // ========== 步骤9: 持久化支付明细 ==========
        log.info("步骤9: 持久化所有支付单及流水");
        for (PaymentAggregate payment : payments) {
            paymentRepository.save(payment);
            log.info("支付单 {} 及其流水已持久化", payment.getCode());
        }

        log.info("批量支付执行完成，渠道交易id: {}", paymentResponse.getChannelPaymentRecordId());
        return paymentResponse.getChannelPaymentRecordId();
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
        return paymentChannelServices.stream().filter(channal -> channal.isAvailable(resellerId))
                .map(IPaymentChannelService::getChannelType)
                .collect(Collectors.toList());
    }

    /**
     * 根据渠道类型查找渠道服务
     *
     * @param channel 支付渠道
     * @return 渠道服务
     */
    private IPaymentChannelService findChannelService(PaymentChannel channel) {
        return paymentChannelServices.stream()
                .filter(service -> service.getChannelType() == channel)
                .findFirst()
                .orElseThrow(() -> new PaymentException("支付渠道不可用: " + channel.getDescription()));
    }
}
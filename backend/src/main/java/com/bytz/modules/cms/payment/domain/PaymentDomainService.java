package com.bytz.modules.cms.payment.domain;

import com.bytz.modules.cms.payment.domain.command.StartPaymentCommand;
import com.bytz.modules.cms.payment.domain.enums.PaymentChannel;
import com.bytz.modules.cms.payment.domain.model.PaymentAggregate;
import com.bytz.modules.cms.payment.domain.model.PaymentTransaction;
import com.bytz.modules.cms.payment.domain.repository.IPaymentChannelService;
import com.bytz.modules.cms.payment.domain.repository.IPaymentRepository;
import com.bytz.modules.cms.payment.domain.response.StarPaymentResponse;
import com.bytz.modules.cms.payment.shared.exception.PaymentException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * 支付领域服务
 * Payment Domain Service
 * <p>
 * 统一的支付领域服务，整合了支付验证、支付执行、退款处理、回调处理等核心业务逻辑
 * <p>
 * 职责：
 * - 支付单创建与执行的业务校验
 * - 统一支付执行编排（单支付单/多支付单合并支付/信用还款）
 * - 退款执行编排（指定流水退款/最新流水退款）
 * - 支付/退款回调处理
 * <p>
 * 术语来源：Glossary.md - 领域服务术语
 * 相关用例：UC-PM-001 ~ UC-PM-009
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentDomainService {

    private final IPaymentRepository paymentRepository;
    private final ApplicationEventPublisher eventPublisher;
    private final List<IPaymentChannelService> paymentChannelServices;


    // ==================== 支付执行相关方法 ====================


    @Transactional
    public String executeSinglePayment(
            PaymentAggregate payment,
            IPaymentChannelService paymentChannelService,
            BigDecimal amount,
            String businessRemark,
            String resellerId) {
        PaymentChannel channelType = paymentChannelService.getChannelType();
        validateCanPay(payment, amount);
        PaymentTransaction paymentTransaction = payment.executePayment(channelType, amount, businessRemark);
        StartPaymentCommand command = StartPaymentCommand.builder()
                .amount(amount)
                .resellerId(resellerId)
                .paymentTransaction(paymentTransaction)
                .build();
        StarPaymentResponse paymentResponse = paymentChannelService.starPaymentRequest(command);
        if (paymentResponse.getTransactionStatus() != null) {
            paymentTransaction.setTransactionStatus(paymentResponse.getTransactionStatus());
            paymentTransaction.setChannelTransactionNumber(paymentResponse.getChannelTransactionNumber());
            paymentTransaction.setChannelPaymentRecordId(paymentResponse.getChannelPaymentRecordId());
            paymentRepository.save(payment);
            return paymentResponse.getChannelPaymentRecordId();
        } else {
            throw new PaymentException("支付渠道异常");
        }

    }


    /**
     * 根据支付单集合与分配金额执行一次统一支付（UC-PM-003/008），适用于所有支付类型
     * <p>
     * 处理流程：
     * 1. 验证支付单集合
     * 2. 验证金额分配
     * 3. 获取支付渠道服务
     * 4. 验证渠道可用性
     * 5. 调用渠道支付
     * 6. 创建支付流水
     * 7. 更新支付单状态
     * 8. 持久化支付明细
     * <p>
     * 支持场景：
     * - 单支付单支付（特例）
     * - 多支付单合并支付（统一流程）
     * - 信用还款支付单（作为普通支付处理）
     * <p>
     * 用例来源：UC-PM-003步骤5-12、UC-PM-008步骤4-11
     *
     * @param payments              支付单列表
     * @param allocatedAmounts      每个支付单的分配金额（与payments列表一一对应）
     * @param paymentChannelService 支付渠道服务
     * @return 渠道支付记录ID
     */
    public String executeUnifiedPayment(
            List<PaymentAggregate> payments,
            Map<String, BigDecimal> allocatedAmounts,
            IPaymentChannelService paymentChannelService,
            String resellerId) {
        PaymentChannel paymentChannel = paymentChannelService.getChannelType();

        log.info("开始执行统一支付，支付单数量: {}, 支付渠道: {}",
                payments.size(), paymentChannel.getDescription());
        payments.forEach(payment -> {
            BigDecimal amount = allocatedAmounts.get(payment.getId());
            validateCanPay(payment, amount);
            PaymentTransaction paymentTransaction = payment.executePayment(paymentChannel, amount, "businessRemark");
        });
        BigDecimal total = allocatedAmounts.values().stream()
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        StartPaymentCommand command = StartPaymentCommand.builder()
                .amount(total)
                .resellerId(resellerId)
                .build();
        StarPaymentResponse paymentResponse = paymentChannelService.starPaymentRequest(command);
        if (paymentResponse.getTransactionStatus() != null) {

            payments.forEach(payment -> {
                PaymentTransaction runningTransaction = payment.getRunningTransaction();
                runningTransaction.setTransactionStatus(paymentResponse.getTransactionStatus());
                runningTransaction.setChannelTransactionNumber(paymentResponse.getChannelTransactionNumber());
                runningTransaction.setChannelPaymentRecordId(paymentResponse.getChannelPaymentRecordId());
                paymentRepository.save(payment);
            });
            log.info("统一支付执行完成，渠道支付记录ID: {}", paymentResponse.getChannelPaymentRecordId());
            return paymentResponse.getChannelPaymentRecordId();
        } else {
            throw new PaymentException("支付渠道异常");
        }
    }


    /**
     * 校验执行支付前置条件（UC-PM-003），适用于所有支付类型
     *
     * @param payment 待支付的支付单
     * @throws PaymentException 如果验证失败
     */
    public void validateCanPay(PaymentAggregate payment, BigDecimal amount) {
        // 验证支付单状态是否允许支付
        if (!payment.canPay()) {
            throw new PaymentException(
                    String.format("支付单 %s 当前状态 %s 不允许支付",
                            payment.getCode(),
                            payment.getPaymentStatus().getDescription()));
        }

        // 验证待支付金额
        BigDecimal pendingAmount = payment.getPendingAmount();
        if (pendingAmount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new PaymentException(
                    String.format("支付单 %s 待支付金额必须大于0", payment.getCode()));
        }

        if (amount.compareTo(pendingAmount) > 0) {
            throw new PaymentException(
                    String.format("支付单 %s 的分配金额 %s 超过待支付金额 %s",
                            payment.getCode(), amount, pendingAmount));
        }
    }

    /**
     * 校验支付金额分配（UC-PM-003步骤6）
     * <p>
     * 验证规则：
     * - 每个支付单的分配金额 <= 待支付金额
     * - 总分配金额 = 支付渠道支付金额
     * <p>
     * 用例来源：UC-PM-003步骤6
     *
     * @param payments         支付单列表
     * @param allocatedAmounts 分配金额列表（与支付单列表一一对应）
     * @param totalAmount      总支付金额
     * @throws PaymentException 如果验证失败
     */
    public void validateAmountAllocation(
            List<PaymentAggregate> payments,
            List<BigDecimal> allocatedAmounts,
            BigDecimal totalAmount) {

        log.debug("开始校验金额分配，支付单数量: {}, 总金额: {}", payments.size(), totalAmount);

        if (payments.size() != allocatedAmounts.size()) {
            throw new IllegalArgumentException("支付单数量与分配金额数量不匹配");
        }

        // 计算总分配金额
        BigDecimal sumAllocated = allocatedAmounts.stream()
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // 验证总分配金额等于支付渠道支付金额
        if (sumAllocated.compareTo(totalAmount) != 0) {
            throw new PaymentException(
                    String.format("总分配金额 %s 不等于支付渠道支付金额 %s",
                            sumAllocated, totalAmount));
        }

        // 验证每个支付单的分配金额不超过待支付金额
        for (int i = 0; i < payments.size(); i++) {
            PaymentAggregate payment = payments.get(i);
            BigDecimal allocatedAmount = allocatedAmounts.get(i);
            BigDecimal pendingAmount = payment.getPendingAmount();

            if (allocatedAmount.compareTo(pendingAmount) > 0) {
                throw new PaymentException(
                        String.format("支付单 %s 的分配金额 %s 超过待支付金额 %s",
                                payment.getCode(), allocatedAmount, pendingAmount));
            }

            if (allocatedAmount.compareTo(BigDecimal.ZERO) <= 0) {
                throw new PaymentException(
                        String.format("支付单 %s 的分配金额必须大于0", payment.getCode()));
            }
        }

        log.debug("金额分配校验通过");
    }

    /**
     * 校验退款前置条件（UC-PM-006）
     * <p>
     * 验证项：
     * - 支付单状态可退款
     * - 可退款金额足够
     * - 退款流水可选
     * - 业务授权
     * <p>
     * 用例来源：UC-PM-006步骤2-3
     *
     * @param payment      支付单
     * @param refundAmount 退款金额
     * @throws PaymentException 如果验证失败
     */
    public void validateRefund(PaymentAggregate payment, BigDecimal refundAmount) {
        log.debug("开始校验退款前置条件，支付单: {}, 退款金额: {}",
                payment.getCode(), refundAmount);

        // 验证退款金额
        if (refundAmount == null || refundAmount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("退款金额必须大于0");
        }

        // 验证支付单状态是否允许退款
        if (!payment.canRefund()) {
            throw new PaymentException(
                    String.format("支付单 %s 当前状态不允许退款", payment.getCode()));
        }

        // 验证可退款金额
        BigDecimal refundableAmount = payment.getPaidAmount().subtract(payment.getRefundedAmount());
        if (refundAmount.compareTo(refundableAmount) > 0) {
            throw new PaymentException(
                    String.format("退款金额 %s 超过可退款金额 %s",
                            refundAmount, refundableAmount));
        }

        log.debug("退款前置条件校验通过");
    }


    /**
     * 校验并应用各支付单的金额分配，不得超过各自待支付金额
     * <p>
     * 验证规则：
     * - 每个支付单的分配金额 <= 待支付金额
     * - 总分配金额 = 支付渠道支付金额
     * <p>
     * 用例来源：UC-PM-003步骤6
     *
     * @param payments    支付单列表
     * @param amountMap   支付单ID到分配金额的映射
     * @param totalAmount 总支付金额
     */
    public void allocateAmounts(
            List<PaymentAggregate> payments,
            Map<String, BigDecimal> amountMap,
            BigDecimal totalAmount) {

        log.debug("开始分配金额，支付单数量: {}, 总金额: {}", payments.size(), totalAmount);

        // 转换为列表以使用统一的验证逻辑
        List<BigDecimal> allocatedAmounts = payments.stream()
                .map(payment -> amountMap.get(payment.getId()))
                .collect(Collectors.toList());

        // 使用验证逻辑进行校验
        validateAmountAllocation(payments, allocatedAmounts, totalAmount);

        log.debug("金额分配验证通过");
    }

    // ==================== 退款相关方法 ====================

    /**
     * 按策略选择流水并下发渠道退款（UC-PM-006）
     * <p>
     * 处理流程：
     * 1. 接收退款指令
     * 2. 验证退款前置条件
     * 3. 选择退款流水（最新流水/指定流水/多流水分摊）
     * 4. 创建退款流水
     * 5. 更新支付单状态
     * <p>
     * 流水选择策略：
     * - 指定流水退款：按指定的原支付流水ID退款
     * - 最新流水退款：选择最新成功的支付流水退款
     * - 多流水分摊退款：按比例分摊到多个支付流水（未实现）
     * <p>
     * 用例来源：UC-PM-006步骤1-8
     *
     * @param paymentId             支付单ID
     * @param refundAmount          退款金额
     * @param originalTransactionId 原支付流水ID（指定流水退款时必填）
     * @param businessOrderId       业务单号（退款单号）
     * @param refundReason          退款原因
     * @return 退款流水
     * @throws PaymentException 如果退款失败
     */
    public PaymentTransaction executeRefund(
            String paymentId,
            BigDecimal refundAmount,
            String originalTransactionId,
            String businessOrderId,
            String refundReason) {

        log.info("开始执行退款，支付单ID: {}, 退款金额: {}, 原流水ID: {}",
                paymentId, refundAmount, originalTransactionId);

        // ========== 步骤1: 查询支付单 ==========
        PaymentAggregate payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new PaymentException("支付单不存在: " + paymentId));

        // ========== 步骤2: 验证退款前置条件 ==========
        validateRefund(payment, refundAmount);

        // ========== 步骤3: 选择退款流水 ==========
        String selectedTransactionId = selectRefundTransaction(
                payment, originalTransactionId);

        log.info("选择退款流水: {}", selectedTransactionId);

        // ========== 步骤4: 创建退款流水 ==========
        PaymentTransaction refundTransaction = payment.executeRefund(
                refundAmount,
                selectedTransactionId,
                businessOrderId,
                refundReason
        );

        // ========== 步骤5: 持久化 ==========
        paymentRepository.save(payment);

        log.info("退款执行成功，退款流水号: {}", refundTransaction.getId());
        return refundTransaction;
    }

    /**
     * 处理退款回调（由PaymentCallbackService调用）
     * <p>
     * 处理流程：
     * 1. 查找退款流水
     * 2. 更新流水状态
     * 3. 调用支付单handleRefundCallback
     * 4. 持久化
     *
     * @param paymentId       支付单ID
     * @param transactionCode 退款流水号
     * @param success         是否成功
     * @param completeTime    完成时间
     */
    public void handleRefundCallback(
            String paymentId,
            String transactionCode,
            boolean success,
            LocalDateTime completeTime) {

        log.info("处理退款回调，支付单ID: {}, 退款流水号: {}, 成功: {}",
                paymentId, transactionCode, success);

        // 查询支付单
        PaymentAggregate payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new PaymentException("支付单不存在: " + paymentId));

        // 调用聚合根方法处理退款回调
        payment.handleRefundCallback(transactionCode, success, completeTime);

        // 持久化
        paymentRepository.save(payment);

        log.info("退款回调处理完成");
    }

    // ==================== 回调处理相关方法 ====================

    /**
     * 处理支付回调（UC-PM-004），更新流水与支付单
     * <p>
     * 处理流程：
     * 1. 验证回调签名（TODO）
     * 2. 查找支付流水
     * 3. 更新流水状态
     * 4. 调用支付单applyPayment
     * 5. 发布领域事件
     * 6. 通知相关系统
     * <p>
     * 用例来源：UC-PM-004步骤1-9
     *
     * @param paymentId       支付单ID
     * @param transactionCode 支付流水号
     * @param success         是否成功
     * @param completeTime    完成时间
     * @param channelMessage  渠道返回消息
     */
    public void processPaymentCallback(
            String paymentId,
            String transactionCode,
            boolean success,
            LocalDateTime completeTime,
            String channelMessage) {

        log.info("处理支付回调，支付单ID: {}, 流水号: {}, 成功: {}",
                paymentId, transactionCode, success);

        // ========== 步骤1: 验证回调签名 ==========
        // TODO: 实现回调签名验证
        validateCallbackSignature(channelMessage);

        // ========== 步骤2: 查找支付流水 ==========
        PaymentAggregate payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new PaymentException("支付单不存在: " + paymentId));

        // ========== 步骤3-4: 更新流水状态并调用支付单处理回调 ==========
        payment.handlePaymentCallback(transactionCode, success, completeTime);

        // ========== 步骤5: 持久化 ==========
        payment = paymentRepository.save(payment);

        log.info("支付回调处理完成，支付单号: {}", payment.getCode());
    }

    /**
     * 处理退款回调，更新流水与支付单
     * <p>
     * 处理流程：
     * 1. 验证回调签名
     * 2. 查找退款流水
     * 3. 更新流水状态
     * 4. 调用支付单applyRefund
     * 5. 发布领域事件
     * 6. 通知相关系统
     * <p>
     * 需求来源：需求文档4.8节退款管理
     *
     * @param paymentId       支付单ID
     * @param transactionCode 退款流水号
     * @param success         是否成功
     * @param completeTime    完成时间
     * @param channelMessage  渠道返回消息
     */
    public void processRefundCallback(
            String paymentId,
            String transactionCode,
            boolean success,
            LocalDateTime completeTime,
            String channelMessage) {

        log.info("处理退款回调，支付单ID: {}, 流水号: {}, 成功: {}",
                paymentId, transactionCode, success);

        // 验证回调签名
        validateCallbackSignature(channelMessage);

        // 查找支付单
        PaymentAggregate payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new PaymentException("支付单不存在: " + paymentId));

        // 更新流水状态并调用支付单处理退款回调
        payment.handleRefundCallback(transactionCode, success, completeTime);

        // 持久化
        payment = paymentRepository.save(payment);

        log.info("退款回调处理完成，支付单号: {}", payment.getCode());
    }

    // ==================== 私有辅助方法 ====================

    /**
     * 验证渠道可用性
     */
    private void validateChannelAvailability(
            IPaymentChannelService channelService,
            String resellerId,
            BigDecimal totalAmount) {

        log.debug("验证渠道可用性，经销商: {}, 金额: {}", resellerId, totalAmount);

        if (!channelService.isAvailable(resellerId)) {
            throw new PaymentException(
                    String.format("支付渠道 %s 对当前经销商不可用",
                            channelService.getChannelType().getDescription()));
        }

        channelService.supportsAmountForReseller(resellerId, totalAmount);

        log.debug("渠道可用性验证通过");
    }

    /**
     * 创建渠道支付请求
     */
    private StarPaymentResponse createPaymentRequest(
            IPaymentChannelService channelService,
            String resellerId,
            BigDecimal totalAmount) {

        log.debug("创建渠道支付请求，经销商: {}, 金额: {}", resellerId, totalAmount);

        StartPaymentCommand command = StartPaymentCommand.builder()
                .amount(totalAmount)
                .resellerId(resellerId)
                .build();

        return channelService.starPaymentRequest(command);
    }

    /**
     * 创建支付流水
     */
    private void createPaymentTransactions(
            List<PaymentAggregate> payments,
            List<BigDecimal> allocatedAmounts,
            PaymentChannel paymentChannel,
            StarPaymentResponse paymentResponse) {

        log.debug("创建支付流水，支付单数量: {}", payments.size());

        for (int i = 0; i < payments.size(); i++) {
            PaymentAggregate payment = payments.get(i);
            BigDecimal allocatedAmount = allocatedAmounts.get(i);

            PaymentTransaction transaction = payment.executePayment(
                    paymentChannel,
                    allocatedAmount,
                    null
            );

            transaction.setChannelTransactionNumber(paymentResponse.getChannelTransactionNumber());
            transaction.setChannelPaymentRecordId(paymentResponse.getChannelPaymentRecordId());

            log.debug("支付单 {} 创建支付流水，金额: {}, 渠道交易号: {}",
                    payment.getCode(), allocatedAmount,
                    paymentResponse.getChannelTransactionNumber());
        }
    }

    /**
     * 更新支付单状态
     */
    private void updatePaymentStatus(List<PaymentAggregate> payments) {
        log.debug("更新支付单状态为支付中，支付单数量: {}", payments.size());

        for (PaymentAggregate payment : payments) {
            log.debug("支付单 {} 状态已更新为: {}", payment.getCode(),
                    payment.getPaymentStatus().getDescription());
        }
    }

    /**
     * 持久化支付明细
     */
    private void persistPayments(List<PaymentAggregate> payments) {
        log.debug("持久化支付明细，支付单数量: {}", payments.size());

        for (PaymentAggregate payment : payments) {
            paymentRepository.save(payment);
            log.debug("支付单 {} 及其流水已持久化", payment.getCode());
        }
    }

    /**
     * 根据渠道类型查找渠道服务
     */
    private IPaymentChannelService findChannelService(PaymentChannel channel) {
        return paymentChannelServices.stream()
                .filter(service -> service.getChannelType() == channel)
                .findFirst()
                .orElseThrow(() -> new PaymentException(
                        "支付渠道不可用: " + channel.getDescription()));
    }

    /**
     * 选择退款流水
     */
    private String selectRefundTransaction(
            PaymentAggregate payment,
            String originalTransactionId) {

        // 策略1: 指定流水退款
        if (originalTransactionId != null && !originalTransactionId.trim().isEmpty()) {
            log.debug("使用指定流水退款策略");
            return originalTransactionId;
        }

        // 策略2: 最新流水退款
        log.debug("使用最新流水退款策略");
        Optional<PaymentTransaction> latestTransaction = findLatestSuccessfulPaymentTransaction(payment);

        if (!latestTransaction.isPresent()) {
            throw new PaymentException("未找到可退款的支付流水");
        }

        return latestTransaction.get().getId();
    }

    /**
     * 查找最新成功的支付流水
     */
    private Optional<PaymentTransaction> findLatestSuccessfulPaymentTransaction(
            PaymentAggregate payment) {

        return payment.getTransactions().stream()
                .filter(PaymentTransaction::isPaymentTransaction)
                .filter(PaymentTransaction::isSuccess)
                .findFirst();
    }

    /**
     * 验证回调签名
     */
    private void validateCallbackSignature(String channelMessage) {
        // TODO: 实现回调签名验证逻辑
        log.debug("验证回调签名（未实现）");
    }


}
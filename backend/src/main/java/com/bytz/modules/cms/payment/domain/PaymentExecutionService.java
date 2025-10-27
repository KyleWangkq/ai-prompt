package com.bytz.modules.cms.payment.domain;

import com.bytz.modules.cms.payment.domain.enums.PaymentChannel;
import com.bytz.modules.cms.payment.domain.model.PaymentAggregate;
import com.bytz.modules.cms.payment.domain.model.PaymentTransaction;
import com.bytz.modules.cms.payment.domain.repository.IPaymentRepository;
import com.bytz.modules.cms.payment.infrastructure.channel.IPaymentChannelService;
import com.bytz.modules.cms.payment.infrastructure.channel.command.CreatePaymentRequestCommand;
import com.bytz.modules.cms.payment.infrastructure.channel.response.PaymentRequestResponse;
import com.bytz.modules.cms.payment.shared.exception.PaymentException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 支付执行服务
 * Payment Execution Domain Service
 * <p>
 * 领域服务类型：编排服务
 * 职责：统一支付执行编排，包含多支付单合并支付、金额分配与渠道下单，适用于所有支付类型（包括信用还款）
 * <p>
 * 术语来源：Glossary.md - 领域服务术语"支付执行服务(PaymentExecutionService)"
 * 需求来源：需求文档4.2节支付执行流程、4.10节信用还款处理
 * 相关用例：UC-PM-003、UC-PM-008
 * <p>
 * 设计原则：信用还款（paymentType=CREDIT_REPAYMENT）作为普通支付单按统一流程处理，
 * 支付成功后通过领域事件通知信用管理系统。
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentExecutionService {

    private final IPaymentRepository paymentRepository;
    private final PaymentValidationService validationService;
    private final List<IPaymentChannelService> paymentChannelServices;

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
     * @param payments         支付单列表
     * @param allocatedAmounts 每个支付单的分配金额（与payments列表一一对应）
     * @param paymentChannel   支付渠道
     * @return 渠道支付记录ID
     */
    public String executeUnifiedPayment(
            List<PaymentAggregate> payments,
            List<BigDecimal> allocatedAmounts,
            PaymentChannel paymentChannel) {

        log.info("开始执行统一支付，支付单数量: {}, 支付渠道: {}",
                payments.size(), paymentChannel.getDescription());

        // ========== 步骤1: 验证支付单集合 ==========
        validationService.validateExecute(payments);

        // ========== 步骤2: 验证金额分配 ==========
        BigDecimal totalAmount = allocatedAmounts.stream()
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        validationService.validateAmountAllocation(payments, allocatedAmounts, totalAmount);

        // 获取经销商ID（所有支付单属于同一经销商）
        String resellerId = payments.get(0).getResellerId();

        // ========== 步骤3: 获取支付渠道服务 ==========
        IPaymentChannelService channelService = findChannelService(paymentChannel);

        // ========== 步骤4: 验证渠道可用性 ==========
        validateChannelAvailability(channelService, resellerId, totalAmount);

        // ========== 步骤5: 调用渠道支付 ==========
        PaymentRequestResponse paymentResponse = createPaymentRequest(
                channelService, resellerId, totalAmount);

        log.info("渠道支付请求创建成功，渠道交易号: {}, 渠道支付记录ID: {}",
                paymentResponse.getChannelTransactionNumber(),
                paymentResponse.getChannelPaymentRecordId());

        // ========== 步骤6: 创建支付流水 ==========
        createPaymentTransactions(payments, allocatedAmounts, paymentChannel, paymentResponse);

        // ========== 步骤7: 更新支付单状态 ==========
        updatePaymentStatus(payments);

        // ========== 步骤8: 持久化支付明细 ==========
        persistPayments(payments);

        log.info("统一支付执行完成，渠道支付记录ID: {}", paymentResponse.getChannelPaymentRecordId());
        return paymentResponse.getChannelPaymentRecordId();
    }

    /**
     * 验证渠道可用性
     * <p>
     * 验证项：
     * - 渠道对该经销商是否可用
     * - 渠道是否支持该金额的支付
     *
     * @param channelService 渠道服务
     * @param resellerId     经销商ID
     * @param totalAmount    总支付金额
     */
    private void validateChannelAvailability(
            IPaymentChannelService channelService,
            String resellerId,
            BigDecimal totalAmount) {

        log.debug("验证渠道可用性，经销商: {}, 金额: {}", resellerId, totalAmount);

        // 验证渠道对该经销商是否可用
        if (!channelService.isAvailable(resellerId)) {
            throw new PaymentException(
                    String.format("支付渠道 %s 对当前经销商不可用",
                            channelService.getChannelType().getDescription()));
        }

        // 验证渠道是否支持该金额的支付
        channelService.supportsAmountForReseller(resellerId, totalAmount);

        log.debug("渠道可用性验证通过");
    }

    /**
     * 创建渠道支付请求
     *
     * @param channelService 渠道服务
     * @param resellerId     经销商ID
     * @param totalAmount    总支付金额
     * @return 支付请求响应
     */
    private PaymentRequestResponse createPaymentRequest(
            IPaymentChannelService channelService,
            String resellerId,
            BigDecimal totalAmount) {

        log.debug("创建渠道支付请求，经销商: {}, 金额: {}", resellerId, totalAmount);

        CreatePaymentRequestCommand command = CreatePaymentRequestCommand.builder()
                .totalAmount(totalAmount)
                .resellerId(resellerId)
                .build();

        return channelService.createPaymentRequest(command);
    }

    /**
     * 创建支付流水
     * <p>
     * 为每个支付单创建对应的支付流水，并回写渠道信息
     *
     * @param payments         支付单列表
     * @param allocatedAmounts 分配金额列表
     * @param paymentChannel   支付渠道
     * @param paymentResponse  渠道支付响应
     */
    private void createPaymentTransactions(
            List<PaymentAggregate> payments,
            List<BigDecimal> allocatedAmounts,
            PaymentChannel paymentChannel,
            PaymentRequestResponse paymentResponse) {

        log.debug("创建支付流水，支付单数量: {}", payments.size());

        for (int i = 0; i < payments.size(); i++) {
            PaymentAggregate payment = payments.get(i);
            BigDecimal allocatedAmount = allocatedAmounts.get(i);

            // 在聚合根中创建支付流水
            PaymentTransaction transaction = payment.executePayment(
                    paymentChannel,
                    allocatedAmount
            );

            // 回写渠道信息
            transaction.setChannelTransactionNumber(paymentResponse.getChannelTransactionNumber());
            transaction.setChannelPaymentRecordId(paymentResponse.getChannelPaymentRecordId());

            log.debug("支付单 {} 创建支付流水，金额: {}, 渠道交易号: {}",
                    payment.getCode(), allocatedAmount,
                    paymentResponse.getChannelTransactionNumber());
        }
    }

    /**
     * 更新支付单状态
     * <p>
     * 将所有支付单状态更新为"支付中"
     *
     * @param payments 支付单列表
     */
    private void updatePaymentStatus(List<PaymentAggregate> payments) {
        log.debug("更新支付单状态为支付中，支付单数量: {}", payments.size());

        for (PaymentAggregate payment : payments) {
            // 状态已在executePayment中更新为PAYING
            log.debug("支付单 {} 状态已更新为: {}", payment.getCode(),
                    payment.getPaymentStatus().getDescription());
        }
    }

    /**
     * 持久化支付明细
     * <p>
     * 保存所有支付单及其流水
     *
     * @param payments 支付单列表
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
     *
     * @param channel 支付渠道
     * @return 渠道服务
     * @throws PaymentException 如果找不到对应的渠道服务
     */
    private IPaymentChannelService findChannelService(PaymentChannel channel) {
        return paymentChannelServices.stream()
                .filter(service -> service.getChannelType() == channel)
                .findFirst()
                .orElseThrow(() -> new PaymentException(
                        "支付渠道不可用: " + channel.getDescription()));
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
     * @param payments       支付单列表
     * @param amountMap      支付单ID到分配金额的映射
     * @param totalAmount    总支付金额
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

        // 使用验证服务进行校验
        validationService.validateAmountAllocation(payments, allocatedAmounts, totalAmount);

        log.debug("金额分配验证通过");
    }
}

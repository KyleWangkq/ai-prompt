package com.bytz.cms.payment.application.service;

import com.bytz.cms.payment.application.dto.CreatePaymentRequest;
import com.bytz.cms.payment.application.dto.ExecutePaymentRequest;
import com.bytz.cms.payment.application.dto.PaymentResponse;
import com.bytz.cms.payment.domain.model.*;
import com.bytz.cms.payment.domain.repository.PaymentRepository;
import com.bytz.cms.payment.domain.repository.PaymentTransactionRepository;
import com.bytz.cms.payment.domain.service.PaymentValidationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Payment Application Service
 * 支付单应用服务（用例编排）
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class PaymentApplicationService {
    
    private final PaymentRepository paymentRepository;
    private final PaymentTransactionRepository transactionRepository;
    private final PaymentValidationService validationService;
    
    /**
     * 创建支付单（UC-PM-001）
     */
    @Transactional
    public PaymentResponse createPayment(CreatePaymentRequest request) {
        log.info("开始创建支付单: resellerId={}, amount={}, type={}", 
                request.getResellerId(), request.getPaymentAmount(), request.getPaymentType());
        
        // 验证请求
        PaymentType paymentType = PaymentType.valueOf(request.getPaymentType());
        validationService.validateCreate(request.getResellerId(), request.getPaymentAmount(), paymentType);
        
        // 生成支付单号
        String paymentId = generatePaymentId();
        
        // 检查唯一性
        if (paymentRepository.existsById(paymentId)) {
            throw new IllegalStateException("支付单号已存在: " + paymentId);
        }
        
        // 构建关联业务信息
        RelatedBusinessInfo relatedBusinessInfo = null;
        if (request.getRelatedBusinessId() != null) {
            RelatedBusinessType relatedBusinessType = RelatedBusinessType.valueOf(request.getRelatedBusinessType());
            relatedBusinessInfo = RelatedBusinessInfo.of(
                    request.getRelatedBusinessId(),
                    relatedBusinessType,
                    request.getBusinessExpireDate()
            );
            relatedBusinessInfo.validate();
        }
        
        // 创建支付单聚合
        Payment payment = Payment.create(
                paymentId,
                request.getOrderId(),
                request.getResellerId(),
                request.getPaymentAmount(),
                paymentType,
                request.getPaymentDeadline(),
                request.getPriorityLevel(),
                relatedBusinessInfo,
                request.getBusinessDesc(),
                request.getBusinessTags()
        );
        
        // 持久化
        paymentRepository.save(payment);
        
        log.info("支付单创建成功: id={}", paymentId);
        return toResponse(payment);
    }
    
    /**
     * 执行支付（UC-PM-003）- 统一处理单支付单和批量支付
     */
    @Transactional
    public String executePayment(ExecutePaymentRequest request) {
        log.info("开始执行支付: paymentCount={}, channel={}", 
                request.getPaymentItems().size(), request.getPaymentChannel());
        
        PaymentChannel channel = PaymentChannel.valueOf(request.getPaymentChannel());
        
        // 加载所有支付单
        List<Payment> payments = request.getPaymentItems().stream()
                .map(item -> paymentRepository.findById(item.getPaymentId())
                        .orElseThrow(() -> new IllegalArgumentException("支付单不存在: " + item.getPaymentId())))
                .collect(Collectors.toList());
        
        // 验证批量支付前置条件
        if (payments.size() > 1) {
            validationService.validateBatchExecute(payments);
        }
        
        // 验证每个支付单的执行金额
        for (int i = 0; i < payments.size(); i++) {
            Payment payment = payments.get(i);
            BigDecimal executeAmount = request.getPaymentItems().get(i).getAmount();
            validationService.validateExecute(payment, executeAmount);
        }
        
        // 计算总支付金额
        BigDecimal totalAmount = request.getPaymentItems().stream()
                .map(ExecutePaymentRequest.PaymentItem::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        // 生成渠道交易号（合并支付时，所有支付单共享同一个渠道交易号）
        String channelTransactionNumber = generateChannelTransactionNumber();
        
        // 标记所有支付单为支付中
        payments.forEach(Payment::markPaying);
        
        // 为每个支付单创建支付流水
        for (int i = 0; i < payments.size(); i++) {
            Payment payment = payments.get(i);
            BigDecimal amount = request.getPaymentItems().get(i).getAmount();
            
            String transactionId = generateTransactionId();
            PaymentTransaction transaction = PaymentTransaction.start(
                    transactionId,
                    payment.getId(),
                    TransactionType.PAYMENT,
                    amount,
                    channel,
                    request.getPaymentWay(),
                    channelTransactionNumber,
                    LocalDateTime.now().plusHours(2) // 2小时后过期
            );
            
            transactionRepository.save(transaction);
            payment.addTransaction(transaction);
        }
        
        // 更新所有支付单
        payments.forEach(paymentRepository::update);
        
        // TODO: 调用支付渠道接口，发起实际支付
        log.info("支付流水创建成功，待调用支付渠道: channelTransactionNumber={}, totalAmount={}", 
                channelTransactionNumber, totalAmount);
        
        return channelTransactionNumber;
    }
    
    /**
     * 处理支付回调（UC-PM-004）
     */
    @Transactional
    public void processPaymentCallback(String transactionId, boolean success, LocalDateTime completedAt) {
        log.info("处理支付回调: transactionId={}, success={}", transactionId, success);
        
        // 查找流水
        PaymentTransaction transaction = transactionRepository.findById(transactionId)
                .orElseThrow(() -> new IllegalArgumentException("支付流水不存在: " + transactionId));
        
        if (success) {
            // 标记流水成功
            transaction.success(completedAt);
            transactionRepository.update(transaction);
            
            // 更新支付单
            Payment payment = paymentRepository.findById(transaction.getPaymentId())
                    .orElseThrow(() -> new IllegalArgumentException("支付单不存在: " + transaction.getPaymentId()));
            
            payment.applyPayment(
                    transaction.getTransactionAmount(),
                    transaction.getPaymentChannel().name(),
                    transaction.getChannelTransactionNumber(),
                    completedAt
            );
            
            paymentRepository.update(payment);
            log.info("支付成功处理完成: paymentId={}, amount={}", payment.getId(), transaction.getTransactionAmount());
        } else {
            // 标记流水失败
            transaction.fail("支付失败");
            transactionRepository.update(transaction);
            
            // 更新支付单状态（回滚到原状态）
            Payment payment = paymentRepository.findById(transaction.getPaymentId())
                    .orElseThrow(() -> new IllegalArgumentException("支付单不存在: " + transaction.getPaymentId()));
            payment.updateStatusByAmounts();
            paymentRepository.update(payment);
            
            log.warn("支付失败: paymentId={}", payment.getId());
        }
    }
    
    /**
     * 查询支付单详情（UC-PM-005）
     */
    @Transactional(readOnly = true)
    public PaymentResponse getPaymentDetail(String paymentId) {
        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new IllegalArgumentException("支付单不存在: " + paymentId));
        
        // 加载支付流水
        List<PaymentTransaction> transactions = transactionRepository.findByPaymentId(paymentId);
        transactions.forEach(payment::addTransaction);
        
        return toResponse(payment);
    }
    
    /**
     * 筛选支付单（UC-PM-002）
     */
    @Transactional(readOnly = true)
    public List<PaymentResponse> filterPayments(String resellerId, 
                                               List<String> statusStrings,
                                               List<String> typeStrings,
                                               LocalDateTime startTime,
                                               LocalDateTime endTime) {
        List<PaymentStatus> statuses = statusStrings != null ? 
                statusStrings.stream().map(PaymentStatus::valueOf).collect(Collectors.toList()) : null;
        
        List<PaymentType> types = typeStrings != null ?
                typeStrings.stream().map(PaymentType::valueOf).collect(Collectors.toList()) : null;
        
        List<Payment> payments = paymentRepository.filterPayments(resellerId, statuses, types, startTime, endTime);
        
        return payments.stream().map(this::toResponse).collect(Collectors.toList());
    }
    
    /**
     * 转换为响应DTO
     */
    private PaymentResponse toResponse(Payment payment) {
        PaymentResponse response = new PaymentResponse();
        response.setId(payment.getId());
        response.setOrderId(payment.getOrderId());
        response.setResellerId(payment.getResellerId());
        response.setPaymentAmount(payment.getPaymentAmount());
        response.setPaidAmount(payment.getPaidAmount());
        response.setRefundedAmount(payment.getRefundedAmount());
        response.setActualAmount(payment.getActualAmount());
        response.setPendingAmount(payment.getPendingAmount());
        response.setPaymentType(payment.getPaymentType().name());
        response.setPaymentStatus(payment.getPaymentStatus().name());
        response.setRefundStatus(payment.getRefundStatus().name());
        response.setPaymentDeadline(payment.getPaymentDeadline());
        response.setPriorityLevel(payment.getPriorityLevel());
        response.setBusinessDesc(payment.getBusinessDesc());
        response.setCreatedTime(payment.getCreatedTime());
        response.setUpdatedTime(payment.getUpdatedTime());
        
        if (payment.getRelatedBusinessInfo() != null) {
            response.setRelatedBusinessId(payment.getRelatedBusinessInfo().getRelatedBusinessId());
            response.setRelatedBusinessType(payment.getRelatedBusinessInfo().getRelatedBusinessType().name());
        }
        
        return response;
    }
    
    /**
     * 生成支付单号
     */
    private String generatePaymentId() {
        return "PAY" + System.currentTimeMillis() + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }
    
    /**
     * 生成流水号
     */
    private String generateTransactionId() {
        return "TXN" + System.currentTimeMillis() + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }
    
    /**
     * 生成渠道交易号
     */
    private String generateChannelTransactionNumber() {
        return "CHN" + System.currentTimeMillis() + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }
}

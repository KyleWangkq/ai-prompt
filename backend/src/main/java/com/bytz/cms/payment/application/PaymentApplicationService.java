package com.bytz.cms.payment.application;

import com.bytz.cms.payment.application.assembler.PaymentAssembler;
import com.bytz.cms.payment.domain.PaymentQueryService;
import com.bytz.cms.payment.domain.PaymentValidationService;
import com.bytz.cms.payment.domain.command.CreatePaymentCommand;
import com.bytz.cms.payment.domain.model.PaymentAggregate;
import com.bytz.cms.payment.domain.repository.IPaymentRepository;
import com.bytz.cms.payment.domain.enums.PaymentStatus;
import com.bytz.cms.payment.domain.enums.PaymentType;
import com.bytz.cms.payment.interfaces.model.PaymentCreateRO;
import com.bytz.cms.payment.interfaces.model.PaymentVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * 支付单应用服务
 * 
 * <p>协调支付单的创建、查询、筛选等用例</p>
 * <p>术语来源：Glossary.md - 应用服务术语"支付单应用服务(PaymentApplicationService)"</p>
 * <p>相关用例：UC-PM-001、UC-PM-002、UC-PM-005、UC-PM-007</p>
 * 
 * @author DDD设计团队
 * @version 1.0.0
 * @since 2025-01-15
 */
@Slf4j
@Service
public class PaymentApplicationService {
    
    @Resource
    private IPaymentRepository paymentRepository;
    
    @Resource
    private PaymentValidationService validationService;
    
    @Resource
    private PaymentQueryService queryService;
    
    @Resource
    private PaymentAssembler paymentAssembler;
    
    /**
     * 创建支付单（UC-PM-001、UC-PM-007）
     * 
     * <p>用例来源：UC-PM-001接收支付单创建请求、UC-PM-007创建信用还款支付单</p>
     * 
     * @param request 创建支付单请求
     * @return 支付单响应
     */
    @Transactional(rollbackFor = Exception.class)
    public PaymentVO createPayment(PaymentCreateRO request) {
        // 转换为命令对象
        CreatePaymentCommand command = paymentAssembler.toCreateCommand(request);
        
        // 生成支付单号
        String paymentId = generatePaymentId();
        
        // 验证创建请求
        validationService.validateCreate(paymentId, command.getPaymentAmount());
        
        // 创建支付单聚合根
        PaymentAggregate payment = PaymentAggregate.create(
                paymentId,
                command.getOrderId(),
                command.getResellerId(),
                command.getPaymentAmount(),
                command.getPaymentType(),
                command.getPaymentDeadline(),
                command.getPriorityLevel(),
                command.getRelatedBusinessId(),
                command.getRelatedBusinessType(),
                command.getBusinessExpireDate(),
                command.getBusinessDesc(),
                command.getBusinessTags()
        );
        
        // 设置创建人信息
        payment.setCreateBy(command.getCreateBy());
        payment.setCreateByName(command.getCreateByName());
        
        // 保存支付单
        paymentRepository.save(payment);
        
        log.info("创建支付单成功，支付单号：{}, 支付类型：{}", paymentId, command.getPaymentType());
        
        // 转换为响应对象
        return paymentAssembler.toVO(payment);
    }
    
    /**
     * 查询支付单详情（UC-PM-005）
     * 
     * <p>用例来源：UC-PM-005查询支付单信息</p>
     * 
     * @param paymentId 支付单号
     * @return 支付单详情
     */
    public PaymentVO getPaymentDetail(String paymentId) {
        PaymentAggregate payment = queryService.queryPaymentDetail(paymentId);
        return paymentAssembler.toVO(payment);
    }
    
    /**
     * 筛选支付单（UC-PM-002）
     * 
     * <p>用例来源：UC-PM-002筛选支付单</p>
     * 
     * @param resellerId 经销商ID
     * @param paymentStatus 支付状态
     * @param paymentType 支付类型
     * @param startTime 创建开始时间
     * @param endTime 创建结束时间
     * @return 支付单列表
     */
    public List<PaymentVO> filterPayments(
            String resellerId,
            PaymentStatus paymentStatus,
            PaymentType paymentType,
            LocalDateTime startTime,
            LocalDateTime endTime) {
        
        List<PaymentAggregate> payments = queryService.filterPayments(
                resellerId, paymentStatus, paymentType, null, startTime, endTime);
        
        return paymentAssembler.toVOs(payments);
    }
    
    /**
     * 生成支付单号
     * 
     * @return 支付单号（32位UUID）
     */
    private String generatePaymentId() {
        return UUID.randomUUID().toString().replace("-", "");
    }
}

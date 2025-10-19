package com.bytz.cms.payment.domain;

import com.bytz.cms.payment.domain.model.PaymentAggregate;
import com.bytz.cms.payment.domain.repository.IPaymentRepository;
import com.bytz.cms.payment.shared.exception.BusinessException;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.math.BigDecimal;

/**
 * 支付单创建与执行前的业务校验（金额、状态、截止时间、唯一性等）
 * 
 * <p>领域服务类型：验证服务</p>
 * <p>术语来源：Glossary.md - 领域服务术语"支付验证服务"</p>
 * <p>相关用例：UC-PM-001、UC-PM-003、UC-PM-006</p>
 * 
 * @author DDD设计团队
 * @version 1.0.0
 * @since 2025-01-15
 */
@Service
public class PaymentValidationService {
    
    @Resource
    private IPaymentRepository paymentRepository;
    
    /**
     * 校验创建请求（UC-PM-001），包含普通支付单和信用还款支付单
     * 
     * <p>验证项：支付单号唯一性、金额合法性、必填字段完整性、业务规则符合性</p>
     * <p>用例来源：UC-PM-001步骤3-4、UC-PM-007步骤3-4</p>
     * 
     * @param paymentId 支付单号
     * @param paymentAmount 支付金额
     * @throws BusinessException 如果验证失败
     */
    public void validateCreate(String paymentId, BigDecimal paymentAmount) {
        // TODO: 验证支付单号唯一性
        if (paymentRepository.exists(paymentId)) {
            throw new BusinessException("支付单号已存在：" + paymentId);
        }
        
        // TODO: 验证金额合法性
        if (paymentAmount == null || paymentAmount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new BusinessException("支付金额必须大于0");
        }
    }
    
    /**
     * 校验执行支付前置条件（UC-PM-003），适用于所有支付类型
     * 
     * <p>验证项：支付单状态可支付、待支付金额>0、未超过截止时间、经销商权限</p>
     * <p>用例来源：UC-PM-003步骤3-4</p>
     * 
     * @param payment 支付单聚合根
     * @throws BusinessException 如果验证失败
     */
    public void validateExecute(PaymentAggregate payment) {
        // TODO: 调用支付单的validatePayable方法
        payment.validatePayable();
    }
    
    /**
     * 校验退款前置条件（UC-PM-006）
     * 
     * <p>验证项：支付单状态可退款、可退款金额足够、退款流水可选、业务授权</p>
     * <p>用例来源：UC-PM-006步骤2-3</p>
     * 
     * @param payment 支付单聚合根
     * @param refundAmount 退款金额
     * @throws BusinessException 如果验证失败
     */
    public void validateRefund(PaymentAggregate payment, BigDecimal refundAmount) {
        // TODO: 调用支付单的validateRefundable方法
        payment.validateRefundable(refundAmount);
    }
}

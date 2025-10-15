package com.bytz.cms.payment.domain.service;

import com.bytz.cms.payment.domain.model.Payment;
import com.bytz.cms.payment.domain.model.PaymentType;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Payment Validation Service (Domain Service)
 * 支付单创建与执行前的业务校验
 */
@Service
@Slf4j
public class PaymentValidationService {
    
    /**
     * 校验创建请求（UC-PM-001/007）
     */
    public void validateCreate(String resellerId, BigDecimal paymentAmount, 
                               PaymentType paymentType) {
        if (resellerId == null || resellerId.trim().isEmpty()) {
            throw new IllegalArgumentException("经销商ID不能为空");
        }
        
        if (paymentAmount == null || paymentAmount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("支付金额必须大于0");
        }
        
        if (paymentType == null) {
            throw new IllegalArgumentException("支付类型不能为空");
        }
        
        log.debug("创建请求校验通过: resellerId={}, paymentAmount={}, paymentType={}", 
                resellerId, paymentAmount, paymentType);
    }
    
    /**
     * 校验执行支付前置条件（UC-PM-003）
     */
    public void validateExecute(Payment payment, BigDecimal executeAmount) {
        if (payment == null) {
            throw new IllegalArgumentException("支付单不能为空");
        }
        
        if (executeAmount == null || executeAmount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("执行支付金额必须大于0");
        }
        
        // 使用聚合根的验证方法
        payment.validatePayable();
        
        // 验证执行金额不超过待支付金额
        BigDecimal pendingAmount = payment.getPendingAmount();
        if (executeAmount.compareTo(pendingAmount) > 0) {
            throw new IllegalArgumentException(
                String.format("执行支付金额(%s)超过待支付金额(%s)", executeAmount, pendingAmount));
        }
        
        log.debug("执行支付前置条件校验通过: paymentId={}, executeAmount={}", 
                payment.getId(), executeAmount);
    }
    
    /**
     * 校验批量支付前置条件
     */
    public void validateBatchExecute(java.util.List<Payment> payments) {
        if (payments == null || payments.isEmpty()) {
            throw new IllegalArgumentException("支付单列表不能为空");
        }
        
        // 验证所有支付单属于同一经销商
        String resellerId = payments.get(0).getResellerId();
        boolean allSameReseller = payments.stream()
                .allMatch(p -> p.getResellerId().equals(resellerId));
        
        if (!allSameReseller) {
            throw new IllegalArgumentException("批量支付的所有支付单必须属于同一经销商");
        }
        
        // 验证每个支付单都可以支付
        for (Payment payment : payments) {
            payment.validatePayable();
        }
        
        log.debug("批量支付前置条件校验通过: paymentCount={}, resellerId={}", 
                payments.size(), resellerId);
    }
    
    /**
     * 校验退款前置条件（UC-PM-006）
     */
    public void validateRefund(Payment payment, BigDecimal refundAmount) {
        if (payment == null) {
            throw new IllegalArgumentException("支付单不能为空");
        }
        
        if (refundAmount == null || refundAmount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("退款金额必须大于0");
        }
        
        // 使用聚合根的验证方法
        payment.validateRefundable(refundAmount);
        
        log.debug("退款前置条件校验通过: paymentId={}, refundAmount={}", 
                payment.getId(), refundAmount);
    }
    
    /**
     * 校验信用还款创建请求（UC-PM-007）
     */
    public void validateCreditRepayment(String resellerId, BigDecimal amount, 
                                       String relatedBusinessId) {
        if (resellerId == null || resellerId.trim().isEmpty()) {
            throw new IllegalArgumentException("经销商ID不能为空");
        }
        
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("还款金额必须大于0");
        }
        
        if (relatedBusinessId == null || relatedBusinessId.trim().isEmpty()) {
            throw new IllegalArgumentException("信用还款必须关联信用记录ID");
        }
        
        log.debug("信用还款校验通过: resellerId={}, amount={}, relatedBusinessId={}", 
                resellerId, amount, relatedBusinessId);
    }
}

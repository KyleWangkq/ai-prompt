package com.bytz.cms.payment.domain;

import com.bytz.cms.payment.domain.entity.PaymentTransactionEntity;
import com.bytz.cms.payment.domain.enums.TransactionStatus;
import com.bytz.cms.payment.domain.enums.TransactionType;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 支付流水领域服务
 * 
 * <p>负责支付流水的业务操作和验证逻辑</p>
 * 
 * <p>术语来源：DDD设计模式 - 领域服务</p>
 * <p>需求来源：需求文档4.4.2节支付流水表设计</p>
 * 
 * @author DDD设计团队
 * @version 1.0.0
 * @since 2025-01-15
 */
public class PaymentTransactionDomainService {

    /**
     * 标记流水成功并记录完成时间（对应渠道回调成功，UC-PM-004）
     * 
     * <p>用例来源：UC-PM-004处理支付回调</p>
     * <p>状态转换：PROCESSING → SUCCESS</p>
     * 
     * @param transaction 支付流水实体
     * @param completedAt 完成时间
     */
    public void markSuccess(PaymentTransactionEntity transaction, LocalDateTime completedAt) {
        transaction.setTransactionStatus(TransactionStatus.SUCCESS);
        transaction.setCompleteDatetime(completedAt);
        transaction.setUpdatedTime(LocalDateTime.now());
    }

    /**
     * 标记流水失败（渠道回调失败或主动失败）
     * 
     * <p>状态转换：PROCESSING → FAILED</p>
     * 
     * @param transaction 支付流水实体
     * @param reason 失败原因
     */
    public void markFailed(PaymentTransactionEntity transaction, String reason) {
        transaction.setTransactionStatus(TransactionStatus.FAILED);
        transaction.setBusinessRemark(reason);
        transaction.setCompleteDatetime(LocalDateTime.now());
        transaction.setUpdatedTime(LocalDateTime.now());
    }

    /**
     * 校验金额类型一致性
     * 
     * <p>业务规则：PAYMENT时transactionAmount>0，REFUND时transactionAmount<0</p>
     * <p>来源：需求文档4.4.2节支付流水表设计</p>
     * 
     * @param transaction 支付流水实体
     * @return true表示一致，false表示不一致
     */
    public boolean validateAmountTypeConsistency(PaymentTransactionEntity transaction) {
        TransactionType transactionType = transaction.getTransactionType();
        BigDecimal transactionAmount = transaction.getTransactionAmount();
        
        if (transactionType == TransactionType.PAYMENT) {
            return transactionAmount.compareTo(BigDecimal.ZERO) > 0;
        } else if (transactionType == TransactionType.REFUND) {
            return transactionAmount.compareTo(BigDecimal.ZERO) < 0;
        }
        return false;
    }
}

package com.bytz.cms.payment.domain;

import com.bytz.cms.payment.domain.entity.PaymentTransactionEntity;
import com.bytz.cms.payment.domain.enums.TransactionType;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

/**
 * 支付流水领域服务
 * 
 * <p>负责支付流水的跨实体业务操作和验证逻辑</p>
 * <p>注意：单个实体的行为方法（start, success, fail）已在PaymentTransactionEntity中定义</p>
 * 
 * <p>术语来源：DDD设计模式 - 领域服务</p>
 * <p>需求来源：需求文档4.4.2节支付流水表设计</p>
 * 
 * @author DDD设计团队
 * @version 1.0.0
 * @since 2025-01-15
 */
@Service
public class PaymentTransactionDomainService {

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

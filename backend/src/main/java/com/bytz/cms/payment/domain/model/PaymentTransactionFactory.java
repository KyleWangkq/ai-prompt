package com.bytz.cms.payment.domain.model;

import com.bytz.cms.payment.domain.entity.PaymentTransactionEntity;
import com.bytz.cms.payment.domain.enums.PaymentChannel;
import com.bytz.cms.payment.domain.enums.TransactionStatus;
import com.bytz.cms.payment.domain.enums.TransactionType;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 支付流水工厂类
 * 
 * <p>负责创建支付流水实体的工厂类，封装创建逻辑</p>
 * 
 * <p>术语来源：DDD设计模式 - 工厂模式</p>
 * <p>需求来源：需求文档4.4.2节支付流水表设计</p>
 * 
 * @author DDD设计团队
 * @version 1.0.0
 * @since 2025-01-15
 */
public class PaymentTransactionFactory {

    /**
     * 创建流水并置为PROCESSING（UC-PM-003-11/12）
     * 
     * <p>用例来源：UC-PM-003执行支付操作、UC-PM-006接收退款执行指令</p>
     * <p>初始状态：PROCESSING</p>
     * 
     * @param paymentId 支付单ID
     * @param transactionType 流水类型
     * @param transactionAmount 交易金额
     * @param paymentChannel 支付渠道
     * @param paymentWay 支付方式
     * @param channelTransactionNumber 渠道交易号
     * @param expirationTime 过期时间
     * @return 新创建的支付流水实体
     */
    public static PaymentTransactionEntity createTransaction(
            String paymentId,
            TransactionType transactionType,
            BigDecimal transactionAmount,
            PaymentChannel paymentChannel,
            String paymentWay,
            String channelTransactionNumber,
            LocalDateTime expirationTime) {
        
        PaymentTransactionEntity transaction = new PaymentTransactionEntity();
        transaction.setPaymentId(paymentId);
        transaction.setTransactionType(transactionType);
        transaction.setTransactionStatus(TransactionStatus.PROCESSING);
        transaction.setTransactionAmount(transactionAmount);
        transaction.setPaymentChannel(paymentChannel);
        transaction.setPaymentWay(paymentWay);
        transaction.setChannelTransactionNumber(channelTransactionNumber);
        transaction.setExpirationTime(expirationTime);
        transaction.setCreatedTime(LocalDateTime.now());
        transaction.setDelFlag(0);
        
        return transaction;
    }
}

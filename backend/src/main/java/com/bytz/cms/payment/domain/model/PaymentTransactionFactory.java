package com.bytz.cms.payment.domain.model;

import com.bytz.cms.payment.domain.entity.PaymentTransactionEntity;
import com.bytz.cms.payment.domain.enums.PaymentChannel;
import com.bytz.cms.payment.domain.enums.TransactionType;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * 支付流水工厂类
 * 
 * <p>负责创建支付流水实体的工厂类，封装创建逻辑</p>
 * <p>注意：实际创建逻辑已委托给PaymentTransactionEntity.start()静态方法</p>
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
        
        // 生成流水号
        String transactionId = generateTransactionId();
        
        // 委托给实体的静态工厂方法
        return PaymentTransactionEntity.start(
                transactionId,
                paymentId,
                transactionType,
                transactionAmount,
                paymentChannel,
                paymentWay,
                channelTransactionNumber,
                expirationTime
        );
    }
    
    /**
     * 生成流水号
     * 
     * @return 流水号（32位UUID）
     */
    private static String generateTransactionId() {
        return UUID.randomUUID().toString().replace("-", "");
    }
}

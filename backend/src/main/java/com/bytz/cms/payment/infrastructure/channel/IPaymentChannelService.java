package com.bytz.cms.payment.infrastructure.channel;

import com.bytz.cms.payment.domain.enums.PaymentChannel;

import java.math.BigDecimal;

/**
 * 支付渠道接口
 * 
 * <p>定义所有支付渠道必须实现的基础方法</p>
 * <p>具体渠道实现类需要实现此接口并提供渠道特定的业务逻辑</p>
 * 
 * <p>需求来源：需求文档4.5节支付渠道集成、4.6节支付回调处理</p>
 * 
 * @author DDD设计团队
 * @version 1.0.0
 * @since 2025-01-15
 */
public interface IPaymentChannelService {
    
    /**
     * 获取渠道类型
     * 
     * @return 渠道类型枚举
     */
    PaymentChannel getChannelType();
    
    /**
     * 检查渠道是否可用
     * 
     * @param resellerId 经销商ID
     * @return true表示可用，false表示不可用
     */
    boolean isAvailable(String resellerId);
    
    /**
     * 创建支付请求
     * 
     * <p>调用渠道接口创建支付，返回渠道交易号</p>
     * 
     * @param paymentId 支付单号
     * @param resellerId 经销商ID
     * @param amount 支付金额
     * @param description 支付描述
     * @return 渠道支付请求结果
     */
    PaymentChannelResponse createPayment(String paymentId, String resellerId, BigDecimal amount, String description);
    
    /**
     * 查询支付状态
     * 
     * <p>用于补偿查询，主动查询渠道的支付状态</p>
     * 
     * @param channelTransactionNumber 渠道交易号
     * @return 支付状态查询结果
     */
    PaymentStatusQueryResult queryPaymentStatus(String channelTransactionNumber);
    
    /**
     * 创建退款请求
     * 
     * <p>调用渠道接口创建退款</p>
     * 
     * @param originalChannelTransactionNumber 原支付渠道交易号
     * @param refundAmount 退款金额
     * @param refundReason 退款原因
     * @return 渠道退款请求结果
     */
    RefundChannelResponse createRefund(String originalChannelTransactionNumber, BigDecimal refundAmount, String refundReason);
    
    /**
     * 验证回调签名
     * 
     * <p>验证渠道回调的合法性</p>
     * 
     * @param callbackData 回调数据
     * @param signature 签名
     * @return true表示签名有效，false表示签名无效
     */
    boolean validateCallback(String callbackData, String signature);
}

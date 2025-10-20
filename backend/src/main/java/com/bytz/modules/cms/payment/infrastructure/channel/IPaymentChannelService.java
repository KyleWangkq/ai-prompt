package com.bytz.modules.cms.payment.infrastructure.channel;

import com.bytz.modules.cms.payment.domain.enums.PaymentChannel;

import java.math.BigDecimal;
import java.util.Map;

/**
 * 支付渠道接口
 * Payment Channel Interface
 * 
 * 定义支付渠道的通用接口，所有支付渠道实现必须遵循此接口
 */
public interface IPaymentChannelService {
    
    /**
     * 获取渠道类型
     * 
     * @return 渠道类型
     */
    PaymentChannel getChannelType();
    
    /**
     * 创建支付请求
     * 
     * @param totalAmount 支付总金额
     * @param channelParams 渠道特定参数
     * @return 渠道交易号
     * TODO: 实现支付请求创建逻辑，返回渠道交易号
     */
    String createPaymentRequest(BigDecimal totalAmount, Map<String, Object> channelParams);
    
    /**
     * 查询支付状态
     * 
     * @param channelTransactionNumber 渠道交易号
     * @return 支付状态（SUCCESS/FAILED/PROCESSING）
     * TODO: 实现支付状态查询逻辑
     */
    String queryPaymentStatus(String channelTransactionNumber);
    
    /**
     * 创建退款请求
     * 
     * @param channelTransactionNumber 原支付渠道交易号
     * @param refundAmount 退款金额
     * @param refundReason 退款原因
     * @return 退款流水号
     * TODO: 实现退款请求创建逻辑
     */
    String createRefundRequest(String channelTransactionNumber, BigDecimal refundAmount, String refundReason);
    
    /**
     * 查询退款状态
     * 
     * @param refundTransactionNumber 退款流水号
     * @return 退款状态（SUCCESS/FAILED/PROCESSING）
     * TODO: 实现退款状态查询逻辑
     */
    String queryRefundStatus(String refundTransactionNumber);
    
    /**
     * 验证回调签名
     * 
     * @param callbackData 回调数据
     * @return 是否验证通过
     * TODO: 实现回调签名验证逻辑
     */
    boolean validateCallback(Map<String, Object> callbackData);
    
    /**
     * 检查渠道是否可用
     * 
     * @return 是否可用
     * TODO: 实现渠道可用性检查逻辑
     */
    boolean isAvailable();
}

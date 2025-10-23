package com.bytz.modules.cms.payment.infrastructure.channel;

import com.bytz.modules.cms.payment.domain.enums.PaymentChannel;
import com.bytz.modules.cms.payment.infrastructure.channel.command.CreatePaymentRequestCommand;
import com.bytz.modules.cms.payment.infrastructure.channel.command.CreateRefundRequestCommand;
import com.bytz.modules.cms.payment.infrastructure.channel.command.QueryPaymentStatusCommand;
import com.bytz.modules.cms.payment.infrastructure.channel.command.QueryRefundStatusCommand;

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
     * @param command 创建支付请求命令
     * @return 渠道交易号
     * TODO: 实现支付请求创建逻辑，返回渠道交易号
     */
    String createPaymentRequest(CreatePaymentRequestCommand command);
    
    /**
     * 查询支付状态
     * 
     * @param command 查询支付状态命令
     * @return 支付状态（SUCCESS/FAILED/PROCESSING）
     * TODO: 实现支付状态查询逻辑
     */
    String queryPaymentStatus(QueryPaymentStatusCommand command);
    
    /**
     * 创建退款请求
     * 
     * @param command 创建退款请求命令
     * @return 退款流水号
     * TODO: 实现退款请求创建逻辑
     */
    String createRefundRequest(CreateRefundRequestCommand command);
    
    /**
     * 查询退款状态
     * 
     * @param command 查询退款状态命令
     * @return 退款状态（SUCCESS/FAILED/PROCESSING）
     * TODO: 实现退款状态查询逻辑
     */
    String queryRefundStatus(QueryRefundStatusCommand command);
    
    /**
     * 检查渠道对经销商是否可用
     * 
     * @param resellerId 经销商ID
     * @return 是否可用
     * TODO: 实现渠道可用性检查逻辑，每个渠道自行定义经销商可用性规则
     */
    boolean isAvailable(String resellerId);
}

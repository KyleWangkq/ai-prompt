package com.bytz.modules.cms.payment.application;

import com.bytz.modules.cms.payment.domain.command.CreatePaymentCommand;
import com.bytz.modules.cms.payment.domain.command.ExecutePaymentCommand;
import com.bytz.modules.cms.payment.domain.command.ExecuteRefundCommand;
import com.bytz.modules.cms.payment.domain.enums.PaymentChannel;
import com.bytz.modules.cms.payment.domain.model.PaymentAggregate;

import java.util.List;

/**
 * 支付应用服务接口
 * Payment Application Service Interface
 * 
 * 定义支付模块对外和内部提供的应用服务接口
 */
public interface IPaymentApplicationService {
    /**
     * 创建支付单
     * 此方法供订单系统、信用管理系统等内部模块调用
     *
     * 使用场景：
     * - 订单系统创建订单后，调用此接口创建支付单
     * - 信用管理系统创建信用还款支付单
     *
     * @param command 创建支付单命令
     * @return 支付单聚合根
     */
    PaymentAggregate createPayment(CreatePaymentCommand command);

    /**
     * 执行退款
     * 此方法供订单系统等内部模块调用
     *
     * 使用场景：
     * - 订单系统发起退款审批通过后，调用此接口执行退款
     * - 支持全额退款和部分退款
     *
     * @param command 执行退款命令
     * @return 退款流水号
     */
    String executeRefund(ExecuteRefundCommand command);

    /**
     * 执行批量支付
     * 
     * @param command 执行支付命令
     * @return 渠道交易id
     */
    String executeBatchPayment(ExecutePaymentCommand command);
    
    /**
     * 查询经销商可用的支付渠道列表
     * 
     * @param resellerId 经销商ID
     * @return 支付渠道列表（枚举）
     */
    List<PaymentChannel> queryAvailableChannels(String resellerId);
}
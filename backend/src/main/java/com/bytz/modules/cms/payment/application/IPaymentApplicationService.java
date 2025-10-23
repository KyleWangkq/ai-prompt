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
 * 定义支付模块对外提供的应用服务接口
 */
public interface IPaymentApplicationService extends IPaymentInternalService {
    
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
package com.bytz.modules.cms.payment.application;

import com.bytz.modules.cms.payment.domain.command.CreatePaymentCommand;
import com.bytz.modules.cms.payment.domain.command.ExecuteRefundCommand;
import com.bytz.modules.cms.payment.domain.model.PaymentAggregate;

/**
 * 支付内部服务接口
 * Payment Internal Service Interface
 * 
 * 提供给其他模块调用的Java内部接口，用于系统间调用
 * 注意：此接口用于内部Java代码调用，不是REST接口
 */
public interface IPaymentInternalService {
    
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
}

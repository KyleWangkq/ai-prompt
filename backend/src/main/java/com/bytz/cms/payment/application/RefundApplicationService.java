package com.bytz.cms.payment.application;

import org.springframework.stereotype.Service;

/**
 * 退款应用服务
 * Refund Application Service
 * 
 * 术语来源：Glossary.md - 应用服务术语"退款应用服务(RefundApplicationService)"
 * 职责：协调退款执行用例
 * 相关用例：UC-PM-006
 */
@Service
public class RefundApplicationService {
    
    /**
     * 执行退款操作
     * UC-PM-006：接收退款执行指令
     * 
     * 用例流程：
     * 1. 接收订单系统退款指令
     * 2. 调用RefundService.executeRefund
     * 3. 返回退款结果
     */
    public void executeRefund() {
        // TODO: Implement use case orchestration
        // 1. 接收RefundExecutionRequest（支付单号、退款金额、原流水号、业务单号、退款原因等）
        // 2. 调用RefundService.executeRefund()
        // 3. 转换为RefundExecutionVO返回
        
        throw new UnsupportedOperationException("Method not implemented");
    }
    
    /**
     * 处理退款回调
     * 处理退款回调通知
     * 
     * 用例流程：
     * 1. 接收支付渠道退款回调
     * 2. 调用PaymentCallbackService.processRefundCallback
     * 3. 返回处理结果
     */
    public void handleRefundCallback() {
        // TODO: Implement use case orchestration
        // 1. 接收RefundCallbackRequest（渠道回调数据）
        // 2. 调用PaymentCallbackService.processRefundCallback()
        // 3. 返回处理结果
        
        throw new UnsupportedOperationException("Method not implemented");
    }
}

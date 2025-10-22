package com.bytz.modules.cms.payment.domain.command;

import com.bytz.modules.cms.payment.domain.enums.PaymentChannel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

/**
 * 执行支付命令
 * Execute Payment Command
 * 
 * 封装支付执行操作的参数，支持单个或多个支付单的合并支付
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ExecutePaymentCommand {
    
    /**
     * 支付单信息列表
     */
    private List<PaymentItem> paymentItems;
    
    /**
     * 支付渠道
     */
    private PaymentChannel paymentChannel;
    
    /**
     * 操作人
     */
    private String operatorId;
    
    /**
     * 操作人姓名
     */
    private String operatorName;
    
    /**
     * 支付单项
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PaymentItem {
        
        /**
         * 支付单号
         */
        private String paymentCode;
        
        /**
         * 本次支付金额
         */
        private BigDecimal amount;
    }
}

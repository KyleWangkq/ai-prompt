package com.bytz.modules.cms.payment.interfaces.model;

import com.bytz.modules.cms.payment.domain.enums.PaymentChannel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

/**
 * 批量支付结果响应对象
 * Batch Payment Result Value Object
 * 
 * 返回批量支付执行的结果信息
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BatchPaymentResultVO {
    
    /**
     * 支付渠道
     */
    private PaymentChannel paymentChannel;
    
    /**
     * 渠道交易号
     */
    private String channelTransactionNumber;
    
    /**
     * 渠道支付记录ID（渠道系统的支付记录唯一标识，用于数据关联）
     */
    private String channelPaymentRecordId;
    
    /**
     * 支付总金额
     */
    private BigDecimal totalAmount;
    
    /**
     * 支付单数量
     */
    private Integer paymentCount;
    
    /**
     * 支付单结果列表
     */
    private List<PaymentResultItem> paymentResults;
    
    /**
     * 支付单结果项
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PaymentResultItem {
        
        /**
         * 支付单ID
         */
        private String paymentId;
        
        /**
         * 本次支付金额
         */
        private BigDecimal amount;
        
        /**
         * 支付流水号
         */
        private String transactionId;
        
        /**
         * 支付渠道
         */
        private PaymentChannel paymentChannel;
        
        /**
         * 渠道交易号
         */
        private String channelTransactionNumber;
        
        /**
         * 渠道支付记录ID
         */
        private String channelPaymentRecordId;
        
        /**
         * 是否成功
         */
        private Boolean success;
        
        /**
         * 错误信息（如果失败）
         */
        private String errorMessage;
    }
}

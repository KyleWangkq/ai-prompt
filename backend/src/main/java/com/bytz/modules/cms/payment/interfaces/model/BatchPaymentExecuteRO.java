package com.bytz.modules.cms.payment.interfaces.model;

import com.bytz.modules.cms.payment.domain.enums.PaymentChannel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.Valid;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.util.List;

/**
 * 批量支付执行请求对象
 * Batch Payment Execute Request Object
 * 
 * 用于前端提交批量支付请求，支持多个支付单合并支付
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BatchPaymentExecuteRO {
    
    /**
     * 支付单项列表
     */
    @NotEmpty(message = "支付单列表不能为空")
    @Valid
    private List<PaymentItem> paymentItems;
    
    /**
     * 支付渠道
     */
    @NotNull(message = "支付渠道不能为空")
    private PaymentChannel paymentChannel;

    /**
     * 渠道数据业务ID（如银行流水号）
     */
    private String channelBusinessId;
    
    /**
     * 支付单项
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PaymentItem {
        
        /**
         * 支付单ID（数据库主键）
         */
        @NotNull(message = "支付单ID不能为空")
        private String paymentId;
        
        /**
         * 本次支付金额
         */
        @NotNull(message = "支付金额不能为空")
        private BigDecimal amount;
    }
}
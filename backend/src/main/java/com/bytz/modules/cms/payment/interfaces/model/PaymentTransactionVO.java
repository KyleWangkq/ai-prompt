package com.bytz.modules.cms.payment.interfaces.model;

import com.bytz.modules.cms.payment.domain.enums.PaymentChannel;
import com.bytz.modules.cms.payment.domain.enums.TransactionStatus;
import com.bytz.modules.cms.payment.domain.enums.TransactionType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 支付流水响应对象
 * Payment Transaction Response Object
 * 
 * 用于返回支付流水的详细信息
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaymentTransactionVO {
    
    /**
     * 流水号
     */
    private String code;
    
    /**
     * 支付单号
     */
    private String paymentCode;
    
    /**
     * 流水类型
     */
    private TransactionType transactionType;
    
    /**
     * 流水状态
     */
    private TransactionStatus transactionStatus;
    
    /**
     * 交易金额
     */
    private BigDecimal transactionAmount;
    
    /**
     * 支付渠道
     */
    private PaymentChannel paymentChannel;
    
    /**
     * 渠道交易号
     */
    private String channelTransactionNumber;
    
    /**
     * 支付方式
     */
    private String paymentWay;
    
    /**
     * 原流水号（退款时使用）
     */
    private String originalTransactionCode;
    
    /**
     * 业务单号
     */
    private String businessOrderId;
    
    /**
     * 创建时间
     */
    private LocalDateTime createTime;
    
    /**
     * 完成时间
     */
    private LocalDateTime completeDateTime;
    
    /**
     * 业务备注
     */
    private String businessRemark;
}

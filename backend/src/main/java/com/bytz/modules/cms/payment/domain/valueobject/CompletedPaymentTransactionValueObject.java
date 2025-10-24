package com.bytz.modules.cms.payment.domain.valueobject;

import com.bytz.modules.cms.payment.domain.enums.PaymentChannel;
import com.bytz.modules.cms.payment.domain.enums.TransactionStatus;
import com.bytz.modules.cms.payment.domain.enums.TransactionType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 已完成支付流水值对象
 * Completed Payment Transaction Value Object
 * 
 * 已完成的支付流水（SUCCESS或FAILED状态）是不可变的值对象
 * 一旦交易完成，流水信息不应再被修改
 */
@Getter
@Builder
@AllArgsConstructor
public class CompletedPaymentTransactionValueObject {
    
    /**
     * 数据库主键ID
     */
    private final String id;
    
    /**
     * 流水号（业务编码）
     */
    private final String code;
    
    /**
     * 支付单ID
     */
    private final String paymentId;
    
    /**
     * 流水类型（支付/退款）
     */
    private final TransactionType transactionType;
    
    /**
     * 流水状态（SUCCESS或FAILED）
     */
    private final TransactionStatus transactionStatus;
    
    /**
     * 交易金额
     */
    private final BigDecimal transactionAmount;
    
    /**
     * 支付渠道
     */
    private final PaymentChannel paymentChannel;
    
    /**
     * 渠道交易号
     */
    private final String channelTransactionNumber;
    
    /**
     * 渠道支付记录ID
     */
    private final String channelPaymentRecordId;
    
    /**
     * 支付方式
     */
    private final String paymentWay;
    
    /**
     * 原流水ID（退款时使用）
     */
    private final String originalTransactionId;
    
    /**
     * 业务单号
     */
    private final String businessOrderId;
    
    /**
     * 创建时间
     */
    private final LocalDateTime createTime;
    
    /**
     * 完成时间
     */
    private final LocalDateTime completeDateTime;
    
    /**
     * 过期时间
     */
    private final LocalDateTime expirationTime;
    
    /**
     * 业务备注
     */
    private final String businessRemark;
    
    /**
     * 创建人
     */
    private final String createBy;
    
    /**
     * 创建人姓名
     */
    private final String createByName;
    
    /**
     * 更新人
     */
    private final String updateBy;
    
    /**
     * 更新人姓名
     */
    private final String updateByName;
    
    /**
     * 更新时间
     */
    private final LocalDateTime updateTime;
    
    /**
     * 判断是否为支付流水
     */
    public boolean isPaymentTransaction() {
        return TransactionType.PAYMENT.equals(this.transactionType);
    }
    
    /**
     * 判断是否为退款流水
     */
    public boolean isRefundTransaction() {
        return TransactionType.REFUND.equals(this.transactionType);
    }
    
    /**
     * 判断流水是否成功
     */
    public boolean isSuccess() {
        return TransactionStatus.SUCCESS.equals(this.transactionStatus);
    }
    
    /**
     * 判断流水是否失败
     */
    public boolean isFailed() {
        return TransactionStatus.FAILED.equals(this.transactionStatus);
    }
}

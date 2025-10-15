package com.bytz.cms.payment.infrastructure.persistence;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * PaymentTransaction Entity (Infrastructure Layer - Database Mapping)
 * 支付流水数据库实体
 */
@Data
@TableName("cms_payment_transaction")
public class PaymentTransactionEntity {
    
    /**
     * 流水号，主键
     */
    @TableId(value = "id", type = IdType.ASSIGN_ID)
    private String id;
    
    /**
     * 关联支付单号
     */
    @TableField("payment_id")
    private String paymentId;
    
    /**
     * 流水类型：PAYMENT/REFUND
     */
    @TableField("transaction_type")
    private String transactionType;
    
    /**
     * 流水状态：PROCESSING/SUCCESS/FAILED
     */
    @TableField("transaction_status")
    private String transactionStatus;
    
    /**
     * 交易金额
     */
    @TableField("transaction_amount")
    private BigDecimal transactionAmount;
    
    /**
     * 支付渠道
     */
    @TableField("payment_channel")
    private String paymentChannel;
    
    /**
     * 渠道交易号
     */
    @TableField("channel_transaction_number")
    private String channelTransactionNumber;
    
    /**
     * 支付方式
     */
    @TableField("payment_way")
    private String paymentWay;
    
    /**
     * 原交易流水号
     */
    @TableField("original_transaction_id")
    private String originalTransactionId;
    
    /**
     * 业务单号
     */
    @TableField("business_order_id")
    private String businessOrderId;
    
    /**
     * 创建时间
     */
    @TableField(value = "create_time", fill = FieldFill.INSERT)
    private LocalDateTime createTime;
    
    /**
     * 交易完成时间
     */
    @TableField("complete_datetime")
    private LocalDateTime completeDatetime;
    
    /**
     * 支付过期时间
     */
    @TableField("expiration_time")
    private LocalDateTime expirationTime;
    
    /**
     * 业务备注
     */
    @TableField("remark")
    private String remark;
}

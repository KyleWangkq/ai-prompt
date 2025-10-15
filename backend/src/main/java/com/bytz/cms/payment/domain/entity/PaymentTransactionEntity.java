package com.bytz.cms.payment.domain.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.bytz.cms.payment.shared.enums.PaymentChannel;
import com.bytz.cms.payment.shared.enums.TransactionStatus;
import com.bytz.cms.payment.shared.enums.TransactionType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 支付流水实体
 * Payment Transaction Entity
 * 
 * 术语来源：Glossary.md - 实体术语"支付流水实体(PaymentTransaction Entity)"
 * 数据库表：cms_payment_transaction
 * 需求来源：需求文档4.4.2节、4.4.3节
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("cms_payment_transaction")
public class PaymentTransactionEntity implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    /**
     * 流水号，主键（32位字符，全局唯一）
     */
    @TableId(value = "id", type = IdType.INPUT)
    private String id;
    
    /**
     * 关联支付单号（32位字符，外键）
     */
    @TableField("payment_id")
    private String paymentId;
    
    /**
     * 流水类型（支付/退款）
     */
    @TableField("transaction_type")
    private TransactionType transactionType;
    
    /**
     * 流水状态（处理中/成功/失败）
     */
    @TableField("transaction_status")
    private TransactionStatus transactionStatus;
    
    /**
     * 交易金额（6位小数）
     */
    @TableField("transaction_amount")
    private BigDecimal transactionAmount;
    
    /**
     * 支付渠道
     */
    @TableField("payment_channel")
    private PaymentChannel paymentChannel;
    
    /**
     * 渠道交易号（64位字符）
     */
    @TableField("channel_transaction_number")
    private String channelTransactionNumber;
    
    /**
     * 支付方式
     */
    @TableField("payment_way")
    private String paymentWay;
    
    /**
     * 原交易流水号（退款时使用，关联到被退款的支付流水）
     */
    @TableField("original_transaction_id")
    private String originalTransactionId;
    
    /**
     * 业务单号（如退款单号）
     */
    @TableField("business_order_id")
    private String businessOrderId;
    
    /**
     * 业务备注
     */
    @TableField("business_remark")
    private String businessRemark;
    
    /**
     * 删除状态（0-正常，1-删除）
     */
    @TableField("del_flag")
    private Integer delFlag;
    
    /**
     * 创建人ID
     */
    @TableField("create_by")
    private String createBy;
    
    /**
     * 创建人姓名
     */
    @TableField("create_by_name")
    private String createByName;
    
    /**
     * 创建时间
     */
    @TableField("created_time")
    private LocalDateTime createdTime;
    
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
     * 更新人ID
     */
    @TableField("update_by")
    private String updateBy;
    
    /**
     * 更新人姓名
     */
    @TableField("update_by_name")
    private String updateByName;
    
    /**
     * 更新时间
     */
    @TableField("updated_time")
    private LocalDateTime updatedTime;
}

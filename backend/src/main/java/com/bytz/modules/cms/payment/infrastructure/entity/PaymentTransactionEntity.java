package com.bytz.modules.cms.payment.infrastructure.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.FieldStrategy;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
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
 * 支付流水数据库实体
 * Payment Transaction Database Entity
 * 
 * 对应数据库表 cms_payment_transaction
 * 注意：这是数据库实体，仅用于数据持久化，不包含业务逻辑
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("cms_payment_transaction")
public class PaymentTransactionEntity {
    
    /**
     * 主键ID，使用雪花算法生成
     */
    @TableId(type = IdType.ASSIGN_ID)
    private String id;
    
    /**
     * 流水号，业务编码（不可修改）
     */
    @TableField(value = "code", updateStrategy = FieldStrategy.NEVER)
    private String code;
    
    /**
     * 支付单ID，外键关联（不可修改）
     */
    @TableField(value = "payment_id", updateStrategy = FieldStrategy.NEVER)
    private String paymentId;
    
    /**
     * 流水类型（不可修改）
     */
    @TableField(value = "transaction_type", updateStrategy = FieldStrategy.NEVER)
    private TransactionType transactionType;
    
    /**
     * 流水状态
     */
    @TableField("transaction_status")
    private TransactionStatus transactionStatus;
    
    /**
     * 交易金额（不可修改）
     */
    @TableField(value = "transaction_amount", updateStrategy = FieldStrategy.NEVER)
    private BigDecimal transactionAmount;
    
    /**
     * 支付渠道（不可修改）
     */
    @TableField(value = "payment_channel", updateStrategy = FieldStrategy.NEVER)
    private PaymentChannel paymentChannel;
    
    /**
     * 渠道交易号
     */
    @TableField("channel_transaction_number")
    private String channelTransactionNumber;
    
    /**
     * 支付方式（不可修改）
     */
    @TableField(value = "payment_way", updateStrategy = FieldStrategy.NEVER)
    private String paymentWay;
    
    /**
     * 原流水ID（退款时使用，不可修改）
     */
    @TableField(value = "original_transaction_id", updateStrategy = FieldStrategy.NEVER)
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
     * 完成时间
     */
    @TableField("complete_date_time")
    private LocalDateTime completeDateTime;
    
    /**
     * 过期时间
     */
    @TableField("expiration_time")
    private LocalDateTime expirationTime;
    
    /**
     * 业务备注
     */
    @TableField("business_remark")
    private String businessRemark;
    
    /**
     * 删除标识
     */
    @TableLogic
    @TableField(value = "del_flag", fill = FieldFill.INSERT)
    private Integer delFlag;
    
    /**
     * 创建人
     */
    @TableField(value = "create_by", fill = FieldFill.INSERT)
    private String createBy;
    
    /**
     * 创建人姓名
     */
    @TableField(value = "create_by_name", fill = FieldFill.INSERT)
    private String createByName;
    
    /**
     * 更新人
     */
    @TableField(value = "update_by", fill = FieldFill.INSERT_UPDATE)
    private String updateBy;
    
    /**
     * 更新人姓名
     */
    @TableField(value = "update_by_name", fill = FieldFill.INSERT_UPDATE)
    private String updateByName;
    
    /**
     * 更新时间
     */
    @TableField(value = "update_time", fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}

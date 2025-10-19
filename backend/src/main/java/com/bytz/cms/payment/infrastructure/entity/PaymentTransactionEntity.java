package com.bytz.cms.payment.infrastructure.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
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
     * 流水号，主键
     */
    @TableId(type = IdType.ASSIGN_ID)
    private String id;
    
    /**
     * 支付单号
     */
    @TableField("payment_id")
    private String paymentId;
    
    /**
     * 流水类型
     */
    @TableField("transaction_type")
    private String transactionType;
    
    /**
     * 流水状态
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
     * 原流水号
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
    @TableField("create_time")
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
    @TableField("del_flag")
    private Integer delFlag;
    
    /**
     * 创建人
     */
    @TableField("create_by")
    private String createBy;
    
    /**
     * 创建人姓名
     */
    @TableField("create_by_name")
    private String createByName;
    
    /**
     * 更新人
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
    @TableField("update_time")
    private LocalDateTime updateTime;
}

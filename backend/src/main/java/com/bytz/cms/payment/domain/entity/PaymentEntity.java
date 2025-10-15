package com.bytz.cms.payment.domain.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.bytz.cms.payment.shared.enums.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 支付单实体
 * Payment Entity
 * 
 * 术语来源：Glossary.md - 核心业务术语"支付单(Payment)"
 * 数据库表：cms_payment
 * 需求来源：需求文档4.4.1节、4.4.3节
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("cms_payment")
public class PaymentEntity implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    /**
     * 支付单号，主键（32位字符，全局唯一）
     */
    @TableId(value = "id", type = IdType.INPUT)
    private String id;
    
    /**
     * 关联订单号（32位字符，可选）
     */
    @TableField("order_id")
    private String orderId;
    
    /**
     * 经销商ID（32位字符，必填）
     */
    @TableField("reseller_id")
    private String resellerId;
    
    /**
     * 目标支付金额（6位小数）
     */
    @TableField("payment_amount")
    private BigDecimal paymentAmount;
    
    /**
     * 已支付金额（6位小数）
     */
    @TableField("paid_amount")
    private BigDecimal paidAmount;
    
    /**
     * 已退款金额（6位小数）
     */
    @TableField("refunded_amount")
    private BigDecimal refundedAmount;
    
    /**
     * 实际收款金额（6位小数，计算公式：paidAmount - refundedAmount）
     */
    @TableField("actual_amount")
    private BigDecimal actualAmount;
    
    /**
     * 币种（固定为CNY人民币）
     */
    @TableField("currency")
    private String currency;
    
    /**
     * 支付类型
     */
    @TableField("payment_type")
    private PaymentType paymentType;
    
    /**
     * 支付状态
     */
    @TableField("payment_status")
    private PaymentStatus paymentStatus;
    
    /**
     * 退款状态
     */
    @TableField("refund_status")
    private RefundStatus refundStatus;
    
    /**
     * 支付截止时间
     */
    @TableField("payment_deadline")
    private LocalDateTime paymentDeadline;
    
    /**
     * 优先级（1-高，2-中，3-低）
     */
    @TableField("priority_level")
    private Integer priorityLevel;
    
    /**
     * 关联业务ID
     */
    @TableField("related_business_id")
    private String relatedBusinessId;
    
    /**
     * 关联业务类型
     */
    @TableField("related_business_type")
    private RelatedBusinessType relatedBusinessType;
    
    /**
     * 关联业务到期日
     */
    @TableField("business_expire_date")
    private LocalDateTime businessExpireDate;
    
    /**
     * 业务描述
     */
    @TableField("business_desc")
    private String businessDesc;
    
    /**
     * 业务标签（JSON字符串）
     */
    @TableField("business_tags")
    private String businessTags;
    
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

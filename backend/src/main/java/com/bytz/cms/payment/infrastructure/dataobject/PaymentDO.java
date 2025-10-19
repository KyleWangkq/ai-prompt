package com.bytz.cms.payment.infrastructure.dataobject;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 支付单数据对象
 * 
 * <p>对应数据库表：cms_payment</p>
 * <p>负责支付单数据的持久化</p>
 * 
 * <p>术语来源：Glossary.md - 数据对象术语"支付单数据对象(PaymentDO)"</p>
 * <p>需求来源：需求文档4.4.1节、4.4.3节</p>
 * 
 * @author DDD设计团队
 * @version 1.0.0
 * @since 2025-01-15
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@TableName("cms_payment")
public class PaymentDO {
    
    /**
     * 支付单号，主键
     */
    @TableId
    private String id;
    
    /**
     * 关联订单号
     */
    @TableField("order_id")
    private String orderId;
    
    /**
     * 经销商ID
     */
    @TableField("reseller_id")
    private String resellerId;
    
    /**
     * 支付金额
     */
    @TableField("payment_amount")
    private BigDecimal paymentAmount;
    
    /**
     * 已支付金额
     */
    @TableField("paid_amount")
    private BigDecimal paidAmount;
    
    /**
     * 已退款金额
     */
    @TableField("refunded_amount")
    private BigDecimal refundedAmount;
    
    /**
     * 实际收款金额
     */
    @TableField("actual_amount")
    private BigDecimal actualAmount;
    
    /**
     * 币种
     */
    @TableField("currency")
    private String currency;
    
    /**
     * 支付类型
     */
    @TableField("payment_type")
    private String paymentType;
    
    /**
     * 支付状态
     */
    @TableField("payment_status")
    private String paymentStatus;
    
    /**
     * 退款状态
     */
    @TableField("refund_status")
    private String refundStatus;
    
    /**
     * 业务描述
     */
    @TableField("business_desc")
    private String businessDesc;
    
    /**
     * 支付截止时间
     */
    @TableField("payment_deadline")
    private LocalDateTime paymentDeadline;
    
    /**
     * 优先级
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
    private String relatedBusinessType;
    
    /**
     * 业务到期日
     */
    @TableField("business_expire_date")
    private LocalDateTime businessExpireDate;
    
    /**
     * 业务标签
     */
    @TableField("business_tags")
    private String businessTags;
    
    /**
     * 删除状态（0-正常，1-删除）
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
     * 创建时间
     */
    @TableField("create_time")
    private LocalDateTime createTime;
    
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

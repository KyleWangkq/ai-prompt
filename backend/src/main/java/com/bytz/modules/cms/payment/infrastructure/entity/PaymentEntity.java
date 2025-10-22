package com.bytz.modules.cms.payment.infrastructure.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import com.bytz.modules.cms.payment.domain.enums.PaymentStatus;
import com.bytz.modules.cms.payment.domain.enums.PaymentType;
import com.bytz.modules.cms.payment.domain.enums.RefundStatus;
import com.bytz.modules.cms.payment.domain.enums.RelatedBusinessType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 支付单数据库实体
 * Payment Database Entity
 * 
 * 对应数据库表 cms_payment
 * 注意：这是数据库实体，仅用于数据持久化，不包含业务逻辑
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("cms_payment")
public class PaymentEntity {
    
    /**
     * 主键ID，使用雪花算法生成
     */
    @TableId(type = IdType.ASSIGN_ID)
    private String id;
    
    /**
     * 支付单号，业务编码
     */
    @TableField("code")
    private String code;
    
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
     * 业务到期日
     */
    @TableField("business_expire_date")
    private LocalDateTime businessExpireDate;
    
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
     * 创建时间
     */
    @TableField(value = "create_time", fill = FieldFill.INSERT)
    private LocalDateTime createTime;
    
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

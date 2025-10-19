package com.bytz.cms.payment.domain.command;

import com.bytz.cms.payment.domain.enums.PaymentType;
import com.bytz.cms.payment.domain.enums.RelatedBusinessType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 创建支付单命令
 * Create Payment Command
 * 
 * 当创建支付单方法参数超过3个时，使用此命令对象封装参数
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreatePaymentCommand {
    
    /**
     * 关联订单号（必填）
     */
    private String orderId;
    
    /**
     * 经销商ID（必填）
     */
    private String resellerId;
    
    /**
     * 支付金额（必填）
     */
    private BigDecimal paymentAmount;
    
    /**
     * 币种（可选，默认CNY）
     */
    private String currency;
    
    /**
     * 支付类型（必填）
     */
    private PaymentType paymentType;
    
    /**
     * 业务描述（可选）
     */
    private String businessDesc;
    
    /**
     * 支付截止时间（可选）
     */
    private LocalDateTime paymentDeadline;
    
    /**
     * 优先级（可选，1-高，2-中，3-低）
     */
    private Integer priorityLevel;
    
    /**
     * 关联业务ID（可选）
     */
    private String relatedBusinessId;
    
    /**
     * 关联业务类型（可选）
     */
    private RelatedBusinessType relatedBusinessType;
    
    /**
     * 业务到期日（可选）
     */
    private LocalDateTime businessExpireDate;
    
    /**
     * 业务标签（可选，JSON格式）
     */
    private String businessTags;
    
    /**
     * 创建人（可选）
     */
    private String createBy;
    
    /**
     * 创建人姓名（可选）
     */
    private String createByName;
}

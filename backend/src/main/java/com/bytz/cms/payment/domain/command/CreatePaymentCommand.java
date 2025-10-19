package com.bytz.cms.payment.domain.command;

import com.bytz.cms.payment.domain.enums.PaymentType;
import com.bytz.cms.payment.domain.enums.RelatedBusinessType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 创建支付单命令
 * 
 * <p>封装创建支付单所需的所有参数，支持普通支付单和信用还款支付单</p>
 * 
 * <p>用例来源：UC-PM-001接收支付单创建请求、UC-PM-007创建信用还款支付单</p>
 * 
 * @author DDD设计团队
 * @version 1.0.0
 * @since 2025-01-15
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreatePaymentCommand {
    
    /**
     * 关联订单号（32位字符，可选，信用还款支付单可能无订单号）
     */
    private String orderId;
    
    /**
     * 经销商ID（买方标识，32位字符，必填）
     */
    private String resellerId;
    
    /**
     * 目标支付金额（6位小数，必须为正数）
     */
    private BigDecimal paymentAmount;
    
    /**
     * 支付类型枚举：ADVANCE_PAYMENT(预付款)/FINAL_PAYMENT(尾款)/OTHER_PAYMENT(其他费用)/CREDIT_REPAYMENT(信用还款)
     */
    private PaymentType paymentType;
    
    /**
     * 支付截止时间（可选，建议的支付完成时间）
     */
    private LocalDateTime paymentDeadline;
    
    /**
     * 优先级（可选，1-高，2-中，3-低）
     */
    private Integer priorityLevel;
    
    /**
     * 关联业务ID（32位字符，可选，如信用记录ID、提货单ID等）
     */
    private String relatedBusinessId;
    
    /**
     * 关联业务类型枚举（可选，如CREDIT_RECORD/DELIVERY_ORDER/ORDER等）
     */
    private RelatedBusinessType relatedBusinessType;
    
    /**
     * 关联业务到期日（可选，如信用还款到期日、提货到期日等）
     */
    private LocalDateTime businessExpireDate;
    
    /**
     * 业务描述（500位字符，可选，支付用途说明）
     */
    private String businessDesc;
    
    /**
     * 业务标签（JSON字符串，可选，相关业务标记）
     */
    private String businessTags;
    
    /**
     * 创建人ID（32位字符）
     */
    private String createBy;
    
    /**
     * 创建人姓名（32位字符）
     */
    private String createByName;
}

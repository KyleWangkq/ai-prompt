package com.bytz.modules.cms.payment.interfaces.model;

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
import java.util.List;

/**
 * 支付单响应对象
 * Payment Response Object
 * 
 * 用于返回支付单的完整信息，包括支付流水
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaymentVO {
    
    /**
     * 支付单号
     */
    private String code;
    
    /**
     * 关联订单号
     */
    private String orderId;
    
    /**
     * 经销商ID
     */
    private String resellerId;
    
    /**
     * 支付金额
     */
    private BigDecimal paymentAmount;
    
    /**
     * 已支付金额
     */
    private BigDecimal paidAmount;
    
    /**
     * 已退款金额
     */
    private BigDecimal refundedAmount;
    
    /**
     * 实际收款金额
     */
    private BigDecimal actualAmount;
    
    /**
     * 待支付金额
     */
    private BigDecimal pendingAmount;
    
    /**
     * 币种
     */
    private String currency;
    
    /**
     * 支付类型
     */
    private PaymentType paymentType;
    
    /**
     * 支付状态
     */
    private PaymentStatus paymentStatus;
    
    /**
     * 退款状态
     */
    private RefundStatus refundStatus;
    
    /**
     * 业务描述
     */
    private String businessDesc;
    
    /**
     * 支付截止时间
     */
    private LocalDateTime paymentDeadline;
    
    /**
     * 关联业务ID
     */
    private String relatedBusinessId;
    
    /**
     * 关联业务类型
     */
    private RelatedBusinessType relatedBusinessType;
    
    /**
     * 业务到期日
     */
    private LocalDateTime businessExpireDate;
    
    /**
     * 创建时间
     */
    private LocalDateTime createTime;
    
    /**
     * 更新时间
     */
    private LocalDateTime updateTime;
    
    /**
     * 支付流水列表
     */
    private List<PaymentTransactionVO> transactions;
}

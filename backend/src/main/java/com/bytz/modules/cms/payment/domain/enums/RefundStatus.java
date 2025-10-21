package com.bytz.modules.cms.payment.domain.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;

/**
 * 退款状态枚举
 * Refund Status Enumeration
 * 
 * 定义支付单的退款状态
 */
public enum RefundStatus {
    
    /**
     * 未退款 - 支付单未发生任何退款
     */
    NO_REFUND("NO_REFUND", "未退款", "No Refund"),
    
    /**
     * 退款中 - 退款请求已发起，等待退款渠道确认
     */
    REFUNDING("REFUNDING", "退款中", "Refunding"),
    
    /**
     * 部分退款 - 支付单已发生部分退款，仍有金额未退款
     */
    PARTIAL_REFUNDED("PARTIAL_REFUNDED", "部分退款", "Partial Refunded"),
    
    /**
     * 全额退款 - 支付单已全额退款
     */
    FULL_REFUNDED("FULL_REFUNDED", "全额退款", "Full Refunded"),
    
    /**
     * 退款失败 - 退款过程中出现错误，退款未成功
     */
    REFUND_FAILED("REFUND_FAILED", "退款失败", "Refund Failed");
    
    @EnumValue
    private final String code;
    private final String description;
    private final String englishName;
    
    RefundStatus(String code, String description, String englishName) {
        this.code = code;
        this.description = description;
        this.englishName = englishName;
    }
    
    public String getCode() {
        return code;
    }
    
    public String getDescription() {
        return description;
    }
    
    public String getEnglishName() {
        return englishName;
    }
}

package com.bytz.modules.cms.payment.domain.enums;

import com.baomidou.mybatisplus.annotation.IEnum;

/**
 * 支付状态枚举
 * Payment Status Enumeration
 * 
 * 定义支付单的各种状态，按照业务流程进行状态转换
 */
public enum PaymentStatus implements IEnum<Integer> {
    
    /**
     * 未支付 - 支付单刚创建，尚未开始支付
     */
    UNPAID(0, "未支付", "Unpaid"),
    
    /**
     * 支付中 - 支付请求已发起，等待支付渠道确认
     */
    PAYING(1, "支付中", "Paying"),
    
    /**
     * 部分支付 - 支付单已支付部分金额，仍有余额待支付
     */
    PARTIAL_PAID(2, "部分支付", "Partial Paid"),
    
    /**
     * 已支付 - 支付单全额支付完成
     */
    PAID(3, "已支付", "Paid"),
    
    /**
     * 支付失败 - 支付过程中出现错误，支付未成功
     */
    FAILED(4, "支付失败", "Failed"),
    
    /**
     * 已停止 - 因业务原因主动停止支付操作
     */
    STOPPED(5, "已停止", "Stopped"),
    
    /**
     * 已冻结 - 支付单因特殊原因暂时无法操作
     */
    FROZEN(6, "已冻结", "Frozen");
    
    private final Integer code;
    private final String description;
    private final String englishName;
    
    PaymentStatus(Integer code, String description, String englishName) {
        this.code = code;
        this.description = description;
        this.englishName = englishName;
    }
    
    @Override
    public Integer getValue() {
        return code;
    }
    
    public Integer getCode() {
        return code;
    }
    
    public String getDescription() {
        return description;
    }
    
    public String getEnglishName() {
        return englishName;
    }
}

package com.bytz.cms.payment.domain.valueobject;

import lombok.Value;

import java.math.BigDecimal;

/**
 * 支付金额值对象
 * Payment Amount Value Object
 * 
 * 封装支付金额及其相关验证逻辑，确保金额的不变性和有效性
 */
@Value
public class PaymentAmount {
    
    /**
     * 金额值，使用BigDecimal确保精度
     */
    BigDecimal amount;
    
    /**
     * 币种，默认为CNY（人民币）
     */
    String currency;
    
    /**
     * 构造函数，创建支付金额值对象
     * 
     * @param amount 金额值
     * @param currency 币种
     * @throws IllegalArgumentException 如果金额无效
     */
    public PaymentAmount(BigDecimal amount, String currency) {
        if (amount == null) {
            throw new IllegalArgumentException("金额不能为空");
        }
        if (amount.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("金额不能为负数");
        }
        if (currency == null || currency.trim().isEmpty()) {
            throw new IllegalArgumentException("币种不能为空");
        }
        
        // 确保金额最多6位小数
        this.amount = amount.setScale(6, BigDecimal.ROUND_HALF_UP);
        this.currency = currency;
    }
    
    /**
     * 创建人民币金额
     * 
     * @param amount 金额值
     * @return 人民币金额值对象
     */
    public static PaymentAmount ofCNY(BigDecimal amount) {
        return new PaymentAmount(amount, "CNY");
    }
    
    /**
     * 创建零金额
     * 
     * @return 零金额值对象
     */
    public static PaymentAmount zero() {
        return new PaymentAmount(BigDecimal.ZERO, "CNY");
    }
    
    /**
     * 金额相加
     * 
     * @param other 另一个金额
     * @return 相加后的新金额对象
     * @throws IllegalArgumentException 如果币种不同
     */
    public PaymentAmount add(PaymentAmount other) {
        if (!this.currency.equals(other.currency)) {
            throw new IllegalArgumentException("不同币种的金额不能相加");
        }
        return new PaymentAmount(this.amount.add(other.amount), this.currency);
    }
    
    /**
     * 金额相减
     * 
     * @param other 另一个金额
     * @return 相减后的新金额对象
     * @throws IllegalArgumentException 如果币种不同或结果为负数
     */
    public PaymentAmount subtract(PaymentAmount other) {
        if (!this.currency.equals(other.currency)) {
            throw new IllegalArgumentException("不同币种的金额不能相减");
        }
        BigDecimal result = this.amount.subtract(other.amount);
        if (result.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("金额相减结果不能为负数");
        }
        return new PaymentAmount(result, this.currency);
    }
    
    /**
     * 比较金额大小
     * 
     * @param other 另一个金额
     * @return 比较结果：-1(小于), 0(等于), 1(大于)
     * @throws IllegalArgumentException 如果币种不同
     */
    public int compareTo(PaymentAmount other) {
        if (!this.currency.equals(other.currency)) {
            throw new IllegalArgumentException("不同币种的金额不能比较");
        }
        return this.amount.compareTo(other.amount);
    }
    
    /**
     * 判断金额是否为零
     * 
     * @return true如果金额为零，否则false
     */
    public boolean isZero() {
        return this.amount.compareTo(BigDecimal.ZERO) == 0;
    }
    
    /**
     * 判断金额是否大于另一个金额
     * 
     * @param other 另一个金额
     * @return true如果当前金额大于指定金额，否则false
     */
    public boolean isGreaterThan(PaymentAmount other) {
        return compareTo(other) > 0;
    }
    
    /**
     * 判断金额是否小于另一个金额
     * 
     * @param other 另一个金额
     * @return true如果当前金额小于指定金额，否则false
     */
    public boolean isLessThan(PaymentAmount other) {
        return compareTo(other) < 0;
    }
    
    /**
     * 判断金额是否等于另一个金额
     * 
     * @param other 另一个金额
     * @return true如果当前金额等于指定金额，否则false
     */
    public boolean isEqualTo(PaymentAmount other) {
        return compareTo(other) == 0;
    }
}

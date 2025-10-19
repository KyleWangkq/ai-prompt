package com.bytz.cms.payment.domain.valueobject;

import lombok.Value;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * 支付金额值对象
 * 
 * <p>封装支付单的金额信息，确保金额的有效性和不变性</p>
 * 
 * <p>术语来源：Glossary.md - 值对象术语"支付金额(PaymentAmount)"</p>
 * <p>需求来源：需求文档4.4.1节</p>
 * 
 * <p>不变式：</p>
 * <ul>
 *   <li>金额必须大于0</li>
 *   <li>金额最多6位小数</li>
 *   <li>值对象不可变</li>
 * </ul>
 * 
 * @author DDD设计团队
 * @version 1.0.0
 * @since 2025-01-15
 */
@Value
public class PaymentAmount {
    
    /**
     * 金额值（最多6位小数）
     */
    BigDecimal amount;

    /**
     * 构造函数，创建支付金额值对象
     * 
     * @param amount 金额值
     * @throws IllegalArgumentException 如果金额无效
     */
    public PaymentAmount(BigDecimal amount) {
        if (amount == null) {
            throw new IllegalArgumentException("支付金额不能为空");
        }
        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("支付金额必须大于0");
        }
        // 保留6位小数
        this.amount = amount.setScale(6, RoundingMode.HALF_UP);
    }

    /**
     * 创建支付金额值对象的工厂方法
     * 
     * @param amount 金额值
     * @return 支付金额值对象
     */
    public static PaymentAmount of(BigDecimal amount) {
        return new PaymentAmount(amount);
    }

    /**
     * 判断金额是否为零
     * 
     * @return true表示金额为零，false表示金额不为零
     */
    public boolean isZero() {
        return amount.compareTo(BigDecimal.ZERO) == 0;
    }

    /**
     * 判断当前金额是否大于指定金额
     * 
     * @param other 待比较的金额
     * @return true表示大于，false表示小于或等于
     */
    public boolean isGreaterThan(PaymentAmount other) {
        return this.amount.compareTo(other.amount) > 0;
    }

    /**
     * 判断当前金额是否大于或等于指定金额
     * 
     * @param other 待比较的金额
     * @return true表示大于或等于，false表示小于
     */
    public boolean isGreaterThanOrEqual(PaymentAmount other) {
        return this.amount.compareTo(other.amount) >= 0;
    }

    /**
     * 加法运算
     * 
     * @param other 待加的金额
     * @return 新的支付金额值对象
     */
    public PaymentAmount add(PaymentAmount other) {
        return new PaymentAmount(this.amount.add(other.amount));
    }

    /**
     * 减法运算
     * 
     * @param other 待减的金额
     * @return 新的支付金额值对象
     */
    public PaymentAmount subtract(PaymentAmount other) {
        BigDecimal result = this.amount.subtract(other.amount);
        if (result.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("减法运算结果不能为负数");
        }
        return new PaymentAmount(result);
    }

    /**
     * 获取金额的BigDecimal表示
     * 
     * @return BigDecimal金额
     */
    public BigDecimal getValue() {
        return amount;
    }
}

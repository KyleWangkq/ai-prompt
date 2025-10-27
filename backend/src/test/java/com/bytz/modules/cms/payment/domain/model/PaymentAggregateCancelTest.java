package com.bytz.modules.cms.payment.domain.model;

import com.bytz.modules.cms.payment.domain.enums.PaymentChannel;
import com.bytz.modules.cms.payment.domain.enums.PaymentStatus;
import com.bytz.modules.cms.payment.domain.enums.PaymentType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 支付单取消功能测试
 * Payment Cancel Feature Test
 */
class PaymentAggregateCancelTest {

    private PaymentAggregate payment;

    @BeforeEach
    void setUp() {
        // 创建一个未支付的支付单用于测试
        payment = PaymentAggregate.create(
                "ORDER001",
                "RESELLER001",
                new BigDecimal("1000.00"),
                "CNY",
                PaymentType.ADVANCE_PAYMENT,
                "测试支付单",
                null,
                null,
                null,
                null
        );
        payment.setId("PAYMENT001");
    }

    @Test
    void testCancel_Success() {
        // 验证初始状态
        assertEquals(PaymentStatus.UNPAID, payment.getPaymentStatus());
        assertTrue(payment.canCancel());

        // 执行取消
        payment.cancel("用户主动取消");

        // 验证取消后的状态
        assertEquals(PaymentStatus.CANCELED, payment.getPaymentStatus());
        assertTrue(payment.getBusinessDesc().contains("取消原因: 用户主动取消"));
        assertNotNull(payment.getUpdateTime());
    }

    @Test
    void testCanCancel_WhenUnpaidWithNoTransactions() {
        assertTrue(payment.canCancel());
    }

    @Test
    void testCannotCancel_WhenStatusIsNotUnpaid() {
        // 修改状态为支付中
        payment.setPaymentStatus(PaymentStatus.PAYING);

        assertFalse(payment.canCancel());

        // 尝试取消应该抛出异常
        IllegalStateException exception = assertThrows(IllegalStateException.class, () -> {
            payment.cancel("测试取消");
        });
        assertTrue(exception.getMessage().contains("只有未支付状态才能取消"));
    }

    @Test
    void testCannotCancel_WhenHasRunningTransaction() {
        // 执行支付，创建运行期流水
        payment.executePayment(PaymentChannel.ONLINE_PAYMENT, new BigDecimal("100.00"));

        assertFalse(payment.canCancel());

        // 尝试取消应该抛出异常
        IllegalStateException exception = assertThrows(IllegalStateException.class, () -> {
            payment.cancel("测试取消");
        });
        assertTrue(exception.getMessage().contains("支付单存在运行中的流水，无法取消"));
    }

    @Test
    void testCannotCancel_WhenHasCompletedTransactions() {
        // 创建一个新的支付单，用于测试已完成流水的场景
        PaymentAggregate paymentWithCompletedTx = PaymentAggregate.create(
                "ORDER002",
                "RESELLER001",
                new BigDecimal("1000.00"),
                "CNY",
                PaymentType.ADVANCE_PAYMENT,
                "测试支付单",
                null,
                null,
                null,
                null
        );
        paymentWithCompletedTx.setId("PAYMENT002");
        
        // 创建支付流水并完成
        paymentWithCompletedTx.executePayment(PaymentChannel.ONLINE_PAYMENT, new BigDecimal("100.00"));
        paymentWithCompletedTx.handlePaymentCallback("TX001", true, null);

        assertFalse(paymentWithCompletedTx.canCancel());

        // 尝试取消应该抛出异常（状态已变为PARTIAL_PAID，不是UNPAID）
        IllegalStateException exception = assertThrows(IllegalStateException.class, () -> {
            paymentWithCompletedTx.cancel("测试取消");
        });
        assertTrue(exception.getMessage().contains("只有未支付状态才能取消"));
    }

    @Test
    void testCannotCancel_WhenHasPaidAmount() {
        // 直接设置已支付金额（模拟异常场景）
        payment.setPaidAmount(new BigDecimal("100.00"));

        // 尝试取消应该抛出异常
        IllegalStateException exception = assertThrows(IllegalStateException.class, () -> {
            payment.cancel("测试取消");
        });
        assertTrue(exception.getMessage().contains("支付单已有支付金额，无法取消"));
    }

    @Test
    void testCancelWithNullReason() {
        // 使用null原因取消
        assertDoesNotThrow(() -> payment.cancel(null));
        assertEquals(PaymentStatus.CANCELED, payment.getPaymentStatus());
    }

    @Test
    void testCancelMultipleTimes() {
        // 第一次取消成功
        payment.cancel("第一次取消");
        assertEquals(PaymentStatus.CANCELED, payment.getPaymentStatus());

        // 第二次取消应该失败
        IllegalStateException exception = assertThrows(IllegalStateException.class, () -> {
            payment.cancel("第二次取消");
        });
        assertTrue(exception.getMessage().contains("只有未支付状态才能取消"));
    }

    @Test
    void testCancelPreservesExistingBusinessDesc() {
        // 设置初始业务描述
        payment.setBusinessDesc("初始业务描述");

        // 执行取消
        payment.cancel("取消原因");

        // 验证业务描述包含原始内容和取消原因
        assertTrue(payment.getBusinessDesc().contains("初始业务描述"));
        assertTrue(payment.getBusinessDesc().contains("取消原因: 取消原因"));
    }
}

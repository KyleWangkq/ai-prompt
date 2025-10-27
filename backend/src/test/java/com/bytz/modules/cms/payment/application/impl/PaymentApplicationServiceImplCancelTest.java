package com.bytz.modules.cms.payment.application.impl;

import com.bytz.modules.cms.payment.application.command.CancelPaymentCommand;
import com.bytz.modules.cms.payment.domain.enums.PaymentStatus;
import com.bytz.modules.cms.payment.domain.enums.PaymentType;
import com.bytz.modules.cms.payment.domain.model.PaymentAggregate;
import com.bytz.modules.cms.payment.domain.repository.IPaymentRepository;
import com.bytz.modules.cms.payment.shared.exception.PaymentException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * 支付单取消应用服务测试
 * Payment Cancel Application Service Test
 */
@ExtendWith(MockitoExtension.class)
class PaymentApplicationServiceImplCancelTest {

    @Mock
    private IPaymentRepository paymentRepository;

    @Mock
    private ApplicationEventPublisher eventPublisher;

    @InjectMocks
    private PaymentApplicationServiceImpl paymentApplicationService;

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
        payment.setCode("PAY001");
    }

    @Test
    void testCancelPayment_Success() {
        // 准备测试数据
        CancelPaymentCommand command = CancelPaymentCommand.builder()
                .paymentId("PAYMENT001")
                .reason("用户主动取消")
                .operatorId("OPERATOR001")
                .operatorName("测试操作员")
                .build();

        // Mock repository 行为
        when(paymentRepository.findById("PAYMENT001")).thenReturn(Optional.of(payment));
        when(paymentRepository.save(any(PaymentAggregate.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // 执行取消
        paymentApplicationService.cancelPayment(command);

        // 验证支付单状态已更新
        assertEquals(PaymentStatus.CANCELED, payment.getPaymentStatus());
        assertTrue(payment.getBusinessDesc().contains("取消原因: 用户主动取消"));
        assertEquals("OPERATOR001", payment.getUpdateBy());
        assertEquals("测试操作员", payment.getUpdateByName());

        // 验证 repository.save 被调用
        verify(paymentRepository, times(1)).save(payment);
    }

    @Test
    void testCancelPayment_PaymentNotFound() {
        // 准备测试数据
        CancelPaymentCommand command = CancelPaymentCommand.builder()
                .paymentId("NONEXISTENT")
                .reason("用户主动取消")
                .build();

        // Mock repository 返回空
        when(paymentRepository.findById("NONEXISTENT")).thenReturn(Optional.empty());

        // 执行取消应该抛出异常
        PaymentException exception = assertThrows(PaymentException.class, () -> {
            paymentApplicationService.cancelPayment(command);
        });

        assertEquals("支付单不存在: NONEXISTENT", exception.getMessage());

        // 验证 repository.save 未被调用
        verify(paymentRepository, never()).save(any());
    }

    @Test
    void testCancelPayment_InvalidCommand_NullPaymentId() {
        // 准备测试数据（paymentId为null）
        CancelPaymentCommand command = CancelPaymentCommand.builder()
                .paymentId(null)
                .reason("用户主动取消")
                .build();

        // 执行取消应该抛出异常
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            paymentApplicationService.cancelPayment(command);
        });

        assertEquals("支付单ID不能为空", exception.getMessage());

        // 验证 repository 未被调用
        verify(paymentRepository, never()).findById(any());
        verify(paymentRepository, never()).save(any());
    }

    @Test
    void testCancelPayment_InvalidCommand_EmptyPaymentId() {
        // 准备测试数据（paymentId为空字符串）
        CancelPaymentCommand command = CancelPaymentCommand.builder()
                .paymentId("   ")
                .reason("用户主动取消")
                .build();

        // 执行取消应该抛出异常
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            paymentApplicationService.cancelPayment(command);
        });

        assertEquals("支付单ID不能为空", exception.getMessage());
    }

    @Test
    void testCancelPayment_StatusNotUnpaid() {
        // 准备测试数据
        payment.setPaymentStatus(PaymentStatus.PAYING);

        CancelPaymentCommand command = CancelPaymentCommand.builder()
                .paymentId("PAYMENT001")
                .reason("尝试取消")
                .build();

        // Mock repository 行为
        when(paymentRepository.findById("PAYMENT001")).thenReturn(Optional.of(payment));

        // 执行取消应该抛出异常
        IllegalStateException exception = assertThrows(IllegalStateException.class, () -> {
            paymentApplicationService.cancelPayment(command);
        });

        assertTrue(exception.getMessage().contains("只有未支付状态才能取消"));

        // 验证 repository.save 未被调用
        verify(paymentRepository, never()).save(any());
    }

    @Test
    void testCancelPayment_WithoutReason() {
        // 准备测试数据（没有取消原因）
        CancelPaymentCommand command = CancelPaymentCommand.builder()
                .paymentId("PAYMENT001")
                .reason(null)
                .build();

        // Mock repository 行为
        when(paymentRepository.findById("PAYMENT001")).thenReturn(Optional.of(payment));
        when(paymentRepository.save(any(PaymentAggregate.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // 执行取消
        paymentApplicationService.cancelPayment(command);

        // 验证使用默认原因
        assertTrue(payment.getBusinessDesc().contains("取消原因: 用户主动取消"));
        assertEquals(PaymentStatus.CANCELED, payment.getPaymentStatus());
    }

    @Test
    void testCancelPayment_WithoutOperatorInfo() {
        // 准备测试数据（没有操作人信息）
        CancelPaymentCommand command = CancelPaymentCommand.builder()
                .paymentId("PAYMENT001")
                .reason("取消订单")
                .build();

        // Mock repository 行为
        when(paymentRepository.findById("PAYMENT001")).thenReturn(Optional.of(payment));
        when(paymentRepository.save(any(PaymentAggregate.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // 执行取消
        paymentApplicationService.cancelPayment(command);

        // 验证操作人信息未设置
        assertNull(payment.getUpdateBy());
        assertNull(payment.getUpdateByName());
        assertEquals(PaymentStatus.CANCELED, payment.getPaymentStatus());
    }
}

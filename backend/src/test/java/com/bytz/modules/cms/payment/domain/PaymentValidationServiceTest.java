package com.bytz.modules.cms.payment.domain;

import com.bytz.modules.cms.payment.domain.enums.PaymentType;
import com.bytz.modules.cms.payment.domain.enums.RelatedBusinessType;
import com.bytz.modules.cms.payment.domain.model.PaymentAggregate;
import com.bytz.modules.cms.payment.domain.enums.PaymentStatus;
import com.bytz.modules.cms.payment.domain.enums.RefundStatus;
import com.bytz.modules.cms.payment.shared.exception.PaymentException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * PaymentValidationService 单元测试
 */
@DisplayName("PaymentValidationService 单元测试")
class PaymentValidationServiceTest {

    private PaymentValidationService validationService;

    @BeforeEach
    void setUp() {
        validationService = new PaymentValidationService();
    }

    @Test
    @DisplayName("校验创建请求 - 正常流程")
    void testValidateCreate_Success() {
        assertDoesNotThrow(() -> {
            validationService.validateCreate(
                    "ORDER-001",
                    "RESELLER-001",
                    new BigDecimal("10000.00"),
                    PaymentType.ADVANCE_PAYMENT,
                    null,
                    null
            );
        });
    }

    @Test
    @DisplayName("校验创建请求 - 订单号为空")
    void testValidateCreate_NullOrderId() {
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> validationService.validateCreate(
                        null,
                        "RESELLER-001",
                        new BigDecimal("10000.00"),
                        PaymentType.ADVANCE_PAYMENT,
                        null,
                        null
                )
        );
        assertTrue(exception.getMessage().contains("订单号不能为空"));
    }

    @Test
    @DisplayName("校验创建请求 - 经销商ID为空")
    void testValidateCreate_NullResellerId() {
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> validationService.validateCreate(
                        "ORDER-001",
                        null,
                        new BigDecimal("10000.00"),
                        PaymentType.ADVANCE_PAYMENT,
                        null,
                        null
                )
        );
        assertTrue(exception.getMessage().contains("经销商ID不能为空"));
    }

    @Test
    @DisplayName("校验创建请求 - 金额小于等于0")
    void testValidateCreate_InvalidAmount() {
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> validationService.validateCreate(
                        "ORDER-001",
                        "RESELLER-001",
                        BigDecimal.ZERO,
                        PaymentType.ADVANCE_PAYMENT,
                        null,
                        null
                )
        );
        assertTrue(exception.getMessage().contains("支付金额必须大于0"));
    }

    @Test
    @DisplayName("校验创建请求 - 信用还款缺少关联业务ID")
    void testValidateCreate_CreditRepaymentMissingBusinessId() {
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> validationService.validateCreate(
                        "ORDER-001",
                        "RESELLER-001",
                        new BigDecimal("10000.00"),
                        PaymentType.CREDIT_REPAYMENT,
                        null,
                        null
                )
        );
        assertTrue(exception.getMessage().contains("信用还款支付单必须提供关联业务ID"));
    }

    @Test
    @DisplayName("校验创建请求 - 信用还款业务类型不正确")
    void testValidateCreate_CreditRepaymentInvalidBusinessType() {
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> validationService.validateCreate(
                        "ORDER-001",
                        "RESELLER-001",
                        new BigDecimal("10000.00"),
                        PaymentType.CREDIT_REPAYMENT,
                        "CREDIT-001",
                        RelatedBusinessType.ORDER
                )
        );
        assertTrue(exception.getMessage().contains("关联业务类型必须为CREDIT_RECORD"));
    }

    @Test
    @DisplayName("校验执行支付 - 正常流程")
    void testValidateExecute_Success() {
        PaymentAggregate payment = PaymentAggregate.builder()
                .code("PAY-001")
                .resellerId("RESELLER-001")
                .paymentAmount(new BigDecimal("10000.00"))
                .paidAmount(BigDecimal.ZERO)
                .refundedAmount(BigDecimal.ZERO)
                .paymentStatus(PaymentStatus.UNPAID)
                .runningTransaction(null)
                .completedTransactions(new ArrayList<>())
                .build();

        assertDoesNotThrow(() -> {
            validationService.validateExecute(Arrays.asList(payment));
        });
    }

    @Test
    @DisplayName("校验执行支付 - 不同经销商异常")
    void testValidateExecute_DifferentReseller() {
        PaymentAggregate payment1 = PaymentAggregate.builder()
                .code("PAY-001")
                .resellerId("RESELLER-001")
                .paymentAmount(new BigDecimal("10000.00"))
                .paidAmount(BigDecimal.ZERO)
                .paymentStatus(PaymentStatus.UNPAID)
                .runningTransaction(null)
                .completedTransactions(new ArrayList<>())
                .build();

        PaymentAggregate payment2 = PaymentAggregate.builder()
                .code("PAY-002")
                .resellerId("RESELLER-002")  // 不同的经销商
                .paymentAmount(new BigDecimal("10000.00"))
                .paidAmount(BigDecimal.ZERO)
                .paymentStatus(PaymentStatus.UNPAID)
                .runningTransaction(null)
                .completedTransactions(new ArrayList<>())
                .build();

        PaymentException exception = assertThrows(
                PaymentException.class,
                () -> validationService.validateExecute(Arrays.asList(payment1, payment2))
        );
        assertTrue(exception.getMessage().contains("同一经销商"));
    }

    @Test
    @DisplayName("校验金额分配 - 正常流程")
    void testValidateAmountAllocation_Success() {
        PaymentAggregate payment = PaymentAggregate.builder()
                .code("PAY-001")
                .paymentAmount(new BigDecimal("10000.00"))
                .paidAmount(BigDecimal.ZERO)
                .build();

        List<BigDecimal> allocatedAmounts = Arrays.asList(new BigDecimal("10000.00"));
        BigDecimal totalAmount = new BigDecimal("10000.00");

        assertDoesNotThrow(() -> {
            validationService.validateAmountAllocation(
                    Arrays.asList(payment),
                    allocatedAmounts,
                    totalAmount
            );
        });
    }

    @Test
    @DisplayName("校验金额分配 - 分配金额超过待支付金额")
    void testValidateAmountAllocation_ExceedsPending() {
        PaymentAggregate payment = PaymentAggregate.builder()
                .code("PAY-001")
                .paymentAmount(new BigDecimal("10000.00"))
                .paidAmount(BigDecimal.ZERO)
                .build();

        List<BigDecimal> allocatedAmounts = Arrays.asList(new BigDecimal("15000.00"));
        BigDecimal totalAmount = new BigDecimal("15000.00");

        PaymentException exception = assertThrows(
                PaymentException.class,
                () -> validationService.validateAmountAllocation(
                        Arrays.asList(payment),
                        allocatedAmounts,
                        totalAmount
                )
        );
        assertTrue(exception.getMessage().contains("超过待支付金额"));
    }

    @Test
    @DisplayName("校验退款 - 正常流程")
    void testValidateRefund_Success() {
        PaymentAggregate payment = PaymentAggregate.builder()
                .code("PAY-001")
                .paidAmount(new BigDecimal("10000.00"))
                .refundedAmount(BigDecimal.ZERO)
                .paymentStatus(PaymentStatus.PAID)
                .refundStatus(RefundStatus.NO_REFUND)
                .runningTransaction(null)
                .completedTransactions(new ArrayList<>())
                .build();

        assertDoesNotThrow(() -> {
            validationService.validateRefund(payment, new BigDecimal("5000.00"));
        });
    }

    @Test
    @DisplayName("校验退款 - 退款金额超过可退款金额")
    void testValidateRefund_ExceedsRefundable() {
        PaymentAggregate payment = PaymentAggregate.builder()
                .code("PAY-001")
                .paidAmount(new BigDecimal("10000.00"))
                .refundedAmount(BigDecimal.ZERO)
                .paymentStatus(PaymentStatus.PAID)
                .refundStatus(RefundStatus.NO_REFUND)
                .runningTransaction(null)
                .completedTransactions(new ArrayList<>())
                .build();

        PaymentException exception = assertThrows(
                PaymentException.class,
                () -> validationService.validateRefund(payment, new BigDecimal("15000.00"))
        );
        assertTrue(exception.getMessage().contains("超过可退款金额"));
    }
}

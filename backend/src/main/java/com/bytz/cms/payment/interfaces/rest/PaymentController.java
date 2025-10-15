package com.bytz.cms.payment.interfaces.rest;

import com.bytz.cms.payment.application.dto.CreatePaymentRequest;
import com.bytz.cms.payment.application.dto.ExecutePaymentRequest;
import com.bytz.cms.payment.application.dto.PaymentResponse;
import com.bytz.cms.payment.application.service.PaymentApplicationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Payment REST Controller
 * 支付单REST API控制器
 */
@RestController
@RequestMapping("/api/v1/payments")
@RequiredArgsConstructor
@Slf4j
@Validated
public class PaymentController {
    
    private final PaymentApplicationService paymentApplicationService;
    
    /**
     * 创建支付单（UC-PM-001）
     */
    @PostMapping
    public ResponseEntity<PaymentResponse> createPayment(@Valid @RequestBody CreatePaymentRequest request) {
        log.info("接收创建支付单请求: resellerId={}", request.getResellerId());
        PaymentResponse response = paymentApplicationService.createPayment(request);
        return ResponseEntity.ok(response);
    }
    
    /**
     * 执行支付（UC-PM-003）
     */
    @PostMapping("/execute")
    public ResponseEntity<String> executePayment(@Valid @RequestBody ExecutePaymentRequest request) {
        log.info("接收执行支付请求: paymentCount={}", request.getPaymentItems().size());
        String channelTransactionNumber = paymentApplicationService.executePayment(request);
        return ResponseEntity.ok(channelTransactionNumber);
    }
    
    /**
     * 查询支付单详情（UC-PM-005）
     */
    @GetMapping("/{paymentId}")
    public ResponseEntity<PaymentResponse> getPaymentDetail(@PathVariable String paymentId) {
        log.info("查询支付单详情: paymentId={}", paymentId);
        PaymentResponse response = paymentApplicationService.getPaymentDetail(paymentId);
        return ResponseEntity.ok(response);
    }
    
    /**
     * 筛选支付单（UC-PM-002）
     */
    @GetMapping("/filter")
    public ResponseEntity<List<PaymentResponse>> filterPayments(
            @RequestParam(required = false) String resellerId,
            @RequestParam(required = false) List<String> statuses,
            @RequestParam(required = false) List<String> types,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startTime,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endTime) {
        
        log.info("筛选支付单: resellerId={}, statuses={}", resellerId, statuses);
        List<PaymentResponse> responses = paymentApplicationService.filterPayments(
                resellerId, statuses, types, startTime, endTime);
        return ResponseEntity.ok(responses);
    }
    
    /**
     * 支付回调（UC-PM-004）
     * 注意：实际生产环境中需要验证回调签名
     */
    @PostMapping("/callback/{transactionId}")
    public ResponseEntity<Void> paymentCallback(
            @PathVariable String transactionId,
            @RequestParam boolean success,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime completedAt) {
        
        log.info("接收支付回调: transactionId={}, success={}", transactionId, success);
        paymentApplicationService.processPaymentCallback(
                transactionId, success, completedAt != null ? completedAt : LocalDateTime.now());
        return ResponseEntity.ok().build();
    }
}

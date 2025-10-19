package com.bytz.cms.payment.interfaces;

import com.bytz.cms.payment.application.PaymentApplicationService;
import com.bytz.cms.payment.domain.enums.PaymentStatus;
import com.bytz.cms.payment.domain.enums.PaymentType;
import com.bytz.cms.payment.interfaces.model.PaymentCreateRO;
import com.bytz.cms.payment.interfaces.model.PaymentVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 支付单控制器
 * 
 * <p>提供支付单相关的REST API</p>
 * <p>相关用例：UC-PM-001、UC-PM-002、UC-PM-005、UC-PM-007</p>
 * 
 * @author DDD设计团队
 * @version 1.0.0
 * @since 2025-01-15
 */
@Slf4j
@RestController
@RequestMapping("/api/payments")
public class PaymentController {
    
    @Resource
    private PaymentApplicationService paymentApplicationService;
    
    /**
     * 创建支付单（UC-PM-001、UC-PM-007）
     * 
     * @param request 创建支付单请求
     * @return 支付单响应
     */
    @PostMapping
    public ResponseEntity<PaymentVO> createPayment(@Validated @RequestBody PaymentCreateRO request) {
        log.info("接收创建支付单请求，经销商ID：{}, 支付金额：{}, 支付类型：{}", 
                request.getResellerId(), request.getPaymentAmount(), request.getPaymentType());
        
        PaymentVO payment = paymentApplicationService.createPayment(request);
        
        return ResponseEntity.ok(payment);
    }
    
    /**
     * 查询支付单详情（UC-PM-005）
     * 
     * @param paymentId 支付单号
     * @return 支付单详情
     */
    @GetMapping("/{paymentId}")
    public ResponseEntity<PaymentVO> getPaymentDetail(@PathVariable String paymentId) {
        log.info("查询支付单详情，支付单号：{}", paymentId);
        
        PaymentVO payment = paymentApplicationService.getPaymentDetail(paymentId);
        
        return ResponseEntity.ok(payment);
    }
    
    /**
     * 筛选支付单（UC-PM-002）
     * 
     * @param resellerId 经销商ID
     * @param paymentStatus 支付状态
     * @param paymentType 支付类型
     * @param startTime 创建开始时间
     * @param endTime 创建结束时间
     * @return 支付单列表
     */
    @GetMapping
    public ResponseEntity<List<PaymentVO>> filterPayments(
            @RequestParam String resellerId,
            @RequestParam(required = false) PaymentStatus paymentStatus,
            @RequestParam(required = false) PaymentType paymentType,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startTime,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endTime) {
        
        log.info("筛选支付单，经销商ID：{}, 支付状态：{}, 支付类型：{}", 
                resellerId, paymentStatus, paymentType);
        
        List<PaymentVO> payments = paymentApplicationService.filterPayments(
                resellerId, paymentStatus, paymentType, startTime, endTime);
        
        return ResponseEntity.ok(payments);
    }
}

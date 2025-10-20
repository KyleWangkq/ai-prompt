package com.bytz.cms.payment.interfaces.controller;

import com.bytz.cms.payment.application.PaymentApplicationService;
import com.bytz.cms.payment.application.assembler.PaymentAssembler;
import com.bytz.cms.payment.domain.command.CreatePaymentCommand;
import com.bytz.cms.payment.domain.model.PaymentAggregate;
import com.bytz.cms.payment.interfaces.model.PaymentCreateRO;
import com.bytz.cms.payment.interfaces.model.PaymentVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

/**
 * 支付单控制器
 * Payment Controller
 * 
 * 提供支付单相关的REST API接口
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/payments")
@RequiredArgsConstructor
@Validated
public class PaymentController {
    
    private final PaymentApplicationService paymentApplicationService;
    private final PaymentAssembler paymentAssembler;
    
    /**
     * 创建支付单
     * 
     * ⚠️ 已废弃：支付单创建应该由系统内部调用，不应该通过外部REST接口创建
     * 请使用 PaymentApplicationService.createPayment() 方法在系统内部创建支付单
     * 
     * @deprecated 支付单创建来源于系统内部，不通过接口创建
     */
    @Deprecated
    @PostMapping
    public ResponseEntity<PaymentVO> createPayment(@Valid @RequestBody PaymentCreateRO ro) {
        log.warn("警告：通过REST接口创建支付单已废弃，支付单应该由系统内部创建");
        log.info("收到创建支付单请求，订单号: {}, 支付类型: {}", ro.getOrderId(), ro.getPaymentType());
        
        // 转换为命令对象
        CreatePaymentCommand command = paymentAssembler.toCreateCommand(ro);
        
        // 调用应用服务
        PaymentAggregate payment = paymentApplicationService.createPayment(command);
        
        // 转换为响应对象
        PaymentVO vo = paymentAssembler.toVO(payment);
        
        log.info("支付单创建成功，支付单号: {}", vo.getId());
        return ResponseEntity.ok(vo);
    }
    
    /**
     * 根据支付单号查询支付单
     * 
     * GET /api/v1/payments/{id}
     * 
     * @param id 支付单号
     * @return 支付单响应对象
     */
    @GetMapping("/{id}")
    public ResponseEntity<PaymentVO> getPaymentById(@PathVariable String id) {
        log.info("查询支付单，支付单号: {}", id);
        
        PaymentAggregate payment = paymentApplicationService.getPaymentById(id);
        if (payment == null) {
            return ResponseEntity.notFound().build();
        }
        
        PaymentVO vo = paymentAssembler.toVO(payment);
        return ResponseEntity.ok(vo);
    }
    
    /**
     * 根据订单号查询支付单列表
     * 
     * GET /api/v1/payments/by-order/{orderId}
     * 
     * @param orderId 订单号
     * @return 支付单列表
     */
    @GetMapping("/by-order/{orderId}")
    public ResponseEntity<List<PaymentVO>> getPaymentsByOrderId(@PathVariable String orderId) {
        log.info("根据订单号查询支付单列表，订单号: {}", orderId);
        
        List<PaymentAggregate> payments = paymentApplicationService.getPaymentsByOrderId(orderId);
        List<PaymentVO> vos = paymentAssembler.toVOs(payments);
        
        return ResponseEntity.ok(vos);
    }
    
    /**
     * 根据经销商ID查询支付单列表
     * 
     * GET /api/v1/payments/by-reseller/{resellerId}
     * 
     * @param resellerId 经销商ID
     * @return 支付单列表
     */
    @GetMapping("/by-reseller/{resellerId}")
    public ResponseEntity<List<PaymentVO>> getPaymentsByResellerId(@PathVariable String resellerId) {
        log.info("根据经销商ID查询支付单列表，经销商ID: {}", resellerId);
        
        List<PaymentAggregate> payments = paymentApplicationService.getPaymentsByResellerId(resellerId);
        List<PaymentVO> vos = paymentAssembler.toVOs(payments);
        
        return ResponseEntity.ok(vos);
    }
    
    /**
     * 根据关联业务ID查询支付单列表（用于信用还款查询）
     * 
     * GET /api/v1/payments/by-business/{businessId}
     * 
     * @param businessId 关联业务ID
     * @return 支付单列表
     */
    @GetMapping("/by-business/{businessId}")
    public ResponseEntity<List<PaymentVO>> getPaymentsByBusinessId(@PathVariable String businessId) {
        log.info("根据关联业务ID查询支付单列表，关联业务ID: {}", businessId);
        
        List<PaymentAggregate> payments = paymentApplicationService.getPaymentsByRelatedBusinessId(businessId);
        List<PaymentVO> vos = paymentAssembler.toVOs(payments);
        
        return ResponseEntity.ok(vos);
    }
}

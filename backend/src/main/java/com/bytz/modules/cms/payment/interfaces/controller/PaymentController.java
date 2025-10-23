package com.bytz.modules.cms.payment.interfaces.controller;

import com.bytz.modules.cms.payment.application.IPaymentApplicationService;
import com.bytz.modules.cms.payment.application.PaymentQueryService;
import com.bytz.modules.cms.payment.application.assembler.PaymentAssembler;
import com.bytz.modules.cms.payment.domain.command.CreatePaymentCommand;
import com.bytz.modules.cms.payment.domain.command.ExecutePaymentCommand;
import com.bytz.modules.cms.payment.domain.enums.PaymentChannel;
import com.bytz.modules.cms.payment.domain.model.PaymentAggregate;
import com.bytz.modules.cms.payment.infrastructure.entity.PaymentEntity;
import com.bytz.modules.cms.payment.interfaces.model.BatchPaymentExecuteRO;
import com.bytz.modules.cms.payment.interfaces.model.BatchPaymentResultVO;
import com.bytz.modules.cms.payment.interfaces.model.PaymentChannelVO;
import com.bytz.modules.cms.payment.interfaces.model.PaymentCreateRO;
import com.bytz.modules.cms.payment.interfaces.model.PaymentVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

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
    
    private final IPaymentApplicationService paymentApplicationService;
    private final PaymentQueryService paymentQueryService;
    private final PaymentAssembler paymentAssembler;
    
    /**
     * 根据支付单号查询支付单
     * 
     * GET /api/v1/payments/byCode/{code}
     * 
     * @param code 支付单号
     * @return 支付单响应对象
     */
    @GetMapping("/byCode/{code}")
    public ResponseEntity<PaymentVO> getPaymentByCode(@PathVariable String code) {
        log.info("查询支付单，支付单号: {}", code);
        
        PaymentEntity payment = paymentQueryService.getPaymentByCode(code);
        if (payment == null) {
            return ResponseEntity.notFound().build();
        }
        
        PaymentVO vo = paymentAssembler.entityToVO(payment);
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
        
        List<PaymentEntity> payments = paymentQueryService.getPaymentsByOrderId(orderId);
        List<PaymentVO> vos = paymentAssembler.entitiesToVOs(payments);
        
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
        
        List<PaymentEntity> payments = paymentQueryService.getPaymentsByResellerId(resellerId);
        List<PaymentVO> vos = paymentAssembler.entitiesToVOs(payments);
        
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
        
        List<PaymentEntity> payments = paymentQueryService.getPaymentsByRelatedBusinessId(businessId);
        List<PaymentVO> vos = paymentAssembler.entitiesToVOs(payments);
        
        return ResponseEntity.ok(vos);
    }
    
    /**
     * 批量支付接口
     * 前端调用批量支付接口，支持多个支付单合并支付
     * 
     * POST /api/v1/payments/batch-pay
     * 
     * @param ro 批量支付执行请求对象
     * @param resellerId 经销商ID（从请求头或上下文获取）
     * @return 批量支付结果响应对象
     */
    @PostMapping("/batch-pay")
    public ResponseEntity<BatchPaymentResultVO> batchPayment(
            @Valid @RequestBody BatchPaymentExecuteRO ro,
            @RequestHeader(value = "X-Reseller-Id", required = false) String resellerId) {
        
        log.info("收到批量支付请求，支付单数量: {}, 支付渠道: {}, 经销商ID: {}", 
                ro.getPaymentItems().size(), ro.getPaymentChannel(), resellerId);
        
        // TODO: 从认证上下文获取真实的经销商ID，这里暂时从请求头获取
        if (resellerId == null || resellerId.trim().isEmpty()) {
            resellerId = "RESELLER_DEFAULT";  // 默认值，实际应该从认证上下文获取
            log.warn("未提供经销商ID，使用默认值");
        }
        
        // 转换RO为Command（RO中已经包含paymentId）
        ExecutePaymentCommand command = paymentAssembler.toBatchPaymentCommand(ro, resellerId);
        
        // 执行批量支付
        String channelPaymentRecordId = paymentApplicationService.executeBatchPayment(command);
        
        // 构建响应对象
        BatchPaymentResultVO result = buildBatchPaymentResult(ro, ro.getPaymentChannel(), channelPaymentRecordId);
        
        log.info("批量支付执行完成，渠道交易id: {}", channelPaymentRecordId);
        return ResponseEntity.ok(result);
    }
    
    /**
     * 查询当前经销商可用支付渠道
     * 
     * GET /api/v1/payments/available-channels
     * 
     * @param resellerId 经销商ID（从请求头或上下文获取）
     * @return 可用支付渠道枚举列表
     */
    @GetMapping("/available-channels")
    public ResponseEntity<List<PaymentChannel>> getAvailableChannels(
            @RequestHeader(value = "X-Reseller-Id", required = false) String resellerId) {
        
        log.info("查询经销商可用支付渠道，经销商ID: {}", resellerId);
        
        // TODO: 从认证上下文获取真实的经销商ID，这里暂时从请求头获取
        if (resellerId == null || resellerId.trim().isEmpty()) {
            resellerId = "RESELLER_DEFAULT";  // 默认值，实际应该从认证上下文获取
            log.warn("未提供经销商ID，使用默认值");
        }
        
        // 查询可用渠道，直接返回枚举列表
        List<PaymentChannel> availableChannels = paymentApplicationService.queryAvailableChannels(resellerId);
        
        log.info("经销商 {} 可用支付渠道数量: {}", resellerId, availableChannels.size());
        return ResponseEntity.ok(availableChannels);
    }
    
    /**
     * 构建批量支付结果响应对象
     * 
     * @param ro 批量支付执行请求对象
     * @param paymentChannel 支付渠道
     * @param channelPaymentRecordId 渠道交易id
     * @return 批量支付结果响应对象
     */
    private BatchPaymentResultVO buildBatchPaymentResult(BatchPaymentExecuteRO ro, PaymentChannel paymentChannel, String channelPaymentRecordId) {
        // 计算总金额
        BigDecimal totalAmount = ro.getPaymentItems().stream()
                .map(BatchPaymentExecuteRO.PaymentItem::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        // 构建每个支付单的结果
        List<BatchPaymentResultVO.PaymentResultItem> paymentResults = ro.getPaymentItems().stream()
                .map(item -> BatchPaymentResultVO.PaymentResultItem.builder()
                        .paymentId(item.getPaymentId())
                        .amount(item.getAmount())
                        .paymentChannel(paymentChannel)
                        .channelPaymentRecordId(channelPaymentRecordId)
                        .success(true)  // 此时已提交到渠道，最终结果需要等待回调
                        .build())
                .collect(Collectors.toList());
        
        return BatchPaymentResultVO.builder()
                .paymentChannel(paymentChannel)
                .channelPaymentRecordId(channelPaymentRecordId)
                .totalAmount(totalAmount)
                .paymentCount(ro.getPaymentItems().size())
                .paymentResults(paymentResults)
                .build();
    }
}
package com.bytz.cms.payment.domain;

import com.bytz.cms.payment.domain.command.ExecutePaymentCommand;
import com.bytz.cms.payment.domain.command.ExecuteRefundCommand;
import com.bytz.cms.payment.domain.enums.PaymentChannel;
import com.bytz.cms.payment.domain.model.PaymentAggregate;
import com.bytz.cms.payment.domain.repository.IPaymentRepository;
import com.bytz.cms.payment.domain.valueobject.PaymentAmount;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 支付领域服务
 * Payment Domain Service
 * 
 * 处理跨聚合的支付业务逻辑，协调支付单聚合和支付渠道
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentDomainService {
    
    private final IPaymentRepository paymentRepository;
    
    /**
     * 执行批量支付操作（合并支付）
     * 
     * @param command 支付执行命令
     * @return 渠道交易号
     * TODO: 实现批量支付的完整逻辑
     * 需求：
     * 1. 验证所有支付单属于同一经销商
     * 2. 验证每个支付单的状态是否允许支付
     * 3. 验证每个支付单的支付金额是否合法
     * 4. 调用支付渠道创建支付请求
     * 5. 为每个支付单创建支付流水
     * 6. 更新所有支付单状态为"支付中"
     */
    public String executeBatchPayment(ExecutePaymentCommand command) {
        log.info("开始执行批量支付，支付单数量: {}, 支付渠道: {}", 
                command.getPaymentItems().size(), command.getPaymentChannel());
        
        // TODO: 实现批量支付逻辑
        // 1. 查询所有支付单
        List<PaymentAggregate> payments = command.getPaymentItems().stream()
                .map(item -> paymentRepository.findById(item.getPaymentId()))
                .collect(Collectors.toList());
        
        // 2. 验证所有支付单属于同一经销商
        String resellerId = payments.get(0).getResellerId();
        boolean sameReseller = payments.stream()
                .allMatch(p -> p.getResellerId().equals(resellerId));
        if (!sameReseller) {
            throw new IllegalArgumentException("所有支付单必须属于同一经销商");
        }
        
        // 3. 验证每个支付单的状态是否允许支付
        boolean allCanPay = payments.stream().allMatch(PaymentAggregate::canPay);
        if (!allCanPay) {
            throw new IllegalArgumentException("存在不允许支付的支付单");
        }
        
        // 4. 计算总支付金额
        // TODO: 实现金额计算和验证
        
        // 5. 调用支付渠道创建支付请求
        // TODO: 实现支付渠道调用
        String channelTransactionNumber = "MOCK_" + System.currentTimeMillis();
        
        // 6. 为每个支付单创建支付流水
        for (int i = 0; i < payments.size(); i++) {
            PaymentAggregate payment = payments.get(i);
            ExecutePaymentCommand.PaymentItem item = command.getPaymentItems().get(i);
            PaymentAmount amount = PaymentAmount.ofCNY(item.getAmount());
            
            payment.executePayment(command.getPaymentChannel(), amount, channelTransactionNumber);
            paymentRepository.save(payment);
        }
        
        log.info("批量支付执行完成，渠道交易号: {}", channelTransactionNumber);
        return channelTransactionNumber;
    }
    
    /**
     * 验证支付渠道是否可用
     * 
     * @param channel 支付渠道
     * @param resellerId 经销商ID
     * @return 是否可用
     * TODO: 实现渠道可用性验证逻辑
     */
    public boolean validateChannelAvailability(PaymentChannel channel, String resellerId) {
        log.info("验证支付渠道可用性，渠道: {}, 经销商: {}", channel, resellerId);
        
        // TODO: 实现渠道可用性验证
        // 1. 检查渠道是否启用
        // 2. 检查经销商是否有权限使用该渠道
        // 3. 检查渠道当前状态是否正常
        
        return true; // 暂时返回true
    }
    
    /**
     * 选择最佳支付流水进行退款
     * 
     * @param payment 支付单聚合
     * @param command 退款命令
     * @return 选中的支付流水ID
     * TODO: 实现退款流水选择策略
     * 需求：
     * 1. 如果指定了原流水号，使用指定的流水
     * 2. 如果未指定，按时间倒序选择最新的支付流水
     * 3. 如果退款金额超过单笔流水，需要分摊到多个流水
     */
    public String selectTransactionForRefund(PaymentAggregate payment, ExecuteRefundCommand command) {
        log.info("选择退款流水，支付单: {}, 退款金额: {}", command.getPaymentId(), command.getRefundAmount());
        
        // TODO: 实现流水选择逻辑
        if (command.getOriginalTransactionId() != null) {
            return command.getOriginalTransactionId();
        }
        
        // 默认选择最新的支付流水
        return payment.getTransactions().stream()
                .filter(t -> t.isPaymentTransaction() && t.isSuccess())
                .max((t1, t2) -> t1.getCreateTime().compareTo(t2.getCreateTime()))
                .map(t -> t.getId())
                .orElseThrow(() -> new IllegalArgumentException("未找到可用的支付流水"));
    }
}

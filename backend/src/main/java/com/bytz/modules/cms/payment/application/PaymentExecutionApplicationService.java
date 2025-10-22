package com.bytz.modules.cms.payment.application;

import com.bytz.modules.cms.payment.domain.PaymentDomainService;
import com.bytz.modules.cms.payment.domain.command.ExecutePaymentCommand;
import com.bytz.modules.cms.payment.domain.enums.PaymentChannel;
import com.bytz.modules.cms.payment.domain.model.PaymentAggregate;
import com.bytz.modules.cms.payment.domain.repository.IPaymentRepository;
import com.bytz.modules.cms.payment.infrastructure.channel.IPaymentChannelService;
import com.bytz.modules.cms.payment.shared.exception.PaymentException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 支付执行应用服务
 * Payment Execution Application Service
 * 
 * 协调批量支付执行、支付渠道查询等用例
 * 应用层不包含业务逻辑，仅负责用例协调
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentExecutionApplicationService {
    
    private final IPaymentRepository paymentRepository;
    private final PaymentDomainService paymentDomainService;
    private final List<IPaymentChannelService> paymentChannelServices;
    
    /**
     * 执行批量支付
     * 
     * @param command 执行支付命令
     * @return 渠道交易号
     */
    @Transactional(rollbackFor = Exception.class)
    public String executeBatchPayment(ExecutePaymentCommand command) {
        log.info("开始执行批量支付，支付单数量: {}, 支付渠道: {}", 
                command.getPaymentItems().size(), command.getPaymentChannel());
        
        // 委托给领域服务执行批量支付
        String channelTransactionNumber = paymentDomainService.executeBatchPayment(command);
        
        log.info("批量支付执行完成，渠道交易号: {}", channelTransactionNumber);
        return channelTransactionNumber;
    }
    
    /**
     * 查询经销商可用的支付渠道列表
     * 
     * @param resellerId 经销商ID
     * @return 支付渠道列表（枚举）
     */
    public List<PaymentChannel> queryAvailableChannels(String resellerId) {
        log.info("查询经销商可用支付渠道，经销商ID: {}", resellerId);
        
        if (resellerId == null || resellerId.trim().isEmpty()) {
            throw new IllegalArgumentException("经销商ID不能为空");
        }
        
        // 获取所有支付渠道枚举
        List<PaymentChannel> allChannels = Arrays.asList(PaymentChannel.values());
        
        // 过滤出可用的渠道
        List<PaymentChannel> availableChannels = allChannels.stream()
                .filter(channel -> isChannelAvailable(channel, resellerId))
                .collect(Collectors.toList());
        
        log.info("经销商 {} 可用支付渠道数量: {}", resellerId, availableChannels.size());
        return availableChannels;
    }
    
    /**
     * 查询经销商可用的支付渠道详细信息
     * 
     * @param resellerId 经销商ID
     * @return 渠道信息映射（渠道 -> 可用性及原因）
     */
    public Map<PaymentChannel, ChannelAvailability> queryChannelAvailabilityDetails(String resellerId) {
        log.info("查询经销商支付渠道详细信息，经销商ID: {}", resellerId);
        
        if (resellerId == null || resellerId.trim().isEmpty()) {
            throw new IllegalArgumentException("经销商ID不能为空");
        }
        
        // 获取所有支付渠道枚举
        List<PaymentChannel> allChannels = Arrays.asList(PaymentChannel.values());
        
        // 构建每个渠道的可用性信息
        Map<PaymentChannel, ChannelAvailability> channelAvailabilityMap = allChannels.stream()
                .collect(Collectors.toMap(
                        channel -> channel,
                        channel -> getChannelAvailability(channel, resellerId)
                ));
        
        return channelAvailabilityMap;
    }
    
    /**
     * 检查渠道是否可用
     * 
     * @param channel 支付渠道
     * @param resellerId 经销商ID
     * @return 是否可用
     */
    private boolean isChannelAvailable(PaymentChannel channel, String resellerId) {
        // 1. 检查渠道服务是否存在
        IPaymentChannelService channelService = findChannelService(channel);
        if (channelService == null) {
            log.warn("渠道服务未找到: {}", channel);
            return false;
        }
        
        // 2. 检查渠道是否可用
        if (!channelService.isAvailable()) {
            log.warn("渠道不可用: {}", channel);
            return false;
        }
        
        // 3. 检查经销商是否有权限使用该渠道
        boolean hasPermission = paymentDomainService.validateChannelAvailability(channel, resellerId);
        if (!hasPermission) {
            log.warn("经销商 {} 无权限使用渠道: {}", resellerId, channel);
            return false;
        }
        
        return true;
    }
    
    /**
     * 获取渠道可用性详细信息
     * 
     * @param channel 支付渠道
     * @param resellerId 经销商ID
     * @return 渠道可用性信息
     */
    private ChannelAvailability getChannelAvailability(PaymentChannel channel, String resellerId) {
        IPaymentChannelService channelService = findChannelService(channel);
        
        // 渠道服务不存在
        if (channelService == null) {
            return new ChannelAvailability(false, "渠道服务未配置");
        }
        
        // 渠道本身不可用
        if (!channelService.isAvailable()) {
            return new ChannelAvailability(false, "渠道暂时不可用");
        }
        
        // 检查经销商权限
        boolean hasPermission = paymentDomainService.validateChannelAvailability(channel, resellerId);
        if (!hasPermission) {
            return new ChannelAvailability(false, "您暂无使用该渠道的权限");
        }
        
        return new ChannelAvailability(true, null);
    }
    
    /**
     * 根据渠道类型查找渠道服务
     * 
     * @param channel 支付渠道
     * @return 渠道服务
     */
    private IPaymentChannelService findChannelService(PaymentChannel channel) {
        return paymentChannelServices.stream()
                .filter(service -> service.getChannelType() == channel)
                .findFirst()
                .orElse(null);
    }
    
    /**
     * 渠道可用性信息
     */
    public static class ChannelAvailability {
        private final boolean available;
        private final String reason;
        
        public ChannelAvailability(boolean available, String reason) {
            this.available = available;
            this.reason = reason;
        }
        
        public boolean isAvailable() {
            return available;
        }
        
        public String getReason() {
            return reason;
        }
    }
}

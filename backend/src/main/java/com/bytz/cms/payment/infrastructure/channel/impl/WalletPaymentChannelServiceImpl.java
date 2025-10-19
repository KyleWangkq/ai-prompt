package com.bytz.cms.payment.infrastructure.channel.impl;

import com.bytz.cms.payment.domain.enums.PaymentChannel;
import com.bytz.cms.payment.infrastructure.channel.IPaymentChannelService;
import com.bytz.cms.payment.infrastructure.channel.PaymentChannelResponse;
import com.bytz.cms.payment.infrastructure.channel.PaymentStatusQueryResult;
import com.bytz.cms.payment.infrastructure.channel.RefundChannelResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import java.math.BigDecimal;

/**
 * 钱包支付渠道实现
 * TODO: 实现企业内部资金账户支付接口调用逻辑
 */
@Slf4j
@Service
public class WalletPaymentChannelServiceImpl implements IPaymentChannelService {
    @Override
    public PaymentChannel getChannelType() {
        return PaymentChannel.WALLET_PAYMENT;
    }

    @Override
    public boolean isAvailable(String resellerId) {
        // TODO: 实现钱包余额检查和可用性验证
        log.info("检查钱包支付渠道可用性，经销商ID：{}", resellerId);
        return true;
    }

    @Override
    public PaymentChannelResponse createPayment(String paymentId, String resellerId, BigDecimal amount, String description) {
        // TODO: 实现钱包扣款逻辑
        log.info("创建钱包支付请求，支付单号：{}，金额：{}", paymentId, amount);
        return new PaymentChannelResponse(true, "WALLET_" + System.currentTimeMillis(), null);
    }

    @Override
    public PaymentStatusQueryResult queryPaymentStatus(String channelTransactionNumber) {
        // TODO: 实现钱包支付状态查询
        log.info("查询钱包支付状态，渠道交易号：{}", channelTransactionNumber);
        return new PaymentStatusQueryResult("SUCCESS", channelTransactionNumber, null);
    }

    @Override
    public RefundChannelResponse createRefund(String originalChannelTransactionNumber, BigDecimal refundAmount, String refundReason) {
        // TODO: 实现钱包退款逻辑
        log.info("创建钱包退款，原交易号：{}，退款金额：{}", originalChannelTransactionNumber, refundAmount);
        return new RefundChannelResponse(true, "REFUND_WALLET_" + System.currentTimeMillis(), null);
    }

    @Override
    public boolean validateCallback(String callbackData, String signature) {
        // 钱包支付一般为内部系统，可能不需要签名验证
        return true;
    }
}

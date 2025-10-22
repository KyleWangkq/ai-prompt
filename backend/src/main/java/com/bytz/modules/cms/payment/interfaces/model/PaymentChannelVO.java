package com.bytz.modules.cms.payment.interfaces.model;

import com.bytz.modules.cms.payment.domain.enums.PaymentChannel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 支付渠道响应对象
 * Payment Channel Value Object
 * 
 * 返回支付渠道信息
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaymentChannelVO {
    
    /**
     * 渠道代码
     */
    private String channelCode;
    
    /**
     * 渠道名称
     */
    private String channelName;
    
    /**
     * 渠道描述
     */
    private String channelDescription;
    
    /**
     * 是否可用
     */
    private Boolean available;
    
    /**
     * 不可用原因（如果不可用）
     */
    private String unavailableReason;
    
    /**
     * 从枚举创建VO
     * 
     * @param channel 支付渠道枚举
     * @param available 是否可用
     * @return 支付渠道VO
     */
    public static PaymentChannelVO from(PaymentChannel channel, boolean available) {
        return PaymentChannelVO.builder()
                .channelCode(channel.getCode())
                .channelName(channel.getDescription())
                .channelDescription(channel.getEnglishName())
                .available(available)
                .build();
    }
    
    /**
     * 从枚举创建VO（带不可用原因）
     * 
     * @param channel 支付渠道枚举
     * @param available 是否可用
     * @param unavailableReason 不可用原因
     * @return 支付渠道VO
     */
    public static PaymentChannelVO from(PaymentChannel channel, boolean available, String unavailableReason) {
        return PaymentChannelVO.builder()
                .channelCode(channel.getCode())
                .channelName(channel.getDescription())
                .channelDescription(channel.getEnglishName())
                .available(available)
                .unavailableReason(unavailableReason)
                .build();
    }
}

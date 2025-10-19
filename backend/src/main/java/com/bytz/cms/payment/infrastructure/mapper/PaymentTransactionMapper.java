package com.bytz.cms.payment.infrastructure.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.bytz.cms.payment.infrastructure.dataobject.PaymentTransactionDO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 支付流水Mapper接口
 * 
 * <p>继承MyBatis-Plus的BaseMapper，提供基础CRUD操作</p>
 * <p>复杂查询可在对应XML文件中定义</p>
 * 
 * <p>需求来源：需求文档4.4.3节数据库表结构设计</p>
 * 
 * @author DDD设计团队
 * @version 1.0.0
 * @since 2025-01-15
 */
@Mapper
public interface PaymentTransactionMapper extends BaseMapper<PaymentTransactionDO> {
    
    /**
     * 根据支付单号查询所有流水记录
     * 
     * @param paymentId 支付单号
     * @return 流水记录列表
     */
    @Select("SELECT * FROM cms_payment_transaction WHERE payment_id = #{paymentId} AND del_flag = 0 ORDER BY create_time DESC")
    List<PaymentTransactionDO> selectByPaymentId(String paymentId);
    
    /**
     * 根据渠道交易号查询流水记录（用于合并支付场景）
     * 
     * @param channelTransactionNumber 渠道交易号
     * @return 流水记录列表
     */
    @Select("SELECT * FROM cms_payment_transaction WHERE channel_transaction_number = #{channelTransactionNumber} AND del_flag = 0")
    List<PaymentTransactionDO> selectByChannelTransactionNumber(String channelTransactionNumber);
}

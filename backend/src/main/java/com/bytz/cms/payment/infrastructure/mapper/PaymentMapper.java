package com.bytz.cms.payment.infrastructure.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.bytz.cms.payment.domain.entity.PaymentEntity;
import org.apache.ibatis.annotations.Mapper;

/**
 * 支付单Mapper接口
 * Payment Mapper Interface
 * 
 * 职责：支付单数据库操作
 * 表：cms_payment
 */
@Mapper
public interface PaymentMapper extends BaseMapper<PaymentEntity> {
    
    // MyBatis-Plus提供的基础CRUD方法已足够
    // 如需自定义SQL查询，可在此添加方法并创建对应的XML映射文件
}

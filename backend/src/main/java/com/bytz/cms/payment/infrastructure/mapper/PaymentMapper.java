package com.bytz.cms.payment.infrastructure.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.bytz.cms.payment.infrastructure.persistence.PaymentEntity;
import org.apache.ibatis.annotations.Mapper;

/**
 * Payment MyBatis-Plus Mapper
 * 支付单数据访问Mapper
 */
@Mapper
public interface PaymentMapper extends BaseMapper<PaymentEntity> {
}

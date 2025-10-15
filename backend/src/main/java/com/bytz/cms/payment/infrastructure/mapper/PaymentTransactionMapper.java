package com.bytz.cms.payment.infrastructure.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.bytz.cms.payment.infrastructure.persistence.PaymentTransactionEntity;
import org.apache.ibatis.annotations.Mapper;

/**
 * PaymentTransaction MyBatis-Plus Mapper
 * 支付流水数据访问Mapper
 */
@Mapper
public interface PaymentTransactionMapper extends BaseMapper<PaymentTransactionEntity> {
}

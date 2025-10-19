package com.bytz.cms.payment.infrastructure.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.bytz.cms.payment.infrastructure.entity.PaymentEntity;
import org.apache.ibatis.annotations.Mapper;

/**
 * 支付单Mapper接口
 * Payment Mapper Interface
 * 
 * 继承MyBatis-Plus的BaseMapper，提供基本的CRUD操作
 */
@Mapper
public interface PaymentMapper extends BaseMapper<PaymentEntity> {
    
    // BaseMapper提供的基本方法：
    // - insert(T entity): 插入一条记录
    // - deleteById(Serializable id): 根据ID删除
    // - updateById(T entity): 根据ID更新
    // - selectById(Serializable id): 根据ID查询
    // - selectList(Wrapper<T> queryWrapper): 根据条件查询列表
    // - selectPage(Page<T> page, Wrapper<T> queryWrapper): 分页查询
    
    // TODO: 如果需要自定义复杂SQL查询，可以在此添加方法并在XML文件中实现
}

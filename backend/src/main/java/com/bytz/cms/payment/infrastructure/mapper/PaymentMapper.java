package com.bytz.cms.payment.infrastructure.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.bytz.cms.payment.infrastructure.dataobject.PaymentDO;
import org.apache.ibatis.annotations.Mapper;

/**
 * 支付单Mapper接口
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
public interface PaymentMapper extends BaseMapper<PaymentDO> {
    // BaseMapper提供的基础方法：
    // - insert: 插入一条记录
    // - deleteById: 根据ID删除
    // - updateById: 根据ID更新
    // - selectById: 根据ID查询
    // - selectList: 查询列表
    // - selectPage: 分页查询
    // 等等
    
    // 如需自定义SQL，可在此声明方法并在XML中实现
}

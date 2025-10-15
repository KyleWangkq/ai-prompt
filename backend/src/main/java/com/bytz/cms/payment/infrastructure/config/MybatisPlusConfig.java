package com.bytz.cms.payment.infrastructure.config;

import com.baomidou.mybatisplus.annotation.DbType;
import com.baomidou.mybatisplus.extension.plugins.MybatisPlusInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.PaginationInnerInterceptor;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * MyBatis-Plus配置
 * MyBatis-Plus Configuration
 */
@Configuration
@MapperScan("com.bytz.cms.payment.infrastructure.mapper")
public class MybatisPlusConfig {
    
    /**
     * 配置MyBatis-Plus拦截器
     * 包含分页插件
     */
    @Bean
    public MybatisPlusInterceptor mybatisPlusInterceptor() {
        MybatisPlusInterceptor interceptor = new MybatisPlusInterceptor();
        
        // 分页插件
        PaginationInnerInterceptor paginationInterceptor = new PaginationInnerInterceptor(DbType.MYSQL);
        paginationInterceptor.setMaxLimit(500L); // 单页最大限制500条
        paginationInterceptor.setOverflow(false); // 溢出总页数后不进行处理
        
        interceptor.addInnerInterceptor(paginationInterceptor);
        
        return interceptor;
    }
}

package com.bytz.modules.cms.payment.infrastructure.config;

import com.baomidou.mybatisplus.core.incrementer.IdentifierGenerator;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * MyBatis-Plus 配置类
 * MyBatis-Plus Configuration
 * 
 * 配置MyBatis-Plus相关功能，包括：
 * 1. Mapper扫描路径
 * 2. 自定义ID生成器
 */
@Configuration
@MapperScan("com.bytz.modules.cms.payment.infrastructure.mapper")
public class MybatisPlusConfig {
    
    /**
     * 注册自定义ID生成器到MyBatis-Plus
     * 
     * @param customIdGenerator 自定义ID生成器
     * @return IdentifierGenerator实例
     */
    @Bean
    public IdentifierGenerator identifierGenerator(CustomIdGenerator customIdGenerator) {
        return customIdGenerator;
    }
}

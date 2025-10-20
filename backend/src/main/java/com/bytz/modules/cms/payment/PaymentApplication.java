package com.bytz.modules.cms.payment;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * 支付模块启动类
 * Payment Module Application
 * 
 * Spring Boot主启动类
 */
@SpringBootApplication
@MapperScan("com.bytz.modules.cms.payment.infrastructure.mapper")
public class PaymentApplication {
    
    public static void main(String[] args) {
        SpringApplication.run(PaymentApplication.class, args);
    }
}

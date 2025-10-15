package com.bytz.cms.payment;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Payment Service Application
 * 支付服务Spring Boot启动类
 */
@SpringBootApplication
@MapperScan("com.bytz.cms.payment.infrastructure.mapper")
public class PaymentServiceApplication {
    
    public static void main(String[] args) {
        SpringApplication.run(PaymentServiceApplication.class, args);
    }
}

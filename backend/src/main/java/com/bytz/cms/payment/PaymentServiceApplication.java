package com.bytz.cms.payment;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * 支付服务启动类
 * 
 * <p>莱宝经销商订单交易系统 - 支付模块主启动类</p>
 * <p>基于DDD五层架构设计，提供完整的B2B工业设备交易支付解决方案</p>
 * 
 * <p>核心功能：</p>
 * <ul>
 *   <li>支付单创建与管理</li>
 *   <li>统一支付处理（单支付单、合并支付、部分支付）</li>
 *   <li>退款管理</li>
 *   <li>信用还款处理</li>
 *   <li>支付状态查询与补偿</li>
 * </ul>
 * 
 * @author DDD设计团队
 * @version 1.0.0
 * @since 2025-01-15
 */
@SpringBootApplication
@MapperScan("com.bytz.cms.payment.infrastructure.mapper")
public class PaymentServiceApplication {

    /**
     * 应用程序入口
     * 
     * @param args 命令行参数
     */
    public static void main(String[] args) {
        SpringApplication.run(PaymentServiceApplication.class, args);
    }
}

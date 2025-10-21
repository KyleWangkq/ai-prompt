package com.bytz.modules.cms.payment.infrastructure.config;

import com.baomidou.mybatisplus.core.incrementer.DefaultIdentifierGenerator;
import com.baomidou.mybatisplus.core.incrementer.IdentifierGenerator;
import com.bytz.modules.cms.payment.infrastructure.entity.PaymentEntity;
import com.bytz.modules.cms.payment.infrastructure.entity.PaymentTransactionEntity;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * 自定义ID生成器
 * Custom ID Generator for Payment Module
 * 
 * 包装MyBatis-Plus的DefaultIdentifierGenerator，根据业务对象类型添加前缀
 * 
 * ID特性：
 * 1. 时间有序：基于DefaultIdentifierGenerator的雪花算法，保证时间顺序
 * 2. 毫秒精度：包含毫秒级时间信息
 * 3. 长度控制：总长度不超过32个字符
 * 4. 分布式支持：雪花算法支持分布式环境
 * 
 * ID格式：
 * - 支付单号：PAY + DefaultIdentifierGenerator生成的ID
 * - 流水号：TXN + DefaultIdentifierGenerator生成的ID
 */
@Slf4j
@Component
public class CustomIdGenerator implements IdentifierGenerator {
    
    /**
     * MyBatis-Plus默认ID生成器（雪花算法）
     */
    private final DefaultIdentifierGenerator defaultGenerator = new DefaultIdentifierGenerator();
    
    /**
     * 生成唯一ID（Number类型）
     * 
     * @param entity 实体对象
     * @return 生成的唯一ID
     */
    @Override
    public Number nextId(Object entity) {
        return defaultGenerator.nextId(entity);
    }
    
    /**
     * 生成String类型的唯一ID
     * 根据实体类型自动添加业务前缀
     * 
     * @param entity 实体对象
     * @return 生成的唯一ID（String类型，带业务前缀）
     */
    @Override
    public String nextUUID(Object entity) {
        // 使用DefaultIdentifierGenerator生成基础ID
        String baseId = String.valueOf(defaultGenerator.nextId(entity));
        
        // 根据实体类型确定前缀
        String prefix = determinePrefix(entity);
        
        String fullId = prefix + baseId;
        log.debug("生成ID: {} (实体类型: {})", fullId, entity != null ? entity.getClass().getSimpleName() : "null");
        
        return fullId;
    }
    
    /**
     * 生成支付单号
     * 格式：PAY + DefaultIdentifierGenerator生成的ID
     * 
     * @return 支付单号
     */
    public String generatePaymentId() {
        // 传入PaymentEntity类型，自动添加PAY前缀
        return nextUUID(new PaymentEntity());
    }
    
    /**
     * 生成支付流水号
     * 格式：TXN + DefaultIdentifierGenerator生成的ID
     * 
     * @return 支付流水号
     */
    public String generateTransactionId() {
        // 传入PaymentTransactionEntity类型，自动添加TXN前缀
        return nextUUID(new PaymentTransactionEntity());
    }
    
    /**
     * 根据实体类型确定业务前缀
     * 
     * @param entity 实体对象
     * @return 业务前缀
     */
    private String determinePrefix(Object entity) {
        if (entity == null) {
            return "";
        }
        
        // 根据实体类型返回对应的业务前缀
        if (entity instanceof PaymentEntity) {
            return "PAY";
        } else if (entity instanceof PaymentTransactionEntity) {
            return "TXN";
        }
        
        // 默认无前缀
        return "";
    }
}

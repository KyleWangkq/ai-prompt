package com.bytz.modules.cms.payment.infrastructure.config;

import com.baomidou.mybatisplus.core.incrementer.IdentifierGenerator;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.ThreadLocalRandom;

/**
 * 自定义ID生成器
 * Custom ID Generator for Payment Module
 * 
 * 实现MyBatis-Plus的IdentifierGenerator接口，提供时间有序、毫秒精度的ID生成
 * 
 * ID特性：
 * 1. 时间有序：基于时间戳生成，保证时间顺序
 * 2. 毫秒精度：包含毫秒级时间信息
 * 3. 长度控制：总长度不超过32个字符
 * 4. 分布式支持：通过随机数降低分布式环境下的ID冲突概率
 * 
 * ID格式：
 * - 支付单号：PAY + YYYYMMDDHHmmssSSS(17位) + 随机数(8位) = 28个字符
 * - 流水号：TXN + YYYYMMDDHHmmssSSS(17位) + 随机数(8位) = 28个字符
 */
@Slf4j
@Component
public class CustomIdGenerator implements IdentifierGenerator {
    
    /**
     * 时间戳格式：年月日时分秒毫秒（17位）
     */
    private static final DateTimeFormatter TIMESTAMP_FORMATTER = 
            DateTimeFormatter.ofPattern("yyyyMMddHHmmssSSS");
    
    /**
     * 随机数长度（8位）
     */
    private static final int RANDOM_LENGTH = 8;
    
    /**
     * 生成唯一ID
     * 
     * @param entity 实体对象（未使用，保留以符合接口定义）
     * @return 生成的唯一ID
     */
    @Override
    public Number nextId(Object entity) {
        // 此方法返回Number类型，但我们使用String类型的ID
        // 因此此方法不会被使用
        throw new UnsupportedOperationException("请使用 nextUUID 方法生成String类型的ID");
    }
    
    /**
     * 生成String类型的唯一ID
     * 
     * @param entity 实体对象（未使用，保留以符合接口定义）
     * @return 生成的唯一ID（String类型）
     */
    @Override
    public String nextUUID(Object entity) {
        // 获取当前时间戳（毫秒精度，17位）
        String timestamp = LocalDateTime.now().format(TIMESTAMP_FORMATTER);
        
        // 生成8位随机数（使用ThreadLocalRandom提高性能和线程安全）
        String random = generateRandomString(RANDOM_LENGTH);
        
        // 拼接生成ID：时间戳 + 随机数 = 25个字符（不含前缀）
        return timestamp + random;
    }
    
    /**
     * 生成支付单号
     * 格式：PAY + 时间戳(17位) + 随机数(8位) = 28个字符
     * 
     * @return 支付单号
     */
    public String generatePaymentId() {
        String baseId = nextUUID(null);
        String paymentId = "PAY" + baseId;
        log.debug("生成支付单号: {}", paymentId);
        return paymentId;
    }
    
    /**
     * 生成支付流水号
     * 格式：TXN + 时间戳(17位) + 随机数(8位) = 28个字符
     * 
     * @return 支付流水号
     */
    public String generateTransactionId() {
        String baseId = nextUUID(null);
        String transactionId = "TXN" + baseId;
        log.debug("生成支付流水号: {}", transactionId);
        return transactionId;
    }
    
    /**
     * 生成指定长度的随机字符串（数字+大写字母）
     * 
     * @param length 随机字符串长度
     * @return 随机字符串
     */
    private String generateRandomString(int length) {
        // 使用数字和大写字母（36进制），提高随机性
        StringBuilder sb = new StringBuilder(length);
        ThreadLocalRandom random = ThreadLocalRandom.current();
        
        for (int i = 0; i < length; i++) {
            // 生成0-35的随机数：0-9为数字，10-35为A-Z
            int num = random.nextInt(36);
            if (num < 10) {
                sb.append((char) ('0' + num));
            } else {
                sb.append((char) ('A' + num - 10));
            }
        }
        
        return sb.toString();
    }
}

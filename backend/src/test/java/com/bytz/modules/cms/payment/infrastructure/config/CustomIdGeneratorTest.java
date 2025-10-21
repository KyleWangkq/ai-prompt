package com.bytz.modules.cms.payment.infrastructure.config;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

/**
 * CustomIdGenerator 单元测试
 * 测试自定义ID生成器的功能
 */
@DisplayName("CustomIdGenerator 单元测试")
class CustomIdGeneratorTest {

    private CustomIdGenerator idGenerator;

    @BeforeEach
    void setUp() {
        idGenerator = new CustomIdGenerator();
    }

    @Test
    @DisplayName("生成支付单号 - 基本格式验证")
    void testGeneratePaymentId_Format() {
        // When
        String paymentId = idGenerator.generatePaymentId();

        // Then
        assertNotNull(paymentId);
        assertTrue(paymentId.startsWith("PAY"), "支付单号应以PAY开头");
        assertEquals(28, paymentId.length(), "支付单号长度应为28个字符");
        
        // 验证格式：PAY + 17位时间戳 + 8位随机数
        String timestamp = paymentId.substring(3, 20);
        String random = paymentId.substring(20);
        
        assertTrue(timestamp.matches("\\d{17}"), "时间戳应为17位数字");
        assertTrue(random.matches("[0-9A-Z]{8}"), "随机数应为8位大写字母或数字");
    }

    @Test
    @DisplayName("生成支付流水号 - 基本格式验证")
    void testGenerateTransactionId_Format() {
        // When
        String transactionId = idGenerator.generateTransactionId();

        // Then
        assertNotNull(transactionId);
        assertTrue(transactionId.startsWith("TXN"), "流水号应以TXN开头");
        assertEquals(28, transactionId.length(), "流水号长度应为28个字符");
        
        // 验证格式：TXN + 17位时间戳 + 8位随机数
        String timestamp = transactionId.substring(3, 20);
        String random = transactionId.substring(20);
        
        assertTrue(timestamp.matches("\\d{17}"), "时间戳应为17位数字");
        assertTrue(random.matches("[0-9A-Z]{8}"), "随机数应为8位大写字母或数字");
    }

    @Test
    @DisplayName("生成多个支付单号 - 唯一性验证")
    void testGeneratePaymentId_Uniqueness() {
        // Given
        int count = 1000;
        Set<String> ids = new HashSet<>();

        // When
        for (int i = 0; i < count; i++) {
            String id = idGenerator.generatePaymentId();
            ids.add(id);
        }

        // Then
        assertEquals(count, ids.size(), "生成的支付单号应全部唯一");
    }

    @Test
    @DisplayName("生成多个流水号 - 唯一性验证")
    void testGenerateTransactionId_Uniqueness() {
        // Given
        int count = 1000;
        Set<String> ids = new HashSet<>();

        // When
        for (int i = 0; i < count; i++) {
            String id = idGenerator.generateTransactionId();
            ids.add(id);
        }

        // Then
        assertEquals(count, ids.size(), "生成的流水号应全部唯一");
    }

    @Test
    @DisplayName("时间有序性验证 - 连续生成的ID时间戳递增")
    void testTimeOrdering() throws InterruptedException {
        // Given
        String id1 = idGenerator.generatePaymentId();
        Thread.sleep(5); // 等待5毫秒确保时间戳不同
        String id2 = idGenerator.generatePaymentId();

        // When
        String timestamp1 = id1.substring(3, 20);
        String timestamp2 = id2.substring(3, 20);

        // Then
        assertTrue(timestamp2.compareTo(timestamp1) >= 0, 
                "后生成的ID时间戳应大于或等于先生成的ID");
    }

    @Test
    @DisplayName("时间精度验证 - 包含毫秒信息")
    void testMillisecondPrecision() {
        // When
        String id1 = idGenerator.generatePaymentId();
        String id2 = idGenerator.generatePaymentId();

        // Then
        String timestamp1 = id1.substring(3, 20);
        String timestamp2 = id2.substring(3, 20);
        
        // 时间戳格式为YYYYMMDDHHmmssSSS，最后3位是毫秒
        String millis1 = timestamp1.substring(14, 17);
        String millis2 = timestamp2.substring(14, 17);
        
        assertTrue(millis1.matches("\\d{3}"), "时间戳应包含3位毫秒数");
        assertTrue(millis2.matches("\\d{3}"), "时间戳应包含3位毫秒数");
    }

    @Test
    @DisplayName("长度限制验证 - 不超过32个字符")
    void testLengthConstraint() {
        // When
        String paymentId = idGenerator.generatePaymentId();
        String transactionId = idGenerator.generateTransactionId();

        // Then
        assertTrue(paymentId.length() <= 32, 
                "支付单号长度应不超过32个字符，实际长度：" + paymentId.length());
        assertTrue(transactionId.length() <= 32, 
                "流水号长度应不超过32个字符，实际长度：" + transactionId.length());
    }

    @Test
    @DisplayName("并发测试 - 模拟分布式场景")
    void testConcurrency() throws InterruptedException {
        // Given
        int threadCount = 10;
        int idsPerThread = 100;
        ExecutorService executorService = Executors.newFixedThreadPool(threadCount);
        CountDownLatch latch = new CountDownLatch(threadCount);
        Set<String> allIds = new HashSet<>();
        AtomicInteger duplicateCount = new AtomicInteger(0);

        // When
        for (int i = 0; i < threadCount; i++) {
            executorService.submit(() -> {
                try {
                    Set<String> threadIds = new HashSet<>();
                    for (int j = 0; j < idsPerThread; j++) {
                        String id = idGenerator.generatePaymentId();
                        threadIds.add(id);
                    }
                    
                    synchronized (allIds) {
                        int beforeSize = allIds.size();
                        allIds.addAll(threadIds);
                        int afterSize = allIds.size();
                        int added = afterSize - beforeSize;
                        if (added < threadIds.size()) {
                            duplicateCount.addAndGet(threadIds.size() - added);
                        }
                    }
                } finally {
                    latch.countDown();
                }
            });
        }

        latch.await();
        executorService.shutdown();

        // Then
        int totalGenerated = threadCount * idsPerThread;
        int uniqueIds = allIds.size();
        double uniqueRate = (double) uniqueIds / totalGenerated * 100;
        
        System.out.println("并发测试结果：");
        System.out.println("  总生成数：" + totalGenerated);
        System.out.println("  唯一ID数：" + uniqueIds);
        System.out.println("  重复数量：" + duplicateCount.get());
        System.out.println("  唯一率：" + String.format("%.2f%%", uniqueRate));
        
        // 对于低概率分布式场景，允许少量冲突，但唯一率应该很高（>99%）
        assertTrue(uniqueRate > 99.0, 
                "唯一率应大于99%，实际唯一率：" + String.format("%.2f%%", uniqueRate));
    }

    @Test
    @DisplayName("nextUUID方法测试 - 基础ID生成")
    void testNextUUID() {
        // When
        String uuid = idGenerator.nextUUID(null);

        // Then
        assertNotNull(uuid);
        assertEquals(25, uuid.length(), "基础ID长度应为25个字符（17位时间戳 + 8位随机数）");
        
        String timestamp = uuid.substring(0, 17);
        String random = uuid.substring(17);
        
        assertTrue(timestamp.matches("\\d{17}"), "时间戳应为17位数字");
        assertTrue(random.matches("[0-9A-Z]{8}"), "随机数应为8位大写字母或数字");
    }

    @Test
    @DisplayName("nextId方法测试 - 应抛出异常")
    void testNextId_ThrowsException() {
        // When & Then
        assertThrows(UnsupportedOperationException.class, () -> {
            idGenerator.nextId(null);
        }, "nextId方法应抛出UnsupportedOperationException");
    }

    @Test
    @DisplayName("随机性验证 - 相同时间戳的ID应有不同的随机部分")
    void testRandomness() {
        // Given
        Set<String> randomParts = new HashSet<>();
        int count = 100;

        // When - 快速生成多个ID，大部分可能有相同的时间戳
        for (int i = 0; i < count; i++) {
            String id = idGenerator.generatePaymentId();
            String randomPart = id.substring(20); // 提取随机部分
            randomParts.add(randomPart);
        }

        // Then - 随机部分应该有很高的差异性
        assertTrue(randomParts.size() > count * 0.95, 
                "随机部分应有较高的差异性，实际唯一数：" + randomParts.size() + "/" + count);
    }
}

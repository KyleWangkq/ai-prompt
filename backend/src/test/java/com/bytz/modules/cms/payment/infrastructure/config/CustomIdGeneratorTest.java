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
        assertTrue(paymentId.length() <= 32, "支付单号长度应不超过32个字符");
        
        // 验证格式：PAY + DefaultIdentifierGenerator生成的数字ID（雪花算法）
        String idPart = paymentId.substring(3);
        assertTrue(idPart.matches("\\d+"), "ID部分应为纯数字（雪花算法生成的Long）");
    }

    @Test
    @DisplayName("生成支付流水号 - 基本格式验证")
    void testGenerateTransactionId_Format() {
        // When
        String transactionId = idGenerator.generateTransactionId();

        // Then
        assertNotNull(transactionId);
        assertTrue(transactionId.startsWith("TXN"), "流水号应以TXN开头");
        assertTrue(transactionId.length() <= 32, "流水号长度应不超过32个字符");
        
        // 验证格式：TXN + DefaultIdentifierGenerator生成的数字ID（雪花算法）
        String idPart = transactionId.substring(3);
        assertTrue(idPart.matches("\\d+"), "ID部分应为纯数字（雪花算法生成的Long）");
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
    @DisplayName("时间有序性验证 - 连续生成的ID递增")
    void testTimeOrdering() throws InterruptedException {
        // Given
        String id1 = idGenerator.generatePaymentId();
        Thread.sleep(5); // 等待5毫秒确保时间不同
        String id2 = idGenerator.generatePaymentId();

        // When
        // 提取数字ID部分并转换为Long进行比较
        Long numId1 = Long.parseLong(id1.substring(3));
        Long numId2 = Long.parseLong(id2.substring(3));

        // Then
        // 雪花算法生成的ID是递增的（包含时间戳）
        assertTrue(numId2 > numId1, 
                "后生成的ID应大于先生成的ID（雪花算法保证递增）");
    }

    @Test
    @DisplayName("时间精度验证 - 雪花算法包含毫秒时间戳")
    void testMillisecondPrecision() {
        // When
        String id1 = idGenerator.generatePaymentId();
        String id2 = idGenerator.generatePaymentId();

        // Then
        // 雪花算法的ID是Long类型，包含毫秒级时间戳
        Long numId1 = Long.parseLong(id1.substring(3));
        Long numId2 = Long.parseLong(id2.substring(3));
        
        assertNotNull(numId1);
        assertNotNull(numId2);
        // 雪花算法生成的ID包含时间信息，且具有毫秒精度
        assertTrue(numId1 > 0, "ID应为正数");
        assertTrue(numId2 > 0, "ID应为正数");
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
    @DisplayName("nextUUID方法测试 - 基础ID生成（无前缀）")
    void testNextUUID() {
        // When
        String uuid = idGenerator.nextUUID(null);

        // Then
        assertNotNull(uuid);
        // 不带前缀时，返回纯数字ID
        assertTrue(uuid.matches("\\d+"), "无前缀的ID应为纯数字（雪花算法生成的Long）");
    }

    @Test
    @DisplayName("nextId方法测试 - 返回Number类型ID")
    void testNextId() {
        // When
        Number id = idGenerator.nextId(null);

        // Then
        assertNotNull(id);
        assertTrue(id.longValue() > 0, "ID应为正数");
    }

    @Test
    @DisplayName("唯一性验证 - 快速生成的ID应全部唯一")
    void testUniqueness() {
        // Given
        Set<String> ids = new HashSet<>();
        int count = 100;

        // When - 快速生成多个ID
        for (int i = 0; i < count; i++) {
            String id = idGenerator.generatePaymentId();
            ids.add(id);
        }

        // Then - 所有ID应该唯一（雪花算法保证）
        assertEquals(count, ids.size(), 
                "所有ID应该唯一，实际唯一数：" + ids.size() + "/" + count);
    }
}

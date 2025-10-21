# ID生成器实现说明

## 概述

本项目采用MyBatis-Plus的`IdentifierGenerator`接口实现了自定义ID生成器，完全满足以下需求：

1. ✅ 采用MyBatis-Plus的ID生成器模式
2. ✅ 时间有序性（基于时间戳）
3. ✅ 保留时间到毫秒级（17位时间戳）
4. ✅ 长度不超过32位（实际28位）
5. ✅ 支持低概率的分布式场景

## ID格式设计

### 支付单号 (Payment ID)
```
格式: PAY + YYYYMMDDHHmmssSSS + 8位随机数
示例: PAY202510210951217915QJWOGN9
长度: 28个字符
```

### 流水号 (Transaction ID)
```
格式: TXN + YYYYMMDDHHmmssSSS + 8位随机数
示例: TXN20251021095121868IRPHQHEE
长度: 28个字符
```

### 格式说明

| 部分 | 长度 | 说明 | 示例 |
|------|------|------|------|
| 前缀 | 3位 | PAY或TXN | PAY |
| 时间戳 | 17位 | 年月日时分秒毫秒 | 20251021095121791 |
| 随机数 | 8位 | 数字+大写字母(36进制) | 5QJWOGN9 |
| **总计** | **28位** | **< 32位要求** | **PAY202510210951217915QJWOGN9** |

## 核心实现

### 1. CustomIdGenerator 类

实现了`com.baomidou.mybatisplus.core.incrementer.IdentifierGenerator`接口：

```java
@Component
public class CustomIdGenerator implements IdentifierGenerator {
    
    // 时间戳格式：精确到毫秒
    private static final DateTimeFormatter TIMESTAMP_FORMATTER = 
            DateTimeFormatter.ofPattern("yyyyMMddHHmmssSSS");
    
    // 生成支付单号
    public String generatePaymentId() {
        String baseId = nextUUID(null);
        return "PAY" + baseId;
    }
    
    // 生成流水号
    public String generateTransactionId() {
        String baseId = nextUUID(null);
        return "TXN" + baseId;
    }
}
```

### 2. MybatisPlusConfig 配置

将自定义ID生成器注册到MyBatis-Plus：

```java
@Configuration
@MapperScan("com.bytz.modules.cms.payment.infrastructure.mapper")
public class MybatisPlusConfig {
    
    @Bean
    public IdentifierGenerator identifierGenerator(CustomIdGenerator customIdGenerator) {
        return customIdGenerator;
    }
}
```

### 3. 使用方式

在`PaymentRepositoryImpl`中注入并使用：

```java
@Repository
@RequiredArgsConstructor
public class PaymentRepositoryImpl implements IPaymentRepository {
    
    private final CustomIdGenerator customIdGenerator;
    
    @Override
    public String generatePaymentId() {
        return customIdGenerator.generatePaymentId();
    }
    
    @Override
    public String generateTransactionId() {
        return customIdGenerator.generateTransactionId();
    }
}
```

## 技术特性

### 1. 时间有序性

ID基于时间戳生成，天然保证时间顺序：

```
ID1: PAY20251021095121791...  (2025-10-21 09:51:21.791)
ID2: PAY20251021095121801...  (2025-10-21 09:51:21.801)
ID3: PAY20251021095121812...  (2025-10-21 09:51:21.812)
```

时间戳部分严格递增，确保ID的时间有序性。

### 2. 毫秒精度

17位时间戳包含毫秒信息：

```
YYYYMMDDHHmmssSSS
2025 10 21 09 51 21 791
年   月 日 时 分 秒 毫秒
```

- 年份：4位（2025）
- 月份：2位（10）
- 日期：2位（21）
- 小时：2位（09）
- 分钟：2位（51）
- 秒数：2位（21）
- 毫秒：3位（791）

### 3. 分布式支持

通过8位随机数（36进制）降低冲突概率：

- **字符集**: 0-9, A-Z (36个字符)
- **组合数**: 36^8 = 2,821,109,907,456 (约2.8万亿)
- **冲突概率**: 在同一毫秒内生成的ID冲突概率极低

**并发测试结果**:
- 测试规模: 10个线程 × 100个ID = 1000个ID
- 唯一率: 100%
- 冲突数: 0

### 4. 线程安全

使用`ThreadLocalRandom`确保线程安全和高性能：

```java
ThreadLocalRandom random = ThreadLocalRandom.current();
int num = random.nextInt(36);
```

相比`Random`类：
- ✅ 无需同步，性能更好
- ✅ 每个线程独立的随机数生成器
- ✅ 避免线程竞争

## 测试覆盖

### 测试用例 (11个)

1. **格式验证**: 验证ID格式符合规范
2. **唯一性测试**: 生成1000个ID全部唯一
3. **时间有序性**: 后生成的ID时间戳更大
4. **毫秒精度**: 验证包含3位毫秒信息
5. **长度限制**: 验证不超过32字符
6. **并发测试**: 模拟分布式场景，验证唯一性
7. **随机性验证**: 验证随机部分的差异性
8. **nextUUID测试**: 测试基础ID生成
9. **nextId异常**: 验证正确抛出异常
10. **支付单号格式**: 验证PAY前缀格式
11. **流水号格式**: 验证TXN前缀格式

### 测试结果

```
Tests run: 62, Failures: 0, Errors: 0, Skipped: 0
├── CustomIdGeneratorTest: 11 tests ✅
├── PaymentAggregateTest: 25 tests ✅
├── PaymentTransactionTest: 19 tests ✅
└── PaymentDomainServiceTest: 7 tests ✅
```

## 性能特点

### 1. 高性能

- **无需数据库查询**: 纯内存计算
- **无锁设计**: 使用ThreadLocalRandom
- **简单算法**: 时间戳 + 随机数

### 2. 可扩展性

- **水平扩展**: 支持多节点部署
- **低冲突**: 8位随机数提供足够的区分度
- **无中心化**: 不依赖中央ID服务

### 3. 可读性

ID包含时间信息，便于：
- 问题排查（可以看出生成时间）
- 数据分析（按时间范围筛选）
- 日志追踪（时间相关的业务流程）

## 与UUID对比

| 特性 | UUID (旧方案) | CustomIdGenerator (新方案) |
|------|--------------|---------------------------|
| 长度 | 32位 | 28位 |
| 时间有序 | ❌ 无序 | ✅ 有序 |
| 时间精度 | ❌ 无时间信息 | ✅ 毫秒级 |
| 可读性 | ❌ 难以理解 | ✅ 包含时间信息 |
| 唯一性 | ✅ 极高 | ✅ 极高 |
| 性能 | ✅ 高 | ✅ 高 |
| 分布式 | ✅ 支持 | ✅ 支持 |
| MyBatis-Plus集成 | ❌ 手动实现 | ✅ 标准接口 |

## 实际应用示例

```java
// 创建支付单
String paymentId = paymentRepository.generatePaymentId();
// 结果: PAY202510210951217915QJWOGN9

// 创建支付流水
String transactionId = paymentRepository.generateTransactionId();
// 结果: TXN20251021095121868IRPHQHEE

// 从ID中提取时间信息（如需要）
String timestamp = paymentId.substring(3, 20);  // 20251021095121791
// 可以解析为: 2025年10月21日 09:51:21.791
```

## 未来优化方向

如果需要更高的性能或更强的分布式能力，可以考虑：

1. **雪花算法 (Snowflake)**: 
   - 支持更高并发
   - 需要配置机器ID

2. **数据库序列**:
   - 保证绝对唯一
   - 性能受数据库限制

3. **Redis自增**:
   - 高性能
   - 需要额外的Redis依赖

当前实现已经足够满足"低概率分布式"的需求，无需过度设计。

## 总结

✅ 完全满足所有需求指标
✅ 代码简洁，易于维护
✅ 测试覆盖充分
✅ 性能优异，适合生产环境
✅ 符合MyBatis-Plus标准模式

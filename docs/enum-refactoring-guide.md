# 支付模块枚举重构说明

## 概述

本次重构将支付模块的所有枚举类型改造为使用MyBatis-Plus的`IEnum`接口，实现枚举与数据库字段的自动转换。

## 变更内容

### 1. 枚举类改造

所有枚举类实现了`IEnum<Integer>`接口，并添加了`code`字段用于数据库存储和业务编码。

#### 改造前

```java
public enum PaymentStatus {
    UNPAID("未支付", "Unpaid"),
    PAYING("支付中", "Paying");
    
    private final String description;
    private final String englishName;
    
    PaymentStatus(String description, String englishName) {
        this.description = description;
        this.englishName = englishName;
    }
    
    public String getDescription() {
        return description;
    }
}
```

#### 改造后

```java
public enum PaymentStatus implements IEnum<Integer> {
    UNPAID(0, "未支付", "Unpaid"),
    PAYING(1, "支付中", "Paying");
    
    private final Integer code;
    private final String description;
    private final String englishName;
    
    PaymentStatus(Integer code, String description, String englishName) {
        this.code = code;
        this.description = description;
        this.englishName = englishName;
    }
    
    @Override
    public Integer getValue() {
        return code;
    }
    
    public Integer getCode() {
        return code;
    }
    
    public String getDescription() {
        return description;
    }
}
```

### 2. 枚举Code映射表

| 枚举类 | 枚举值 | Code |
|-------|--------|------|
| **PaymentStatus** | UNPAID | 0 |
| | PAYING | 1 |
| | PARTIAL_PAID | 2 |
| | PAID | 3 |
| | FAILED | 4 |
| | STOPPED | 5 |
| | FROZEN | 6 |
| **PaymentType** | ADVANCE_PAYMENT | 1 |
| | FINAL_PAYMENT | 2 |
| | OTHER_PAYMENT | 3 |
| | CREDIT_REPAYMENT | 4 |
| **PaymentChannel** | ONLINE_PAYMENT | 1 |
| | WALLET_PAYMENT | 2 |
| | WIRE_TRANSFER | 3 |
| | CREDIT_ACCOUNT | 4 |
| **TransactionType** | PAYMENT | 1 |
| | REFUND | 2 |
| **TransactionStatus** | PROCESSING | 0 |
| | SUCCESS | 1 |
| | FAILED | 2 |
| **RefundStatus** | NO_REFUND | 0 |
| | REFUNDING | 1 |
| | PARTIAL_REFUNDED | 2 |
| | FULL_REFUNDED | 3 |
| | REFUND_FAILED | 4 |
| **RelatedBusinessType** | CREDIT_RECORD | 1 |
| | DELIVERY_ORDER | 2 |
| | ORDER | 3 |

### 3. 实体类改造

实体类中的枚举字段从`String`类型改为对应的枚举类型。

#### PaymentEntity

```java
// 改造前
@TableField("payment_type")
private String paymentType;

@TableField("payment_status")
private String paymentStatus;

// 改造后
@TableField("payment_type")
private PaymentType paymentType;

@TableField("payment_status")
private PaymentStatus paymentStatus;
```

#### PaymentTransactionEntity

```java
// 改造前
@TableField("transaction_type")
private String transactionType;

@TableField("payment_channel")
private String paymentChannel;

// 改造后
@TableField("transaction_type")
private TransactionType transactionType;

@TableField("payment_channel")
private PaymentChannel paymentChannel;
```

### 4. MyBatis-Plus配置

在`application.yml`中添加枚举处理配置：

```yaml
mybatis-plus:
  configuration:
    # 默认枚举处理器
    default-enum-type-handler: org.apache.ibatis.type.EnumTypeHandler
  # 扫描枚举包
  type-enums-package: com.bytz.modules.cms.payment.domain.enums
```

### 5. InfrastructureAssembler简化

移除了所有手动枚举转换方法（约80行代码），MyBatis-Plus现在自动处理枚举转换：

```java
// 改造前
@Mapping(target = "paymentType", source = "paymentType", qualifiedByName = "paymentTypeToString")
@Mapping(target = "paymentStatus", source = "paymentStatus", qualifiedByName = "paymentStatusToString")
PaymentEntity toPaymentEntity(PaymentAggregate aggregate);

@Named("paymentTypeToString")
default String paymentTypeToString(PaymentType paymentType) {
    return paymentType != null ? paymentType.name() : null;
}

// 改造后 - 不需要手动转换
PaymentEntity toPaymentEntity(PaymentAggregate aggregate);
```

## 使用指南

### 1. 获取枚举Code

```java
PaymentStatus status = PaymentStatus.PAID;
Integer code = status.getCode();  // 返回 3
Integer value = status.getValue(); // 返回 3 (IEnum接口方法)
```

### 2. 数据库存储

数据库中存储的是枚举的`code`值（Integer类型），MyBatis-Plus会自动处理转换：

```sql
-- 数据库中存储
INSERT INTO cms_payment (payment_status) VALUES (3);

-- Java代码中自动转换为
PaymentStatus.PAID
```

### 3. 业务编码使用

`code`字段可用于：
- 与外部系统对接时的编码标识
- 前端展示时的状态码
- 业务流程中的条件判断

```java
// 根据code判断
if (payment.getPaymentStatus().getCode() == 3) {
    // 已支付状态
}

// 返回给前端
{
    "paymentStatus": 3,
    "paymentStatusDesc": "已支付"
}
```

## 数据迁移说明

**重要提示**：本次改造涉及数据库字段类型变更，需要执行数据迁移。

### 数据库变更SQL

```sql
-- 1. PaymentEntity相关字段
ALTER TABLE cms_payment 
    MODIFY COLUMN payment_type INT COMMENT '支付类型(1:预付款 2:尾款 3:其他费用 4:信用还款)',
    MODIFY COLUMN payment_status INT COMMENT '支付状态(0:未支付 1:支付中 2:部分支付 3:已支付 4:支付失败 5:已停止 6:已冻结)',
    MODIFY COLUMN refund_status INT COMMENT '退款状态(0:未退款 1:退款中 2:部分退款 3:全额退款 4:退款失败)',
    MODIFY COLUMN related_business_type INT COMMENT '关联业务类型(1:信用记录 2:提货单 3:订单)';

-- 2. PaymentTransactionEntity相关字段  
ALTER TABLE cms_payment_transaction
    MODIFY COLUMN transaction_type INT COMMENT '流水类型(1:支付 2:退款)',
    MODIFY COLUMN transaction_status INT COMMENT '流水状态(0:处理中 1:成功 2:失败)',
    MODIFY COLUMN payment_channel INT COMMENT '支付渠道(1:线上支付 2:钱包支付 3:电汇支付 4:信用账户)';
```

### 数据转换脚本

如果数据库中已有数据，需要先将字符串值转换为对应的code值。

```sql
-- 示例：转换PaymentStatus
UPDATE cms_payment SET payment_status = 
    CASE payment_status
        WHEN 'UNPAID' THEN 0
        WHEN 'PAYING' THEN 1
        WHEN 'PARTIAL_PAID' THEN 2
        WHEN 'PAID' THEN 3
        WHEN 'FAILED' THEN 4
        WHEN 'STOPPED' THEN 5
        WHEN 'FROZEN' THEN 6
    END;
```

## 优势

1. **代码简化**：移除了大量手动转换代码
2. **类型安全**：编译期检查，避免字符串拼写错误
3. **自动转换**：MyBatis-Plus自动处理枚举与数据库字段的转换
4. **业务编码**：code字段便于与外部系统对接和业务流程控制
5. **易于维护**：枚举定义集中，修改方便

## 注意事项

1. 数据库字段类型需要从VARCHAR改为INT
2. 已有数据需要执行迁移脚本
3. code值一旦分配，不应该修改，以保证数据一致性
4. 新增枚举值时，需要分配唯一的code值

## 测试验证

所有51个单元测试通过，确保：
- 枚举定义正确
- MapStruct转换正常
- 业务逻辑不受影响

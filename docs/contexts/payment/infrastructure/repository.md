# Payment Context - 基础设施层架构设计

## 文档信息
| 项目 | 内容 |
|------|------|
| **文档名称** | Payment基础设施层架构设计文档 |
| **文档版本** | v6.0 |
| **创建日期** | 2025年9月28日 |
| **更新日期** | 2025年1月 |
| **需求基准** | 支付模块需求设计文档 v1.5 |
| **用例基准** | 支付模块用例模型文档 v1.0 |
| **术语基准** | 全局词汇表 v3.0 |
| **领域基准** | Payment领域层设计 v6.0 |

> **设计原则**: 基于需求文档数据模型和领域层Repository接口，设计清晰的基础设施架构蓝图
> **架构重点**: 数据接口清晰性、持久化架构纯净性、集成契约标准化
> **技术栈**: Spring Boot + MyBatis-Plus + MySQL + Redis + RocketMQ

## Repository接口架构概览

基于需求设计文档第4.4节数据模型和领域层Repository接口定义，建立完整的基础设施架构映射：

| Repository接口 | 实现规范类 | 聚合根 | 数据库表 | Mapper接口 | 接口复杂度 |
|---|---|---|---|---|---|
| PaymentRepository | PaymentRepositoryImpl | Payment | cms_payment | PaymentMapper | 高复杂度 |
| - | PaymentTransactionService | PaymentTransaction | cms_payment_transaction | PaymentTransactionMapper | 中复杂度 |

> **架构设计原则**: 
> - PaymentTransaction作为Payment聚合内实体，通过PaymentRepository统一管理数据一致性
> - 所有Repository接口在领域层定义，实现在基础设施层
> - 数据库表命名遵循需求文档4.4.3节的命名规范（cms_前缀）
> - 严格遵循领域驱动设计的分层架构原则

## Repository接口设计规范详情

### PaymentRepository

#### 接口基本信息
- **接口名称**: PaymentRepository (领域层定义)
- **实现规范名**: PaymentRepositoryImpl (基础设施层实现)
- **管理聚合**: Payment (支付单聚合根)
- **主数据表**: cms_payment (遵循需求文档表命名规范)
- **关联表**: cms_payment_transaction (交易流水表)  
- **MyBatis Mapper**: PaymentMapper, PaymentTransactionMapper
- **接口复杂度**: 高复杂度 (聚合根完整管理)

#### 核心接口方法规范

基于领域层PaymentRepository接口定义，提供完整的实现规范和数据契约：

**基础CRUD接口**:
```java
// 保存聚合根 (包含所有内部实体)
Payment save(Payment aggregate);

// 根据支付单ID查找完整聚合  
Optional<Payment> findById(PaymentId id);

// 软删除聚合根 (设置删除标识，保持审计完整性)
void delete(Payment aggregate);

// 检查支付单存在性 (排除已删除记录)
boolean exists(PaymentId id);
```

> **实现要求**: 所有CRUD操作必须保证聚合内部一致性，支付单和交易流水必须在同一事务内操作

**业务查询接口**:
基于需求文档4.7节查询需求和领域层接口定义：

```java
// 根据订单ID查找支付单 (需求文档4.7.1 单条查询)
List<Payment> findByOrderId(OrderId orderId);

// 根据经销商ID查找支付单 (需求文档4.7.1 批量查询)
List<Payment> findByResellerId(ResellerId resellerId);

// 根据支付状态查找支付单 (需求文档4.7.1 条件筛选)
List<Payment> findByPaymentStatus(PaymentStatus status);

// 根据支付类型查找支付单 (需求文档4.7.1 条件筛选)
List<Payment> findByPaymentType(PaymentType type);

// 根据关联业务查找支付单 (支持信用还款等场景)
List<Payment> findByRelatedBusiness(RelatedBusinessType businessType, String businessId);

// 按金额范围查找支付单 (需求文档4.7.1 条件筛选)
List<Payment> findByAmountRange(BigDecimal minAmount, BigDecimal maxAmount);

// 按时间范围查找支付单 (需求文档4.7.1 条件筛选)
List<Payment> findByDateRange(LocalDateTime startDate, LocalDateTime endDate);

// 查找支付截止时间在指定时间前的支付单 (逾期支付处理)
List<Payment> findByDeadlineBefore(LocalDateTime deadline);

// 查找指定经销商未完成支付的支付单 (支持合并支付筛选)
List<Payment> findUnpaidOrPartialPaid(ResellerId resellerId);

// 查找有待处理退款的支付单 (退款处理监控)
List<Payment> findPendingRefund();
```

> **查询设计原则**: 所有查询方法都基于需求文档的实际业务场景，支持完整的支付单生命周期管理

**批量操作接口**:
支持需求文档4.3节合并支付和批量管理功能：

```java
// 批量保存支付单 (合并支付场景，保证事务一致性)
List<Payment> saveAll(List<Payment> payments);

// 批量查询支付单 (合并支付处理，批量状态检查)
List<Payment> findByIds(List<PaymentId> ids);

// 统计指定状态的支付单数量 (支付状态分布统计)
Long countByStatus(PaymentStatus status);
```

**统计分析接口**:
支持需求文档4.9节对账管理和业务统计需求：

```java
// 计算时间范围内的支付金额总和 (收入统计、业绩分析)
BigDecimal sumAmountByDateRange(LocalDateTime startDate, LocalDateTime endDate);

// 按支付类型和状态统计支付单数量 (支付类型分析、完成率统计)
Long countByPaymentTypeAndStatus(PaymentType type, PaymentStatus status);

// 查找支付金额最高的经销商 (客户价值分析、VIP客户识别)
List<ResellerPaymentSummary> findTopResellersByAmount(Integer limit);
```

> **统计原则**: 使用BigDecimal统一处理金额，避免浮点数精度问题，与需求文档4.4节数据精度要求一致

#### 数据对象设计

基于需求文档4.4.3节数据库表结构设计，建立完整的数据对象映射：

**PaymentDO (支付单数据对象)**:
严格对应需求文档cms_payment表结构设计：

```java
@TableName("cms_payment")
public class PaymentDO {
    @TableId("id")
    private String id;                        // 支付单号 (VARCHAR 32)
    private String orderId;                   // 关联订单号 (VARCHAR 32)
    private String resellerId;                // 经销商ID (VARCHAR 32)
    private BigDecimal paymentAmount;         // 支付金额 (DECIMAL 20,6)
    private BigDecimal paidAmount;            // 已支付金额 (DECIMAL 20,6)
    private BigDecimal refundedAmount;        // 已退款金额 (DECIMAL 20,6)
    // 实际收款金额 = paidAmount - refundedAmount (计算属性，数据库生成列)
    private String currency;                  // 币种 (VARCHAR 3, 默认CNY)
    private String paymentType;               // 支付类型 (VARCHAR 20)
    private String paymentStatus;             // 支付状态 (VARCHAR 20)
    private String refundStatus;              // 退款状态 (VARCHAR 20)
    private String businessDesc;              // 业务描述 (VARCHAR 500)
    private LocalDateTime paymentDeadline;    // 支付截止时间 (DATETIME)
    private Integer priorityLevel;            // 优先级 (TINYINT, 1-高2-中3-低)
    private String relatedBusinessId;         // 关联业务ID (VARCHAR 32)
    private String relatedBusinessType;       // 关联业务类型 (VARCHAR 20)
    private LocalDateTime businessExpireDate; // 业务到期日 (DATETIME)
    private String businessTags;              // 业务标签 (JSON)
    private LocalDateTime createTime;         // 创建时间 (DATETIME)
    private LocalDateTime updateTime;         // 更新时间 (DATETIME)
    private String createBy;                  // 创建人ID (VARCHAR 32)
    private String createByName;              // 创建人姓名 (VARCHAR 50)
    private String updateBy;                  // 更新人ID (VARCHAR 32)
    private String updateByName;              // 更新人姓名 (VARCHAR 50)
    @TableLogic
    private Integer delFlag;                  // 删除标识 (TINYINT 0-正常1-删除)
    @Version
    private Integer version;                  // 乐观锁版本号 (INT)
    
    // 关联交易流水(非数据库字段)
    @TableField(exist = false)
    private List<PaymentTransactionDO> transactions;
    
    // 转换为领域对象
    public Payment toDomain() {
        // 构建货币对象
        Money paymentAmount = new Money(this.paymentAmount, Currency.valueOf(this.currency));
        Money paidAmount = new Money(this.paidAmount, Currency.valueOf(this.currency));
        Money refundedAmount = new Money(this.refundedAmount, Currency.valueOf(this.currency));
        
        // 构建关联业务信息
        RelatedBusinessInfo relatedBusinessInfo = null;
        if (this.relatedBusinessId != null) {
            relatedBusinessInfo = new RelatedBusinessInfo(
                this.relatedBusinessId,
                RelatedBusinessType.valueOf(this.relatedBusinessType),
                this.businessExpireDate
            );
        }
        
        // 构建业务标签
        BusinessTags businessTags = BusinessTags.fromJson(this.businessTags);
        
        // 构建支付单对象
        Payment payment = new Payment(
            new PaymentId(this.id),
            new OrderId(this.orderId),
            new ResellerId(this.resellerId),
            paymentAmount,
            paidAmount,
            refundedAmount,
            PaymentType.valueOf(this.paymentType),
            PaymentStatus.valueOf(this.paymentStatus),
            RefundStatus.valueOf(this.refundStatus),
            this.businessDesc,
            this.paymentDeadline,
            PriorityLevel.valueOf(this.priorityLevel),
            relatedBusinessInfo,
            businessTags,
            this.createTime,
            this.updateTime,
            this.createBy,
            this.createByName,
            this.updateBy,
            this.updateByName
        );
        
        // 添加交易流水
        if (this.transactions != null) {
            List<PaymentTransaction> domainTransactions = this.transactions.stream()
                .map(PaymentTransactionDO::toDomain)
                .collect(Collectors.toList());
            payment.addTransactions(domainTransactions);
        }
        
        return payment;
    }
    
    // 从领域对象转换
    public static PaymentDO fromDomain(Payment payment) {
        PaymentDO paymentDO = new PaymentDO();
        paymentDO.setId(payment.getId().getValue());
        paymentDO.setOrderId(payment.getOrderId().getValue());
        paymentDO.setResellerId(payment.getResellerId().getValue());
        paymentDO.setPaymentAmount(payment.getPaymentAmount().getAmount());
        paymentDO.setPaidAmount(payment.getPaidAmount().getAmount());
        paymentDO.setRefundedAmount(payment.getRefundedAmount().getAmount());
        paymentDO.setCurrency(payment.getCurrency().name());
        paymentDO.setPaymentType(payment.getPaymentType().name());
        paymentDO.setPaymentStatus(payment.getPaymentStatus().name());
        paymentDO.setRefundStatus(payment.getRefundStatus().name());
        paymentDO.setBusinessDesc(payment.getBusinessDesc());
        paymentDO.setPaymentDeadline(payment.getPaymentDeadline());
        paymentDO.setPriorityLevel(payment.getPriorityLevel().getValue());
        
        // 设置关联业务信息
        RelatedBusinessInfo relatedBusinessInfo = payment.getRelatedBusinessInfo();
        if (relatedBusinessInfo != null) {
            paymentDO.setRelatedBusinessId(relatedBusinessInfo.getBusinessId());
            paymentDO.setRelatedBusinessType(relatedBusinessInfo.getBusinessType().name());
            paymentDO.setBusinessExpireDate(relatedBusinessInfo.getExpireDate());
        }
        
        // 设置业务标签
        if (payment.getBusinessTags() != null) {
            paymentDO.setBusinessTags(payment.getBusinessTags().toJsonString());
        }
        
        // 设置系统属性
        paymentDO.setCreateTime(payment.getCreateTime());
        paymentDO.setUpdateTime(payment.getUpdateTime());
        paymentDO.setCreateBy(payment.getCreateBy());
        paymentDO.setCreateByName(payment.getCreateByName());
        paymentDO.setUpdateBy(payment.getUpdateBy());
        paymentDO.setUpdateByName(payment.getUpdateByName());
        paymentDO.setDelFlag(payment.getDelFlag().getValue());
        
        return paymentDO;
    }
}
```

**PaymentTransactionDO (支付交易数据对象)**:
严格对应需求文档cms_payment_transaction表结构设计：

```java
@TableName("cms_payment_transaction")
public class PaymentTransactionDO {
    @TableId("id")
    private String id;                        // 交易单号 (VARCHAR 32)
    private String paymentId;                 // 支付单号 (VARCHAR 32)
    private String thirdPartyTransId;         // 第三方交易号 (VARCHAR 64)
    private String paymentChannel;            // 支付渠道 (VARCHAR 20)
    private String transactionType;           // 交易类型 (VARCHAR 20)
    private BigDecimal transactionAmount;     // 交易金额 (DECIMAL 20,6)
    private String transactionStatus;         // 交易状态 (VARCHAR 20)
    private String failureReason;             // 失败原因 (VARCHAR 500)
    private LocalDateTime requestTime;        // 请求时间 (DATETIME)
    private LocalDateTime responseTime;       // 响应时间 (DATETIME)
    private String requestData;               // 请求数据 (JSON)
    private String responseData;              // 响应数据 (JSON)
    private String notificationData;          // 通知数据 (JSON)
    private Integer retryCount;               // 重试次数 (TINYINT DEFAULT 0)
    private LocalDateTime nextRetryTime;      // 下次重试时间 (DATETIME)
    private String remarks;                   // 备注 (VARCHAR 500)
    private LocalDateTime createTime;         // 创建时间 (DATETIME)
    private LocalDateTime updateTime;         // 更新时间 (DATETIME)
    private String createBy;                  // 创建人ID (VARCHAR 32)
    private String createByName;              // 创建人姓名 (VARCHAR 50)
    private String updateBy;                  // 更新人ID (VARCHAR 32)
    private String updateByName;              // 更新人姓名 (VARCHAR 50)
    @TableLogic
    private Integer delFlag;                  // 删除标识 (TINYINT 0-正常1-删除)
    @Version
    private Integer version;                  // 乐观锁版本号 (INT)
    
    // 转换为领域对象 (基于需求文档业务模型)
    public PaymentTransaction toDomain() {
        return new PaymentTransaction(
            new TransactionId(this.id),
            new PaymentId(this.paymentId),
            this.thirdPartyTransId,
            PaymentChannel.valueOf(this.paymentChannel),
            TransactionType.valueOf(this.transactionType),
            new Money(this.transactionAmount, Currency.CNY),
            TransactionStatus.valueOf(this.transactionStatus),
            this.failureReason,
            this.requestTime,
            this.responseTime,
            this.requestData,
            this.responseData,
            this.notificationData,
            this.retryCount,
            this.nextRetryTime,
            this.remarks,
            this.createTime,
            this.updateTime,
            this.createBy,
            this.createByName,
            this.updateBy,
            this.updateByName
        );
    }
    
    // 从领域对象转换 (基于需求文档业务模型)
    public static PaymentTransactionDO fromDomain(PaymentTransaction transaction) {
        PaymentTransactionDO transactionDO = new PaymentTransactionDO();
        transactionDO.setId(transaction.getId().getValue());
        transactionDO.setPaymentId(transaction.getPaymentId().getValue());
        transactionDO.setThirdPartyTransId(transaction.getThirdPartyTransId());
        transactionDO.setPaymentChannel(transaction.getPaymentChannel().name());
        transactionDO.setTransactionType(transaction.getTransactionType().name());
        transactionDO.setTransactionAmount(transaction.getTransactionAmount().getAmount());
        transactionDO.setTransactionStatus(transaction.getTransactionStatus().name());
        transactionDO.setFailureReason(transaction.getFailureReason());
        transactionDO.setRequestTime(transaction.getRequestTime());
        transactionDO.setResponseTime(transaction.getResponseTime());
        transactionDO.setRequestData(transaction.getRequestData());
        transactionDO.setResponseData(transaction.getResponseData());
        transactionDO.setNotificationData(transaction.getNotificationData());
        transactionDO.setRetryCount(transaction.getRetryCount());
        transactionDO.setNextRetryTime(transaction.getNextRetryTime());
        transactionDO.setRemarks(transaction.getRemarks());
        transactionDO.setCreateTime(transaction.getCreateTime());
        transactionDO.setUpdateTime(transaction.getUpdateTime());
        transactionDO.setCreateBy(transaction.getCreateBy());
        transactionDO.setCreateByName(transaction.getCreateByName());
        transactionDO.setUpdateBy(transaction.getUpdateBy());
        transactionDO.setUpdateByName(transaction.getUpdateByName());
        transactionDO.setVersion(transaction.getVersion());
        transactionDO.setDelFlag(transaction.getDelFlag().getValue());
        
        return transactionDO;
    }
}
```

#### 对象转换规则

**领域对象 -> 数据对象**:
- PaymentId -> String id (支付单号值对象转字符串)
- OrderId -> String orderId (订单号值对象转字符串)
- ResellerId -> String resellerId (经销商ID值对象转字符串)
- Money -> BigDecimal amount + String currency (金额值对象拆解)
- PaymentType -> String paymentType (枚举转字符串)
- PaymentStatus -> String paymentStatus (枚举转字符串)
- RefundStatus -> String refundStatus (枚举转字符串)
- PriorityLevel -> Integer priorityLevel (优先级枚举转数字)
- RelatedBusinessInfo -> String businessId + String businessType + DateTime expireDate (值对象拆解)
- BusinessTags -> String businessTags (值对象JSON序列化)
- PaymentChannel -> String channelCode (支付渠道值对象转代码)
- TransactionId -> String id (流水号值对象转字符串)
- List<PaymentTransaction> -> List<PaymentTransactionDO> (聚合内实体集合转换)

**数据对象 -> 领域对象**:
- 验证必填字段完整性 (id, orderId, resellerId, paymentAmount等)
- 重建值对象 (Money, PaymentId, PaymentChannel等)
- 恢复枚举类型 (PaymentType, PaymentStatus, TransactionType等)
- 重建聚合内部关系 (Payment与PaymentTransaction的父子关系)
- 验证业务不变式 (金额一致性、状态转换合法性等)
- 恢复聚合根的完整性约束

**转换异常处理**:
- 数据格式错误: DataFormatException
- 枚举值无效: InvalidEnumValueException
- 业务规则违反: BusinessRuleViolationException
- 数据完整性异常: DataIntegrityViolationException

#### 查询优化策略

**索引设计**:
- 主键索引: id (支付单号主键)
- 业务单列索引: order_id, reseller_id, payment_status, payment_type, refund_status
- 复合索引: (reseller_id, payment_status), (order_id, payment_type), (payment_status, create_time)
- 时间索引: create_time, update_time, payment_deadline, business_expire_date
- 关联业务索引: (related_business_type, related_business_id)
- 交易流水索引: (payment_id, transaction_type), channel_transaction_number

**查询优化**:
- 避免N+1查询问题: 使用MyBatis Plus的@TableField(exist = false)和自定义ResultMap
- 聚合加载策略: 一次查询加载Payment和PaymentTransaction，避免多次数据库访问
- 分页查询优化: 使用MyBatis Plus的Page插件，支持count查询优化
- 缓存策略: 
  - 支付单详情缓存5分钟 (key: payment:detail:{paymentId})
  - 经销商支付列表缓存1分钟 (key: payment:reseller:{resellerId})
  - 统计数据缓存30分钟 (key: payment:stats:{date})
- 读写分离: 查询操作使用只读数据源，更新操作使用主数据源

**性能监控指标**:
- 查询响应时间: 单表查询<50ms, 关联查询<100ms
- 缓存命中率: >80%
- 数据库连接池使用率: <70%

## MyBatis映射配置

### Mapper接口定义

```java
@Mapper
public interface PaymentMapper extends BaseMapper<PaymentDO> {
    // 基础CRUD由BaseMapper提供
    
    // 业务查询方法
    List<PaymentDO> selectByOrderId(@Param("orderId") String orderId);
    
    List<PaymentDO> selectByResellerId(@Param("resellerId") String resellerId);
    
    List<PaymentDO> selectByStatus(@Param("status") String status);
    
    List<PaymentDO> selectByPaymentType(@Param("paymentType") String paymentType);
    
    List<PaymentDO> selectByRelatedBusiness(@Param("businessType") String businessType, 
                                           @Param("businessId") String businessId);
    
    // 复合条件查询
    List<PaymentDO> selectByAmountRange(@Param("minAmount") BigDecimal minAmount,
                                       @Param("maxAmount") BigDecimal maxAmount);
    
    List<PaymentDO> selectByDateRange(@Param("startDate") LocalDateTime startDate,
                                     @Param("endDate") LocalDateTime endDate);
    
    List<PaymentDO> selectByDeadlineBefore(@Param("deadline") LocalDateTime deadline);
    
    List<PaymentDO> selectUnpaidOrPartialPaid(@Param("resellerId") String resellerId);
    
    List<PaymentDO> selectPendingRefund();
    
    // 批量查询
    List<PaymentDO> selectByIds(@Param("ids") List<String> ids);
    
    // 统计查询
    Long countByStatus(@Param("status") String status);
    
    Long countByPaymentTypeAndStatus(@Param("paymentType") String paymentType,
                                    @Param("status") String status);
    
    BigDecimal sumAmountByDateRange(@Param("startDate") LocalDateTime startDate,
                                   @Param("endDate") LocalDateTime endDate);
    
    List<ResellerPaymentSummaryDO> selectTopResellersByAmount(@Param("limit") Integer limit);
    
    // 关联查询 (包含交易流水)
    PaymentDO selectWithTransactionsById(@Param("id") String id);
    
    List<PaymentDO> selectWithTransactionsByIds(@Param("ids") List<String> ids);
}

@Mapper
public interface PaymentTransactionMapper extends BaseMapper<PaymentTransactionDO> {
    // 基础CRUD由BaseMapper提供
    
    // 按支付单查询流水
    List<PaymentTransactionDO> selectByPaymentId(@Param("paymentId") String paymentId);
    
    // 按第三方交易号查询 (对应需求文档业务流程)
    PaymentTransactionDO selectByThirdPartyTransId(@Param("thirdPartyTransId") String thirdPartyTransId);
    
    // 按交易类型查询
    List<PaymentTransactionDO> selectByTransactionType(@Param("transactionType") String transactionType);
    
    // 按交易状态查询
    List<PaymentTransactionDO> selectByTransactionStatus(@Param("transactionStatus") String transactionStatus);
    
    // 查询失败的交易 (需要重试)
    List<PaymentTransactionDO> selectFailedTransactions(@Param("paymentId") String paymentId);
    
    // 查询待重试的交易
    List<PaymentTransactionDO> selectRetryableTransactions(@Param("maxRetryTime") LocalDateTime maxRetryTime);
    
    // 渠道对账查询
    List<PaymentTransactionDO> selectByDateRangeAndChannel(@Param("startDate") LocalDateTime startDate,
                                                         @Param("endDate") LocalDateTime endDate,
                                                         @Param("channel") String channel);
    
    // 查询未对账交易
    List<PaymentTransactionDO> selectUnreconciledTransactions(@Param("reconcileDate") LocalDateTime reconcileDate);
}
```

### SQL映射示例

```xml
<!-- PaymentMapper.xml -->
<?xml version="1.0" encoding="UTF-8"?>
<!DOCTYPE mapper PUBLIC "-//mybatis.org//DTD Mapper 3.0//EN" "http://mybatis.org/dtd/mybatis-3-mapper.dtd">
<mapper namespace="com.example.payment.infrastructure.mapper.PaymentMapper">

    <!-- 聚合查询ResultMap (基于cms_表结构) -->
    <resultMap id="PaymentWithTransactionsResultMap" type="PaymentDO">
        <id property="id" column="p_id"/>
        <result property="orderId" column="p_order_id"/>
        <result property="resellerId" column="p_reseller_id"/>
        <result property="paymentAmount" column="p_payment_amount"/>
        <result property="paidAmount" column="p_paid_amount"/>
        <result property="refundedAmount" column="p_refunded_amount"/>
        <result property="currency" column="p_currency"/>
        <result property="paymentType" column="p_payment_type"/>
        <result property="paymentStatus" column="p_payment_status"/>
        <result property="refundStatus" column="p_refund_status"/>
        <result property="businessDesc" column="p_business_desc"/>
        <result property="paymentDeadline" column="p_payment_deadline"/>
        <result property="priorityLevel" column="p_priority_level"/>
        <result property="relatedBusinessId" column="p_related_business_id"/>
        <result property="relatedBusinessType" column="p_related_business_type"/>
        <result property="businessExpireDate" column="p_business_expire_date"/>
        <result property="businessTags" column="p_business_tags"/>
        <result property="createTime" column="p_create_time"/>
        <result property="updateTime" column="p_update_time"/>
        <result property="createBy" column="p_create_by"/>
        <result property="createByName" column="p_create_by_name"/>
        <result property="updateBy" column="p_update_by"/>
        <result property="updateByName" column="p_update_by_name"/>
        <result property="delFlag" column="p_del_flag"/>
        <result property="version" column="p_version"/>
        <collection property="transactions" ofType="PaymentTransactionDO">
            <id property="id" column="t_id"/>
            <result property="paymentId" column="t_payment_id"/>
            <result property="thirdPartyTransId" column="t_third_party_trans_id"/>
            <result property="paymentChannel" column="t_payment_channel"/>
            <result property="transactionType" column="t_transaction_type"/>
            <result property="transactionAmount" column="t_transaction_amount"/>
            <result property="transactionStatus" column="t_transaction_status"/>
            <result property="failureReason" column="t_failure_reason"/>
            <result property="requestTime" column="t_request_time"/>
            <result property="responseTime" column="t_response_time"/>
            <result property="requestData" column="t_request_data"/>
            <result property="responseData" column="t_response_data"/>
            <result property="notificationData" column="t_notification_data"/>
            <result property="retryCount" column="t_retry_count"/>
            <result property="nextRetryTime" column="t_next_retry_time"/>
            <result property="remarks" column="t_remarks"/>
            <result property="createTime" column="t_create_time"/>
            <result property="updateTime" column="t_update_time"/>
            <result property="createBy" column="t_create_by"/>
            <result property="createByName" column="t_create_by_name"/>
            <result property="updateBy" column="t_update_by"/>
            <result property="updateByName" column="t_update_by_name"/>
            <result property="delFlag" column="t_del_flag"/>
            <result property="version" column="t_version"/>
        </collection>
    </resultMap>

    <!-- 查询支付单包含交易流水 -->
    <select id="selectWithTransactionsById" resultMap="PaymentWithTransactionsResultMap">
        SELECT 
            p.id as p_id, p.order_id as p_order_id, p.reseller_id as p_reseller_id,
            p.payment_amount as p_payment_amount, p.paid_amount as p_paid_amount, 
            p.refunded_amount as p_refunded_amount, p.currency as p_currency,
            p.payment_type as p_payment_type, p.payment_status as p_payment_status,
            p.refund_status as p_refund_status, p.business_desc as p_business_desc,
            p.payment_deadline as p_payment_deadline, p.priority_level as p_priority_level,
            p.related_business_id as p_related_business_id, p.related_business_type as p_related_business_type,
            p.business_expire_date as p_business_expire_date, p.business_tags as p_business_tags,
            p.create_time as p_create_time, p.update_time as p_update_time,
            p.create_by as p_create_by, p.create_by_name as p_create_by_name,
            p.update_by as p_update_by, p.update_by_name as p_update_by_name,
            p.del_flag as p_del_flag, p.version as p_version,
            t.id as t_id, t.payment_id as t_payment_id, t.transaction_type as t_transaction_type,
            t.transaction_status as t_transaction_status, t.transaction_amount as t_transaction_amount,
            t.payment_channel as t_payment_channel, t.channel_transaction_number as t_channel_transaction_number,
            t.payment_way as t_payment_way, t.original_transaction_id as t_original_transaction_id,
            t.business_order_id as t_business_order_id, t.create_time as t_create_time,
            t.complete_datetime as t_complete_datetime, t.expiration_time as t_expiration_time,
            t.business_remark as t_business_remark, t.create_by as t_create_by,
            t.create_by_name as t_create_by_name, t.update_by as t_update_by,
            t.update_by_name as t_update_by_name, t.update_time as t_update_time, t.del_flag as t_del_flag
        FROM cms_payment p
        LEFT JOIN cms_payment_transaction t ON p.id = t.payment_id AND t.del_flag = 0
        WHERE p.id = #{id} AND p.del_flag = 0
    </select>

    <!-- 按经销商ID查询 (基于cms_表结构) -->
    <select id="selectByResellerId" resultType="PaymentDO">
        SELECT * FROM cms_payment 
        WHERE reseller_id = #{resellerId} AND del_flag = 0
        ORDER BY create_time DESC
    </select>

    <!-- 按关联业务查询 (基于cms_表结构) -->
    <select id="selectByRelatedBusiness" resultType="PaymentDO">
        SELECT * FROM cms_payment 
        WHERE related_business_type = #{businessType} 
        AND related_business_id = #{businessId} 
        AND del_flag = 0
        ORDER BY create_time DESC
    </select>

    <!-- 查询未完成支付的支付单 (基于cms_表结构) -->
    <select id="selectUnpaidOrPartialPaid" resultType="PaymentDO">
        SELECT * FROM cms_payment 
        WHERE reseller_id = #{resellerId} 
        AND payment_status IN ('UNPAID', 'PAYING', 'PARTIAL_PAID')
        AND del_flag = 0
        ORDER BY priority_level ASC, create_time ASC
    </select>

    <!-- 查询有待处理退款的支付单 (基于cms_表结构) -->
    <select id="selectPendingRefund" resultType="PaymentDO">
        SELECT * FROM cms_payment 
        WHERE refund_status IN ('REFUNDING', 'REFUND_FAILED')
        AND del_flag = 0
        ORDER BY create_time ASC
    </select>

    <!-- 统计查询 (基于cms_表结构) -->
    <select id="sumAmountByDateRange" resultType="java.math.BigDecimal">
        SELECT COALESCE(SUM(payment_amount), 0) 
        FROM cms_payment 
        WHERE create_time BETWEEN #{startDate} AND #{endDate}
        AND del_flag = 0
    </select>

    <!-- 查找支付金额最高的经销商 (基于cms_表结构) -->
    <select id="selectTopResellersByAmount" resultType="ResellerPaymentSummaryDO">
        SELECT 
            reseller_id,
            COUNT(*) as payment_count,
            SUM(payment_amount) as total_amount,
            SUM(paid_amount) as total_paid_amount
        FROM cms_payment 
        WHERE del_flag = 0
        GROUP BY reseller_id
        ORDER BY total_amount DESC
        LIMIT #{limit}
    </select>

</mapper>

<!-- PaymentTransactionMapper.xml -->
<?xml version="1.0" encoding="UTF-8"?>
<!DOCTYPE mapper PUBLIC "-//mybatis.org//DTD Mapper 3.0//EN" "http://mybatis.org/dtd/mybatis-3-mapper.dtd">
<mapper namespace="com.example.payment.infrastructure.mapper.PaymentTransactionMapper">

    <!-- 按第三方交易号查询 (基于cms_表结构) -->
    <select id="selectByThirdPartyTransId" resultType="PaymentTransactionDO">
        SELECT * FROM cms_payment_transaction 
        WHERE third_party_trans_id = #{thirdPartyTransId} 
        AND del_flag = 0
    </select>

    <!-- 查询失败的交易 (需要重试) -->
    <select id="selectFailedTransactions" resultType="PaymentTransactionDO">
        SELECT * FROM cms_payment_transaction 
        WHERE payment_id = #{paymentId} 
        AND transaction_status IN ('FAILED', 'TIMEOUT')
        AND del_flag = 0
        ORDER BY create_time DESC
    </select>

    <!-- 渠道对账查询 (基于cms_表结构) -->
    <select id="selectByDateRangeAndChannel" resultType="PaymentTransactionDO">
        SELECT * FROM cms_payment_transaction 
        WHERE create_time BETWEEN #{startDate} AND #{endDate}
        AND payment_channel = #{channel}
        AND del_flag = 0
        ORDER BY create_time ASC
    </select>

</mapper>
```

## 数据库设计

### 表结构设计

```sql
-- 支付单主表 (基于需求文档4.4.3节cms_payment表设计)
CREATE TABLE cms_payment (
    id VARCHAR(32) PRIMARY KEY COMMENT '支付单号',
    order_id VARCHAR(32) NOT NULL COMMENT '关联订单号', 
    reseller_id VARCHAR(32) NOT NULL COMMENT '经销商ID',
    payment_amount DECIMAL(20,6) NOT NULL COMMENT '支付金额',
    paid_amount DECIMAL(20,6) NOT NULL DEFAULT 0.000000 COMMENT '已支付金额',
    refunded_amount DECIMAL(20,6) NOT NULL DEFAULT 0.000000 COMMENT '已退款金额',
    actual_amount DECIMAL(20,6) GENERATED ALWAYS AS (paid_amount - refunded_amount) STORED COMMENT '实际收款金额',
    currency VARCHAR(3) NOT NULL DEFAULT 'CNY' COMMENT '币种',
    payment_type VARCHAR(20) NOT NULL COMMENT '支付类型:ADVANCE_PAYMENT,FINAL_PAYMENT,OTHER_FEE,CREDIT_REPAYMENT',
    payment_status VARCHAR(20) NOT NULL COMMENT '支付状态:UNPAID,PAYING,PARTIAL_PAID,PAID,FAILED,STOPPED,FROZEN',
    refund_status VARCHAR(20) NOT NULL DEFAULT 'NO_REFUND' COMMENT '退款状态:NO_REFUND,REFUNDING,PARTIAL_REFUNDED,FULL_REFUNDED,REFUND_FAILED',
    business_desc VARCHAR(500) COMMENT '业务描述',
    payment_deadline DATETIME COMMENT '支付截止时间',
    priority_level TINYINT NOT NULL DEFAULT 2 COMMENT '优先级:1-高,2-中,3-低',
    related_business_id VARCHAR(32) COMMENT '关联业务ID',
    related_business_type VARCHAR(20) COMMENT '关联业务类型:CREDIT_RECORD,DELIVERY_ORDER,ADDITIONAL_SERVICE',
    business_expire_date DATETIME COMMENT '业务到期日',
    business_tags JSON COMMENT '业务标签',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    create_by VARCHAR(32) NOT NULL COMMENT '创建人ID',
    create_by_name VARCHAR(50) NOT NULL COMMENT '创建人姓名',
    update_by VARCHAR(32) COMMENT '更新人ID',
    update_by_name VARCHAR(50) COMMENT '更新人姓名',
    del_flag TINYINT NOT NULL DEFAULT 0 COMMENT '删除标识:0-正常,1-删除',
    version INT NOT NULL DEFAULT 0 COMMENT '乐观锁版本号',
    
    -- 约束定义
    CONSTRAINT chk_payment_amount CHECK (payment_amount > 0),
    CONSTRAINT chk_paid_amount CHECK (paid_amount >= 0 AND paid_amount <= payment_amount),
    CONSTRAINT chk_refunded_amount CHECK (refunded_amount >= 0 AND refunded_amount <= paid_amount),
    CONSTRAINT chk_priority_level CHECK (priority_level IN (1, 2, 3)),
    
    -- 外键约束(如果需要)
    -- FOREIGN KEY (order_id) REFERENCES orders(id),
    -- FOREIGN KEY (reseller_id) REFERENCES resellers(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='支付单表';

-- 支付交易流水表 (基于需求文档4.4.3节cms_payment_transaction表设计)
CREATE TABLE cms_payment_transaction (
    id VARCHAR(32) PRIMARY KEY COMMENT '交易流水号',
    payment_id VARCHAR(32) NOT NULL COMMENT '支付单号',
    third_party_trans_id VARCHAR(64) COMMENT '第三方交易号',
    payment_channel VARCHAR(20) NOT NULL COMMENT '支付渠道:ALIPAY,WECHAT,BANK_CARD,ENTERPRISE_ACCOUNT',
    transaction_type VARCHAR(20) NOT NULL COMMENT '交易类型:PAYMENT,REFUND,TRANSFER',
    transaction_amount DECIMAL(20,6) NOT NULL COMMENT '交易金额',
    transaction_status VARCHAR(20) NOT NULL COMMENT '交易状态:PENDING,PROCESSING,SUCCESS,FAILED,TIMEOUT,CANCELLED',
    failure_reason VARCHAR(500) COMMENT '失败原因',
    request_time DATETIME COMMENT '请求时间',
    response_time DATETIME COMMENT '响应时间',
    request_data JSON COMMENT '请求数据',
    response_data JSON COMMENT '响应数据',
    notification_data JSON COMMENT '通知数据',
    retry_count TINYINT DEFAULT 0 COMMENT '重试次数',
    next_retry_time DATETIME COMMENT '下次重试时间',
    remarks VARCHAR(500) COMMENT '备注',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    create_by VARCHAR(32) NOT NULL COMMENT '创建人ID',
    create_by_name VARCHAR(50) NOT NULL COMMENT '创建人姓名',
    update_by VARCHAR(32) COMMENT '更新人ID',
    update_by_name VARCHAR(50) COMMENT '更新人姓名',
    del_flag TINYINT NOT NULL DEFAULT 0 COMMENT '删除标识:0-正常,1-删除',
    version INT NOT NULL DEFAULT 0 COMMENT '乐观锁版本号',
    
    -- 约束定义
    CONSTRAINT chk_transaction_amount CHECK (transaction_amount != 0),
    CONSTRAINT chk_retry_count CHECK (retry_count >= 0 AND retry_count <= 10),
    
    -- 外键约束
    FOREIGN KEY (payment_id) REFERENCES cms_payment(id) ON DELETE CASCADE,
    INDEX idx_fk_payment_id (payment_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='支付交易流水表';

-- cms_payment表索引 (基于需求文档业务查询需求)
-- 主键索引
-- PRIMARY KEY (id) 自动创建

-- 单列索引
CREATE INDEX idx_cms_payment_order_id ON cms_payment(order_id) COMMENT '订单号索引';
CREATE INDEX idx_cms_payment_reseller_id ON cms_payment(reseller_id) COMMENT '经销商ID索引';
CREATE INDEX idx_cms_payment_status ON cms_payment(payment_status) COMMENT '支付状态索引';
CREATE INDEX idx_cms_payment_type ON cms_payment(payment_type) COMMENT '支付类型索引';
CREATE INDEX idx_cms_payment_refund_status ON cms_payment(refund_status) COMMENT '退款状态索引';
CREATE INDEX idx_payment_create_time ON payment(create_time) COMMENT '创建时间索引';
CREATE INDEX idx_payment_deadline ON payment(payment_deadline) COMMENT '支付截止时间索引';
CREATE INDEX idx_payment_business_expire_date ON payment(business_expire_date) COMMENT '业务到期日索引';

-- 复合索引
CREATE INDEX idx_payment_reseller_status ON payment(reseller_id, payment_status) COMMENT '经销商支付状态复合索引';
CREATE INDEX idx_payment_order_type ON payment(order_id, payment_type) COMMENT '订单支付类型复合索引';
CREATE INDEX idx_payment_status_time ON payment(payment_status, create_time) COMMENT '支付状态时间复合索引';
CREATE INDEX idx_payment_related_business ON payment(related_business_type, related_business_id) COMMENT '关联业务复合索引';
CREATE INDEX idx_payment_del_flag_status ON payment(del_flag, payment_status) COMMENT '删除标识状态复合索引';

-- 交易流水表索引
-- 单列索引
CREATE INDEX idx_transaction_payment_id ON payment_transaction(payment_id) COMMENT '支付单号索引';
CREATE INDEX idx_transaction_channel_number ON payment_transaction(channel_transaction_number) COMMENT '渠道交易号索引';
CREATE INDEX idx_transaction_type ON payment_transaction(transaction_type) COMMENT '交易类型索引';
CREATE INDEX idx_transaction_status ON payment_transaction(transaction_status) COMMENT '交易状态索引';
CREATE INDEX idx_transaction_channel ON payment_transaction(payment_channel) COMMENT '支付渠道索引';
CREATE INDEX idx_transaction_create_time ON payment_transaction(create_time) COMMENT '创建时间索引';
CREATE INDEX idx_transaction_complete_time ON payment_transaction(complete_datetime) COMMENT '完成时间索引';

-- 复合索引
CREATE INDEX idx_transaction_type_status ON payment_transaction(transaction_type, transaction_status) COMMENT '交易类型状态复合索引';
CREATE INDEX idx_transaction_channel_time ON payment_transaction(payment_channel, create_time) COMMENT '渠道时间复合索引';
CREATE INDEX idx_transaction_payment_type ON payment_transaction(payment_id, transaction_type) COMMENT '支付单交易类型复合索引';
CREATE INDEX idx_transaction_del_flag_status ON payment_transaction(del_flag, transaction_status) COMMENT '删除标识状态复合索引';

-- 分区表设计(可选，大数据量时考虑)
-- ALTER TABLE payment_transaction PARTITION BY RANGE (YEAR(create_time)) (
--     PARTITION p2024 VALUES LESS THAN (2025),
--     PARTITION p2025 VALUES LESS THAN (2026),
--     PARTITION p_future VALUES LESS THAN MAXVALUE
-- );
```

### 数据一致性保证

**事务策略**:
- Repository方法不包含@Transactional注解，保持技术无关性
- 事务边界由应用层PaymentApplicationService统一控制
- 聚合内部一致性：支付单和交易流水在同一事务内更新
- 跨聚合一致性：通过领域事件实现最终一致性
- 分布式事务：使用Saga模式处理跨系统事务

**并发控制**:
- 乐观锁：使用version字段防止更新冲突，适合大部分业务场景
- 悲观锁：重要金额操作时使用SELECT FOR UPDATE，如合并支付
- 重试机制：乐观锁冲突时自动重试，最多3次
- 防重处理：基于业务键（如channelTransactionNumber）实现幂等

**数据完整性约束**:
- 数据库约束：通过CHECK约束保证金额合法性
- 外键约束：确保交易流水与支付单的关联关系
- 唯一性约束：防止重复的渠道交易号
- 业务规则约束：在领域层和数据库层双重保障

**容错机制**:
- 自动重试：数据库死锁、连接超时等场景自动重试
- 降级策略：数据库不可用时使用缓存提供只读服务
- 数据修复：定期检查和修复数据一致性问题

## 外部系统集成

### 集成架构设计

**防腐层模式 (Anti-Corruption Layer)**:
- 为每个外部系统设计独立的适配器层
- 将外部系统的数据结构转换为本域的领域对象
- 隔离外部系统变更对支付域的影响
- 提供统一的异常处理和重试机制

### 外部系统集成详情

#### 1. 订单系统集成

**集成场景**:
- 支付单创建时验证订单信息
- 支付完成后通知订单系统
- 退款完成后更新订单状态

**集成方式**:
```java
// 订单系统适配器接口
public interface OrderSystemAdapter {
    /**
     * 查询订单信息
     * @param orderId 订单ID
     * @return 订单信息
     * @throws OrderNotFoundException 订单不存在
     * @throws OrderSystemException 订单系统异常
     */
    OrderInfo queryOrderInfo(OrderId orderId);
    
    /**
     * 验证订单支付条件
     * @param orderId 订单ID
     * @param paymentAmount 支付金额
     * @return 验证结果
     */
    OrderPaymentValidationResult validateOrderPayment(OrderId orderId, Money paymentAmount);
    
    /**
     * 通知订单系统支付状态变更
     * @param notification 支付状态通知
     */
    void notifyPaymentStatus(PaymentStatusNotification notification);
    
    /**
     * 通知订单系统退款完成
     * @param notification 退款通知
     */
    void notifyRefundCompleted(RefundNotification notification);
}

// 适配器实现
@Component
public class OrderSystemAdapterImpl implements OrderSystemAdapter {
    private final RestTemplate restTemplate;
    private final CircuitBreaker circuitBreaker;
    private final OrderSystemConfig config;
    
    @Override
    @Retryable(value = {OrderSystemException.class}, maxAttempts = 3)
    public OrderInfo queryOrderInfo(OrderId orderId) {
        try {
            String url = config.getBaseUrl() + "/orders/" + orderId.getValue();
            OrderResponse response = circuitBreaker.executeSupplier(() -> 
                restTemplate.getForObject(url, OrderResponse.class)
            );
            
            if (response == null) {
                throw new OrderNotFoundException("订单不存在: " + orderId.getValue());
            }
            
            return OrderDataConverter.toOrderInfo(response);
        } catch (ResourceAccessException e) {
            throw new OrderSystemException("订单系统连接异常", e);
        } catch (HttpClientErrorException.NotFound e) {
            throw new OrderNotFoundException("订单不存在: " + orderId.getValue());
        }
    }
}
```

#### 2. 支付渠道集成

**支持的渠道类型**:
- 线上支付渠道 (银联、网银)
- 钱包支付渠道 (企业资金账户)
- 电汇支付渠道 (银行转账凭证)
- 信用支付渠道 (企业信用额度)

**渠道适配器设计**:
```java
// 支付渠道适配器接口
public interface PaymentChannelAdapter {
    /**
     * 获取支持的渠道类型
     */
    PaymentChannel getSupportedChannel();
    
    /**
     * 执行支付操作
     * @param request 支付请求
     * @return 支付结果
     */
    PaymentExecutionResult executePayment(PaymentExecutionRequest request);
    
    /**
     * 执行退款操作
     * @param request 退款请求
     * @return 退款结果
     */
    RefundExecutionResult executeRefund(RefundExecutionRequest request);
    
    /**
     * 查询支付状态
     * @param channelTransactionId 渠道交易号
     * @return 支付状态
     */
    PaymentStatusQueryResult queryPaymentStatus(String channelTransactionId);
    
    /**
     * 处理异步回调
     * @param callback 回调数据
     * @return 处理结果
     */
    CallbackProcessResult processCallback(PaymentCallback callback);
    
    /**
     * 渠道健康检查
     * @return 健康状态
     */
    ChannelHealthStatus checkHealth();
}

// 线上支付渠道适配器
@Component
public class OnlinePaymentChannelAdapter implements PaymentChannelAdapter {
    private final UnionPayClient unionPayClient;
    private final PaymentChannelConfig config;
    
    @Override
    public PaymentChannel getSupportedChannel() {
        return PaymentChannel.ONLINE_PAYMENT;
    }
    
    @Override
    public PaymentExecutionResult executePayment(PaymentExecutionRequest request) {
        try {
            // 构建银联支付请求
            UnionPayRequest unionPayRequest = UnionPayRequestBuilder.builder()
                .merchantId(config.getMerchantId())
                .orderId(request.getPaymentId().getValue())
                .amount(request.getAmount().getAmount())
                .currency(request.getAmount().getCurrency().name())
                .notifyUrl(config.getNotifyUrl())
                .returnUrl(config.getReturnUrl())
                .build();
                
            // 调用银联接口
            UnionPayResponse response = unionPayClient.pay(unionPayRequest);
            
            // 转换响应结果
            return PaymentExecutionResult.builder()
                .success(response.isSuccess())
                .channelTransactionId(response.getTransactionId())
                .paymentUrl(response.getPaymentUrl())
                .message(response.getMessage())
                .build();
                
        } catch (Exception e) {
            return PaymentExecutionResult.failure("线上支付执行失败: " + e.getMessage());
        }
    }
}

// 钱包支付渠道适配器
@Component  
public class WalletPaymentChannelAdapter implements PaymentChannelAdapter {
    private final WalletAccountService walletService;
    
    @Override
    public PaymentChannel getSupportedChannel() {
        return PaymentChannel.WALLET_PAYMENT;
    }
    
    @Override
    public PaymentExecutionResult executePayment(PaymentExecutionRequest request) {
        try {
            // 检查账户余额
            WalletAccount account = walletService.getAccount(request.getResellerId());
            if (!account.hasEnoughBalance(request.getAmount())) {
                return PaymentExecutionResult.failure("账户余额不足");
            }
            
            // 执行扣款
            DebitResult debitResult = walletService.debit(
                request.getResellerId(),
                request.getAmount(),
                request.getPaymentId().getValue()
            );
            
            return PaymentExecutionResult.builder()
                .success(debitResult.isSuccess())
                .channelTransactionId(debitResult.getTransactionId())
                .message(debitResult.getMessage())
                .build();
                
        } catch (Exception e) {
            return PaymentExecutionResult.failure("钱包支付执行失败: " + e.getMessage());
        }
    }
}
```

#### 3. 信用管理系统集成

**集成场景**:
- 信用还款支付时查询信用记录
- 信用还款完成后更新信用状态
- 信用额度变更通知

**集成方式**:
```java
// 信用管理系统适配器
public interface CreditManagementAdapter {
    /**
     * 查询信用记录
     * @param creditRecordId 信用记录ID
     * @return 信用记录信息
     */
    CreditRecordInfo queryCreditRecord(String creditRecordId);
    
    /**
     * 验证信用还款
     * @param creditRecordId 信用记录ID
     * @param repaymentAmount 还款金额
     * @return 验证结果
     */
    CreditRepaymentValidationResult validateRepayment(String creditRecordId, Money repaymentAmount);
    
    /**
     * 通知信用还款完成
     * @param notification 还款通知
     */
    void notifyRepaymentCompleted(CreditRepaymentNotification notification);
    
    /**
     * 查询信用额度状态
     * @param resellerId 经销商ID
     * @return 信用额度信息
     */
    CreditLimitInfo queryCreditLimit(ResellerId resellerId);
}
```

#### 4. 通知系统集成

**集成场景**:
- 支付成功/失败通知
- 退款完成通知
- 异常状态预警

**集成方式**:
```java
// 通知系统适配器
public interface NotificationAdapter {
    /**
     * 发送支付通知
     * @param notification 支付通知
     */
    void sendPaymentNotification(PaymentNotification notification);
    
    /**
     * 发送退款通知
     * @param notification 退款通知
     */
    void sendRefundNotification(RefundNotification notification);
    
    /**
     * 发送异常预警
     * @param alert 预警信息
     */
    void sendAlert(PaymentAlert alert);
}
```

### 适配器工厂设计

**渠道适配器工厂**:
```java
@Component
public class PaymentChannelAdapterFactory {
    private final Map<PaymentChannel, PaymentChannelAdapter> adapters;
    
    public PaymentChannelAdapterFactory(List<PaymentChannelAdapter> adapterList) {
        this.adapters = adapterList.stream()
            .collect(Collectors.toMap(
                PaymentChannelAdapter::getSupportedChannel,
                Function.identity()
            ));
    }
    
    public PaymentChannelAdapter getAdapter(PaymentChannel channel) {
        PaymentChannelAdapter adapter = adapters.get(channel);
        if (adapter == null) {
            throw new UnsupportedPaymentChannelException("不支持的支付渠道: " + channel);
        }
        return adapter;
    }
    
    public List<PaymentChannel> getSupportedChannels() {
        return new ArrayList<>(adapters.keySet());
    }
}
```

## 技术选型说明

### 持久化技术栈
- **ORM框架**: MyBatis-Plus 3.5.x
  - 优势: 提供增强的CRUD功能，支持代码生成，Lambda查询
  - 配置: 自动分页插件，乐观锁插件，逻辑删除插件
- **数据库**: MySQL 8.0+
  - 优势: 支持JSON字段，生成列，CTE查询，窗口函数
  - 配置: InnoDB引擎，utf8mb4字符集，默认隔离级别READ-COMMITTED
- **连接池**: HikariCP
  - 优势: 高性能，轻量级，Spring Boot默认连接池
  - 配置: 主库20连接，从库10连接，连接超时30秒
- **缓存**: Redis Cluster 6.0+
  - 优势: 高可用，分片存储，支持复杂数据结构
  - 配置: 3主3从集群，连接池20个连接，超时2秒

### 集成技术栈
- **HTTP客户端**: RestTemplate + Resilience4j
  - 熔断器: 5秒窗口，50%失败率触发熔断
  - 重试: 指数退避，最多3次重试
  - 超时: 连接超时5秒，读取超时10秒
- **消息队列**: RocketMQ 4.9.x
  - 生产者: 同步发送，3秒超时，失败重试2次
  - 消费者: 集群消费，最大重试16次，死信队列处理
  - 顺序消息: 按支付单ID分区，保证同一支付单事件有序
- **配置管理**: Nacos 2.0+
  - 动态配置: 支付渠道配置，限额配置，开关配置
  - 服务发现: 外部系统服务地址发现
  - 配置热更新: 支持运行时配置变更
- **监控**: Micrometer + Prometheus + Grafana
  - 指标收集: JVM指标，业务指标，数据库指标
  - 告警规则: 支付失败率，处理时延，系统资源

### 序列化技术
- **JSON处理**: Jackson 2.13.x
  - 配置: 时间格式统一，空值处理，枚举序列化
  - 特性: 支持Java 8时间API，多态类型处理
- **事件序列化**: Avro Schema Registry
  - 优势: Schema进化，高性能，跨语言支持
  - 配置: 事件版本控制，向后兼容性检查
- **缓存序列化**: Kryo 5.x
  - 优势: 高性能二进制序列化，体积小
  - 配置: 预注册类，线程安全，压缩存储

### 安全技术栈
- **数据加密**: AES-256-GCM
  - 敏感字段加密: 渠道密钥，回调签名
  - 传输加密: HTTPS，TLS 1.2+
- **访问控制**: Spring Security + OAuth2
  - 接口认证: JWT Token，角色权限控制
  - 审计日志: 操作记录，数据变更跟踪
- **数据脱敏**: 
  - 日志脱敏: 金额部分脱敏，敏感信息掩码
  - 接口脱敏: 返回数据敏感字段处理

## 性能考虑

### 查询性能优化
- 合理设计数据库索引，覆盖常用查询路径
- 避免深度嵌套查询，使用ResultMap优化关联查询
- 实现读写分离策略，查询操作使用只读数据源
- 使用HikariCP连接池管理数据库连接

### 缓存策略
- **一级缓存**: MyBatis Session缓存 (事务内有效)
- **二级缓存**: Redis分布式缓存 (跨Session共享)
- **缓存更新**: 基于领域事件的缓存失效机制
- **缓存Key设计**: 层次化命名，支持批量失效

```java
// 缓存Key设计示例
public class PaymentCacheKeys {
    private static final String PREFIX = "payment:";
    
    public static String paymentDetail(String paymentId) {
        return PREFIX + "detail:" + paymentId;
    }
    
    public static String paymentsByReseller(String resellerId) {
        return PREFIX + "reseller:" + resellerId;
    }
    
    public static String paymentStatistics(String date) {
        return PREFIX + "stats:" + date;
    }
}
```

### 事务优化
- **事务边界**: 最小化事务范围，避免长事务
- **批量操作**: 使用MyBatis批量插入/更新功能
- **连接管理**: 合理设置连接池大小，避免连接泄漏

## 错误处理

### 异常类型定义
```java
// 数据访问异常基类
public class PaymentDataAccessException extends RuntimeException {
    private final String errorCode;
    private final String operation;
    
    public PaymentDataAccessException(String errorCode, String message, String operation) {
        super(message);
        this.errorCode = errorCode;
        this.operation = operation;
    }
}

// 数据完整性异常
public class PaymentDataIntegrityException extends PaymentDataAccessException {
    public PaymentDataIntegrityException(String message, String operation) {
        super("DATA_INTEGRITY_VIOLATION", message, operation);
    }
}

// 并发更新异常
public class PaymentConcurrencyException extends PaymentDataAccessException {
    public PaymentConcurrencyException(String paymentId) {
        super("OPTIMISTIC_LOCK_FAILURE", 
              "支付单[" + paymentId + "]存在并发更新", 
              "UPDATE_PAYMENT");
    }
}

// 外部集成异常
public class ExternalIntegrationException extends RuntimeException {
    private final String systemName;
    private final String operation;
    
    public ExternalIntegrationException(String systemName, String operation, String message, Throwable cause) {
        super(message, cause);
        this.systemName = systemName;
        this.operation = operation;
    }
}
```

### 异常处理策略
- **Repository层**: 抛出技术相关的数据访问异常
- **适配器层**: 处理外部系统集成异常，转换为领域可理解的异常
- **应用层**: 负责异常转换和统一错误响应
- **领域层**: 保持异常纯净性，只关注业务规则异常

### 错误恢复机制
```java
@Component
public class PaymentRepositoryImpl implements PaymentRepository {
    
    @Retryable(value = {DataAccessException.class}, maxAttempts = 3)
    public Payment save(Payment payment) {
        try {
            PaymentDO paymentDO = PaymentDO.fromDomain(payment);
            paymentMapper.saveOrUpdate(paymentDO);
            return payment;
        } catch (OptimisticLockingFailureException e) {
            throw new PaymentConcurrencyException(payment.getId().getValue());
        } catch (DataIntegrityViolationException e) {
            throw new PaymentDataIntegrityException("支付单数据完整性违反", "SAVE_PAYMENT");
        }
    }
}
```

---

## 配置示例

### MyBatis-Plus配置
```yaml
mybatis-plus:
  # Mapper XML文件位置
  mapper-locations: classpath*:mapper/payment/*Mapper.xml
  # 类型别名包路径
  type-aliases-package: com.example.payment.infrastructure.dataobject
  # MyBatis配置
  configuration:
    # 下划线转驼峰
    map-underscore-to-camel-case: true
    # 开启缓存
    cache-enabled: true
    # 懒加载
    lazy-loading-enabled: true
    # 日志实现
    log-impl: org.apache.ibatis.logging.slf4j.Slf4jImpl
    # JDBC类型映射
    jdbc-type-for-null: NULL
  # 全局配置
  global-config:
    # 数据库配置
    db-config:
      # ID生成策略
      id-type: ASSIGN_ID
      # 逻辑删除字段
      logic-delete-field: delFlag
      logic-delete-value: 1
      logic-not-delete-value: 0
      # 表名前缀
      table-prefix: ""
      # 字段策略
      insert-strategy: NOT_NULL
      update-strategy: NOT_NULL
      select-strategy: NOT_EMPTY
    # 控制台打印banner
    banner: false
```

### 数据源配置
```yaml
spring:
  # 主数据源配置
  datasource:
    primary:
      type: com.zaxxer.hikari.HikariDataSource
      driver-class-name: com.mysql.cj.jdbc.Driver
      url: jdbc:mysql://mysql-master:3306/payment_db?useUnicode=true&characterEncoding=utf8mb4&useSSL=true&serverTimezone=Asia/Shanghai&allowPublicKeyRetrieval=true
      username: ${PAYMENT_DB_USER:payment_user}
      password: ${PAYMENT_DB_PASSWORD:payment_pass}
      hikari:
        pool-name: PaymentPrimaryCP
        maximum-pool-size: 20
        minimum-idle: 5
        connection-timeout: 30000
        idle-timeout: 600000
        max-lifetime: 1800000
        leak-detection-threshold: 60000
        connection-test-query: SELECT 1
    
    # 只读数据源配置
    replica:
      type: com.zaxxer.hikari.HikariDataSource
      driver-class-name: com.mysql.cj.jdbc.Driver
      url: jdbc:mysql://mysql-slave:3306/payment_db?useUnicode=true&characterEncoding=utf8mb4&useSSL=true&serverTimezone=Asia/Shanghai&allowPublicKeyRetrieval=true
      username: ${PAYMENT_DB_READ_USER:payment_read_user}
      password: ${PAYMENT_DB_READ_PASSWORD:payment_read_pass}
      hikari:
        pool-name: PaymentReplicaCP
        maximum-pool-size: 10
        minimum-idle: 3
        connection-timeout: 30000
        idle-timeout: 600000
        max-lifetime: 1800000
        read-only: true
        connection-test-query: SELECT 1
```

### Redis缓存配置
```yaml
spring:
  redis:
    # 集群配置
    cluster:
      nodes: 
        - ${REDIS_NODE_1:redis-node1:6379}
        - ${REDIS_NODE_2:redis-node2:6379}
        - ${REDIS_NODE_3:redis-node3:6379}
        - ${REDIS_NODE_4:redis-node4:6379}
        - ${REDIS_NODE_5:redis-node5:6379}
        - ${REDIS_NODE_6:redis-node6:6379}
      max-redirects: 3
    # 认证
    password: ${REDIS_PASSWORD:payment_redis_pass}
    username: ${REDIS_USERNAME:}
    # 连接池配置
    lettuce:
      pool:
        max-active: 20
        max-idle: 10
        min-idle: 5
        max-wait: 2000ms
      # 集群配置
      cluster:
        refresh:
          adaptive: true
          period: 30s
    # 超时配置
    timeout: 2000ms
    connect-timeout: 5000ms
    # 客户端名称
    client-name: payment-service
    # 序列化配置
    serialization:
      key-serializer: string
      value-serializer: json
      hash-key-serializer: string
      hash-value-serializer: json
```

### RocketMQ配置
```yaml
rocketmq:
  # 名称服务器
  name-server: ${ROCKETMQ_NAME_SERVER:rocketmq-nameserver:9876}
  # 生产者配置
  producer:
    group: payment-producer-group
    send-message-timeout: 3000
    compress-message-body-threshold: 4096
    max-message-size: 4194304
    retry-times-when-send-failed: 2
    retry-times-when-send-async-failed: 2
    retry-another-broker-when-not-store-ok: false
  # 消费者配置
  consumer:
    group: payment-consumer-group
    message-model: CLUSTERING
    consume-from-where: CONSUME_FROM_LAST_OFFSET
    consume-thread-min: 20
    consume-thread-max: 64
    adjust-thread-pool-nums-threshold: 100000
    max-reconsume-times: 16

# 支付渠道配置
payment:
  channels:
    # 银联在线支付
    unionpay:
      merchant-id: ${UNIONPAY_MERCHANT_ID}
      api-key: ${UNIONPAY_API_KEY}
      api-url: ${UNIONPAY_API_URL:https://gateway.unionpay.com/api/v2}
      notify-url: ${PAYMENT_NOTIFY_BASE_URL}/payment/callback/unionpay
      return-url: ${PAYMENT_RETURN_BASE_URL}/payment/return/unionpay
      timeout: 30000ms
      retry-times: 2
      
    # 企业钱包支付
    wallet:
      service-url: ${WALLET_SERVICE_URL:http://wallet-service/api/v1}
      api-key: ${WALLET_API_KEY}
      timeout: 10000ms
      retry-times: 3
      
    # 银行转账
    bank-transfer:
      verification-service-url: ${BANK_TRANSFER_SERVICE_URL:http://verification-service/api/v1}
      api-key: ${BANK_TRANSFER_API_KEY}
      supported-proof-types: ["pdf", "jpg", "png", "jpeg"]
      max-proof-size: 10MB
      
    # 信用账户
    credit:
      service-url: ${CREDIT_SERVICE_URL:http://credit-service/api/v1}
      api-key: ${CREDIT_API_KEY}
      timeout: 15000ms
      health-check-interval: 60s
      
# 外部系统配置
external-systems:
  # 订单系统
  order-system:
    base-url: ${ORDER_SERVICE_URL:http://order-service/api/v1}
    timeout: 10000ms
    retry-times: 3
    circuit-breaker:
      failure-rate-threshold: 50
      slow-call-rate-threshold: 100
      slow-call-duration-threshold: 5000ms
      wait-duration-in-open-state: 30s
      sliding-window-size: 10
      
  # 信用管理系统
  credit-system:
    base-url: ${CREDIT_MANAGEMENT_URL:http://credit-management/api/v1}
    timeout: 15000ms
    retry-times: 2
    
  # 通知系统
  notification-system:
    base-url: ${NOTIFICATION_SERVICE_URL:http://notification-service/api/v1}
    timeout: 5000ms
    retry-times: 1
```

---

## 质量检查清单

### 设计质量验证
- [x] Repository接口在领域层定义,实现在基础设施层
- [x] 数据对象与领域对象职责分离清晰
- [x] 对象转换逻辑完整且双向可转换
- [x] 数据库映射关系正确且优化合理

### 技术实现验证  
- [x] MyBatis配置正确且SQL优化
- [x] 数据库索引设计合理
- [x] 事务边界控制在应用层
- [x] 异常处理符合分层架构原则

### 术语一致性验证
- [x] 所有术语与全局词汇表保持一致
- [x] 技术术语与业务术语映射清晰
- [x] 数据库命名符合约定规范
- [x] 代码注释使用标准业务术语

---

## 部署和运维

### 环境要求
- **JDK**: OpenJDK 11+ 或 Oracle JDK 11+
- **MySQL**: 8.0+ (主从复制配置)
- **Redis**: 6.0+ (集群模式)
- **RocketMQ**: 4.9+ (集群部署)
- **内存**: 最小2GB，推荐4GB+
- **CPU**: 最小2核，推荐4核+

### 监控指标
- **业务指标**: 支付成功率、平均处理时间、异常比例
- **技术指标**: JVM内存、GC频率、数据库连接数、缓存命中率
- **告警阈值**: 支付失败率>5%、响应时间>10s、内存使用>80%

### 运维脚本
```bash
# 数据库初始化
mysql -u root -p < payment_schema.sql

# 应用启动
java -Xms2g -Xmx4g -jar payment-service.jar --spring.profiles.active=prod

# 健康检查
curl -s http://localhost:8080/actuator/health

# 性能监控
curl -s http://localhost:8080/actuator/metrics/payment.success.rate
```

### 故障处理
- **数据库连接异常**: 检查连接池配置，重启数据库连接
- **缓存不可用**: 启用降级模式，直接查询数据库
- **消息队列异常**: 检查网络连接，重启消费者
- **支付渠道异常**: 切换备用渠道，通知相关人员

---

## 质量检查清单

### 设计质量验证
- [x] Repository接口在领域层定义，实现在基础设施层
- [x] 数据对象与领域对象职责分离清晰
- [x] 对象转换逻辑完整且双向可转换
- [x] 数据库映射关系正确且优化合理
- [x] 外部系统适配器设计符合防腐层模式

### 技术实现验证  
- [x] MyBatis-Plus配置正确且SQL优化
- [x] 数据库索引设计合理，覆盖主要查询路径
- [x] 事务边界控制在应用层
- [x] 异常处理符合分层架构原则
- [x] 缓存策略合理，支持数据一致性
- [x] 并发控制机制完善

### 术语一致性验证
- [x] 所有术语与全局词汇表v4.0保持一致
- [x] 技术术语与业务术语映射清晰
- [x] 数据库命名符合约定规范
- [x] 代码注释使用标准业务术语
- [x] 接口定义与领域模型术语统一

### 性能和安全验证
- [x] 查询性能指标明确定义
- [x] 缓存策略支持高并发访问
- [x] 数据库连接池配置合理
- [x] 敏感数据加密和脱敏处理
- [x] 外部系统集成安全机制
- [x] 监控和告警机制完整

---

**文档状态**: ✅ 已完成  
**版本**: v4.0  
**最后更新**: 2025年9月27日  
**术语基准**: 全局词汇表 v4.0  
**上下文基准**: 支付上下文设计 v4.0  
**领域依赖**: 支付领域层设计 v4.0  
**审核状态**: 已完成技术设计

### 文档变更记录
| 版本 | 日期 | 变更内容 | 责任人 |
|------|------|----------|--------|
| v4.0 | 2025-09-27 | 基于最新领域模型重构基础设施层设计，完善外部系统集成和技术选型 | GitHub Copilot |
| v3.0 | 2024-12-19 | 完善Repository实现和外部集成设计 | 系统架构师 |
| v2.0 | 2024-11-15 | 添加缓存策略和性能优化 | 技术负责人 |
| v1.0 | 2024-10-01 | 初版基础设施层设计 | 开发团队 |

### PaymentTransactionDO (支付流水数据对象)

#### DO名称(Data Object Name)
PaymentTransactionDO

#### 对应表名(Table Name)
payment_transaction

#### 字段映射(Field Mapping)
```text
字段名称                类型           长度    约束                   说明
id                     VARCHAR        32      PRIMARY KEY           交易流水ID
payment_order_id       VARCHAR        32      NOT NULL             支付单ID
payment_amount         DECIMAL        15,2    NOT NULL             支付金额
channel_code           VARCHAR        20      NOT NULL             渠道代码
channel_transaction_id VARCHAR        64      NULL                 渠道交易号
transaction_status     VARCHAR        20      NOT NULL             交易状态
payment_method         VARCHAR        20      NOT NULL             支付方式
initiated_at          DATETIME        -       NOT NULL             发起时间
completed_at          DATETIME        -       NULL                 完成时间
failure_reason        VARCHAR        200     NULL                 失败原因
created_at            DATETIME        -       NOT NULL             创建时间
updated_at            DATETIME        -       NOT NULL             更新时间
```

### RefundTransactionDO (退款流水数据对象)

#### DO名称(Data Object Name)
RefundTransactionDO

#### 对应表名(Table Name)
refund_transaction

#### 字段映射(Field Mapping)
```text
字段名称                类型           长度    约束                   说明
id                     VARCHAR        32      PRIMARY KEY           退款流水ID
payment_order_id       VARCHAR        32      NOT NULL             支付单ID
original_transaction_id VARCHAR       32      NOT NULL             原支付流水ID
refund_amount          DECIMAL        15,2    NOT NULL             退款金额
refund_channel         VARCHAR        20      NOT NULL             退款渠道
channel_refund_id      VARCHAR        64      NULL                 渠道退款号
refund_status          VARCHAR        20      NOT NULL             退款状态
refund_reason          VARCHAR        200     NOT NULL             退款原因
order_refund_id        VARCHAR        32      NOT NULL             订单退款单号
initiated_at          DATETIME        -       NOT NULL             发起时间
completed_at          DATETIME        -       NULL                 完成时间
failure_reason        VARCHAR        200     NULL                 失败原因
created_at            DATETIME        -       NOT NULL             创建时间
updated_at            DATETIME        -       NOT NULL             更新时间
```

### PaymentChannelDO (支付渠道数据对象)

#### DO名称(Data Object Name)
PaymentChannelDO

#### 对应表名(Table Name)
payment_channel

#### 字段映射(Field Mapping)
```text
字段名称                类型           长度    约束                   说明
id                     VARCHAR        32      PRIMARY KEY           渠道ID
channel_code           VARCHAR        20      UNIQUE NOT NULL      渠道代码
channel_name           VARCHAR        50      NOT NULL             渠道名称
channel_type           VARCHAR        20      NOT NULL             渠道类型
channel_status         VARCHAR        20      NOT NULL             渠道状态
provider_code          VARCHAR        20      NOT NULL             提供方代码
provider_name          VARCHAR        50      NOT NULL             提供方名称
supported_currencies   JSON           -       NOT NULL             支持货币
supported_methods      JSON           -       NOT NULL             支持方式
payment_limits         JSON           -       NOT NULL             支付限额
created_at            DATETIME        -       NOT NULL             创建时间
updated_at            DATETIME        -       NOT NULL             更新时间
last_health_check     DATETIME        -       NULL                 最后检查时间
```

## 外部集成适配器(External Integration Adapters)

### PaymentChannelAdapter (支付渠道适配器)

#### 适配器职责
统一不同支付渠道的接口调用，提供防腐层功能

#### 接口定义
```java
public interface PaymentChannelAdapter {
    // 支付操作
    PaymentResult executePayment(PaymentRequest request);
    
    // 退款操作
    RefundResult executeRefund(RefundRequest request);
    
    // 状态查询
    PaymentStatus queryPaymentStatus(String channelTransactionId);
    RefundStatus queryRefundStatus(String channelRefundId);
    
    // 渠道健康检查
    HealthCheckResult checkHealth(PaymentChannel channel);
    
    // 批量支付操作
    BatchPaymentResult executeBatchPayment(List<PaymentRequest> requests);
    
    // 信用支付操作
    CreditPaymentResult executeCreditPayment(CreditPaymentRequest request);
    
    // 渠道限额查询
    ChannelLimitInfo queryChannelLimit();
    
    // 支付凭证验证
    PaymentProofValidationResult validatePaymentProof(PaymentProofInfo proofInfo);
}
```

#### 具体适配器实现

##### 线上支付渠道
```java
@Component
public class UnionPayChannelAdapter implements PaymentChannelAdapter {
    // 支持银联在线支付、企业网银支付
    private final UnionPayClient unionPayClient;
    private final UnionPayConfig config;
    
    @Override
    public PaymentResult executePayment(PaymentRequest request) {
        // 实现银联支付逻辑
        // 处理大额支付的特殊验证
        // 支持异步通知处理
    }
}
```

##### 钱包支付渠道
```java
@Component
public class WalletPayChannelAdapter implements PaymentChannelAdapter {
    // 支持企业钱包余额支付
    private final WalletAccountService walletService;
    
    @Override
    public PaymentResult executePayment(PaymentRequest request) {
        // 实现钱包支付逻辑
        // 处理账户余额检查
        // 执行实时扣款
    }
}
```

##### 电汇支付渠道
```java
@Component
public class BankTransferChannelAdapter implements PaymentChannelAdapter {
    // 支持银行转账凭证上传和确认
    private final BankTransferVerificationService verificationService;
    
    @Override
    public PaymentProofValidationResult validatePaymentProof(PaymentProofInfo proofInfo) {
        // 实现转账凭证验证逻辑
        // 支持多种凭证格式
        // 提供人工审核接口
    }
}
```

##### 信用支付渠道
```java
@Component
public class CreditAccountChannelAdapter implements PaymentChannelAdapter {
    // 支持企业信用额度支付
    private final CreditAccountService creditService;
    private final CreditLimitValidator limitValidator;
    
    @Override
    public CreditPaymentResult executeCreditPayment(CreditPaymentRequest request) {
        // 实现信用支付逻辑
        // 检查信用额度
        // 创建还款计划
        // 记录信用支付历史
    }
}
```

### OrderServiceClient (订单服务客户端)

#### 客户端职责
与订单上下文进行集成，获取订单信息和发送支付状态

#### 接口定义
```java
public interface OrderServiceClient {
    OrderInfo getOrderInfo(OrderId orderId);
    void notifyPaymentStatus(PaymentStatusNotification notification);
    boolean validateOrderPayment(OrderId orderId, Money amount);
}
```

#### 实现方式
- 使用HTTP客户端调用订单服务REST API
- 实现重试机制和熔断保护
- 提供Mock实现用于测试

## 事件发布基础设施(Event Publishing Infrastructure)

### DomainEventPublisher (领域事件发布器)

#### 发布器职责
将领域事件发布到消息中间件，实现最终一致性

#### 接口定义
```java
public interface DomainEventPublisher {
    void publish(DomainEvent event);
    void publishBatch(List<DomainEvent> events);
}
```

#### 实现策略
- 使用RocketMQ/Kafka作为消息中间件
- 实现事务消息保证事件发布的可靠性
- 提供事件序列化和反序列化支持
- 支持事件重试和死信队列处理

# 基础设施层通用组件

## 1. 异常处理机制

### 1.1 异常体系设计

```java
public class PaymentInfrastructureException extends RuntimeException {
    private final String errorCode;
    private final String businessCode;
    private final Map<String, Object> errorContext;
    
    // 数据访问异常
    public static class DataAccessException extends PaymentInfrastructureException {
        // 数据库操作异常
        public static class DatabaseException extends DataAccessException {}
        // 缓存操作异常
        public static class CacheException extends DataAccessException {}
    }
    
    // 外部服务异常
    public static class ExternalServiceException extends PaymentInfrastructureException {
        // 支付渠道异常
        public static class PaymentChannelException extends ExternalServiceException {}
        // 订单服务异常
        public static class OrderServiceException extends ExternalServiceException {}
    }
    
    // 消息处理异常
    public static class MessageHandlingException extends PaymentInfrastructureException {
        // 消息发送异常
        public static class MessagePublishException extends MessageHandlingException {}
        // 消息消费异常
        public static class MessageConsumeException extends MessageHandlingException {}
    }
}
```

### 1.2 错误码定义

```java
public enum PaymentErrorCode {
    // 数据库错误 (1000-1099)
    DB_CONNECTION_ERROR("1000", "数据库连接异常"),
    DB_TRANSACTION_ERROR("1001", "数据库事务异常"),
    DB_DEADLOCK_ERROR("1002", "数据库死锁异常"),
    
    // 缓存错误 (1100-1199)
    CACHE_CONNECTION_ERROR("1100", "缓存连接异常"),
    CACHE_DATA_ERROR("1101", "缓存数据异常"),
    
    // 支付渠道错误 (1200-1299)
    CHANNEL_CONNECTION_ERROR("1200", "渠道连接异常"),
    CHANNEL_TIMEOUT_ERROR("1201", "渠道超时异常"),
    CHANNEL_RESPONSE_ERROR("1202", "渠道响应异常"),
    
    // 订单服务错误 (1300-1399)
    ORDER_SERVICE_ERROR("1300", "订单服务异常"),
    ORDER_DATA_ERROR("1301", "订单数据异常"),
    
    // 消息队列错误 (1400-1499)
    MQ_CONNECTION_ERROR("1400", "消息队列连接异常"),
    MQ_PUBLISH_ERROR("1401", "消息发送异常"),
    MQ_CONSUME_ERROR("1402", "消息消费异常");
    
    private final String code;
    private final String message;
}
```

## 2. 监控和指标收集

### 2.1 核心监控指标

```java
public class PaymentMetrics {
    // 支付交易指标
    private final Counter paymentRequestCounter;      // 支付请求计数
    private final Counter paymentSuccessCounter;      // 支付成功计数
    private final Counter paymentFailureCounter;      // 支付失败计数
    private final Timer paymentProcessingTimer;       // 支付处理时间
    private final Gauge activePaymentsGauge;         // 活跃支付数量
    
    // 退款交易指标
    private final Counter refundRequestCounter;       // 退款请求计数
    private final Counter refundSuccessCounter;       // 退款成功计数
    private final Timer refundProcessingTimer;        // 退款处理时间
    
    // 渠道性能指标
    private final Map<String, Timer> channelTimers;   // 各渠道响应时间
    private final Map<String, Counter> channelErrors; // 各渠道错误计数
    
    // 系统性能指标
    private final Timer dbOperationTimer;             // 数据库操作时间
    private final Timer cacheOperationTimer;          // 缓存操作时间
    private final Counter deadlockCounter;            // 死锁计数器
}
```

### 2.2 告警规则设置

```yaml
alerts:
  # 支付失败率告警
  payment_failure_rate:
    condition: failure_rate > 5%
    duration: 5m
    severity: critical
    
  # 支付处理时间告警
  payment_processing_time:
    condition: processing_time > 10s
    duration: 5m
    severity: warning
    
  # 渠道可用性告警
  channel_availability:
    condition: success_rate < 95%
    duration: 5m
    severity: critical
    
  # 系统资源告警
  system_resources:
    cpu_usage:
      condition: usage > 80%
      duration: 5m
      severity: warning
    memory_usage:
      condition: usage > 80%
      duration: 5m
      severity: warning
```

## 3. 配置管理

### 3.1 数据源配置

```yaml
spring:
  datasource:
    primary:
      url: jdbc:mysql://master:3306/payment
      username: ${PAYMENT_DB_USER}
      password: ${PAYMENT_DB_PASSWORD}
      hikari:
        maximum-pool-size: 20
        minimum-idle: 5
    replica:
      url: jdbc:mysql://slave:3306/payment
      username: ${PAYMENT_DB_READ_USER}
      password: ${PAYMENT_DB_READ_PASSWORD}
      hikari:
        maximum-pool-size: 10
        minimum-idle: 3
```

### 3.2 缓存配置

```yaml
spring:
  redis:
    cluster:
      nodes: 
        - redis-node1:6379
        - redis-node2:6379
        - redis-node3:6379
    password: ${REDIS_PASSWORD}
    lettuce:
      pool:
        max-active: 20
        max-idle: 10
        min-idle: 5
```

### 3.3 消息队列配置

```yaml
spring:
  rocketmq:
    name-server: rocketmq-server:9876
    producer:
      group: payment-producer
      send-message-timeout: 3000
      retry-times-when-send-failed: 2
    
  kafka:
    bootstrap-servers: kafka-server:9092
    producer:
      retries: 3
      batch-size: 16384
      buffer-memory: 33554432
    consumer:
      group-id: payment-consumer
      auto-offset-reset: earliest
```

### 3.4 支付渠道配置

```yaml
payment:
  channels:
    unionpay:
      merchant-id: ${UNIONPAY_MERCHANT_ID}
      api-key: ${UNIONPAY_API_KEY}
      api-url: https://gateway.unionpay.com/api/v2
      notify-url: https://api.example.com/payment/callback/unionpay
      
    wallet:
      api-url: http://internal-wallet-service/api
      api-key: ${WALLET_API_KEY}
      
    bank-transfer:
      verification-url: http://internal-verification-service/api
      allowed-proof-types: ["pdf", "jpg", "png"]
      
    credit:
      service-url: http://credit-service/api
      api-key: ${CREDIT_API_KEY}
      check-interval: 60s
```
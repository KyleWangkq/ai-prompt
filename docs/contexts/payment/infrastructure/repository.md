# Payment Infrastructure Layer (支付基础设施层)

## 基础设施层设计文档

## Repository实现设计(Repository Implementation Design)

### PaymentOrderRepositoryImpl

#### 实现类名称(Implementation Name)
PaymentOrderRepositoryImpl

#### 接口名称(Interface Name)
PaymentOrderRepository

#### 数据库表映射(Table Mapping)
- 主表: payment_order (支付单表)
- 关联表: payment_transaction (支付流水表)
- 关联表: refund_transaction (退款流水表)

#### MyBatis Mapper接口(Mapper Interface)
- PaymentOrderMapper: 支付单数据操作
- PaymentTransactionMapper: 支付流水数据操作  
- RefundTransactionMapper: 退款流水数据操作

#### 领域对象转换(Domain Object Conversion)

##### PaymentOrder聚合转换规则
```text
领域对象 -> 数据对象:
- PaymentOrder -> PaymentOrderDO
  - paymentOrderId.getId() -> id
  - relatedOrderId.getId() -> related_order_id
  - companyUserId.getId() -> company_user_id
  - paymentAmount.getAmount() -> payment_amount
  - paymentAmount.getCurrency() -> currency
  - paidAmount.getAmount() -> paid_amount
  - refundedAmount.getAmount() -> refunded_amount
  - paymentType.name() -> payment_type
  - paymentStatus.name() -> payment_status
  - paymentDeadline -> payment_deadline
  - businessDescription -> business_description
  - createdAt -> created_at
  - updatedAt -> updated_at

数据对象 -> 领域对象:
- PaymentOrderDO -> PaymentOrder
  - 通过PaymentOrderFactory.create()创建聚合根
  - 加载关联的PaymentTransaction和RefundTransaction实体
  - 重建聚合内的实体关系
```

##### PaymentTransaction实体转换规则
```text
领域对象 -> 数据对象:
- PaymentTransaction -> PaymentTransactionDO
  - paymentTransactionId.getId() -> id
  - paymentOrderId.getId() -> payment_order_id
  - paymentAmount.getAmount() -> payment_amount
  - paymentChannel.getChannelCode() -> channel_code
  - channelTransactionId -> channel_transaction_id
  - transactionStatus.name() -> transaction_status
  - paymentMethod.name() -> payment_method
  - initiatedAt -> initiated_at
  - completedAt -> completed_at
  - failureReason -> failure_reason

数据对象 -> 领域对象:
- PaymentTransactionDO -> PaymentTransaction
  - 通过PaymentTransactionFactory.create()创建实体
  - 重建值对象和枚举类型
```

#### 查询优化(Query Optimization)

##### 索引设计
```text
主键索引:
- PRIMARY KEY (id) on payment_order
- PRIMARY KEY (id) on payment_transaction  
- PRIMARY KEY (id) on refund_transaction

业务索引:
- UNIQUE INDEX uk_payment_order_id (id) on payment_order
- INDEX idx_related_order_id (related_order_id) on payment_order
- INDEX idx_company_user_id (company_user_id) on payment_order
- INDEX idx_payment_status (payment_status) on payment_order
- INDEX idx_payment_type (payment_type) on payment_order
- INDEX idx_created_at (created_at) on payment_order
- INDEX idx_payment_deadline (payment_deadline) on payment_order

关联查询索引:
- INDEX idx_payment_order_id (payment_order_id) on payment_transaction
- INDEX idx_payment_order_id (payment_order_id) on refund_transaction
- INDEX idx_channel_transaction_id (channel_transaction_id) on payment_transaction

复合索引:
- INDEX idx_status_type (payment_status, payment_type) on payment_order
- INDEX idx_user_status (company_user_id, payment_status) on payment_order
- INDEX idx_amount_range (payment_amount, currency) on payment_order
```

##### 查询性能优化
```text
分页查询优化:
- 使用LIMIT和OFFSET进行分页
- 大数据量时使用基于ID的游标分页
- 避免深度分页，推荐使用滚动查询

关联查询优化:
- 支付单与流水表使用LEFT JOIN
- 批量查询时使用IN操作符
- 复杂查询拆分为多次简单查询

缓存策略:
- 支付单基本信息缓存1分钟
- 支付渠道配置缓存10分钟
- 查询结果集缓存30秒（适用于统计查询）
```

### PaymentChannelRepositoryImpl

#### 实现类名称(Implementation Name)
PaymentChannelRepositoryImpl

#### 接口名称(Interface Name)
PaymentChannelRepository

#### 数据库表映射(Table Mapping)
- 主表: payment_channel (支付渠道表)
- 关联表: channel_configuration (渠道配置表)
- 关联表: fee_rule (费率规则表)

#### MyBatis Mapper接口(Mapper Interface)
- PaymentChannelMapper: 支付渠道数据操作
- ChannelConfigurationMapper: 渠道配置数据操作
- FeeRuleMapper: 费率规则数据操作

#### 领域对象转换(Domain Object Conversion)

##### PaymentChannel聚合转换规则
```text
领域对象 -> 数据对象:
- PaymentChannel -> PaymentChannelDO
  - channelId.getId() -> id
  - channelCode -> channel_code
  - channelName -> channel_name
  - channelType.name() -> channel_type
  - channelStatus.name() -> channel_status
  - provider.getProviderCode() -> provider_code
  - provider.getProviderName() -> provider_name
  - supportedCurrencies -> supported_currencies (JSON)
  - supportedMethods -> supported_methods (JSON)
  - limits -> payment_limits (JSON)
  - createdAt -> created_at
  - updatedAt -> updated_at
  - lastHealthCheck -> last_health_check

数据对象 -> 领域对象:
- PaymentChannelDO -> PaymentChannel
  - 通过PaymentChannelFactory.create()创建聚合根
  - JSON字段反序列化为集合对象
  - 加载关联的ChannelConfiguration和FeeRule实体
```

## 数据对象设计(Data Object Design)

### PaymentOrderDO (支付单数据对象)

#### DO名称(Data Object Name)
PaymentOrderDO

#### 对应表名(Table Name)
payment_order

#### 字段映射(Field Mapping)
```text
字段名称                类型           长度    约束                   说明
id                     VARCHAR        32      PRIMARY KEY           支付单ID
related_order_id       VARCHAR        32      NOT NULL             关联订单ID
company_user_id        VARCHAR        32      NOT NULL             企业用户ID
payment_amount         DECIMAL        15,2    NOT NULL             支付金额
currency               VARCHAR        3       NOT NULL             货币代码
paid_amount            DECIMAL        15,2    DEFAULT 0            已支付金额
refunded_amount        DECIMAL        15,2    DEFAULT 0            已退款金额
payment_type           VARCHAR        20      NOT NULL             支付类型
payment_status         VARCHAR        20      NOT NULL             支付状态
payment_deadline       DATETIME       -       NULL                 支付截止时间
business_description   VARCHAR        500     NULL                 业务描述
created_at            DATETIME        -       NOT NULL             创建时间
updated_at            DATETIME        -       NOT NULL             更新时间
version               INT             -       DEFAULT 0            乐观锁版本号
```

#### 主键策略(Primary Key Strategy)
使用UUID生成32位字符串作为主键，保证全局唯一性

#### 转换方法(Conversion Methods)
```java
// DO -> 领域对象
public PaymentOrder toDomainObject() {
    return PaymentOrderFactory.create(
        PaymentOrderId.of(this.id),
        OrderId.of(this.relatedOrderId),
        CompanyUserId.of(this.companyUserId),
        Money.of(this.paymentAmount, Currency.of(this.currency)),
        Money.of(this.paidAmount, Currency.of(this.currency)),
        Money.of(this.refundedAmount, Currency.of(this.currency)),
        PaymentType.valueOf(this.paymentType),
        PaymentStatus.valueOf(this.paymentStatus),
        this.paymentDeadline,
        this.businessDescription,
        this.createdAt,
        this.updatedAt
    );
}

// 领域对象 -> DO  
public static PaymentOrderDO fromDomainObject(PaymentOrder paymentOrder) {
    PaymentOrderDO orderDO = new PaymentOrderDO();
    orderDO.setId(paymentOrder.getPaymentOrderId().getId());
    orderDO.setRelatedOrderId(paymentOrder.getRelatedOrderId().getId());
    orderDO.setCompanyUserId(paymentOrder.getCompanyUserId().getId());
    orderDO.setPaymentAmount(paymentOrder.getPaymentAmount().getAmount());
    orderDO.setCurrency(paymentOrder.getPaymentAmount().getCurrency().getCode());
    orderDO.setPaidAmount(paymentOrder.getPaidAmount().getAmount());
    orderDO.setRefundedAmount(paymentOrder.getRefundedAmount().getAmount());
    orderDO.setPaymentType(paymentOrder.getPaymentType().name());
    orderDO.setPaymentStatus(paymentOrder.getPaymentStatus().name());
    orderDO.setPaymentDeadline(paymentOrder.getPaymentDeadline());
    orderDO.setBusinessDescription(paymentOrder.getBusinessDescription());
    orderDO.setCreatedAt(paymentOrder.getCreatedAt());
    orderDO.setUpdatedAt(paymentOrder.getUpdatedAt());
    return orderDO;
}
```

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
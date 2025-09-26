# Payment Aggregate (支付聚合)

## 聚合设计文档

### 聚合根名称(Aggregate Root)
Payment (支付)

### 聚合描述(Description)
管理企业间特种设备定制交易中的支付完整生命周期，包括支付创建、支付执行、状态跟踪、退款处理等核心业务逻辑。支付作为聚合根，统一管理所有相关的支付流水和退款记录。

### 所属上下文(Bounded Context)
Payment Context (支付上下文)

### 聚合根ID(Root Entity ID)
PaymentId (支付唯一标识)

## 实体列表(Entities)

### Payment (支付实体)
```text
实体名称(Entity Name): Payment
唯一标识(Identity): PaymentId (String, 32位)
属性列表(Attributes):
  - paymentId: PaymentId (支付号)
  - relatedOrderId: OrderId (关联订单号)
  - companyUserId: CompanyUserId (企业用户ID)
  - paymentAmount: Money (支付金额)
  - paidAmount: Money (已支付金额)
  - refundedAmount: Money (已退款金额)
  - actualAmount: Money (实际收款金额, 计算属性)
  - paymentType: PaymentType (支付类型)
  - paymentStatus: PaymentStatus (支付状态)
  - paymentDeadline: DateTime (支付截止时间)
  - businessDescription: String (业务描述)
  - createdAt: DateTime (创建时间)
  - updatedAt: DateTime (更新时间)
  - paymentTransactions: List<PaymentTransaction> (支付流水列表)
  - refundTransactions: List<RefundTransaction> (退款流水列表)

行为方法(Behaviors):
  - createPayment(): 创建支付
  - executePayment(amount, channel): 执行支付
  - processPaymentCallback(result): 处理支付回调
  - executeRefund(amount, reason): 执行退款
  - processRefundCallback(result): 处理退款回调
  - updatePaymentStatus(status): 更新支付状态
  - calculateActualAmount(): 计算实际收款金额
  - canExecutePayment(): 是否可以执行支付
  - canExecuteRefund(amount): 是否可以执行退款

生命周期(Lifecycle):
  - 创建: 由订单系统创建支付请求时创建
  - 修改: 支付执行、退款操作时更新状态和金额
  - 删除: 支付不允许物理删除，仅状态变更
```

### PaymentTransaction (支付流水实体)
```text
实体名称(Entity Name): PaymentTransaction
唯一标识(Identity): PaymentTransactionId (String, 32位)
属性列表(Attributes):
  - paymentTransactionId: PaymentTransactionId (支付流水号)
  - paymentId: PaymentId (关联支付号)
  - paymentAmount: Money (支付金额)
  - paymentChannel: PaymentChannel (支付渠道)
  - channelTransactionId: String (渠道交易号)
  - transactionStatus: TransactionStatus (交易状态)
  - paymentMethod: PaymentMethod (支付方式)
  - initiatedAt: DateTime (发起时间)
  - completedAt: DateTime (完成时间)
  - failureReason: String (失败原因)

行为方法(Behaviors):
  - initiateTransaction(): 发起交易
  - completeTransaction(): 完成交易
  - failTransaction(reason): 交易失败
  - isCompleted(): 是否已完成
  - canRetry(): 是否可以重试

生命周期(Lifecycle):
  - 创建: 支付执行时创建
  - 修改: 交易状态变更时修改
  - 删除: 不允许删除
```

### RefundTransaction (退款流水实体)
```text
实体名称(Entity Name): RefundTransaction
唯一标识(Identity): RefundTransactionId (String, 32位)
属性列表(Attributes):
  - refundTransactionId: RefundTransactionId (退款流水号)
  - paymentId: PaymentId (关联支付号)
  - originalTransactionId: PaymentTransactionId (原支付流水号)
  - refundAmount: Money (退款金额)
  - refundChannel: PaymentChannel (退款渠道)
  - channelRefundId: String (渠道退款号)
  - refundStatus: RefundStatus (退款状态)
  - refundReason: String (退款原因)
  - orderRefundId: String (订单退款单号)
  - initiatedAt: DateTime (发起时间)
  - completedAt: DateTime (完成时间)
  - failureReason: String (失败原因)

行为方法(Behaviors):
  - initiateRefund(): 发起退款
  - completeRefund(): 完成退款
  - failRefund(reason): 退款失败
  - isCompleted(): 是否已完成
  - canRetry(): 是否可以重试

生命周期(Lifecycle):
  - 创建: 退款执行时创建
  - 修改: 退款状态变更时修改
  - 删除: 不允许删除
```

## 值对象列表(Value Objects)

### Money (金额值对象)
```text
值对象名称(Value Object Name): Money
属性组合(Attributes):
  - amount: BigDecimal (金额数值)
  - currency: Currency (货币类型)

不变性保证(Immutability): 
  - 金额对象一旦创建不可修改
  - 所有操作返回新的Money实例

相等性规则(Equality):
  - 相同金额和货币类型的Money对象相等
  - 必须金额和货币都相同才认为相等

验证规则(Validation):
  - 金额必须大于等于0
  - 货币类型不能为空
  - 金额精度最多保留2位小数
```

### PaymentChannel (支付渠道值对象)
```text
值对象名称(Value Object Name): PaymentChannel
属性组合(Attributes):
  - channelCode: String (渠道代码)
  - channelName: String (渠道名称)
  - channelType: ChannelType (渠道类型)

不变性保证(Immutability): 支付渠道信息不可修改
相等性规则(Equality): 渠道代码相同即为相等
验证规则(Validation): 渠道代码不能为空且符合规范格式
```

### PaymentId (支付ID值对象)
```text
值对象名称(Value Object Name): PaymentId
属性组合(Attributes):
  - id: String (支付ID字符串)

不变性保证(Immutability): ID一旦生成不可修改
相等性规则(Equality): ID字符串相同即为相等
验证规则(Validation): 
  - ID不能为空
  - ID长度必须为32位
  - ID格式必须符合系统规范
```

## 枚举定义(Enumerations)

### PaymentStatus (支付状态)
```text
- UNPAID: 未支付
- PAYING: 支付中
- PARTIAL_PAID: 部分支付
- PAID: 已支付
- PARTIAL_REFUNDED: 部分退款
- FULL_REFUNDED: 全额退款
- FAILED: 支付失败
- STOPPED: 已停止
- FROZEN: 已冻结
```

### PaymentType (支付类型)
```text
- ADVANCE_PAYMENT: 预付款
- FINAL_PAYMENT: 尾款
- OTHER_PAYMENT: 其他费用
```

### TransactionStatus (交易状态)
```text
- INITIATED: 已发起
- PROCESSING: 处理中
- SUCCESS: 成功
- FAILED: 失败
- TIMEOUT: 超时
```

## 强制不变式(Enforced Invariants)

1. **金额约束**:
   - 支付金额必须大于0
   - 已支付金额不能超过支付总金额
   - 已退款金额不能超过已支付金额
   - 实际收款金额 = 已支付金额 - 已退款金额

2. **状态约束**:
   - 支付状态变更必须符合状态机规则
   - 只有未支付或部分支付状态才能执行支付
   - 只有已支付或部分退款状态才能执行退款
   - 冻结状态下不能执行支付和退款操作

3. **业务规则约束**:
   - 支付创建后不能修改支付金额
   - 每次支付操作必须创建对应的支付流水
   - 每次退款操作必须创建对应的退款流水
   - 支付渠道必须是系统支持的有效渠道

4. **时间约束**:
   - 支付流水的完成时间必须晚于发起时间
   - 退款流水的完成时间必须晚于发起时间
   - 支付更新时间必须更新为当前时间

## 领域事件(Domain Events)

### PaymentCreated (支付已创建)
```text
事件名称(Event Name): PaymentCreated
触发条件(Trigger Condition): 支付成功创建时
事件属性(Event Data):
  - paymentId: PaymentId
  - relatedOrderId: OrderId
  - companyUserId: CompanyUserId
  - paymentAmount: Money
  - paymentType: PaymentType
  - createdAt: DateTime
发布时机(Publishing Timing): 支付创建事务提交后
订阅者(Subscribers): Order Context, Finance Context
```

### PaymentExecuted (支付已执行)
```text
事件名称(Event Name): PaymentExecuted
触发条件(Trigger Condition): 支付操作成功完成时
事件属性(Event Data):
  - paymentId: PaymentId
  - transactionId: PaymentTransactionId
  - paymentAmount: Money
  - paymentChannel: PaymentChannel
  - completedAt: DateTime
发布时机(Publishing Timing): 支付回调处理完成后
订阅者(Subscribers): Order Context, Finance Context
```

### PaymentStatusChanged (支付状态已变更)
```text
事件名称(Event Name): PaymentStatusChanged
触发条件(Trigger Condition): 支付单状态发生变更时
事件属性(Event Data):
  - paymentId: PaymentId
  - previousStatus: PaymentStatus
  - currentStatus: PaymentStatus
  - changedAt: DateTime
发布时机(Publishing Timing): 状态更新事务提交后
订阅者(Subscribers): Order Context, Notification Context
```

### RefundExecuted (退款已执行)
```text
事件名称(Event Name): RefundExecuted
触发条件(Trigger Condition): 退款操作成功完成时
事件属性(Event Data):
  - paymentId: PaymentId
  - refundTransactionId: RefundTransactionId
  - refundAmount: Money
  - refundChannel: PaymentChannel
  - completedAt: DateTime
发布时机(Publishing Timing): 退款回调处理完成后
订阅者(Subscribers): Order Context, Finance Context
```

## 仓储接口(Repository Interface)

### PaymentRepository
```text
仓储接口名称(Repository Interface): PaymentRepository
聚合根类型(Aggregate Root Type): Payment

基础方法(Basic Methods):
  - save(payment: Payment): void
  - findById(id: PaymentId): Optional<Payment>
  - findByRelatedOrderId(orderId: OrderId): List<Payment>
  - remove(payment: Payment): void

查询方法(Query Methods):
  - findByCompanyUserId(companyUserId: CompanyUserId): List<Payment>
  - findByPaymentStatus(status: PaymentStatus): List<Payment>
  - findByPaymentType(type: PaymentType): List<Payment>
  - findByAmountRange(minAmount: Money, maxAmount: Money): List<Payment>
  - findByDateRange(startDate: DateTime, endDate: DateTime): List<Payment>
  - findUnpaidOrPartialPaid(): List<Payment>

批量操作(Batch Operations):
  - saveAll(payments: List<Payment>): void
  - findByIds(ids: List<PaymentId>): List<Payment>
  - updateStatusBatch(ids: List<PaymentId>, status: PaymentStatus): void
```

## 领域服务(Domain Services)

### PaymentExecutionService (支付执行领域服务)
```text
服务职责: 协调支付渠道调用和支付状态更新
主要方法:
  - executePayment(payment: Payment, amount: Money, channel: PaymentChannel): PaymentResult
  - processPaymentCallback(payment: Payment, callbackData: PaymentCallbackData): void
  - executeRefund(payment: Payment, refundRequest: RefundRequest): RefundResult
  - processRefundCallback(payment: Payment, callbackData: RefundCallbackData): void
```

### PaymentValidationService (支付验证领域服务)
```text
服务职责: 验证支付操作的业务规则
主要方法:
  - validatePaymentRequest(payment: Payment, amount: Money): ValidationResult
  - validateRefundRequest(payment: Payment, amount: Money): ValidationResult
  - validatePaymentChannel(channel: PaymentChannel): ValidationResult
  - validateBusinessRules(payment: Payment): ValidationResult
```
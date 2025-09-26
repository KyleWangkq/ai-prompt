# Payment Aggregate (支付聚合)

## 聚合设计文档 v2.0

### 文档信息
| 项目 | 内容 |
|------|------|
| **文档名称** | Payment聚合设计文档 |
| **文档版本** | v2.0 |
| **创建日期** | 2025年9月26日 |
| **更新日期** | 2025年9月26日 |
| **术语基准** | 全局词汇表 v2.0 |
| **上下文基准** | 支付上下文设计 v2.0 |

### 聚合根名称(Aggregate Root)
**Payment** (支付单)

### 聚合描述(Description)
管理企业间特种设备定制交易中的**支付单**完整生命周期，包括支付单创建、支付执行、状态跟踪、退款执行、信用还款等核心业务逻辑。支付单作为聚合根，统一管理所有相关的**交易流水**记录，确保支付业务的数据一致性和业务规则完整性。

### 所属上下文(Bounded Context)
**Payment Context** (支付上下文)

### 聚合根ID(Root Entity ID)
**PaymentId** (支付单唯一标识)

## 实体列表(Entities)

### Payment (支付单实体 - 聚合根)
> **术语对照**: Payment ↔ 支付单 (全局词汇表 v2.0)

```text
实体名称(Entity Name): Payment
实体角色(Role): Aggregate Root (聚合根)
唯一标识(Identity): PaymentId (String, 32位)

核心属性(Core Attributes):
  - id: PaymentId (支付单号，主键)
  - orderId: OrderId (关联订单号)
  - resellerId: ResellerId (经销商ID)
  - paymentAmount: Money (支付金额)
  - paidAmount: Money (已支付金额，默认0)
  - refundedAmount: Money (已退款金额，默认0)
  - actualAmount: Money (实际收款金额，计算属性 = paidAmount - refundedAmount)
  - currency: Currency (币种，固定为CNY)
  
业务属性(Business Attributes):
  - paymentType: PaymentType (支付类型：预付款/尾款/其他费用/信用还款)
  - paymentStatus: PaymentStatus (支付状态)
  - refundStatus: RefundStatus (退款状态，默认NO_REFUND)
  - businessDesc: String (业务描述，可选)
  - paymentDeadline: DateTime (支付截止时间，可选)
  - priorityLevel: PriorityLevel (优先级：1-高，2-中，3-低)

关联业务属性(Related Business Attributes):
  - relatedBusinessId: String (关联业务ID，支持信用记录、提货单等)
  - relatedBusinessType: RelatedBusinessType (关联业务类型，如CREDIT_RECORD)
  - businessExpireDate: DateTime (业务到期日，如信用还款到期日)
  - businessTags: BusinessTags (业务标签，JSON格式)

系统属性(System Attributes):
  - createTime: DateTime (创建时间)
  - updateTime: DateTime (更新时间)
  - createBy: String (创建人ID)
  - createByName: String (创建人姓名)
  - updateBy: String (更新人ID)
  - updateByName: String (更新人姓名)
  - delFlag: DeleteFlag (删除标识，0-正常，1-删除)

聚合内实体(Aggregate Entities):
  - transactions: List<PaymentTransaction> (交易流水列表，统一管理支付和退款)

核心行为方法(Core Behaviors):
  - createPayment(paymentRequest: PaymentCreationRequest): Payment
  - executePayment(paymentExecutionRequest: PaymentExecutionRequest): PaymentTransaction
  - processPaymentCallback(callbackData: PaymentCallbackData): void
  - executeRefund(refundExecutionRequest: RefundExecutionRequest): PaymentTransaction
  - processRefundCallback(callbackData: RefundCallbackData): void
  
状态管理方法(State Management):
  - updatePaymentStatus(newStatus: PaymentStatus): void
  - updateRefundStatus(newStatus: RefundStatus): void
  - recalculateAmounts(): void
  
业务规则方法(Business Rules):
  - canExecutePayment(amount: Money): Boolean
  - canExecuteRefund(amount: Money): Boolean
  - validatePaymentAmount(amount: Money): ValidationResult
  - validateRefundAmount(amount: Money): ValidationResult
  - isPaymentCompleted(): Boolean
  - isFullyRefunded(): Boolean
  
计算方法(Calculation Methods):
  - calculatePendingAmount(): Money (待支付金额 = paymentAmount - paidAmount)
  - calculateActualAmount(): Money (实际收款金额 = paidAmount - refundedAmount)
  - calculateRefundableAmount(): Money (可退款金额 = paidAmount - refundedAmount)

生命周期(Lifecycle):
  - 创建: 接收订单系统或信用管理系统创建支付单请求
  - 修改: 支付执行、退款操作、状态变更时更新
  - 软删除: 通过delFlag标记删除，保持数据完整性
  
业务不变式(Business Invariants):
  1. 支付金额必须大于0
  2. 已支付金额不能超过支付金额
  3. 已退款金额不能超过已支付金额
  4. 实际收款金额 = 已支付金额 - 已退款金额
  5. 信用还款类型必须关联有效的业务记录
  6. 状态转换必须符合状态机规则
```

### PaymentTransaction (交易流水实体 - 聚合内实体)
> **术语对照**: PaymentTransaction ↔ 交易流水 (全局词汇表 v2.0)
> **设计说明**: 统一管理支付和退款流水，通过transactionType区分操作类型

```text
实体名称(Entity Name): PaymentTransaction
实体角色(Role): Entity (聚合内实体)
唯一标识(Identity): TransactionId (String, 32位)
父聚合根(Parent Aggregate): Payment

核心属性(Core Attributes):
  - id: TransactionId (流水号，主键)
  - paymentId: PaymentId (关联支付单号)
  - transactionType: TransactionType (流水类型：支付/退款)
  - transactionStatus: TransactionStatus (流水状态：处理中/成功/失败)
  - transactionAmount: Money (交易金额，支付为正数，退款为负数)

渠道属性(Channel Attributes):
  - paymentChannel: PaymentChannel (支付渠道：线上支付/钱包支付/电汇支付/信用账户)
  - channelTransactionNumber: String (渠道交易号，可选)
  - paymentWay: String (支付方式，具体的支付方式)

关联属性(Association Attributes):
  - originalTransactionId: TransactionId (原流水号，退款时关联的原支付流水号)
  - businessOrderId: String (业务单号，如退款单号)

时间属性(Time Attributes):
  - createTime: DateTime (流水记录创建时间)
  - completeDatetime: DateTime (交易完成时间，可选)
  - expirationTime: DateTime (支付过期时间，可选)

系统属性(System Attributes):
  - delFlag: DeleteFlag (删除标识，0-正常，1-删除)
  - createBy: String (创建人ID)
  - createByName: String (创建人姓名)
  - updateBy: String (更新人ID)
  - updateByName: String (更新人姓名)
  - updateTime: DateTime (最后更新时间)
  
扩展属性(Extension Attributes):
  - businessRemark: String (业务备注，交易备注信息)

核心行为方法(Core Behaviors):
  - createPaymentTransaction(request: PaymentTransactionRequest): PaymentTransaction
  - createRefundTransaction(request: RefundTransactionRequest): PaymentTransaction
  - updateTransactionStatus(status: TransactionStatus): void
  - completeTransaction(completionData: TransactionCompletionData): void
  - failTransaction(reason: String): void

状态查询方法(Status Query Methods):
  - isPaymentTransaction(): Boolean
  - isRefundTransaction(): Boolean  
  - isCompleted(): Boolean
  - isFailed(): Boolean
  - isProcessing(): Boolean
  - canRetry(): Boolean

业务规则方法(Business Rules):
  - validateTransactionAmount(amount: Money, type: TransactionType): ValidationResult
  - validateOriginalTransaction(originalId: TransactionId): ValidationResult
  - validateChannel(channel: PaymentChannel): ValidationResult

生命周期(Lifecycle):
  - 创建: 支付执行或退款执行时创建
  - 修改: 交易状态变更、回调处理时更新
  - 软删除: 通过delFlag标记删除，保持审计追踪
  
业务不变式(Business Invariants):
  1. 交易金额必须不为0
  2. 支付交易金额必须为正数
  3. 退款交易金额必须为负数
  4. 退款交易必须关联有效的原支付流水
  5. 流水状态转换必须符合状态机规则
  6. 同一支付单的退款金额总和不能超过支付金额总和
```

## 值对象列表(Value Objects)

### Money (金额值对象)
> **术语对照**: Money ↔ 金额概念 (全局词汇表 v2.0)

```text
值对象名称(Value Object Name): Money
业务含义(Business Meaning): 表示支付系统中的金额概念，包含数值和货币单位

属性组合(Attributes):
  - amount: BigDecimal (金额数值，使用decimal(20,6)精度)
  - currency: Currency (货币类型，固定为CNY人民币)

不变性保证(Immutability): 
  - 金额对象一旦创建不可修改
  - 所有操作返回新的Money实例
  - 支持加法、减法、比较等操作

相等性规则(Equality):
  - 相同金额和货币类型的Money对象相等
  - 必须金额数值和货币类型都完全相同

验证规则(Validation):
  - 金额必须大于等于0（支付金额场景）
  - 退款场景允许负数金额
  - 货币类型不能为空
  - 金额精度最多保留6位小数
  - 金额范围在合理的业务范围内

业务方法(Business Methods):
  - add(other: Money): Money (金额加法)
  - subtract(other: Money): Money (金额减法)
  - multiply(factor: BigDecimal): Money (金额乘法)
  - isZero(): Boolean (是否为零)
  - isPositive(): Boolean (是否为正数)
  - isGreaterThan(other: Money): Boolean (比较大小)
  - toCNY(): Money (转换为人民币，当前固定返回自身)
```

### PaymentChannel (支付渠道值对象)
> **术语对照**: PaymentChannel ↔ 支付渠道 (全局词汇表 v2.0)

```text
值对象名称(Value Object Name): PaymentChannel
业务含义(Business Meaning): 表示支付执行的具体路径和方式

属性组合(Attributes):
  - channelCode: String (渠道代码，如ONLINE_PAY、WALLET_PAY)
  - channelName: String (渠道显示名称，如"线上支付"、"钱包支付")
  - channelType: ChannelType (渠道分类)

支持的渠道类型(Supported Channel Types):
  - ONLINE_PAYMENT: 线上支付（银联、网银等第三方支付平台）
  - WALLET_PAYMENT: 钱包支付（企业内部资金账户支付）
  - WIRE_TRANSFER: 电汇支付（银行转账方式）
  - CREDIT_ACCOUNT: 信用账户（基于企业信用额度）

不变性保证(Immutability): 支付渠道信息不可修改
相等性规则(Equality): 渠道代码相同即为相等
验证规则(Validation): 
  - 渠道代码不能为空且符合规范格式
  - 渠道名称不能为空
  - 渠道类型必须在支持范围内

业务特性(Business Characteristics):
  - 不同渠道有不同的处理时间特征
  - 线上支付适合B2B大额支付，确认时限较长
  - 钱包支付支持快速支付和资金优化配置
  - 电汇支付适应传统企业支付习惯
  - 信用账户支持延期付款
```

### PaymentId (支付单ID值对象)
> **术语对照**: PaymentId ↔ 支付单号 (全局词汇表 v2.0)

```text
值对象名称(Value Object Name): PaymentId
业务含义(Business Meaning): 支付单的唯一业务标识

属性组合(Attributes):
  - value: String (支付单ID字符串，32位唯一标识)

不变性保证(Immutability): ID一旦生成不可修改
相等性规则(Equality): ID字符串相同即为相等
验证规则(Validation): 
  - ID不能为空
  - ID长度必须为32位
  - ID格式必须符合系统规范（如包含特定前缀）
  - ID必须全局唯一

生成规则(Generation Rules):
  - 由支付模块统一生成
  - 包含业务含义的前缀标识
  - 确保在分布式环境下的唯一性
```

### RelatedBusinessInfo (关联业务信息值对象)
> **术语对照**: RelatedBusinessInfo ↔ 关联业务 (全局词汇表 v2.0)

```text
值对象名称(Value Object Name): RelatedBusinessInfo
业务含义(Business Meaning): 支付单关联的具体业务记录信息

属性组合(Attributes):
  - businessId: String (关联的业务记录标识，如信用记录ID、提货单ID)
  - businessType: RelatedBusinessType (关联业务类型)
  - expireDate: DateTime (业务相关的到期日期，可选)

支持的业务类型(Supported Business Types):
  - CREDIT_RECORD: 信用记录（信用还款场景）
  - DELIVERY_ORDER: 提货单（提货费用场景）
  - ADDITIONAL_SERVICE: 附加服务（额外服务费用）

不变性保证(Immutability): 关联业务信息一旦设置不可修改
相等性规则(Equality): 业务ID和业务类型都相同即为相等
验证规则(Validation): 
  - 业务ID不能为空
  - 业务类型必须在支持范围内
  - 到期日期必须为未来时间（如果提供）

业务方法(Business Methods):
  - isCreditRepayment(): Boolean (是否为信用还款)
  - isExpired(): Boolean (是否已过期)
  - hasExpireDate(): Boolean (是否设置了到期日期)
```

### BusinessTags (业务标签值对象)
> **术语对照**: BusinessTags ↔ 业务标签 (全局词汇表 v2.0)

```text
值对象名称(Value Object Name): BusinessTags
业务含义(Business Meaning): 支付单的业务分类和标记信息

属性组合(Attributes):
  - tags: Map<String, String> (标签键值对集合)

常用标签类型(Common Tag Types):
  - urgency: 紧急程度（high、medium、low）
  - source: 来源系统（order、credit、manual）
  - category: 业务分类（equipment、service、penalty）
  - project: 项目标识（项目编号或代码）

不变性保证(Immutability): 标签集合一旦创建不可修改
相等性规则(Equality): 所有标签键值对都相同即为相等
验证规则(Validation): 
  - 标签键不能为空
  - 标签值可以为空但不能为null
  - 标签总数不超过限制（如10个）

业务方法(Business Methods):
  - hasTag(key: String): Boolean
  - getTagValue(key: String): Optional<String>
  - isEmpty(): Boolean
  - toJsonString(): String
```

## 枚举定义(Enumerations)
> **术语基准**: 严格遵循全局词汇表 v2.0 中的状态枚举术语定义

### PaymentStatus (支付状态)
> **术语对照**: PaymentStatus ↔ 支付状态 (全局词汇表 v2.0)

```text
枚举值定义(Enum Values):
- UNPAID: 未支付 (支付单刚创建，尚未开始支付操作的初始状态)
- PAYING: 支付中 (支付请求已发起，等待支付渠道确认结果的过程状态)
- PARTIAL_PAID: 部分支付 (支付单已支付部分金额，仍有余额待支付的中间状态)
- PAID: 已支付 (支付单全额支付完成的终态)
- FAILED: 支付失败 (支付过程中出现错误，支付未成功的异常状态)
- STOPPED: 已停止 (因业务原因主动停止支付操作的管控状态)
- FROZEN: 已冻结 (支付单因特殊原因暂时无法操作的保护状态)

状态转换规则(State Transition Rules):
- UNPAID → PAYING, STOPPED, FROZEN
- PAYING → PARTIAL_PAID, PAID, FAILED, STOPPED
- PARTIAL_PAID → PAYING, PAID, STOPPED, FROZEN
- PAID → STOPPED, FROZEN (仅管理操作)
- FAILED → PAYING, STOPPED (支持重试)
- STOPPED → UNPAID, PARTIAL_PAID (恢复操作)
- FROZEN → 任意合法状态 (解冻操作)
```

### RefundStatus (退款状态)
> **术语对照**: RefundStatus ↔ 退款状态 (全局词汇表 v2.0)

```text
枚举值定义(Enum Values):
- NO_REFUND: 未退款 (支付单未发生任何退款操作的初始状态)
- REFUNDING: 退款中 (退款请求已发起，等待退款渠道确认的过程状态)
- PARTIAL_REFUNDED: 部分退款 (支付单已发生部分退款，仍有金额未退款的中间状态)
- FULL_REFUNDED: 全额退款 (支付单已全额退款完成的终态)
- REFUND_FAILED: 退款失败 (退款过程中出现错误，退款未成功的异常状态)

状态转换规则(State Transition Rules):
- NO_REFUND → REFUNDING
- REFUNDING → PARTIAL_REFUNDED, FULL_REFUNDED, REFUND_FAILED
- PARTIAL_REFUNDED → REFUNDING, FULL_REFUNDED
- FULL_REFUNDED → (终态，不可转换)
- REFUND_FAILED → REFUNDING (支持重试)
```

### PaymentType (支付类型)
> **术语对照**: PaymentType ↔ 支付类型 (全局词汇表 v2.0)

```text
枚举值定义(Enum Values):
- ADVANCE_PAYMENT: 预付款 (订单确认后的首期付款，通常占总金额一定比例)
- FINAL_PAYMENT: 尾款 (商品发货或完工后的最终付款)
- OTHER_FEE: 其他费用 (除预付款和尾款外的其他相关费用支付)
- CREDIT_REPAYMENT: 信用还款 (企业对之前使用信用额度进行还款的支付类型)

业务特性(Business Characteristics):
- ADVANCE_PAYMENT: 确保订单有效性，为生产方提供启动资金
- FINAL_PAYMENT: 完成交易闭环，确保服务质量后的最终资金结算
- OTHER_FEE: 覆盖交易过程中的额外成本
- CREDIT_REPAYMENT: 信用支付的后续还款环节，维护企业信用记录
```

### TransactionType (流水类型)
> **术语对照**: TransactionType ↔ 流水类型 (全局词汇表 v2.0)

```text
枚举值定义(Enum Values):
- PAYMENT: 支付 (正向支付操作的流水记录)
- REFUND: 退款 (反向退款操作的流水记录)

金额规则(Amount Rules):
- PAYMENT: 交易金额为正数
- REFUND: 交易金额为负数
```

### TransactionStatus (流水状态)
> **术语对照**: TransactionStatus ↔ 流水状态 (全局词汇表 v2.0)

```text
枚举值定义(Enum Values):
- PROCESSING: 处理中 (流水正在处理，等待最终结果的过程状态)
- SUCCESS: 成功 (流水处理成功完成的终态)
- FAILED: 失败 (流水处理失败的异常状态)

状态转换规则(State Transition Rules):
- PROCESSING → SUCCESS, FAILED
- SUCCESS → (终态，不可转换)
- FAILED → PROCESSING (支持重试，需要创建新流水)
```

### RelatedBusinessType (关联业务类型)
> **术语对照**: RelatedBusinessType ↔ 关联业务类型 (全局词汇表 v2.0)

```text
枚举值定义(Enum Values):
- CREDIT_RECORD: 信用记录 (信用支付相关的业务记录)
- DELIVERY_ORDER: 提货单 (提货相关的业务记录)
- ADDITIONAL_SERVICE: 附加服务 (额外服务相关的业务记录)

扩展支持(Extension Support):
- 支持未来新增其他业务类型
- 保持向前兼容性
```

### PriorityLevel (优先级)
```text
枚举值定义(Enum Values):
- HIGH(1): 高优先级 (重要支付，需要优先处理)
- MEDIUM(2): 中优先级 (普通支付，标准处理)
- LOW(3): 低优先级 (非紧急支付，可延后处理)

业务规则(Business Rules):
- 高优先级支付在系统负载高时优先处理
- 优先级影响支付通知的时效性
- 默认为中优先级
```

### Currency (货币类型)
```text
枚举值定义(Enum Values):
- CNY: 人民币 (当前系统固定使用人民币)

扩展说明(Extension Notes):
- 当前业务范围限定为人民币交易
- 预留国际化扩展能力
```

### DeleteFlag (删除标识)
```text
枚举值定义(Enum Values):
- NORMAL(0): 正常 (数据正常状态)
- DELETED(1): 已删除 (数据已逻辑删除)

使用规则(Usage Rules):
- 所有实体支持软删除机制
- 删除后数据仍保留用于审计
```

## 强制不变式(Enforced Invariants)
> **不变式说明**: 这些规则在聚合根内部强制执行，确保业务数据的一致性和完整性

### 1. 金额一致性约束 (Amount Consistency Constraints)
```text
基础金额规则(Basic Amount Rules):
- 支付金额(paymentAmount)必须大于0
- 已支付金额(paidAmount) >= 0
- 已退款金额(refundedAmount) >= 0
- 实际收款金额(actualAmount) = 已支付金额 - 已退款金额

金额关系约束(Amount Relationship Constraints):
- 已支付金额不能超过支付金额: paidAmount <= paymentAmount
- 已退款金额不能超过已支付金额: refundedAmount <= paidAmount
- 待支付金额 = 支付金额 - 已支付金额: pendingAmount = paymentAmount - paidAmount
- 可退款金额 = 已支付金额 - 已退款金额: refundableAmount = paidAmount - refundedAmount

特殊场景约束(Special Case Constraints):
- 信用还款类型的支付金额由信用管理系统确定，不允许修改
- 合并支付时，各支付单金额约束独立验证
- 部分支付时，每次支付金额必须大于0且不超过待支付金额
```

### 2. 状态转换约束 (State Transition Constraints)
```text
支付状态约束(Payment Status Constraints):
- 状态转换必须符合PaymentStatus枚举定义的转换规则
- 只有UNPAID、PARTIAL_PAID状态才能执行支付操作
- FROZEN状态下禁止任何支付和退款操作
- STOPPED状态需要明确恢复操作才能继续

退款状态约束(Refund Status Constraints):
- 只有PAID或PARTIAL_REFUNDED状态才能执行退款操作
- 退款状态转换必须符合RefundStatus枚举定义的转换规则
- 全额退款后(FULL_REFUNDED)不能再进行任何退款操作

流水状态约束(Transaction Status Constraints):
- 交易流水状态转换必须符合TransactionStatus枚举规则
- SUCCESS状态的流水不能再次修改
- 支付单状态必须与其流水状态保持一致性
```

### 3. 业务规则约束 (Business Rules Constraints)
```text
支付单创建约束(Payment Creation Constraints):
- 关联订单号必须有效且存在
- 经销商ID必须有效
- 支付类型必须明确指定
- 信用还款类型必须提供有效的关联业务信息

操作权限约束(Operation Permission Constraints):
- 每次支付操作必须创建对应的交易流水记录
- 每次退款操作必须关联有效的原支付流水
- 支付渠道必须是系统支持的有效渠道
- 合并支付的所有支付单必须属于同一经销商

数据完整性约束(Data Integrity Constraints):
- 支付单一旦创建，核心属性(支付金额、订单号、经销商ID)不可修改
- 交易流水的关联支付单号不可修改
- 所有货币金额必须使用CNY(人民币)
- 软删除的记录不能参与业务操作
```

### 4. 时间逻辑约束 (Time Logic Constraints)
```text
时间顺序约束(Time Sequence Constraints):
- 交易流水的完成时间必须晚于或等于创建时间
- 支付单的更新时间必须在状态变更时自动更新
- 支付截止时间(如设置)必须为未来时间

业务时间约束(Business Time Constraints):
- 信用还款的业务到期日必须合理设置
- 支付超时处理必须考虑不同渠道的特性
- 退款操作不受原支付时间限制，但需要满足渠道要求

系统时间约束(System Time Constraints):
- 所有时间字段必须使用系统标准时区
- 创建时间和更新时间必须由系统自动维护
- 业务时间计算必须考虑节假日和工作时间
```

### 5. 聚合一致性约束 (Aggregate Consistency Constraints)
```text
聚合内一致性(Intra-Aggregate Consistency):
- 支付单的金额统计必须与其交易流水记录一致
- 支付单状态必须反映其所有交易流水的综合状态
- 聚合内的任何状态变更必须在同一事务中完成

跨聚合一致性(Cross-Aggregate Consistency):
- 支付单状态变更必须通过领域事件通知相关系统
- 与订单系统的数据同步通过最终一致性保证
- 信用管理系统的额度变更通过事件机制协调

并发控制约束(Concurrency Control Constraints):
- 支付单的并发修改必须通过乐观锁机制控制
- 相同支付单的多个操作必须串行执行
- 合并支付场景下的多支付单操作需要防止死锁
```

### 6. 业务场景约束 (Business Scenario Constraints)
```text
B2B交易约束(B2B Transaction Constraints):
- 大额支付必须支持长时间异步确认机制
- 企业间交易必须保留完整的审计轨迹
- 支付渠道选择必须适应B2B交易特点

信用业务约束(Credit Business Constraints):
- 信用还款必须关联有效的信用记录
- 信用支付额度验证通过信用管理系统
- 还款支付单按普通支付流程处理但业务语义特殊

多渠道约束(Multi-Channel Constraints):
- 不同支付渠道的业务规则差异必须统一处理
- 渠道故障时必须有备用处理机制
- 跨渠道的对账和数据一致性必须保证
```

## 领域事件(Domain Events)
> **事件设计原则**: 领域事件用于通知业务状态变更，支持最终一致性和系统解耦

### PaymentCreated (支付单已创建)
> **术语对照**: PaymentCreated ↔ 支付单创建事件 (全局词汇表 v2.0)

```text
事件名称(Event Name): PaymentCreated
事件类型(Event Type): Domain Event (领域事件)
触发条件(Trigger Condition): 支付单成功创建并持久化后

事件属性(Event Data):
  - paymentId: PaymentId (支付单号)
  - orderId: OrderId (关联订单号)
  - resellerId: ResellerId (经销商ID)
  - paymentAmount: Money (支付金额)
  - paymentType: PaymentType (支付类型)
  - relatedBusinessInfo: RelatedBusinessInfo (关联业务信息，可选)
  - createdAt: DateTime (创建时间)
  - eventId: EventId (事件唯一标识)

发布时机(Publishing Timing): 支付单创建事务提交后异步发布
事件版本(Event Version): v1.0
订阅者(Subscribers): 
  - Order Context: 更新订单支付状态
  - Finance Context: 记录财务流水
  - Notification Context: 发送支付通知

业务含义(Business Meaning):
- 通知订单系统支付单已创建完成
- 触发财务系统的预期收入记录
- 启动支付提醒和通知流程
```

### PaymentExecuted (支付已执行)
> **术语对照**: PaymentExecuted ↔ 支付完成事件 (全局词汇表 v2.0)

```text
事件名称(Event Name): PaymentExecuted  
事件类型(Event Type): Domain Event (领域事件)
触发条件(Trigger Condition): 支付操作成功完成，支付单状态更新后

事件属性(Event Data):
  - paymentId: PaymentId (支付单号)
  - transactionId: TransactionId (交易流水号)
  - executedAmount: Money (本次支付金额)
  - totalPaidAmount: Money (累计已支付金额)
  - paymentChannel: PaymentChannel (支付渠道)
  - channelTransactionNumber: String (渠道交易号)
  - paymentStatus: PaymentStatus (支付单当前状态)
  - executedAt: DateTime (支付执行时间)
  - eventId: EventId (事件唯一标识)

发布时机(Publishing Timing): 支付回调处理完成并更新支付单状态后
事件版本(Event Version): v1.0
订阅者(Subscribers):
  - Order Context: 更新订单状态，触发后续业务流程
  - Finance Context: 更新财务账目，生成收入记录
  - Credit Management Context: 更新信用账户状态（信用还款场景）

业务含义(Business Meaning):
- 通知业务系统支付已到账
- 触发订单履约流程（如生产排产、发货安排）
- 更新企业信用状态（信用还款场景）
```

### PaymentStatusChanged (支付状态已变更)
> **术语对照**: PaymentStatusChanged ↔ 支付状态变更事件 (全局词汇表 v2.0)

```text
事件名称(Event Name): PaymentStatusChanged
事件类型(Event Type): Domain Event (领域事件)
触发条件(Trigger Condition): 支付单的支付状态或退款状态发生变更时

事件属性(Event Data):
  - paymentId: PaymentId (支付单号)
  - previousPaymentStatus: PaymentStatus (变更前支付状态)
  - currentPaymentStatus: PaymentStatus (变更后支付状态)
  - previousRefundStatus: RefundStatus (变更前退款状态)
  - currentRefundStatus: RefundStatus (变更后退款状态)
  - changeReason: String (状态变更原因)
  - changedAt: DateTime (变更时间)
  - eventId: EventId (事件唯一标识)

发布时机(Publishing Timing): 状态更新事务提交后立即发布
事件版本(Event Version): v1.0
订阅者(Subscribers):
  - Order Context: 同步支付状态变化
  - Notification Context: 发送状态变更通知
  - Monitoring Context: 记录状态变更轨迹

业务含义(Business Meaning):
- 实时同步支付状态到相关业务系统
- 触发相应的业务流程调整
- 支持支付过程的实时监控和预警
```

### RefundExecuted (退款已执行)
> **术语对照**: RefundExecuted ↔ 退款完成事件 (全局词汇表 v2.0)

```text
事件名称(Event Name): RefundExecuted
事件类型(Event Type): Domain Event (领域事件)
触发条件(Trigger Condition): 退款操作成功完成，退款状态更新后

事件属性(Event Data):
  - paymentId: PaymentId (支付单号)
  - refundTransactionId: TransactionId (退款流水号)
  - originalTransactionId: TransactionId (原支付流水号)
  - refundAmount: Money (本次退款金额)
  - totalRefundedAmount: Money (累计已退款金额)
  - refundChannel: PaymentChannel (退款渠道)
  - businessOrderId: String (业务退款单号)
  - refundStatus: RefundStatus (退款单当前状态)
  - refundReason: String (退款原因)
  - executedAt: DateTime (退款执行时间)
  - eventId: EventId (事件唯一标识)

发布时机(Publishing Timing): 退款回调处理完成并更新支付单状态后
事件版本(Event Version): v1.0  
订阅者(Subscribers):
  - Order Context: 更新订单退款状态，处理后续业务流程
  - Finance Context: 更新财务账目，记录退款支出
  - Notification Context: 发送退款完成通知

业务含义(Business Meaning):
- 通知业务系统退款已完成
- 触发订单系统的退款后续处理（如库存回库、合同变更）
- 更新财务记录和企业资金状况
```

### CreditRepaymentCompleted (信用还款已完成)
> **术语对照**: CreditRepaymentCompleted ↔ 信用还款完成事件 (全局词汇表 v2.0)

```text
事件名称(Event Name): CreditRepaymentCompleted
事件类型(Event Type): Domain Event (领域事件)
触发条件(Trigger Condition): 信用还款类型的支付单完成支付时

事件属性(Event Data):
  - paymentId: PaymentId (还款支付单号)
  - relatedBusinessId: String (关联的信用记录ID)
  - repaymentAmount: Money (还款金额)
  - creditRecordInfo: CreditRecordInfo (信用记录信息)
  - completedAt: DateTime (还款完成时间)
  - eventId: EventId (事件唯一标识)

发布时机(Publishing Timing): 信用还款支付完成后
事件版本(Event Version): v1.0
订阅者(Subscribers):
  - Credit Management Context: 更新信用记录状态和可用额度
  - Finance Context: 记录还款财务流水
  - Risk Management Context: 更新企业风险评级

业务含义(Business Meaning):
- 通知信用管理系统更新企业信用状态
- 恢复企业可用信用额度
- 更新企业信用评级和风险等级
```

### BatchPaymentCompleted (合并支付已完成)
> **术语对照**: BatchPaymentCompleted ↔ 合并支付完成事件 (全局词汇表 v2.0)

```text
事件名称(Event Name): BatchPaymentCompleted
事件类型(Event Type): Domain Event (领域事件)
触发条件(Trigger Condition): 多个支付单的合并支付操作全部完成时

事件属性(Event Data):
  - batchId: BatchId (合并支付批次ID)
  - paymentIds: List<PaymentId> (参与合并的支付单号列表)
  - totalAmount: Money (合并支付总金额)
  - paymentChannel: PaymentChannel (合并支付使用的渠道)
  - channelTransactionNumber: String (渠道合并交易号)
  - completedAt: DateTime (合并支付完成时间)
  - eventId: EventId (事件唯一标识)

发布时机(Publishing Timing): 所有参与合并的支付单状态更新完成后
事件版本(Event Version): v1.0
订阅者(Subscribers):
  - Order Context: 批量更新相关订单状态
  - Finance Context: 记录合并支付的财务流水
  - Analytics Context: 统计合并支付效果

业务含义(Business Meaning):
- 通知相关系统合并支付批次处理完成
- 支持合并支付效果的统计分析
- 触发批量业务流程的后续处理
```

## 仓储接口(Repository Interface)
> **仓储设计原则**: 仓储接口属于领域层，实现在基础设施层，提供聚合持久化和查询能力

### PaymentRepository (支付单仓储接口)
> **术语对照**: PaymentRepository ↔ 支付单仓储 (全局词汇表 v2.0)

```text
仓储接口名称(Repository Interface): PaymentRepository
聚合根类型(Aggregate Root Type): Payment
接口位置(Interface Location): 领域层 (Domain Layer)
实现位置(Implementation Location): 基础设施层 (Infrastructure Layer)

基础CRUD方法(Basic CRUD Methods):
  - save(payment: Payment): Payment
    功能: 保存或更新支付单聚合，包含所有内部实体
    返回: 保存后的支付单实例
    异常: PaymentPersistenceException
    
  - findById(id: PaymentId): Optional<Payment>
    功能: 根据支付单号查找完整的支付单聚合
    返回: 支付单可选值，包含所有交易流水
    
  - delete(payment: Payment): void
    功能: 软删除支付单，设置删除标识
    说明: 不进行物理删除，保持审计完整性
    
  - exists(id: PaymentId): Boolean
    功能: 检查支付单是否存在（排除已删除）

业务查询方法(Business Query Methods):
  - findByOrderId(orderId: OrderId): List<Payment>
    功能: 查找指定订单的所有支付单
    应用: 订单支付状态汇总、支付历史查询
    
  - findByResellerId(resellerId: ResellerId): List<Payment>  
    功能: 查找指定经销商的所有支付单
    应用: 经销商支付记录管理
    
  - findByPaymentStatus(status: PaymentStatus): List<Payment>
    功能: 按支付状态查找支付单
    应用: 支付状态统计、异常支付处理
    
  - findByPaymentType(type: PaymentType): List<Payment>
    功能: 按支付类型查找支付单
    应用: 预付款/尾款/信用还款分类管理
    
  - findByRelatedBusiness(businessType: RelatedBusinessType, businessId: String): List<Payment>
    功能: 查找关联特定业务记录的支付单
    应用: 信用记录相关还款查询、业务关联追踪

复杂查询方法(Complex Query Methods):
  - findByAmountRange(minAmount: Money, maxAmount: Money): List<Payment>
    功能: 按金额范围查找支付单
    应用: 大额支付监控、金额分析
    
  - findByDateRange(startDate: DateTime, endDate: DateTime): List<Payment>
    功能: 按时间范围查找支付单
    应用: 定期对账、时间维度统计分析
    
  - findByDeadlineBefore(deadline: DateTime): List<Payment>
    功能: 查找支付截止时间在指定时间前的支付单
    应用: 逾期支付预警、催收处理
    
  - findUnpaidOrPartialPaid(resellerId: ResellerId): List<Payment>
    功能: 查找指定经销商未完成支付的支付单
    应用: 待支付支付单管理、合并支付筛选
    
  - findPendingRefund(): List<Payment>
    功能: 查找有待处理退款的支付单
    应用: 退款处理监控、异常退款识别

批量操作方法(Batch Operation Methods):
  - saveAll(payments: List<Payment>): List<Payment>
    功能: 批量保存支付单，保证事务一致性
    应用: 合并支付场景、批量数据迁移
    
  - findByIds(ids: List<PaymentId>): List<Payment>
    功能: 批量查询支付单
    应用: 合并支付处理、批量状态检查
    
  - countByStatus(status: PaymentStatus): Long
    功能: 统计指定状态的支付单数量
    应用: 支付状态分布统计

统计分析方法(Analytics Methods):
  - sumAmountByDateRange(startDate: DateTime, endDate: DateTime): Money
    功能: 计算时间范围内的支付金额总和
    应用: 收入统计、业绩分析
    
  - countByPaymentTypeAndStatus(type: PaymentType, status: PaymentStatus): Long
    功能: 按支付类型和状态统计支付单数量
    应用: 支付类型分析、完成率统计
    
  - findTopResellersByAmount(limit: Integer): List<ResellerPaymentSummary>
    功能: 查找支付金额最高的经销商
    应用: 客户价值分析、VIP客户识别

性能优化方法(Performance Optimization Methods):
  - findWithTransactions(id: PaymentId): Optional<Payment>
    功能: 查找支付单并预加载所有交易流水
    说明: 优化聚合加载性能，减少N+1查询
    
  - findSummaryById(id: PaymentId): Optional<PaymentSummary>
    功能: 查找支付单汇总信息（不包含交易流水）
    应用: 列表展示、快速状态检查

查询条件对象(Query Criteria Objects):
  - PaymentSearchCriteria: 支付单综合查询条件
    属性: resellerId, paymentStatus, paymentType, amountRange, dateRange, businessInfo
    用途: 复杂条件查询、报表筛选
    
  - PaymentStatisticsCriteria: 支付单统计查询条件  
    属性: groupBy, dateRange, filterConditions
    用途: 数据分析、报表统计
```

### TransactionRepository (交易流水仓储接口)
> **术语对照**: TransactionRepository ↔ 交易流水仓储 (全局词汇表 v2.0)

```text
仓储接口名称(Repository Interface): TransactionRepository
实体类型(Entity Type): PaymentTransaction
设计说明(Design Notes): 作为支付单聚合的一部分，通常通过PaymentRepository操作

独立查询方法(Independent Query Methods):
  - findByPaymentId(paymentId: PaymentId): List<PaymentTransaction>
    功能: 查询指定支付单的所有交易流水
    
  - findByChannelTransactionNumber(transactionNumber: String): Optional<PaymentTransaction>
    功能: 根据渠道交易号查找流水记录
    应用: 支付回调处理、对账处理
    
  - findByTransactionType(type: TransactionType): List<PaymentTransaction>
    功能: 按交易类型查找流水（支付/退款）
    
  - findRefundableTransactions(paymentId: PaymentId): List<PaymentTransaction>
    功能: 查找可用于退款的支付交易流水
    应用: 退款流水选择、退款金额验证

对账专用方法(Reconciliation Methods):
  - findByDateRangeAndChannel(startDate: DateTime, endDate: DateTime, channel: PaymentChannel): List<PaymentTransaction>
    功能: 按时间和渠道查找交易流水
    应用: 日常对账、渠道对账
    
  - findUnreconciledTransactions(reconcileDate: DateTime): List<PaymentTransaction>  
    功能: 查找未对账的交易流水
    应用: 对账差异识别
```

## 领域服务(Domain Services)
> **领域服务说明**: 当业务逻辑不适合放在单个聚合内或需要跨聚合协调时，使用领域服务封装

### PaymentDomainService (支付领域服务)
> **术语对照**: PaymentDomainService ↔ 支付领域服务 (全局词汇表 v2.0)

```text
服务名称(Service Name): PaymentDomainService
服务职责(Service Responsibility): 协调支付单聚合的复杂业务逻辑，处理跨聚合的业务规则

核心服务方法(Core Service Methods):

1. executeSinglePayment(request: SinglePaymentRequest): PaymentExecutionResult
   功能描述: 执行单个支付单的支付操作
   业务逻辑: 
   - 验证支付单状态和业务规则
   - 创建支付交易流水
   - 调用支付渠道服务
   - 更新支付单状态和金额
   - 发布支付事件
   参数: SinglePaymentRequest(paymentId, amount, channel, operatorInfo)
   返回: PaymentExecutionResult(success, transactionId, channelResult)
   异常: PaymentExecutionException, BusinessRuleViolationException

2. executeBatchPayment(request: BatchPaymentRequest): BatchPaymentResult
   功能描述: 执行多个支付单的合并支付操作
   业务逻辑:
   - 验证合并支付规则（同一经销商、状态有效）
   - 计算合并支付总金额
   - 创建合并支付交易流水
   - 调用支付渠道进行合并支付
   - 分配支付结果到各支付单
   - 批量更新支付单状态
   - 发布合并支付事件
   参数: BatchPaymentRequest(paymentIds, channel, operatorInfo)
   返回: BatchPaymentResult(overallSuccess, individualResults)

3. executeRefund(request: RefundRequest): RefundExecutionResult
   功能描述: 执行退款操作，选择支付流水并处理退款
   业务逻辑:
   - 验证退款请求合法性
   - 选择适当的支付流水进行退款
   - 创建退款交易流水
   - 调用支付渠道退款接口
   - 更新支付单退款状态和金额
   - 发布退款事件
   参数: RefundRequest(paymentId, refundAmount, refundReason, businessOrderId)
   返回: RefundExecutionResult(success, refundTransactionId, channelResult)

4. processPaymentCallback(callback: PaymentCallbackData): CallbackProcessResult
   功能描述: 处理支付渠道的异步回调通知
   业务逻辑:
   - 验证回调数据的签名和完整性
   - 查找对应的支付单和交易流水
   - 更新交易流水状态
   - 重新计算支付单金额和状态
   - 发布状态变更事件
   参数: PaymentCallbackData(channelTransactionNumber, result, amount, signature)
   返回: CallbackProcessResult(processed, paymentId, statusUpdated)
```

### PaymentValidationService (支付验证领域服务)
> **术语对照**: PaymentValidationService ↔ 支付验证服务 (全局词汇表 v2.0)

```text
服务名称(Service Name): PaymentValidationService  
服务职责(Service Responsibility): 封装复杂的支付业务规则验证逻辑

核心验证方法(Core Validation Methods):

1. validatePaymentExecution(payment: Payment, request: PaymentExecutionRequest): ValidationResult
   功能描述: 验证支付执行请求的完整性和业务合规性
   验证规则:
   - 支付单状态是否允许支付
   - 支付金额是否合法（> 0，<= 待支付金额）
   - 支付渠道是否有效和可用
   - 经销商是否有支付权限
   - 业务截止时间是否满足要求
   返回: ValidationResult(valid, violations, warningMessages)

2. validateBatchPayment(payments: List<Payment>, channel: PaymentChannel): ValidationResult
   功能描述: 验证合并支付的业务规则
   验证规则:
   - 所有支付单必须属于同一经销商
   - 所有支付单状态必须允许支付
   - 合并支付总金额不超过限制
   - 选择的支付渠道支持合并支付
   - 不存在冲突的业务规则
   返回: ValidationResult(valid, violations, batchLimitations)

3. validateRefundExecution(payment: Payment, refundAmount: Money, originalTransactionId: TransactionId): ValidationResult
   功能描述: 验证退款执行的业务合规性
   验证规则:
   - 支付单退款状态是否允许退款
   - 退款金额是否合法（<= 可退款金额）
   - 原支付流水是否存在且有效
   - 退款渠道是否支持
   - 业务时效性要求
   返回: ValidationResult(valid, violations, refundConstraints)

4. validateCreditRepayment(payment: Payment, creditInfo: RelatedBusinessInfo): ValidationResult
   功能描述: 验证信用还款相关的特殊业务规则
   验证规则:
   - 关联业务信息是否有效
   - 信用记录是否存在且状态正确
   - 还款金额是否符合信用管理要求
   - 还款时效是否在允许范围内
   返回: ValidationResult(valid, violations, creditConstraints)
```

### PaymentCalculationService (支付计算领域服务)
> **术语对照**: PaymentCalculationService ↔ 支付计算服务 (全局词汇表 v2.0)

```text
服务名称(Service Name): PaymentCalculationService
服务职责(Service Responsibility): 处理支付相关的复杂金额计算和财务逻辑

核心计算方法(Core Calculation Methods):

1. calculatePaymentDistribution(batchRequest: BatchPaymentRequest, paidAmount: Money): PaymentDistribution
   功能描述: 计算合并支付中各支付单的金额分配
   计算逻辑:
   - 按支付单金额比例分配
   - 处理分配余额的舍入问题
   - 确保分配总额等于实际支付金额
   - 优先分配高优先级支付单
   返回: PaymentDistribution(paymentAllocations, roundingAdjustments)

2. calculateRefundDistribution(payment: Payment, refundAmount: Money): RefundDistribution
   功能描述: 计算退款在多个支付流水间的分配
   计算逻辑:
   - 选择最优的退款流水组合
   - 按时间或金额优先级分配
   - 处理跨渠道退款的复杂性
   - 最小化退款操作次数
   返回: RefundDistribution(refundAllocations, operationCount)

3. calculatePenaltyAmount(payment: Payment, currentDate: DateTime): Money
   功能描述: 计算逾期支付的滞纳金
   计算逻辑:
   - 基于支付截止时间和当前时间
   - 按逾期天数和金额比例计算
   - 考虑节假日和特殊情况
   - 应用最大滞纳金限制
   返回: Money(penaltyAmount)

4. calculateCreditImpact(creditRepayment: Payment): CreditImpact
   功能描述: 计算信用还款对企业信用的影响
   计算逻辑:
   - 还款及时性评分
   - 还款金额对信用额度的恢复
   - 信用历史的更新影响
   - 风险等级的调整建议
   返回: CreditImpact(scoreChange, limitRestoration, riskAdjustment)
```

### PaymentReconciliationService (支付对账领域服务)
> **术语对照**: PaymentReconciliationService ↔ 支付对账服务 (全局词汇表 v2.0)

```text
服务名称(Service Name): PaymentReconciliationService
服务职责(Service Responsibility): 处理支付系统与渠道方的对账逻辑

核心对账方法(Core Reconciliation Methods):

1. performDailyReconciliation(reconcileDate: Date, channel: PaymentChannel): ReconciliationResult
   功能描述: 执行日常对账处理
   对账逻辑:
   - 获取系统内交易流水记录
   - 获取渠道方对账文件数据
   - 比较交易金额、状态、时间等关键信息
   - 识别差异和异常记录
   - 生成对账报告和处理建议
   返回: ReconciliationResult(matchedCount, discrepancies, recommendations)

2. identifyDiscrepancies(systemTransactions: List<PaymentTransaction>, channelData: ChannelReconciliationData): List<Discrepancy>
   功能描述: 识别对账差异
   差异类型:
   - 金额不匹配
   - 状态不一致  
   - 时间偏差异常
   - 缺失交易记录
   - 重复交易处理
   返回: List<Discrepancy>(discrepancyType, systemRecord, channelRecord, impact)

3. generateReconciliationReport(result: ReconciliationResult): ReconciliationReport
   功能描述: 生成对账报告
   报告内容:
   - 对账汇总统计
   - 差异明细列表
   - 处理建议和优先级
   - 风险评估和影响分析
   返回: ReconciliationReport(summary, details, recommendations, riskAssessment)
```

---

## 聚合设计总结 (Aggregate Design Summary)

### 设计决策说明 (Design Decisions)

#### 1. 聚合边界设计 (Aggregate Boundary Design)
- **Payment作为聚合根**: 支付单承载完整的支付业务生命周期，是业务的自然边界
- **PaymentTransaction作为聚合内实体**: 交易流水与支付单强相关，通过聚合根统一管理确保一致性
- **统一流水设计**: 支付和退款流水统一管理，通过transactionType区分，简化数据模型复杂度

#### 2. 业务规则封装 (Business Rules Encapsulation)  
- **聚合内不变式**: 金额一致性、状态转换等核心业务规则在聚合内强制执行
- **领域服务协调**: 复杂的跨聚合业务逻辑通过领域服务封装
- **事件驱动**: 通过领域事件实现与外部系统的松耦合集成

#### 3. 扩展性设计 (Extensibility Design)
- **关联业务支持**: 通过RelatedBusinessInfo支持信用还款等扩展业务场景
- **业务标签**: 通过BusinessTags提供灵活的业务分类和扩展能力
- **渠道适配**: 通过PaymentChannel值对象统一不同支付渠道的差异

#### 4. 性能考虑 (Performance Considerations)
- **聚合大小控制**: 交易流水数量可控，避免聚合过大
- **查询优化**: 仓储接口提供多种查询方式，支持不同业务场景的性能需求
- **事件异步**: 通过异步事件处理减少业务操作的响应时间

### 术语一致性验证 (Terminology Consistency Verification)
✅ **核心业务术语**: 与全局词汇表 v2.0 完全一致
✅ **状态枚举术语**: 使用标准的英文枚举值和中文描述
✅ **业务流程术语**: 符合上下文设计中的通用语言定义
✅ **系统集成术语**: 与外部系统接口的术语映射准确

### 实现指导原则 (Implementation Guidelines)

#### 1. 代码实现建议
- 聚合根和实体使用充血模型，业务逻辑封装在对象内部
- 值对象设计为不可变类，提供丰富的业务方法
- 枚举类型提供业务含义的静态方法，便于业务规则判断

#### 2. 数据持久化建议
- 聚合根和内部实体映射到对应的数据库表
- 值对象可以嵌入到聚合根表中或单独建表
- 软删除机制确保数据完整性和审计需求

#### 3. 事件处理建议
- 领域事件在聚合根状态变更后发布
- 事件处理采用最终一致性，支持重试和幂等
- 事件序列化包含足够的业务上下文信息

### 下一步工作 (Next Steps)
1. ✅ **领域层设计完成**: Payment聚合设计文档已完成
2. 🔄 **应用层设计**: 基于领域模型设计应用服务和用例编排
3. ⏳ **基础设施层设计**: 设计Repository实现和外部系统集成
4. ⏳ **集成层设计**: 设计上下文映射和外部系统协作方案

---

**文档状态**: ✅ 已完成  
**版本**: v2.0  
**最后更新**: 2025年9月26日  
**术语基准**: 全局词汇表 v2.0, 支付上下文设计 v2.0  
**审核状态**: 待技术评审
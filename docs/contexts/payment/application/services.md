# Payment Application Layer (支付应用层)

## 应用层设计文档

### 应用服务设计(Application Service Design)

## PaymentApplicationService (支付应用服务)

### 服务名称(Service Name)
PaymentApplicationService

### 业务用例(Use Cases)
1. 支付创建处理
2. 支付执行操作
3. 支付状态查询
4. 合并支付处理
5. 分批支付管理
6. 退款执行操作
7. 支付状态更新

### 服务方法定义

#### createPayment (创建支付)
```text
方法签名: PaymentResponse 1. 接收CreatePaymentRequest
2. 验证请求参数有效性
3. 检查订单是否存在（调用OrderServiceClient）
4. 验证企业用户权限
5. 创建Payment聚合
6. 保存到PaymentRepository
7. 发布PaymentCreated领域事件
8. 返回PaymentResponseyment(CreatePaymentRequest request)
业务描述: 接收订单系统的支付创建请求，创建支付实体
输入参数: CreatePaymentRequest
输出结果: PaymentResponse
事务边界: 方法级事务
异常处理: 
  - InvalidPaymentAmountException -> 400 Bad Request
  - DuplicatePaymentException -> 409 Conflict
  - OrderNotFoundException -> 404 Not Found
```

#### executePayment (执行支付)
```text
方法签名: PaymentExecutionResponse executePayment(PaymentExecutionRequest request)
业务描述: 执行单个支付单的支付操作，调用支付渠道
输入参数: PaymentExecutionRequest
输出结果: PaymentExecutionResponse
事务边界: 方法级事务
异常处理:
  - PaymentNotFoundException -> 404 Not Found
  - InvalidPaymentStatusException -> 400 Bad Request
  - PaymentChannelUnavailableException -> 503 Service Unavailable
  - InsufficientPaymentAmountException -> 400 Bad Request
```

#### executeBatchPayment (执行合并支付)
```text
方法签名: BatchPaymentResponse executeBatchPayment(BatchPaymentRequest request)
业务描述: 执行多个支付单的合并支付操作
输入参数: BatchPaymentRequest
输出结果: BatchPaymentResponse
事务边界: 方法级事务
异常处理:
  - PaymentsNotFoundException -> 404 Not Found
  - IncompatiblePaymentsException -> 400 Bad Request
  - PaymentChannelUnavailableException -> 503 Service Unavailable
```

#### processPaymentCallback (处理支付回调)
```text
方法签名: void processPaymentCallback(PaymentCallbackRequest request)
业务描述: 处理支付渠道的异步回调通知
输入参数: PaymentCallbackRequest
输出结果: void
事务边界: 方法级事务
异常处理:
  - InvalidSignatureException -> 401 Unauthorized
  - PaymentNotFoundException -> 404 Not Found
  - InvalidCallbackDataException -> 400 Bad Request
```

#### executeRefund (执行退款)
```text
方法签名: RefundExecutionResponse executeRefund(RefundExecutionRequest request)
业务描述: 执行退款操作，更新支付单状态
输入参数: RefundExecutionRequest
输出结果: RefundExecutionResponse
事务边界: 方法级事务
异常处理:
  - PaymentNotFoundException -> 404 Not Found
  - InvalidRefundAmountException -> 400 Bad Request
  - RefundNotAllowedException -> 403 Forbidden
```

#### queryPayment (查询支付)
```text
方法签名: PaymentDetailResponse queryPayment(PaymentQueryRequest request)
业务描述: 查询支付单详细信息和状态
输入参数: PaymentQueryRequest
输出结果: PaymentDetailResponse
事务边界: 只读事务
异常处理:
  - PaymentNotFoundException -> 404 Not Found
  - UnauthorizedAccessException -> 403 Forbidden
```

### 依赖注入(Dependencies)
```text
- PaymentRepository: 支付仓储
- PaymentExecutionService: 支付执行领域服务
- PaymentValidationService: 支付验证领域服务
- ChannelSelectionService: 渠道选择领域服务
- DomainEventPublisher: 领域事件发布器
- PaymentChannelAdapter: 支付渠道适配器(用于第三方渠道集成)
- OrderServiceClient: 订单服务客户端
```

### 事务边界(Transaction Boundary)
- 每个应用服务方法为一个事务单元
- 查询操作使用只读事务
- 跨聚合操作通过领域事件实现最终一致性
- 外部服务调用不在同一事务内

## DTO设计(DTO Design)

### 输入DTO (Input DTOs)

#### CreatePaymentRequest
```text
DTO名称(DTO Name): CreatePaymentRequest
使用场景(Usage Scenario): 创建支付单输入
属性列表(Attributes):
  - relatedOrderId: String (关联订单号)
  - companyUserId: String (企业用户ID)
  - paymentAmount: BigDecimal (支付金额)
  - currency: String (货币类型)
  - paymentType: String (支付类型: ADVANCE_PAYMENT, FINAL_PAYMENT, OTHER_PAYMENT)
  - paymentDeadline: LocalDateTime (支付截止时间, 可选)
  - businessDescription: String (业务描述, 可选)

验证规则(Validation Rules):
  - relatedOrderId: 必填，长度32位
  - companyUserId: 必填，长度32位
  - paymentAmount: 必填，大于0，最多2位小数
  - currency: 必填，符合ISO 4217标准
  - paymentType: 必填，枚举值有效
  - paymentDeadline: 可选，必须是未来时间
  - businessDescription: 可选，最大长度500字符

转换规则(Conversion Rules):
  - paymentAmount + currency -> Money值对象
  - paymentType -> PaymentType枚举
  - relatedOrderId -> OrderId值对象
  - companyUserId -> CompanyUserId值对象
```

#### PaymentExecutionRequest
```text
DTO名称(DTO Name): PaymentExecutionRequest
使用场景(Usage Scenario): 执行支付输入
属性列表(Attributes):
  - paymentId: String (支付号)
  - paymentAmount: BigDecimal (支付金额)
  - channelCode: String (支付渠道代码)
  - paymentMethod: String (支付方式)
  - clientIp: String (客户端IP)
  - returnUrl: String (回调地址, 可选)
  - extraParams: Map<String, String> (额外参数, 可选)

验证规则(Validation Rules):
  - paymentId: 必填，长度32位
  - paymentAmount: 必填，大于0，最多2位小数
  - channelCode: 必填，非空
  - paymentMethod: 必填，枚举值有效
  - clientIp: 必填，有效IP格式
  - returnUrl: 可选，有效URL格式

转换规则(Conversion Rules):
  - paymentId -> PaymentId值对象
  - paymentAmount -> Money值对象
  - channelCode -> PaymentChannel值对象
  - paymentMethod -> PaymentMethod枚举
```

#### BatchPaymentRequest
```text
DTO名称(DTO Name): BatchPaymentRequest
使用场景(Usage Scenario): 合并支付输入
属性列表(Attributes):
  - paymentIds: List<String> (支付号列表)
  - channelCode: String (支付渠道代码)
  - paymentMethod: String (支付方式)
  - clientIp: String (客户端IP)
  - returnUrl: String (回调地址, 可选)

验证规则(Validation Rules):
  - paymentIds: 必填，非空列表，每个元素长度32位
  - channelCode: 必填，非空
  - paymentMethod: 必填，枚举值有效
  - clientIp: 必填，有效IP格式
  - returnUrl: 可选，有效URL格式

转换规则(Conversion Rules):
  - paymentIds -> List<PaymentId>值对象
  - channelCode -> PaymentChannel值对象
  - paymentMethod -> PaymentMethod枚举
```

#### RefundExecutionRequest
```text
DTO名称(DTO Name): RefundExecutionRequest
使用场景(Usage Scenario): 退款执行输入
属性列表(Attributes):
  - paymentId: String (支付号)
  - refundAmount: BigDecimal (退款金额)
  - refundReason: String (退款原因)
  - orderRefundId: String (订单退款单号)
  - originalTransactionId: String (原支付流水号, 可选)

验证规则(Validation Rules):
  - paymentId: 必填，长度32位
  - refundAmount: 必填，大于0，最多2位小数
  - refundReason: 必填，最大长度200字符
  - orderRefundId: 必填，长度32位
  - originalTransactionId: 可选，长度32位

转换规则(Conversion Rules):
  - paymentId -> PaymentId值对象
  - refundAmount -> Money值对象
  - originalTransactionId -> PaymentTransactionId值对象
```

### 输出DTO (Output DTOs)

#### PaymentResponse
```text
DTO名称(DTO Name): PaymentResponse
使用场景(Usage Scenario): 支付创建输出
属性列表(Attributes):
  - paymentId: String (支付号)
  - relatedOrderId: String (关联订单号)
  - paymentAmount: BigDecimal (支付金额)
  - currency: String (货币类型)
  - paymentStatus: String (支付状态)
  - paymentType: String (支付类型)
  - createdAt: LocalDateTime (创建时间)

转换规则(Conversion Rules):
  - Payment实体 -> PaymentResponse DTO
  - Money值对象 -> paymentAmount + currency字段
  - PaymentStatus枚举 -> paymentStatus字符串
  - PaymentType枚举 -> paymentType字符串
```

#### PaymentExecutionResponse
```text
DTO名称(DTO Name): PaymentExecutionResponse
使用场景(Usage Scenario): 支付执行输出
属性列表(Attributes):
  - paymentId: String (支付号)
  - transactionId: String (交易流水号)
  - paymentStatus: String (支付状态)
  - channelResponse: String (渠道响应)
  - redirectUrl: String (跳转地址, 可选)
  - qrCode: String (二维码内容, 可选)
  - estimatedTime: Integer (预计确认时间, 秒)

转换规则(Conversion Rules):
  - PaymentTransaction实体 -> PaymentExecutionResponse DTO
  - 渠道适配器响应 -> channelResponse字段
```

#### PaymentDetailResponse
```text
DTO名称(DTO Name): PaymentDetailResponse
使用场景(Usage Scenario): 支付详情查询输出
属性列表(Attributes):
  - paymentId: String (支付号)
  - relatedOrderId: String (关联订单号)
  - companyUserId: String (企业用户ID)
  - paymentAmount: BigDecimal (支付金额)
  - paidAmount: BigDecimal (已支付金额)
  - refundedAmount: BigDecimal (已退款金额)
  - actualAmount: BigDecimal (实际收款金额)
  - currency: String (货币类型)
  - paymentStatus: String (支付状态)
  - paymentType: String (支付类型)
  - paymentTransactions: List<PaymentTransactionDto> (支付流水)
  - refundTransactions: List<RefundTransactionDto> (退款流水)
  - createdAt: LocalDateTime (创建时间)
  - updatedAt: LocalDateTime (更新时间)

转换规则(Conversion Rules):
  - Payment聚合 -> PaymentDetailResponse DTO
  - 包含所有相关的支付和退款流水信息
```

## 用例编排(Use Case Orchestration)

### 支付单创建用例流程
```text
1. 接收CreatePaymentRequest
2. 验证请求参数有效性
3. 检查订单是否存在（调用OrderServiceClient）
4. 验证企业用户权限
5. 创建Payment聚合
6. 保存到PaymentRepository
7. 发布PaymentCreated领域事件
8. 返回PaymentResponse
```

### 支付执行用例流程
```text
1. 接收PaymentExecutionRequest
2. 验证请求参数有效性
3. 查找Payment聚合
4. 验证支付状态和权限
5. 选择和验证支付渠道
6. 调用PaymentExecutionService执行支付
7. 创建PaymentTransaction实体
8. 更新Payment状态
9. 保存聚合状态
10. 发布PaymentExecuted领域事件
11. 返回PaymentExecutionResponse
```

### 退款执行用例流程
```text
1. 接收RefundExecutionRequest
2. 验证请求参数有效性
3. 查找Payment聚合
4. 验证退款条件和金额
5. 选择原支付流水记录
6. 调用支付渠道退款接口
7. 创建RefundTransaction实体
8. 更新Payment退款金额和状态
9. 保存聚合状态
10. 发布RefundExecuted领域事件
11. 返回RefundExecutionResponse
```

## 领域层交互(Domain Layer Interaction)

### 与Payment聚合的交互
```text
- 通过PaymentRepository获取聚合实例
- 调用聚合根的业务方法处理业务逻辑
- 通过聚合根发布领域事件
- 将聚合状态变更持久化
```

### 与PaymentChannel聚合的交互
```text
- 通过PaymentChannelRepository查询可用渠道
- 使用ChannelSelectionService选择最适合的渠道
- 验证渠道能力和限制条件
- 获取渠道配置信息
```

### 领域服务调用
```text
- PaymentExecutionService: 执行支付业务逻辑
- PaymentValidationService: 验证支付业务规则
- ChannelSelectionService: 选择支付渠道
- ChannelHealthCheckService: 检查渠道健康状态
```
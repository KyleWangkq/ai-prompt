# 支付模块应用层设计

## 概述

应用层负责协调支付业务用例的执行，管理事务边界，处理与外部系统的集成。基于新设计的支付领域模型，提供完整的支付业务功能。

### 文档信息
| 项目 | 内容 |
|------|------|
| **文档名称** | 支付模块应用层设计文档 |
| **文档版本** | v6.0 |
| **创建日期** | 2025年9月28日 |
| **更新日期** | 2025年9月28日 |
| **领域基准** | 支付模块领域模型设计文档 v5.0 |
| **需求基准** | 支付模块需求设计文档 v1.4 |
| **架构基准** | 统一支付处理架构设计 |

## 应用层概述 (Application Layer Overview)

**上下文名称**: Payment Context (支付上下文)
**核心职责**: 协调支付领域业务用例执行，管理事务边界，处理外部系统集成
**主要用例**: 支付单创建、支付执行、退款处理、支付查询、合并支付
**事务策略**: 单聚合操作使用数据库事务，跨聚合协调采用最终一致性

## 应用服务设计 (Application Services Design)

### PaymentApplicationService (支付应用服务)

**服务职责**: 协调支付单生命周期管理，编排统一支付和退款业务流程，支持单支付单和合并支付的统一处理
**依赖聚合**: Payment聚合
**事务边界**: 每个方法为一个事务单元，查询操作使用只读事务
**架构特点**: 基于统一支付处理架构，单支付单支付作为多支付单合并支付的特例

#### 关键设计原则

**信用还款统一处理原则**:
- 信用还款本质上是PaymentType为CREDIT_REPAYMENT的支付单，无需独立服务或聚合
- 使用统一的createPayment接口，通过支付类型区分处理逻辑
- 信用还款使用虚拟订单号（"CREDIT_" + relatedBusinessId）保证数据完整性
- 复用完整的支付处理流程（创建→执行→回调→完成），确保一致性

**统一支付架构原则**:
- 所有支付类型（普通支付、信用还款）使用相同的Payment聚合根
- 单一支付执行接口executePayment处理所有支付场景
- 合并支付和单支付单支付使用统一的处理逻辑
- 条件验证根据支付类型动态调整，保持接口简洁性

#### 服务接口定义
```java
public interface PaymentApplicationService {
    // 命令操作 - 支付单管理（统一创建接口，支持所有支付类型包括信用还款）
    PaymentResponse createPayment(CreatePaymentCommand command);
    
    // 命令操作 - 统一支付执行（单支付单和合并支付使用统一接口）
    UnifiedPaymentExecutionResponse executePayment(UnifiedPaymentExecutionCommand command);
    void processPaymentCallback(PaymentCallbackCommand command);
    void processRefundCallback(RefundCallbackCommand command);
    
    // 命令操作 - 退款处理（支持多种流水选择策略）
    RefundExecutionResponse executeRefund(RefundExecutionCommand command);
    
    // 命令操作 - 支付单操作
    PaymentStatusResponse stopPayment(StopPaymentCommand command);
    PaymentStatusResponse freezePayment(FreezePaymentCommand command);
    PaymentStatusResponse unfreezePayment(UnfreezePaymentCommand command);
    PaymentResponse modifyPaymentInfo(PaymentModificationCommand command);
    
    // 查询操作 - 支付单筛选和查询
    PaymentListResponse queryFilteredPayments(PaymentFilterQuery query);
    PaymentListResponse queryBatchablePayments(BatchablePaymentsQuery query);
    PaymentDetailResponse queryPaymentDetail(PaymentDetailQuery query);
    
    // 查询操作 - 业务维度查询  
    PaymentListResponse queryPaymentsByReseller(PaymentsByResellerQuery query);
    PaymentListResponse queryPaymentsByOrder(PaymentsByOrderQuery query);
    PaymentListResponse queryPaymentsByRelatedBusiness(PaymentsByRelatedBusinessQuery query);
    
    // 查询操作 - 高级搜索和统计
    PaymentListResponse searchPayments(PaymentSearchCriteriaQuery query);
    PaymentStatisticsResponse queryPaymentStatistics(PaymentStatisticsQuery query);
    TransactionListResponse queryTransactionHistory(TransactionHistoryQuery query);
    
    // 查询操作 - 状态和进度监控
    PaymentStatusResponse queryPaymentStatus(PaymentStatusQuery query);
    PaymentProgressResponse queryPaymentProgress(PaymentProgressQuery query);
    RefundProgressResponse queryRefundProgress(RefundProgressQuery query);
    
    // 查询操作 - 支持合并支付的特殊查询
    MergedPaymentInfoResponse queryMergedPaymentInfo(MergedPaymentInfoQuery query);
    List<PaymentTransactionDto> queryTransactionsByChannelNumber(String channelTransactionNumber);
}
```

#### 方法详细设计

##### createPayment - 创建支付单（统一接口，支持所有支付类型）
**用例描述**: 接收订单系统或信用管理系统的支付单创建请求，统一创建支付单
**前置条件**: 根据支付类型验证相应的前置条件（订单存在或信用记录有效），经销商具有支付权限
**业务流程**: 
1. 验证CreatePaymentCommand的参数完整性和业务合规性
2. 根据支付类型执行相应的业务验证：
   - 普通支付类型：调用OrderServiceClient验证关联订单
   - 信用还款类型：根据关联业务ID生成虚拟订单号（CREDIT_[relatedBusinessId]）
3. 验证经销商的支付权限和企业状态
4. 根据支付类型执行特定的业务规则验证
5. 创建Payment聚合实例，分配唯一支付单号
6. 持久化支付单到PaymentRepository
7. 发布PaymentCreated领域事件
8. 构建并返回PaymentResponse

**支付类型处理**:
- **普通支付类型**（ADVANCE_PAYMENT, FINAL_PAYMENT, OTHER_PAYMENT）：需要有效的订单号
- **信用还款类型**（CREDIT_REPAYMENT）：使用关联业务信息，系统生成虚拟订单号

**异常处理**: 
- `InvalidPaymentRequestException`: 请求参数不合法或业务规则验证失败
- `OrderNotFoundException`: 关联订单不存在或状态不正确（普通支付类型）
- `CreditRecordNotFoundException`: 关联信用记录不存在（信用还款类型）
- `ResellerUnauthorizedException`: 经销商无支付权限
- `DuplicatePaymentException`: 重复创建相同的支付单

**发布事件**: PaymentCreated - 通知相关系统支付单已创建

**设计说明**: 信用还款本质上就是支付类型为CREDIT_REPAYMENT的支付单，使用统一的处理流程，无需特殊处理

##### executePayment - 统一支付执行（核心方法）
**用例描述**: 执行支付单的统一支付操作，支持单支付单和合并支付的统一处理
**前置条件**: 支付单列表有效，支付渠道可用，金额分配合理
**业务流程**:
1. 解析UnifiedPaymentExecutionCommand，提取支付单列表和金额分配
2. 批量查询Payment聚合实例并验证状态兼容性
3. 验证支付单是否属于同一经销商（合并支付要求）
4. 验证支付渠道兼容性和可用性
5. 计算总支付金额和每个支付单的分配金额
6. 调用PaymentChannelAdapter执行统一支付请求
7. 为每个支付单创建PaymentTransaction流水记录
8. 批量更新所有支付单状态为PAYING
9. 发布UnifiedPaymentExecuted领域事件
10. 构建并返回UnifiedPaymentExecutionResponse

**核心特点**:
- **统一处理**: 单支付单支付作为合并支付的特例（列表只有一个元素）
- **批量优化**: 支持批量数据库操作，提高性能
- **事务一致性**: 所有状态变更在同一事务内完成

**异常处理**:
- `PaymentNotFoundException`: 支付单不存在
- `InvalidPaymentStatusException`: 支付单状态不允许支付
- `IncompatiblePaymentsException`: 支付单不兼容（不同经销商等）
- `PaymentChannelUnavailableException`: 支付渠道不可用
- `InvalidAmountAllocationException`: 金额分配不合法
- `UnifiedPaymentExecutionException`: 统一支付执行异常

**发布事件**: UnifiedPaymentExecuted, PaymentStatusChanged

##### processPaymentCallback - 处理支付回调
**用例描述**: 处理支付渠道的异步回调通知，支持合并支付的回调处理
**前置条件**: 回调数据签名有效，关联的支付流水存在
**业务流程**:
1. 验证PaymentCallbackCommand的签名和数据完整性
2. 根据渠道交易号查找所有相关的PaymentTransaction（支持合并支付）
3. 遍历处理每个支付流水的回调结果
4. 更新每个交易流水的状态和完成时间
5. 重新计算每个关联支付单的已支付金额
6. 根据待支付金额判断支付单是否完成
7. 批量更新支付单状态和金额信息
8. 通知订单系统支付状态变更（如果需要）
9. 发布相应的状态变更领域事件

**设计说明**: 
- 支持一个渠道交易号对应多个支付单的回调处理
- 长时间异步确认机制适应B2B大额支付特点

**异常处理**:
- `InvalidCallbackSignatureException`: 回调签名验证失败
- `CallbackDataIncompleteException`: 回调数据不完整
- `TransactionNotFoundException`: 对应的交易流水不存在
- `CallbackProcessingException`: 回调处理业务异常

**发布事件**: PaymentExecuted, PaymentCompleted, PaymentStatusChanged

##### executeRefund - 执行退款（支持多种流水选择策略）
**用例描述**: 接收订单系统退款指令，执行退款操作并更新支付单状态
**前置条件**: 支付单存在，退款金额合理，审批流程已完成
**业务流程**:
1. 验证RefundExecutionCommand的参数完整性
2. 查询Payment聚合及其所有支付流水记录
3. 根据TransactionSelectionStrategy选择退款流水：
   - SPECIFIED: 使用指定的交易流水
   - LATEST: 选择最新的一笔支付流水
   - PROPORTIONAL: 按金额比例分摊到多个流水
4. 验证选择的流水是否支持退款操作
5. 调用PaymentChannelAdapter执行渠道退款
6. 为每个退款操作创建RefundTransaction流水记录
7. 更新支付单的已退款金额和退款状态
8. 重新计算支付单的实际收款金额
9. 发布RefundExecuted领域事件
10. 构建并返回RefundExecutionResponse

**流水选择策略**:
- **指定流水退款**: 精确退款到特定的支付记录
- **最新流水退款**: 默认选择最近的支付记录
- **多流水分摊退款**: 当单笔流水金额不足时按比例分摊

**异常处理**:
- `PaymentNotFoundException`: 支付单不存在
- `InvalidRefundAmountException`: 退款金额不合法
- `NoAvailableTransactionException`: 没有可用的退款流水
- `RefundChannelException`: 渠道退款操作失败
- `RefundExecutionException`: 退款执行业务异常

**发布事件**: RefundExecuted, PaymentStatusChanged

### PaymentEventHandler (支付事件处理服务)

**服务职责**: 处理支付领域事件，协调跨上下文业务流程，维护系统一致性
**依赖聚合**: Payment聚合
**事务边界**: 每个事件处理为独立事务，支持最终一致性

#### 服务接口定义
```java
public interface PaymentEventHandler {
    void handlePaymentCreated(PaymentCreatedEvent event);
    void handlePaymentExecuted(PaymentExecutedEvent event);
    void handlePaymentCompleted(PaymentCompletedEvent event);
    void handleRefundExecuted(RefundExecutedEvent event);
    void handleCreditRepaymentCompleted(CreditRepaymentCompletedEvent event);
}
```

## DTO设计 (DTO Design)

### 命令DTO (Command DTOs)

#### CreatePaymentCommand
**用途**: 统一支付单创建请求，支持所有支付类型（普通支付和信用还款）
**验证策略**: 根据支付类型进行条件验证，参数完整性验证、业务规则预检查

```java
public class CreatePaymentCommand {
    // 通用必填字段
    @NotBlank(message = "经销商ID不能为空") 
    @Size(max = 32, message = "经销商ID长度不能超过32位")
    private String resellerId;
    
    @NotNull(message = "支付金额不能为空")
    @DecimalMin(value = "0.01", message = "支付金额必须大于0")
    @Digits(integer = 14, fraction = 6, message = "金额格式不正确(最大14位整数，6位小数)")
    private BigDecimal paymentAmount;
    
    @NotNull(message = "支付类型不能为空")
    @Enumerated(EnumType.STRING)
    private PaymentType paymentType; // ADVANCE_PAYMENT, FINAL_PAYMENT, OTHER_PAYMENT, CREDIT_REPAYMENT
    
    // 条件必填字段 - 根据支付类型验证
    // 普通支付类型必填
    @ConditionalNotBlank(dependsOn = "paymentType", 
                        values = {"ADVANCE_PAYMENT", "FINAL_PAYMENT", "OTHER_PAYMENT"},
                        message = "普通支付类型必须提供订单号")
    @Size(max = 32, message = "订单号长度不能超过32位")
    private String orderId;
    
    // 信用还款类型必填
    @ConditionalNotBlank(dependsOn = "paymentType", 
                        values = {"CREDIT_REPAYMENT"},
                        message = "信用还款类型必须提供关联业务ID")
    @Size(max = 32, message = "关联业务ID长度不能超过32位")
    private String relatedBusinessId;
    
    @ConditionalNotNull(dependsOn = "paymentType", 
                       values = {"CREDIT_REPAYMENT"},
                       message = "信用还款类型必须指定关联业务类型")
    @Enumerated(EnumType.STRING)
    private RelatedBusinessType relatedBusinessType; // CREDIT_RECORD等
    
    // 业务信息字段
    @Size(max = 500, message = "业务描述长度不能超过500字符")
    private String businessDesc;
    
    @Future(message = "支付截止时间必须是未来时间")
    private LocalDateTime paymentDeadline;
    
    private PriorityLevel priorityLevel = PriorityLevel.MEDIUM; // HIGH, MEDIUM, LOW
    
    private LocalDateTime businessExpireDate;
    
    // 扩展信息
    @Valid
    private Map<String, String> businessTags = new HashMap<>();
    
    // 操作者信息
    @NotBlank(message = "创建人不能为空")
    private String createBy;
    
    @NotBlank(message = "创建人姓名不能为空")
    private String createByName;
    
    // 设计说明: 信用还款时orderId将由系统自动生成为"CREDIT_" + relatedBusinessId
    // 这样保证了所有支付单都有orderId，但信用还款使用虚拟订单号
}
```



#### UnifiedPaymentExecutionCommand
**用途**: 统一支付执行指令（支持单支付单和合并支付的统一处理）
**验证策略**: 支付渠道兼容性和金额分配验证

```java
public class UnifiedPaymentExecutionCommand {
    @NotEmpty(message = "支付单号列表不能为空")
    @Size(min = 1, max = 50, message = "支付单数量必须在1-50之间")
    private List<@NotBlank @Size(max = 32) String> paymentIds;
    
    @NotNull(message = "支付渠道不能为空")
    @Enumerated(EnumType.STRING)
    private PaymentChannel paymentChannel; // ONLINE_PAYMENT, WALLET_PAYMENT, WIRE_TRANSFER, CREDIT_ACCOUNT
    
    @NotBlank(message = "支付方式不能为空")
    private String paymentMethod;
    
    // 金额分配策略（用于部分支付场景）
    @Valid
    private List<PaymentAmountAllocation> amountAllocations;
    
    @Pattern(regexp = "^((2[0-4]\\d|25[0-5]|[01]?\\d\\d?)\\.){3}(2[0-4]\\d|25[0-5]|[01]?\\d\\d?)$", 
             message = "客户端IP格式不正确")
    private String clientIp;
    
    @URL(message = "回调地址格式不正确")
    private String returnUrl;
    
    private Map<String, String> extraParams = new HashMap<>();
    
    @NotBlank(message = "操作人不能为空")
    private String operatorId;
    
    @NotBlank(message = "操作人姓名不能为空")
    private String operatorName;
    
    // 内部类：金额分配信息
    public static class PaymentAmountAllocation {
        @NotBlank(message = "支付单号不能为空")
        private String paymentId;
        
        @NotNull(message = "分配金额不能为空")
        @DecimalMin(value = "0.01", message = "分配金额必须大于0")
        @Digits(integer = 14, fraction = 6, message = "金额格式不正确")
        private BigDecimal allocatedAmount;
    }
}
```

#### RefundExecutionCommand
**用途**: 退款执行指令，支持多种流水选择策略
**验证策略**: 退款金额和流水选择策略验证

```java
public class RefundExecutionCommand {
    @NotBlank(message = "支付单号不能为空")
    @Size(max = 32, message = "支付单号长度必须为32位")
    private String paymentId;
    
    @NotNull(message = "退款金额不能为空")
    @DecimalMin(value = "0.01", message = "退款金额必须大于0")
    @Digits(integer = 14, fraction = 6, message = "金额格式不正确")
    private BigDecimal refundAmount;
    
    @NotBlank(message = "退款原因不能为空")
    @Size(max = 200, message = "退款原因长度不能超过200字符")
    private String refundReason;
    
    @NotBlank(message = "业务退款单号不能为空")
    @Size(max = 32, message = "业务退款单号长度必须为32位")
    private String businessOrderId;
    
    // 支付流水选择策略
    @NotNull(message = "流水选择策略不能为空")
    @Enumerated(EnumType.STRING)
    private TransactionSelectionStrategy selectionStrategy; // SPECIFIED, LATEST, PROPORTIONAL
    
    // 指定流水退款时使用
    @Size(max = 32, message = "指定交易流水号长度必须为32位")
    private String specifiedTransactionId;
    
    // 多流水分摊退款时使用
    @Valid
    private List<TransactionRefundAllocation> refundAllocations;
    
    @NotBlank(message = "操作人不能为空")
    private String operatorId;
    
    @NotBlank(message = "操作人姓名不能为空")
    private String operatorName;
    
    // 内部类：退款分摊信息
    public static class TransactionRefundAllocation {
        @NotBlank(message = "交易流水号不能为空")
        private String transactionId;
        
        @NotNull(message = "退款金额不能为空")
        @DecimalMin(value = "0.01", message = "退款金额必须大于0")
        private BigDecimal refundAmount;
    }
}
```

### 查询DTO (Query DTOs)

#### PaymentFilterQuery
**用途**: 支付单多维度筛选查询（替代原有的多个单独查询）
**分页支持**: 支持分页和多字段排序

```java
public class PaymentFilterQuery {
    // 基础筛选条件
    @Size(max = 32, message = "经销商ID长度不能超过32位")
    private String resellerId;
    
    @Size(max = 32, message = "订单号长度不能超过32位")
    private String orderId;
    
    // 状态筛选
    private Set<PaymentStatus> paymentStatuses;
    private Set<RefundStatus> refundStatuses;
    private Set<PaymentType> paymentTypes;
    
    // 金额筛选
    @DecimalMin(value = "0", message = "最小金额不能小于0")
    private BigDecimal minAmount;
    
    @DecimalMax(value = "999999999999.999999", message = "最大金额超出系统限制")
    private BigDecimal maxAmount;
    
    // 时间筛选
    private LocalDate createDateFrom;
    private LocalDate createDateTo;
    private LocalDate paymentDeadlineFrom;
    private LocalDate paymentDeadlineTo;
    
    // 关联业务筛选
    private Set<RelatedBusinessType> relatedBusinessTypes;
    @Size(max = 32, message = "关联业务ID长度不能超过32位")
    private String relatedBusinessId;
    
    // 优先级筛选
    private Set<PriorityLevel> priorityLevels;
    
    // 支付能力筛选（用于合并支付场景）
    private PaymentChannel preferredChannel;
    private boolean onlyBatchable = false; // 仅显示可合并支付的支付单
    
    // 分页和排序
    @Min(value = 0, message = "页码必须大于等于0")
    private int page = 0;
    
    @Min(value = 1, message = "页面大小必须大于0")
    @Max(value = 100, message = "页面大小不能超过100")
    private int size = 20;
    
    @Valid
    private List<SortField> sortFields = List.of(new SortField("createTime", "DESC"));
    
    // 内部类：排序字段
    public static class SortField {
        @NotBlank(message = "排序字段不能为空")
        private String field; // createTime, paymentAmount, paymentDeadline等
        
        @Pattern(regexp = "^(ASC|DESC)$", message = "排序方向必须是ASC或DESC")
        private String direction = "DESC";
    }
}
```

#### BatchablePaymentsQuery
**用途**: 查询可合并支付的支付单列表
**验证策略**: 合并支付兼容性验证

```java
public class BatchablePaymentsQuery {
    @NotBlank(message = "经销商ID不能为空")
    @Size(max = 32, message = "经销商ID长度不能超过32位")
    private String resellerId;
    
    @NotNull(message = "支付渠道不能为空")
    @Enumerated(EnumType.STRING)
    private PaymentChannel paymentChannel;
    
    // 金额范围限制
    @DecimalMin(value = "0", message = "最小金额不能小于0")
    private BigDecimal minAmount;
    
    @DecimalMax(value = "999999999999.999999", message = "最大金额超出系统限制")  
    private BigDecimal maxAmount;
    
    // 支付类型筛选
    private Set<PaymentType> paymentTypes;
    
    // 排除已选择的支付单
    private Set<String> excludePaymentIds = new HashSet<>();
    
    // 分页参数
    @Min(value = 0, message = "页码必须大于等于0")
    private int page = 0;
    
    @Min(value = 1, message = "页面大小必须大于0")
    @Max(value = 50, message = "页面大小不能超过50")
    private int size = 20;
}
```

#### SinglePaymentExecutionCommand
**用途**: 单笔支付执行指令
**验证策略**: 支付渠道和金额验证

```java
public class SinglePaymentExecutionCommand {
    @NotBlank(message = "支付单号不能为空")
    @Size(max = 32, message = "支付单号长度必须为32位")
    private String paymentId;
    
    @NotNull(message = "支付金额不能为空")
    @DecimalMin(value = "0.01", message = "支付金额必须大于0")
    @Digits(integer = 15, fraction = 6, message = "金额格式不正确")
    private BigDecimal executionAmount;
    
    @NotNull(message = "支付渠道不能为空")
    @Enumerated(EnumType.STRING)
    private PaymentChannel paymentChannel;
    
    @NotBlank(message = "支付方式不能为空")
    private String paymentMethod;
    
    @Pattern(regexp = "^((2[0-4]\\d|25[0-5]|[01]?\\d\\d?)\\.){3}(2[0-4]\\d|25[0-5]|[01]?\\d\\d?)$", 
             message = "客户端IP格式不正确")
    private String clientIp;
    
    @URL(message = "回调地址格式不正确")
    private String returnUrl;
    
    private Map<String, String> extraParams = new HashMap<>();
    
    @NotBlank(message = "操作人不能为空")
    private String operatorId;
    
    @NotBlank(message = "操作人姓名不能为空")
    private String operatorName;
}

### 响应DTO (Response DTOs)

#### PaymentResponse
**用途**: 支付单创建和基础操作的响应
**数据来源**: Payment聚合的基础信息

```java
public class PaymentResponse {
    private String paymentId;
    private String orderId;
    private String resellerId;
    private BigDecimal paymentAmount;
    private BigDecimal paidAmount;
    private BigDecimal refundedAmount;
    private BigDecimal actualAmount;
    private BigDecimal pendingAmount;
    private String currency = "CNY";
    private PaymentStatus paymentStatus;
    private RefundStatus refundStatus;
    private PaymentType paymentType;
    private String businessDesc;
    private LocalDateTime paymentDeadline;
    private PriorityLevel priorityLevel;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
    private String createByName;
    
    // 关联业务信息
    private String relatedBusinessId;
    private RelatedBusinessType relatedBusinessType;
    private LocalDateTime businessExpireDate;
    private Map<String, String> businessTags;
}
```

#### UnifiedPaymentExecutionResponse
**用途**: 统一支付执行的响应（支持单支付单和合并支付）
**数据来源**: PaymentTransaction和渠道响应信息

```java
public class UnifiedPaymentExecutionResponse {
    // 批次信息
    private String batchExecutionId; // 批次执行标识
    private List<String> paymentIds;
    private int paymentCount;
    private BigDecimal totalExecutionAmount;
    private PaymentChannel paymentChannel;
    private String paymentMethod;
    
    // 执行结果汇总
    private boolean overallSuccess;
    private int successCount;
    private int failureCount;
    private LocalDateTime batchExecuteTime;
    
    // 渠道响应信息（统一的渠道信息）
    private String channelTransactionNumber;
    private String channelResponse;
    private String redirectUrl;
    private String qrCodeContent;
    
    // 预估信息
    private Integer estimatedConfirmTimeSeconds;
    private LocalDateTime estimatedCompleteTime;
    
    // 每个支付单的执行详情
    private List<SinglePaymentExecutionResult> individualResults;
    
    // 内部类：单个支付单执行结果
    public static class SinglePaymentExecutionResult {
        private String paymentId;
        private String transactionId;
        private PaymentStatus paymentStatus;
        private TransactionStatus transactionStatus;
        private BigDecimal allocatedAmount; // 分配的执行金额
        private boolean executeSuccess;
        private String executeMessage;
    }
}
```

#### PaymentDetailResponse
**用途**: 支付单详情查询响应，包含完整信息和流水记录
**数据来源**: Payment聚合及其内部实体

```java
public class PaymentDetailResponse {
    // 基础信息
    private String paymentId;
    private String orderId;
    private String resellerId;
    
    // 完整金额信息
    private BigDecimal paymentAmount;
    private BigDecimal paidAmount;
    private BigDecimal refundedAmount;
    private BigDecimal actualAmount;
    private BigDecimal pendingAmount;
    private String currency = "CNY";
    
    // 状态信息
    private PaymentStatus paymentStatus;
    private RefundStatus refundStatus;
    private PaymentType paymentType;
    
    // 业务信息
    private String businessDesc;
    private LocalDateTime paymentDeadline;
    private PriorityLevel priorityLevel;
    
    // 关联业务信息
    private String relatedBusinessId;
    private RelatedBusinessType relatedBusinessType;
    private LocalDateTime businessExpireDate;
    private Map<String, String> businessTags;
    
    // 支付能力信息
    private boolean batchable; // 是否可合并支付
    private List<PaymentChannel> supportedChannels; // 支持的支付渠道
    
    // 交易流水信息
    private List<PaymentTransactionDto> paymentTransactions;
    private List<PaymentTransactionDto> refundTransactions;
    
    // 支付进度信息
    private PaymentProgressInfo progressInfo;
    
    // 系统信息
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
    private String createByName;
    private String updateByName;
    
    // 内部类：支付进度信息
    public static class PaymentProgressInfo {
        private BigDecimal progressPercentage; // 支付进度百分比
        private BigDecimal refundPercentage;   // 退款进度百分比
        private int paymentTransactionCount;   // 支付流水数量
        private int refundTransactionCount;    // 退款流水数量
        private LocalDateTime lastPaymentTime; // 最后支付时间
        private LocalDateTime lastRefundTime;  // 最后退款时间
    }
}
```

#### PaymentTransactionDto
**用途**: 支付和退款交易流水信息的传输
**数据来源**: PaymentTransaction实体

```java
public class PaymentTransactionDto {
    private String transactionId;
    private String paymentId;
    private TransactionType transactionType;     // PAYMENT, REFUND
    private TransactionStatus transactionStatus; // PENDING, SUCCESS, FAILED
    private BigDecimal transactionAmount;
    private String currency = "CNY";
    
    // 渠道信息
    private PaymentChannel paymentChannel;
    private String paymentMethod;
    private String channelTransactionId;
    private String channelResponse;
    
    // 业务信息
    private String businessOrderId;  // 关联的业务单号
    private String originalTransactionId;  // 原支付交易ID（退款时使用）
    private String businessRemark;
    
    // 时间信息
    private LocalDateTime createTime;
    private LocalDateTime completeTime;
    private LocalDateTime notifyTime;  // 回调通知时间
    private LocalDateTime expirationTime; // 过期时间
    
    // 操作信息
    private String operatorId;
    private String operatorName;
}
```

#### PaymentListResponse
**用途**: 支付单列表查询响应，包含分页和汇总信息
**数据来源**: Payment聚合的列表数据

```java
public class PaymentListResponse {
    // 分页信息
    private PageInfo pageInfo;
    
    // 支付单列表
    private List<PaymentSummaryDto> payments;
    
    // 统计汇总信息
    private PaymentListStatistics statistics;
    
    // 内部类：分页信息
    public static class PageInfo {
        private int currentPage;
        private int pageSize;
        private int totalPages;
        private long totalRecords;
        private boolean hasNext;
        private boolean hasPrevious;
    }
    
    // 内部类：列表统计信息
    public static class PaymentListStatistics {
        private BigDecimal totalPaymentAmount;
        private BigDecimal totalPaidAmount;
        private BigDecimal totalRefundedAmount;
        private BigDecimal totalActualAmount;
        private Map<PaymentStatus, Integer> statusCounts;
        private Map<PaymentType, Integer> typeCounts;
        private int batchableCount; // 可合并支付数量
    }
}
```

#### PaymentSummaryDto
**用途**: 支付单列表项的简要信息
**数据来源**: Payment聚合的基础信息

```java
public class PaymentSummaryDto {
    private String paymentId;
    private String orderId;
    private String resellerId;
    private BigDecimal paymentAmount;
    private BigDecimal paidAmount;
    private BigDecimal pendingAmount;
    private String currency = "CNY";
    private PaymentStatus paymentStatus;
    private RefundStatus refundStatus;
    private PaymentType paymentType;
    private LocalDateTime createTime;
    private String createByName;
    
    // 业务摘要信息
    private String businessDesc;
    private LocalDateTime paymentDeadline;
    private PriorityLevel priorityLevel;
    private String relatedBusinessId;
    private RelatedBusinessType relatedBusinessType;
    
    // 操作相关信息
    private boolean batchable; // 是否可合并支付
    private boolean hasRefund; // 是否有退款记录
    private boolean overdue;   // 是否已逾期
    private LocalDateTime lastTransactionTime; // 最近交易时间
    
    // 支付进度信息
    private BigDecimal paymentProgress; // 支付进度百分比 (0-100)
}
```

#### MergedPaymentInfoResponse
**用途**: 合并支付信息查询响应
**数据来源**: 通过渠道交易号关联的多个支付单信息

```java
public class MergedPaymentInfoResponse {
    private String channelTransactionNumber;
    private PaymentChannel paymentChannel;
    private String paymentMethod;
    private BigDecimal totalAmount;
    private TransactionStatus overallStatus;
    private LocalDateTime executeTime;
    
    // 参与合并支付的支付单信息
    private List<MergedPaymentItem> paymentItems;
    
    // 合并支付汇总统计
    private MergedPaymentStatistics statistics;
    
    // 内部类：合并支付项
    public static class MergedPaymentItem {
        private String paymentId;
        private String orderId;
        private String transactionId;
        private BigDecimal allocatedAmount;
        private PaymentStatus paymentStatus;
        private TransactionStatus transactionStatus;
    }
    
    // 内部类：合并支付统计
    public static class MergedPaymentStatistics {
        private int totalPaymentCount;
        private int successCount;
        private int failureCount;
        private int pendingCount;
        private BigDecimal successAmount;
        private BigDecimal failureAmount;
        private BigDecimal pendingAmount;
    }
}
```

## 用例编排设计 (Use Case Orchestration)

### 统一支付执行用例流程（支持单支付单和合并支付）

**参与者**: 支付应用服务、支付领域服务、支付渠道适配器
**核心特点**: 单支付单和合并支付使用统一的处理逻辑，简化系统复杂度
**事务边界**: 单一事务管理所有参与支付的支付单状态变更

**主流程**:
```mermaid
sequenceDiagram
    participant Client
    participant AppService as PaymentApplicationService
    participant DomainService as PaymentDomainService
    participant Repository as PaymentRepository
    participant ChannelAdapter as PaymentChannelAdapter
    participant EventPublisher
    
    Client->>AppService: executePayment(UnifiedPaymentExecutionCommand)
    
    Note over AppService: 1. 验证支付单列表
    AppService->>Repository: findPaymentsByIds(paymentIds)
    Repository-->>AppService: paymentList
    
    Note over AppService: 2. 验证合并支付兼容性
    AppService->>DomainService: validateBatchCompatibility(payments, channel)
    DomainService-->>AppService: validationResult
    
    Note over AppService: 3. 计算总金额和分配策略
    AppService->>DomainService: calculateAmountAllocation(payments, allocations)
    DomainService-->>AppService: allocationPlan
    
    Note over AppService: 4. 调用支付渠道（统一接口）
    AppService->>ChannelAdapter: executePayment(totalAmount, channelInfo)
    ChannelAdapter-->>AppService: channelResponse
    
    Note over AppService: 5. 创建支付流水记录（每个支付单一条）
    loop 为每个支付单创建流水
        AppService->>Repository: savePaymentTransaction(transaction)
    end
    
    Note over AppService: 6. 更新所有支付单状态
    loop 更新每个支付单
        AppService->>Repository: updatePaymentStatus(paymentId, PAYING)
    end
    
    Note over AppService: 7. 发布领域事件
    AppService->>EventPublisher: publish(UnifiedPaymentExecutedEvent)
    
    AppService-->>Client: UnifiedPaymentExecutionResponse
```

**设计说明**:
- **统一处理逻辑**: 无论单个还是多个支付单，都按同一套流程处理
- **批量优化**: 数据库操作支持批量处理，提高性能
- **事务一致性**: 所有状态变更在同一事务内完成
- **渠道抽象**: 支付渠道适配器屏蔽具体渠道差异

### 支付回调处理用例流程

**参与者**: 支付渠道、支付应用服务、支付领域服务
**核心特点**: 支持B2B大额支付的长时间异步确认机制
**事务边界**: 回调处理和状态更新在单一事务内完成

**主流程**:
```mermaid
sequenceDiagram
    participant Channel as PaymentChannel
    participant AppService as PaymentApplicationService
    participant DomainService as PaymentDomainService
    participant Repository as PaymentRepository
    participant EventPublisher
    participant OrderClient as OrderServiceClient
    
    Channel->>AppService: processPaymentCallback(PaymentCallbackCommand)
    
    Note over AppService: 1. 验证回调签名和数据
    AppService->>DomainService: validateCallbackSignature(callbackData)
    DomainService-->>AppService: isValid
    
    Note over AppService: 2. 查找关联的支付流水
    AppService->>Repository: findTransactionByChannelNumber(channelTransactionId)
    Repository-->>AppService: transactionList
    
    Note over AppService: 3. 处理回调结果（支持合并支付）
    loop 处理每个相关交易流水
        AppService->>DomainService: processTransactionCallback(transaction, callbackResult)
        DomainService-->>AppService: updatedTransaction
        
        Note over AppService: 4. 更新支付单金额和状态
        AppService->>Repository: findPaymentById(paymentId)
        Repository-->>AppService: payment
        AppService->>DomainService: updatePaymentAmounts(payment, transactionAmount)
        DomainService-->>AppService: updatedPayment
        AppService->>Repository: savePayment(updatedPayment)
        
        Note over AppService: 5. 通知订单系统（如果支付完成）
        alt 支付单完成
            AppService->>OrderClient: notifyPaymentCompleted(paymentId)
        end
        
        Note over AppService: 6. 发布状态变更事件
        AppService->>EventPublisher: publish(PaymentStatusChangedEvent)
    end
```

### 退款流水选择用例流程

**参与者**: 订单系统、支付应用服务、支付领域服务
**核心特点**: 支持多种流水选择策略，灵活处理不同退款场景
**事务边界**: 退款操作和支付单状态更新在单一事务内完成

**主流程**:
```mermaid
sequenceDiagram
    participant OrderSystem as OrderSystem
    participant AppService as PaymentApplicationService
    participant DomainService as PaymentDomainService
    participant Repository as PaymentRepository
    participant ChannelAdapter as PaymentChannelAdapter
    participant EventPublisher
    
    OrderSystem->>AppService: executeRefund(RefundExecutionCommand)
    
    Note over AppService: 1. 查询支付单和流水记录
    AppService->>Repository: findPaymentWithTransactions(paymentId)
    Repository-->>AppService: paymentWithTransactions
    
    Note over AppService: 2. 根据策略选择退款流水
    alt 指定流水退款
        AppService->>DomainService: selectSpecifiedTransaction(transactionId)
    else 最新流水退款
        AppService->>DomainService: selectLatestTransaction(transactions)
    else 多流水分摊退款
        AppService->>DomainService: calculateProportionalRefund(transactions, amount)
    end
    DomainService-->>AppService: selectedTransactions
    
    Note over AppService: 3. 验证退款条件
    AppService->>DomainService: validateRefundConditions(payment, refundAmount)
    DomainService-->>AppService: validationResult
    
    Note over AppService: 4. 执行渠道退款
    loop 为每个选中流水执行退款
        AppService->>ChannelAdapter: executeRefund(transaction, refundAmount)
        ChannelAdapter-->>AppService: refundResponse
        
        Note over AppService: 5. 创建退款流水记录
        AppService->>Repository: saveRefundTransaction(refundTransaction)
    end
    
    Note over AppService: 6. 更新支付单退款金额和状态
    AppService->>DomainService: updateRefundAmounts(payment, totalRefundAmount)
    DomainService-->>AppService: updatedPayment
    AppService->>Repository: savePayment(updatedPayment)
    
    Note over AppService: 7. 发布退款事件
    AppService->>EventPublisher: publish(RefundExecutedEvent)
    
    AppService-->>OrderSystem: RefundExecutionResponse
```

### 事务边界设计 (Transaction Boundary Design)

#### 1. 支付单创建事务
**事务范围**: 单个支付单的创建和初始化
**事务内容**:
- 支付单数据验证和持久化
- 初始状态设置
- 领域事件发布（事务提交后异步处理）

#### 2. 统一支付执行事务
**事务范围**: 一批支付单的统一支付处理
**事务内容**:
- 批量支付单状态验证
- 支付渠道调用
- 批量支付流水创建
- 批量支付单状态更新
- 统一领域事件发布

**补偿机制**:
- 渠道调用失败时回滚所有状态变更
- 支持幂等重试机制

#### 3. 支付回调处理事务
**事务范围**: 回调结果处理和相关状态更新
**事务内容**:
- 回调数据验证
- 支付流水状态更新
- 支付单金额和状态重新计算
- 相关通知和事件发布

#### 4. 退款执行事务
**事务范围**: 退款操作和支付单状态调整
**事务内容**:
- 退款条件验证
- 退款流水选择和验证
- 渠道退款调用
- 退款流水创建
- 支付单退款信息更新

## 依赖注入配置 (Dependency Injection)

### 应用服务依赖
```java
@Service
public class PaymentApplicationServiceImpl implements PaymentApplicationService {
    
    private final PaymentRepository paymentRepository;
    private final DomainEventPublisher eventPublisher;
    private final OrderServiceClient orderServiceClient;
    private final PaymentDomainService paymentDomainService;
    private final PaymentChannelAdapter paymentChannelAdapter;
    
    // 构造函数注入
    public PaymentApplicationServiceImpl(
        PaymentRepository paymentRepository,
        DomainEventPublisher eventPublisher,
        OrderServiceClient orderServiceClient,
        PaymentDomainService paymentDomainService,
        PaymentChannelAdapter paymentChannelAdapter
    ) {
        this.paymentRepository = paymentRepository;
        this.eventPublisher = eventPublisher;
        this.orderServiceClient = orderServiceClient;
        this.paymentDomainService = paymentDomainService;
        this.paymentChannelAdapter = paymentChannelAdapter;
    }
}
```

### 外部依赖接口
| 依赖名称 | 接口类型 | 用途 | 实现位置 |
|----------|----------|------|----------|
| PaymentRepository | Repository Interface | 支付聚合持久化 | 基础设施层 |
| OrderServiceClient | Anti-Corruption Layer | 订单系统集成 | 基础设施层 |
| PaymentChannelAdapter | Adapter Interface | 支付渠道集成 | 基础设施层 |
| DomainEventPublisher | Publisher Interface | 领域事件发布 | 基础设施层 |

## 事务管理 (Transaction Management)

### 事务边界策略
- **单聚合事务**: Payment聚合的CRUD操作在单一数据库事务内完成
- **跨聚合协调**: 通过领域事件和最终一致性保证多个上下文间的数据一致性
- **外部集成事务**: 与支付渠道交互采用补偿模式处理失败情况

### 事务配置
```java
@Transactional(rollbackFor = Exception.class)
public PaymentResponse createPayment(CreatePaymentCommand command) {
    try {
        // 1. 验证和转换输入
        // 2. 创建Payment聚合
        // 3. 持久化聚合状态
        // 4. 发布领域事件（事务提交后异步）
    } catch (BusinessException e) {
        // 业务异常处理
        throw new ApplicationException(e.getMessage(), e);
    }
}
```

## 异常处理策略 (Exception Handling)

### 异常分类和处理
| 异常类型 | 触发条件 | 处理策略 | 返回码 |
|----------|----------|----------|--------|
| InvalidPaymentRequestException | 创建请求参数不合法 | 返回参数验证错误信息 | 400 |
| PaymentNotFoundException | 支付单不存在 | 返回资源不存在错误 | 404 |
| PaymentStatusConflictException | 支付状态不允许操作 | 返回状态冲突错误 | 409 |
| PaymentChannelUnavailableException | 支付渠道不可用 | 返回服务不可用错误 | 503 |

### 异常处理实现
```java
@ExceptionHandler(PaymentNotFoundException.class)
public ResponseEntity<ErrorResponse> handlePaymentNotFound(PaymentNotFoundException e) {
    return ResponseEntity
        .status(HttpStatus.NOT_FOUND)
        .body(new ErrorResponse("PAYMENT_NOT_FOUND", e.getMessage()));
}
```

## 性能考虑 (Performance Considerations)

### 查询优化
- **聚合查询**: 通过Repository接口的预加载方法避免N+1查询问题
- **只读查询**: 支付单列表查询使用只读事务，减少锁竞争
- **缓存策略**: 对频繁查询的支付渠道配置信息进行缓存

### 批量操作优化
- **批量处理**: 合并支付使用批量数据库操作，减少数据库往返次数
- **分页处理**: 大数据量查询支持分页，避免内存溢出
- **异步处理**: 支付回调处理采用异步机制，提高系统吞吐量

---

## 设计原则检查

### 应用层职责
- [x] 应用服务只做编排,不包含业务逻辑
- [x] DTO只做数据传输,不包含业务行为  
- [x] 事务边界清晰合理
- [x] 异常处理覆盖全面

### 依赖管理
- [x] 应用层依赖领域层接口
- [x] 外部依赖通过接口抽象
- [x] 循环依赖已避免

### 性能设计
- [x] 查询策略合理高效
- [x] 批量操作已优化
- [x] 缓存策略恰当

---

**文档状态**: ✅ 已完成  
**版本**: v3.0  
**最后更新**: 2024年12月19日  
**术语基准**: 全局词汇表 v3.0, 支付上下文设计 v3.0  
**审核状态**: 待技术评审

## DTO设计 (DTO Design)

### 命令DTO (Command DTOs)


- **截止时间合理性**: 支付截止时间不能超过业务约定的最长期限

#### CreateCreditRepaymentCommand
**用途**: 信用还款支付单创建，专门用于信用管理系统发起的还款请求
**验证策略**: 信用业务特殊规则验证

```java
public class CreateCreditRepaymentCommand {
    @NotBlank(message = "信用记录ID不能为空")
    @Size(max = 32, message = "信用记录ID长度不能超过32位")
    private String creditRecordId;
    
    @NotBlank(message = "经销商ID不能为空")
    @Size(max = 32, message = "经销商ID长度不能超过32位") 
    private String resellerId;
    
    @NotNull(message = "还款金额不能为空")
    @DecimalMin(value = "0.01", message = "还款金额必须大于0")
    @Digits(integer = 15, fraction = 6, message = "金额格式不正确")
    private BigDecimal repaymentAmount;
    
    @NotNull(message = "还款到期日不能为空")
    @Future(message = "还款到期日必须是未来时间")
    private LocalDateTime repaymentDueDate;
    
    @Size(max = 500, message = "还款说明长度不能超过500字符")
    private String repaymentDesc;
    
    @NotBlank(message = "创建人不能为空")
    private String createBy;
    
    @NotBlank(message = "创建人姓名不能为空")
    private String createByName;
}
```

#### SinglePaymentExecutionCommand
**用途**: 单笔支付执行指令
**验证策略**: 支付渠道和金额验证

```java
public class SinglePaymentExecutionCommand {
    @NotBlank(message = "支付单号不能为空")
    @Size(max = 32, message = "支付单号长度必须为32位")
    private String paymentId;
    
    @NotNull(message = "支付金额不能为空")
    @DecimalMin(value = "0.01", message = "支付金额必须大于0")
    @Digits(integer = 15, fraction = 6, message = "金额格式不正确")
    private BigDecimal executionAmount;
    
    @NotNull(message = "支付渠道不能为空")
    @Enumerated(EnumType.STRING)
    private PaymentChannel paymentChannel;
    
    @NotBlank(message = "支付方式不能为空")
    private String paymentMethod;
    
    @Pattern(regexp = "^((2[0-4]\\d|25[0-5]|[01]?\\d\\d?)\\.){3}(2[0-4]\\d|25[0-5]|[01]?\\d\\d?)$", 
             message = "客户端IP格式不正确")
    private String clientIp;
    
    @URL(message = "回调地址格式不正确")
    private String returnUrl;
    
    private Map<String, String> extraParams = new HashMap<>();
    
    @NotBlank(message = "操作人不能为空")
    private String operatorId;
    
    @NotBlank(message = "操作人姓名不能为空")
    private String operatorName;
}
```

#### BatchPaymentExecutionCommand
**用途**: 合并支付执行指令
**验证策略**: 批量支付业务规则验证

```java
public class BatchPaymentExecutionCommand {
    @NotEmpty(message = "支付单号列表不能为空")
    @Size(min = 2, max = 50, message = "合并支付数量必须在2-50之间")
    private List<@NotBlank @Size(max = 32) String> paymentIds;
    
    @NotNull(message = "支付渠道不能为空")
    @Enumerated(EnumType.STRING)
    private PaymentChannel paymentChannel;
    
    @NotBlank(message = "支付方式不能为空")
    private String paymentMethod;
    
    @Pattern(regexp = "^((2[0-4]\\d|25[0-5]|[01]?\\d\\d?)\\.){3}(2[0-4]\\d|25[0-5]|[01]?\\d\\d?)$", 
             message = "客户端IP格式不正确")
    private String clientIp;
    
    @URL(message = "回调地址格式不正确")
    private String returnUrl;
    
    @NotBlank(message = "操作人不能为空")
    private String operatorId;
    
    @NotBlank(message = "操作人姓名不能为空")
    private String operatorName;
}
```

#### RefundExecutionCommand
**用途**: 退款执行指令，由订单系统审批后发起
**验证策略**: 退款业务规则和金额验证

```java
public class RefundExecutionCommand {
    @NotBlank(message = "支付单号不能为空")
    @Size(max = 32, message = "支付单号长度必须为32位")
    private String paymentId;
    
    @NotNull(message = "退款金额不能为空")
    @DecimalMin(value = "0.01", message = "退款金额必须大于0")
    @Digits(integer = 15, fraction = 6, message = "金额格式不正确")
    private BigDecimal refundAmount;
    
    @NotBlank(message = "退款原因不能为空")
    @Size(max = 200, message = "退款原因长度不能超过200字符")
    private String refundReason;
    
    @NotBlank(message = "业务退款单号不能为空")
    @Size(max = 32, message = "业务退款单号长度必须为32位")
    private String businessOrderId;
    
    @Size(max = 32, message = "原交易流水号长度必须为32位")
    private String originalTransactionId;
    
    @NotBlank(message = "操作人不能为空")
    private String operatorId;
    
    @NotBlank(message = "操作人姓名不能为空")
    private String operatorName;
}
```

### 查询DTO (Query DTOs)

#### PaymentDetailQuery
**查询条件**: 支付单详情查询
**分页支持**: 不支持（单条记录查询）

```java
public class PaymentDetailQuery {
    @NotBlank(message = "支付单号不能为空")
    @Size(max = 32, message = "支付单号长度必须为32位")
    private String paymentId;
    
    @NotBlank(message = "查询人不能为空")
    private String queryUserId;
    
    // 是否包含交易流水详情
    private boolean includeTransactions = true;
    
    // 是否包含关联业务信息
    private boolean includeRelatedBusiness = false;
}
```

#### PaymentsByResellerQuery
**查询条件**: 按经销商查询支付单列表
**分页支持**: 支持分页和排序

```java
public class PaymentsByResellerQuery {
    @NotBlank(message = "经销商ID不能为空")
    @Size(max = 32, message = "经销商ID长度必须为32位")
    private String resellerId;
    
    // 可选筛选条件
    @Enumerated(EnumType.STRING)
    private PaymentStatus paymentStatus;
    
    @Enumerated(EnumType.STRING)
    private PaymentType paymentType;
    
    private LocalDate createDateFrom;
    private LocalDate createDateTo;
    
    private BigDecimal minAmount;
    private BigDecimal maxAmount;
    
    // 分页参数
    @Min(value = 0, message = "页码必须大于等于0")
    private int page = 0;
    
    @Min(value = 1, message = "页面大小必须大于0")
    @Max(value = 100, message = "页面大小不能超过100")
    private int size = 20;
    
    // 排序参数
    private String sortBy = "createTime";
    private String sortDirection = "DESC";
}
```

#### BatchablePaymentsQuery
**查询条件**: 查询可合并支付的支付单
**分页支持**: 支持，用于前端分页选择

```java
public class BatchablePaymentsQuery {
    @NotBlank(message = "经销商ID不能为空")
    @Size(max = 32, message = "经销商ID长度必须为32位")
    private String resellerId;
    
    @NotNull(message = "支付渠道不能为空")
    @Enumerated(EnumType.STRING)
    private PaymentChannel preferredChannel;
    
    // 金额范围限制
    private BigDecimal minAmount;
    private BigDecimal maxAmount;
    
    // 支付类型筛选
    private Set<PaymentType> paymentTypes;
    
    // 分页参数
    @Min(value = 0, message = "页码必须大于等于0")
    private int page = 0;
    
    @Min(value = 1, message = "页面大小必须大于0")
    @Max(value = 50, message = "页面大小不能超过50")
    private int size = 20;
}
```

### 响应DTO (Response DTOs)

#### PaymentTransactionDto
**用途**: 支付和退款交易流水信息的传输
**数据来源**: PaymentTransaction实体

```java
public class PaymentTransactionDto {
    private String transactionId;
    private String paymentId;
    private TransactionType transactionType;     // PAYMENT, REFUND
    private TransactionStatus transactionStatus; // SUCCESS, FAILED, PENDING
    private BigDecimal amount;
    private String currency = "CNY";
    
    // 渠道信息
    private String channelCode;
    private String channelTransactionId;
    private String channelResponse;
    
    // 业务信息
    private String businessOrderId;  // 关联的业务单号（退款时使用）
    private String originalTransactionId;  // 原支付交易ID（退款时使用）
    private Map<String, String> businessMetadata;
    
    // 时间信息
    private LocalDateTime createTime;
    private LocalDateTime completeTime;
    private LocalDateTime notifyTime;  // 通知/回调时间
    
    // 操作信息
    private String operatorId;
    private String operatorName;
    private String remark;
}
```

**字段说明**:
| 字段名 | 类型 | 描述 | 数据来源 |
|--------|------|------|----------|
| transactionId | String | 交易流水号 | Transaction.id |
| transactionType | TransactionType | 交易类型 | Transaction.type |
| transactionStatus | TransactionStatus | 交易状态 | Transaction.status |
| amount | BigDecimal | 交易金额 | Transaction.amount.amount |
| channelTransactionId | String | 渠道交易号 | Transaction.channelInfo.transactionId |
| businessOrderId | String | 业务单号 | Transaction.businessInfo.orderId |

#### PaymentResponse
**用途**: 支付单创建和基础操作的响应
**数据来源**: Payment聚合的基础信息

```java
public class PaymentResponse {
    private String paymentId;
    private String orderId;
    private String resellerId;
    private BigDecimal paymentAmount;
    private String currency = "CNY";
    private PaymentStatus paymentStatus;
    private PaymentType paymentType;
    private String businessDesc;
    private LocalDateTime paymentDeadline;
    private PriorityLevel priorityLevel;
    private LocalDateTime createTime;
    private String createByName;
    
    // 关联业务信息
    private String relatedBusinessId;
    private RelatedBusinessType relatedBusinessType;
    private LocalDateTime businessExpireDate;
}
```

**字段说明**:
| 字段名 | 类型 | 描述 | 数据来源 |
|--------|------|------|----------|
| paymentId | String | 支付单号 | Payment.id |
| orderId | String | 关联订单号 | Payment.orderId |
| paymentStatus | PaymentStatus | 当前支付状态 | Payment.paymentStatus |
| paymentAmount | BigDecimal | 支付金额 | Payment.paymentAmount.amount |

#### PaymentDetailResponse
**用途**: 支付单详情查询响应，包含完整信息
**数据来源**: Payment聚合及其内部实体

```java
public class PaymentDetailResponse {
    // 基础信息
    @NotBlank(message = "支付单号不能为空")
    @Size(max = 32, message = "支付单号长度必须为32位")
    private String paymentId;
    
    @NotBlank(message = "订单号不能为空")
    @Size(max = 32, message = "订单号长度必须为32位")
    private String orderId;
    
    @NotBlank(message = "经销商ID不能为空")
    @Size(max = 32, message = "经销商ID长度必须为32位")
    private String resellerId;
    
    // 金额信息
    @NotNull(message = "支付金额不能为空")
    @DecimalMin(value = "0.01", message = "支付金额必须大于0")
    @Digits(integer = 15, fraction = 6, message = "金额格式不正确")
    private BigDecimal paymentAmount;
    
    @NotNull(message = "已支付金额不能为空")
    @DecimalMin(value = "0.00", message = "已支付金额必须大于等于0")
    @Digits(integer = 15, fraction = 6, message = "金额格式不正确")
    private BigDecimal paidAmount;
    
    @NotNull(message = "已退款金额不能为空")
    @DecimalMin(value = "0.00", message = "已退款金额必须大于等于0")
    @Digits(integer = 15, fraction = 6, message = "金额格式不正确")
    private BigDecimal refundedAmount;
    
    @NotNull(message = "实际金额不能为空")
    @DecimalMin(value = "0.00", message = "实际金额必须大于等于0")
    @Digits(integer = 15, fraction = 6, message = "金额格式不正确")
    private BigDecimal actualAmount;
    
    @NotNull(message = "待处理金额不能为空")
    @DecimalMin(value = "0.00", message = "待处理金额必须大于等于0")
    @Digits(integer = 15, fraction = 6, message = "金额格式不正确")
    private BigDecimal pendingAmount;
    
    @NotBlank(message = "货币类型不能为空")
    @Pattern(regexp = "^[A-Z]{3}$", message = "货币类型必须是3位大写字母")
    private String currency;
    
    // 状态信息
    private PaymentStatus paymentStatus;
    private RefundStatus refundStatus;
    private PaymentType paymentType;
    
    // 业务信息
    private String businessDesc;
    private LocalDateTime paymentDeadline;
    private PriorityLevel priorityLevel;
    
    // 关联业务信息
    private String relatedBusinessId;
    private RelatedBusinessType relatedBusinessType;
    private LocalDateTime businessExpireDate;
    private Map<String, String> businessTags;
    
    // 交易流水
    private List<PaymentTransactionDto> paymentTransactions;
    private List<PaymentTransactionDto> refundTransactions;
    
    // 系统信息
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
    private String createByName;
    private String updateByName;
}
```

#### PaymentExecutionResponse
**用途**: 支付执行操作的响应
**数据来源**: PaymentTransaction和渠道响应

```java
public class PaymentExecutionResponse {
    @NotBlank(message = "支付单号不能为空")
    @Size(max = 32, message = "支付单号长度必须为32位")
    private String paymentId;
    
    @NotBlank(message = "交易流水号不能为空")
    @Size(max = 32, message = "交易流水号长度必须为32位")
    private String transactionId;
    
    @NotNull(message = "支付状态不能为空")
    @Enumerated(EnumType.STRING)
    private PaymentStatus paymentStatus;
    
    @NotNull(message = "交易状态不能为空")
    @Enumerated(EnumType.STRING)
    private TransactionStatus transactionStatus;
    
    // 渠道响应信息
    @Size(max = 2000, message = "渠道响应长度不能超过2000字符")
    private String channelResponse;
    
    @Size(max = 64, message = "渠道交易号长度不能超过64字符")
    private String channelTransactionNumber;
    
    @URL(message = "跳转地址格式不正确")
    private String redirectUrl;
    
    @Size(max = 512, message = "二维码内容长度不能超过512字符")
    private String qrCodeContent;
    
    // 预估信息
    private Integer estimatedConfirmTimeSeconds;
    private LocalDateTime estimatedCompleteTime;
    
    // 执行结果
    private boolean executeSuccess;
    private String executeMessage;
    private LocalDateTime executeTime;
}
```

#### BatchPaymentResponse
**用途**: 合并支付执行的响应
**数据来源**: 多个PaymentTransaction和批量执行结果

```java
public class BatchPaymentResponse {
    private String batchId;
    private List<String> paymentIds;
    private BigDecimal totalAmount;
    private PaymentChannel paymentChannel;
    
    // 批量执行结果
    private boolean overallSuccess;
    private List<SinglePaymentExecutionResult> individualResults;
    
    // 渠道信息
    private String channelTransactionNumber;
    private String redirectUrl;
    private Integer estimatedConfirmTimeSeconds;
    
    // 执行统计
    private int successCount;
    private int failureCount;
    private LocalDateTime batchExecuteTime;
    
    public static class SinglePaymentExecutionResult {
        private String paymentId;
        private String transactionId;
        private boolean success;
        private String message;
        private BigDecimal allocatedAmount;
    }
}
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

#### PaymentListResponse
**用途**: 支付单列表查询响应，包含分页信息
**数据来源**: Payment聚合的列表数据

```java
public class PaymentListResponse {
    // 分页信息
    @NotNull(message = "分页信息不能为空")
    private PageInfo pageInfo;
    
    // 支付单列表
    @NotNull(message = "支付单列表不能为空")
    private List<PaymentSummaryResponse> payments;
    
    // 统计信息
    @NotNull(message = "总金额不能为空")
    @DecimalMin(value = "0.00", message = "总金额必须大于等于0")
    private BigDecimal totalAmount;
    
    private int totalCount;
    
    // 内部分页类
    public static class PageInfo {
        @Min(value = 0, message = "当前页码不能小于0")
        private int currentPage;
        
        @Min(value = 1, message = "每页大小必须大于0")
        private int pageSize;
        
        private int totalPages;
        
        @Min(value = 0, message = "总记录数不能小于0")
        private long totalRecords;
        
        private boolean hasNext;
        private boolean hasPrevious;
    }
}
```

#### PaymentSummaryResponse
**用途**: 支付单列表项的简要信息
**数据来源**: Payment聚合的基础信息

```java
public class PaymentSummaryResponse {
    @NotBlank(message = "支付单号不能为空")
    @Size(max = 32, message = "支付单号长度必须为32位")
    private String paymentId;
    
    @NotBlank(message = "订单号不能为空")
    @Size(max = 32, message = "订单号长度必须为32位")
    private String orderId;
    
    @NotBlank(message = "经销商ID不能为空")
    @Size(max = 32, message = "经销商ID长度必须为32位")
    private String resellerId;
    
    @NotNull(message = "支付金额不能为空")
    @DecimalMin(value = "0.01", message = "支付金额必须大于0")
    private BigDecimal paymentAmount;
    
    @NotNull(message = "已支付金额不能为空")
    @DecimalMin(value = "0.00", message = "已支付金额必须大于等于0")
    private BigDecimal paidAmount;
    
    @NotBlank(message = "货币类型不能为空")
    @Pattern(regexp = "^[A-Z]{3}$", message = "货币类型必须是3位大写字母")
    private String currency = "CNY";
    
    @NotNull(message = "支付状态不能为空")
    @Enumerated(EnumType.STRING)
    private PaymentStatus paymentStatus;
    
    @NotNull(message = "支付类型不能为空")
    @Enumerated(EnumType.STRING)
    private PaymentType paymentType;
    
    @NotNull(message = "创建时间不能为空")
    private LocalDateTime createTime;
    
    @NotBlank(message = "创建人不能为空")
    private String createByName;
    
    private String businessDesc;
    private LocalDateTime paymentDeadline;
    private String relatedBusinessId;
    private RelatedBusinessType relatedBusinessType;
    
    // 是否可合并支付
    private boolean batchable;
    
    // 是否有退款
    private boolean hasRefund;
    
    // 最近交易时间
    private LocalDateTime lastTransactionTime;
}
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

---

## 设计总结

### 核心设计特点

1. **统一支付处理架构**: 单支付单和合并支付使用同一套处理逻辑，简化系统复杂度
2. **完整数据模型支持**: 支持关联业务信息、优先级、业务标签等新增字段
3. **灵活支付流水选择**: 退款时支持指定流水、最新流水、多流水分摊等策略
4. **多维度支付筛选**: 统一的筛选查询接口，支持状态、类型、金额、时间等筛选
5. **信用还款统一处理**: 信用还款作为特殊类型的支付单，复用标准处理流程

### 架构优势

1. **代码复用**: 统一的支付执行接口避免了单支付单和合并支付的重复逻辑
2. **数据一致性**: 清晰的事务边界确保支付状态的强一致性
3. **可扩展性**: 灵活的DTO设计支持未来业务需求扩展
4. **性能优化**: 批量操作和分页查询提供良好的性能表现

### 符合需求文档的关键点

✅ **统一支付处理**: 完全按需求文档要求实现统一处理架构  
✅ **支付单筛选**: 提供完整的多维度筛选功能  
✅ **信用还款统一化**: 修正为支付类型的一种，使用统一创建接口  
✅ **退款流水选择**: 支持三种选择策略，满足不同业务场景  
✅ **合并支付展示**: 提供专门的合并支付信息查询接口  
✅ **完整数据模型**: 支持所有需求文档中定义的字段和业务场景  

## 设计修正说明

**信用还款设计调整**:
根据需求文档和DDD原则，信用还款本质上就是一种支付单，不应该设计为独立的服务。本次修正内容：

1. **移除独立接口**: 删除了createCreditRepayment独立接口和CreateCreditRepaymentCommand
2. **统一创建流程**: 信用还款通过createPayment接口创建，支付类型为CREDIT_REPAYMENT
3. **条件验证逻辑**: CreatePaymentCommand支持条件验证，根据支付类型要求不同的必填字段
4. **虚拟订单号机制**: 信用还款使用"CREDIT_" + relatedBusinessId作为虚拟订单号
5. **统一处理流程**: 信用还款复用完整的支付处理管道，无需特殊处理

这样的设计更符合DDD原则，减少了系统复杂性，提高了代码复用性，同时保持了业务逻辑的一致性。  

---

**文档状态**: ✅ 已完成重新设计  
**版本**: v6.0  
**最后更新**: 2025年9月28日  
**术语基准**: 全局词汇表 v4.0, 支付上下文设计 v6.0  
**需求符合度**: 100% 符合支付模块需求设计文档 v1.4  
**架构特点**: 基于统一支付处理架构的应用层设计  
**审核状态**: 待技术评审
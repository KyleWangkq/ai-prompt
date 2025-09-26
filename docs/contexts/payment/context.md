# Payment Context (支付上下文)

## 限界上下文设计文档

### 上下文名称(Context Name)
Payment Context (支付上下文)

### 核心职责(Core Responsibility)
负责企业间特种设备定制交易系统中的支付处理功能，包括支付管理、支付执行、退款处理、支付渠道集成和支付状态管理。

### 领域专家(Domain Expert)
- 支付业务产品经理
- 财务结算专家
- 支付渠道技术专家
- B2B交易业务专家

### 聚合列表(Aggregates)
1. **Payment** (支付聚合) - 核心聚合，管理支付的完整生命周期
2. **RefundRecord** (退款记录聚合) - 管理退款流水和退款状态
3. **ReconciliationRecord** (对账记录聚合) - 管理对账流程和结果

### 边界定义(Boundary Definition)

#### 包含的功能
- 支付创建、状态管理和生命周期跟踪
- 支付渠道集成与管理
- 支付执行与结果处理
- 退款执行和状态管理
- 支付流水记录管理
- 对账数据处理
- 支付监控与异常处理

#### 不包含的功能
- 订单管理 (属于Order Context)
- 退款审批 (属于Order Context)
- 用户认证与授权 (属于User Context)
- 财务核算与会计处理 (属于Finance Context)
- 商品信息管理 (属于Product Context)

### 语言定义(Ubiquitous Language)

#### 核心概念
- **Payment** (支付): 系统内部生成的支付请求记录
- **Payment Channel** (支付渠道): 第三方支付服务商或企业内部支付系统
- **Transaction** (交易): 支付渠道执行的具体支付操作
- **Refund** (退款): 将已支付金额返还给付款方的操作
- **Reconciliation** (对账): 与支付渠道核对交易数据的过程
- **Settlement** (结算): 支付渠道向商户转移资金的过程

#### 支付状态术语
- **Unpaid** (未支付): 支付刚创建，尚未开始支付
- **Paying** (支付中): 支付请求已发起，等待渠道确认
- **Partial Paid** (部分支付): 支付已支付部分金额
- **Paid** (已支付): 支付全额支付完成
- **Partial Refunded** (部分退款): 支付已发生部分退款
- **Full Refunded** (全额退款): 支付已全额退款

#### 业务流程术语
- **Batch Payment** (合并支付): 多个支付使用同一渠道合并处理
- **Partial Payment** (分批支付): 支付分多次完成支付
- **Payment Callback** (支付回调): 支付渠道异步通知支付结果
- **Payment Retry** (支付重试): 支付失败后的重新尝试机制

### 上下文边界与外部集成

#### 与Order Context的关系
- **关系模式**: Customer-Supplier (客户方-供应方)
- **集成方式**: REST API + Domain Events
- **数据交换**: 接收支付创建请求，发送支付状态通知
- **依赖方向**: Payment Context 为 Order Context 的供应方

#### 与Finance Context的关系
- **关系模式**: Open Host Service (开放主机服务)
- **集成方式**: Message Queue + REST API
- **数据交换**: 提供支付流水数据和对账信息
- **依赖方向**: Finance Context 通过标准接口访问支付数据

#### 与User Context的关系
- **关系模式**: Conformist (遵循者)
- **集成方式**: REST API
- **数据交换**: 获取企业用户信息和权限验证
- **依赖方向**: Payment Context 依赖 User Context

#### 与External Payment Providers的关系
- **关系模式**: Anti-Corruption Layer (防腐层)
- **集成方式**: REST API + Webhook
- **数据交换**: 支付请求和支付结果通知
- **防腐策略**: 通过适配器模式统一不同支付渠道接口

### 技术架构约束
- 支付数据必须保证强一致性
- 关键支付操作需要事务保证
- 支付敏感信息需要加密存储
- 支付接口需要幂等性保证
- 支付回调需要签名验证
- 支付流水需要完整审计日志

### 业务规则约束
- 支付金额必须大于0
- 已支付金额不能超过支付总金额
- 退款金额不能超过已支付金额
- 支付状态变更必须符合状态机规则
- 大额支付需要特殊处理流程
- B2B支付需要支持长时间异步确认
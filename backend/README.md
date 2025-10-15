# 支付模块后端代码 (Payment Module Backend)

## 项目概述 (Project Overview)

基于 `docs/payment.yml` 规范生成的完整 Spring Boot DDD 架构代码，实现莱宝经销商订单交易系统的支付模块。

Generated complete Spring Boot DDD architecture code based on `docs/payment.yml` specification for the Payment Module of Leybold Dealer Order Trading System.

## 技术栈 (Technology Stack)

- **Spring Boot**: 2.7.18
- **MyBatis-Plus**: 3.5.5
- **MySQL**: 8.x
- **Java**: 1.8
- **Lombok**: 1.18.30
- **Maven**: 3.6+

## 项目结构 (Project Structure)

```
com.bytz.cms.payment/
├── interfaces/               # 接口层 (Interface Layer)
│   ├── PaymentController    # 支付单控制器
│   ├── PaymentExecutionController  # 支付执行控制器
│   ├── RefundController     # 退款控制器
│   ├── CreditRepaymentController   # 信用还款控制器
│   └── model/               # DTOs
│       ├── ro/              # Request Objects
│       │   ├── PaymentCreateRO
│       │   ├── PaymentExecutionRO
│       │   └── RefundExecutionRO
│       └── vo/              # Response Objects
│           ├── PaymentVO
│           ├── PaymentExecutionVO
│           └── RefundExecutionVO
├── application/             # 应用服务层 (Application Service Layer)
│   ├── PaymentApplicationService
│   ├── PaymentExecutionApplicationService
│   ├── RefundApplicationService
│   └── CreditRepaymentApplicationService
├── domain/                  # 领域层 (Domain Layer)
│   ├── model/               # 聚合根 (Aggregate Roots)
│   │   ├── PaymentAggregate
│   │   └── PaymentTransactionAggregate
│   ├── entity/              # 实体 (Entities)
│   │   ├── PaymentEntity
│   │   └── PaymentTransactionEntity
│   ├── repository/          # 仓储接口 (Repository Interfaces)
│   │   ├── IPaymentRepository
│   │   └── IPaymentTransactionRepository
│   └── [Domain Services]    # 领域服务 (7 services)
│       ├── PaymentValidationService
│       ├── PaymentExecutionService
│       ├── PaymentCallbackService
│       ├── RefundService
│       ├── CreditRepaymentService
│       ├── PaymentQueryService
│       └── PaymentReconciliationService
├── infrastructure/          # 基础设施层 (Infrastructure Layer)
│   ├── mapper/              # MyBatis-Plus Mappers
│   │   ├── PaymentMapper
│   │   └── PaymentTransactionMapper
│   ├── repository/          # 仓储实现 (Repository Implementations)
│   │   ├── PaymentRepositoryImpl
│   │   └── PaymentTransactionRepositoryImpl
│   └── config/              # 配置类 (Configuration)
│       └── MybatisPlusConfig
└── shared/                  # 共享层 (Shared Layer)
    ├── enums/               # 枚举 (Enumerations)
    │   ├── PaymentType
    │   ├── PaymentStatus
    │   ├── RefundStatus
    │   ├── TransactionType
    │   ├── TransactionStatus
    │   ├── PaymentChannel
    │   └── RelatedBusinessType
    ├── exception/           # 异常 (Exceptions)
    │   ├── BusinessException
    │   └── PaymentException
    └── model/               # 领域事件 (Domain Events)
        ├── DomainEvent
        ├── PaymentCreatedEvent
        ├── PaymentExecutedEvent
        └── RefundExecutedEvent
```

## 核心功能 (Core Features)

### 用例覆盖 (Use Case Coverage)

- **UC-PM-001**: 接收支付单创建请求 (Create Payment)
- **UC-PM-002**: 筛选支付单 (Filter Payments)
- **UC-PM-003**: 执行支付操作 (Execute Payment)
- **UC-PM-004**: 处理支付回调 (Handle Payment Callback)
- **UC-PM-005**: 查询支付单信息 (Query Payment Detail)
- **UC-PM-006**: 接收退款执行指令 (Execute Refund)
- **UC-PM-007**: 创建信用还款支付单 (Create Credit Repayment Payment)
- **UC-PM-008**: 执行信用还款支付 (Execute Credit Repayment)
- **UC-PM-009**: 补偿查询支付状态 (Compensate Payment Status)

### 核心业务能力 (Core Business Capabilities)

1. **统一支付处理**: 单支付单和多支付单合并支付使用统一流程
2. **完整的状态管理**: 支付状态和退款状态的完整生命周期管理
3. **金额处理**: 使用 BigDecimal (6位小数精度) 处理所有金额
4. **多场景业务支持**: 预付款、尾款、其他费用、信用还款
5. **多渠道支持**: 线上支付、钱包支付、电汇支付、信用账户

## 编译和运行 (Build and Run)

### 编译项目 (Compile)

```bash
cd backend
mvn clean compile
```

### 打包项目 (Package)

```bash
mvn clean package
```

### 运行项目 (Run)

```bash
# 方式1: 使用 Maven
mvn spring-boot:run

# 方式2: 运行 JAR
java -jar target/payment-service-1.0.0-SNAPSHOT.jar
```

## 配置说明 (Configuration)

主要配置文件: `src/main/resources/application.yml`

### 数据库配置 (Database Configuration)

```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/cms_payment?useUnicode=true&characterEncoding=utf8&useSSL=false&serverTimezone=Asia/Shanghai
    username: root
    password: password
```

### 服务端口 (Server Port)

默认端口: `8080`
Context Path: `/payment-service`

## API 端点 (API Endpoints)

### 支付单管理 (Payment Management)

- `POST /api/payments` - 创建支付单
- `GET /api/payments` - 筛选支付单
- `GET /api/payments/{id}` - 查询支付单详情

### 支付执行 (Payment Execution)

- `POST /api/payment-execution/execute` - 执行支付
- `POST /api/payment-execution/callback` - 支付回调
- `POST /api/payment-execution/compensate` - 补偿查询

### 退款管理 (Refund Management)

- `POST /api/refunds/execute` - 执行退款
- `POST /api/refunds/callback` - 退款回调

### 信用还款 (Credit Repayment)

- `POST /api/credit-repayment/create` - 创建信用还款支付单

## 数据库表结构 (Database Schema)

### 支付单表 (cms_payment)

主要字段:
- `id`: 支付单号 (主键)
- `order_id`: 关联订单号
- `reseller_id`: 经销商ID
- `payment_amount`: 支付金额
- `paid_amount`: 已支付金额
- `refunded_amount`: 已退款金额
- `actual_amount`: 实际收款金额
- `payment_status`: 支付状态
- `refund_status`: 退款状态
- `payment_type`: 支付类型

### 支付流水表 (cms_payment_transaction)

主要字段:
- `id`: 流水号 (主键)
- `payment_id`: 关联支付单号
- `transaction_type`: 流水类型 (支付/退款)
- `transaction_status`: 流水状态
- `transaction_amount`: 交易金额
- `payment_channel`: 支付渠道
- `channel_transaction_number`: 渠道交易号

## 开发规范 (Development Standards)

### DDD 架构原则 (DDD Architecture Principles)

1. **聚合根**: 业务逻辑必须在聚合根中实现
2. **仓储模式**: 接口定义在 domain 层，实现在 infrastructure 层
3. **应用服务**: 只负责用例编排，不包含业务逻辑
4. **领域事件**: 用于系统解耦和状态同步

### 命名约定 (Naming Conventions)

- Repository 接口: `I` 前缀 (如 `IPaymentRepository`)
- Repository 实现: `Impl` 后缀 (如 `PaymentRepositoryImpl`)
- 聚合根: `Aggregate` 后缀 (如 `PaymentAggregate`)
- 实体: `Entity` 后缀 (如 `PaymentEntity`)
- Request DTO: `RO` 后缀 (如 `PaymentCreateRO`)
- Response DTO: `VO` 后缀 (如 `PaymentVO`)

## 待实现功能 (TODO Items)

所有标记为 `// TODO: Implement business logic` 的方法需要根据具体业务需求实现。

主要待实现模块:
1. 聚合根业务方法实现
2. 领域服务业务逻辑实现
3. 应用服务用例编排实现
4. 仓储实现的实体与聚合转换逻辑
5. 控制器的 DTO 转换逻辑

## 参考文档 (Reference Documents)

- 📄 [支付模块需求设计.md](../支付模块需求设计.md) - 业务需求和功能设计
- 📄 [支付模块领域模型设计总结.md](../支付模块领域模型设计总结.md) - DDD 领域模型设计
- 📄 [docs/payment.yml](../docs/payment.yml) - YAML 技术规范
- 📄 [docs/Glossary.md](../docs/Glossary.md) - 全局术语表
- 📄 [docs/支付模块用例模型.md](../docs/支付模块用例模型.md) - 用例模型

## 许可证 (License)

内部项目，请勿外传。

---

**生成时间**: 2025-10-15  
**版本**: v1.0  
**基于**: payment.yml v2.0

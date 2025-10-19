# 支付模块 Payment Module

## 项目简介

本项目是企业工业设备交易系统的支付模块，采用领域驱动设计（DDD）架构，处理B2B工业设备交易中的复杂支付场景。

## 技术栈

- **Java**: 1.8
- **Spring Boot**: 2.7.18
- **MyBatis-Plus**: 3.5.5
- **Lombok**: 1.18.30
- **MapStruct**: 1.5.5
- **MySQL**: 8.x

## 项目结构

```
com.bytz.cms.payment/
├── interfaces/              # 接口层 - REST API接口
│   ├── controller/          # 控制器类
│   └── model/               # DTO对象（*RO请求对象, *VO响应对象）
├── application/             # 应用层 - 用例协调
│   └── assembler/           # MapStruct转换器
├── domain/                  # 领域层 - 核心业务逻辑
│   ├── model/               # 聚合根（*Aggregate）
│   ├── entity/              # 领域实体（*Entity）
│   ├── valueobject/         # 值对象（*ValueObject）
│   ├── enums/               # 枚举类
│   ├── command/             # 命令对象（*Command）
│   ├── repository/          # 仓储接口（I*Repository）
│   └── *DomainService       # 领域服务
├── infrastructure/          # 基础设施层 - 技术实现
│   ├── entity/              # 数据库实体
│   ├── mapper/              # MyBatis-Plus Mapper
│   ├── repository/          # 仓储实现（*RepositoryImpl）
│   └── channel/             # 支付渠道实现
└── shared/                  # 共享层 - 公共组件
    ├── exception/           # 业务异常
    └── model/               # 领域事件（*Event）
```

## 核心功能

### 1. 支付单管理
- 创建支付单（支持预付款、尾款、其他费用、信用还款）
- 查询支付单（按支付单号、订单号、经销商ID、关联业务ID）
- 支付单状态管理

### 2. 支付执行
- 单支付单支付
- 合并支付（多支付单批量处理）
- 部分支付
- 支付回调处理

### 3. 退款管理
- 接收退款指令
- 选择支付流水
- 执行退款操作
- 退款回调处理

### 4. 支付渠道
- 线上支付（ONLINE_PAYMENT）
- 钱包支付（WALLET_PAYMENT）
- 电汇支付（WIRE_TRANSFER）
- 信用账户（CREDIT_ACCOUNT）

## 数据库初始化

执行 `src/main/resources/db/schema.sql` 创建数据库表：

```sql
-- 创建数据库
CREATE DATABASE IF NOT EXISTS cms_payment DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

-- 执行schema.sql中的建表语句
```

## 配置说明

修改 `src/main/resources/application.yml` 中的数据库连接配置：

```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/cms_payment?useUnicode=true&characterEncoding=utf8&useSSL=false&serverTimezone=Asia/Shanghai
    username: root
    password: your_password
```

## 编译和运行

### 编译项目

```bash
mvn clean install
```

### 运行项目

```bash
mvn spring-boot:run
```

或者：

```bash
java -jar target/payment-module-1.0.0-SNAPSHOT.jar
```

## API接口

### 创建支付单

**请求**:
```
POST /payment/api/v1/payments
Content-Type: application/json

{
  "orderId": "ORDER20231201001",
  "resellerId": "RESELLER001",
  "paymentAmount": 10000.00,
  "paymentType": "ADVANCE_PAYMENT",
  "businessDesc": "预付款",
  "paymentDeadline": "2023-12-31T23:59:59"
}
```

**响应**:
```json
{
  "id": "PAY20231201ABCD1234",
  "orderId": "ORDER20231201001",
  "resellerId": "RESELLER001",
  "paymentAmount": 10000.00,
  "paidAmount": 0.00,
  "refundedAmount": 0.00,
  "actualAmount": 0.00,
  "pendingAmount": 10000.00,
  "currency": "CNY",
  "paymentType": "ADVANCE_PAYMENT",
  "paymentStatus": "UNPAID",
  "refundStatus": "NO_REFUND",
  "createTime": "2023-12-01T10:00:00"
}
```

### 查询支付单

**请求**:
```
GET /payment/api/v1/payments/{id}
```

### 按订单号查询支付单

**请求**:
```
GET /payment/api/v1/payments/by-order/{orderId}
```

### 按经销商ID查询支付单

**请求**:
```
GET /payment/api/v1/payments/by-reseller/{resellerId}
```

### 按关联业务ID查询支付单（信用还款）

**请求**:
```
GET /payment/api/v1/payments/by-business/{businessId}
```

## TODO清单

### 领域层待实现功能
1. PaymentAggregate中的业务方法完整实现
   - 创建支付单时的完整参数验证
   - 信用还款订单号与信用记录绑定验证
   - 支付回调处理的完整逻辑
   - 退款回调处理的完整逻辑

2. PaymentDomainService中的领域服务
   - 批量支付的完整验证和处理
   - 支付流水选择策略实现
   - 渠道可用性验证

### 基础设施层待实现功能
1. 支付渠道实现
   - OnlinePaymentChannelService - 线上支付接口对接
   - WalletPaymentChannelService - 钱包支付接口对接
   - WireTransferChannelService - 电汇支付接口对接
   - CreditAccountChannelService - 信用账户接口对接

2. 仓储实现
   - 优化支付单号和流水号生成策略

### 应用层待实现功能
1. PaymentExecutionApplicationService - 支付执行应用服务
2. RefundApplicationService - 退款应用服务
3. 事件发布机制（Spring Events或消息队列）

### 接口层待实现功能
1. 支付执行相关接口
2. 退款相关接口
3. 全局异常处理器

## DDD设计原则

### 1. 实体对象规则
- **infrastructure/entity** 中的数据库实体仅用于数据持久化，不包含任何业务逻辑
- **domain/entity** 中的领域实体包含业务行为方法
- **domain/model** 中的聚合根是业务逻辑的核心载体

### 2. 转换规则
- 使用 MapStruct 进行各层对象之间的转换
- RO → Command → Aggregate
- Aggregate → VO

### 3. 业务逻辑位置
- 所有业务逻辑必须在领域层（domain包）实现
- 应用层（application包）仅负责用例协调，不包含业务逻辑
- 接口层（interfaces包）仅负责数据转换和HTTP交互

## 开发注意事项

1. 严格遵循DDD分层架构，不跨层调用
2. 使用命令模式封装参数超过3个的方法
3. 值对象要确保不可变性
4. 聚合根要维护聚合内的一致性
5. 仓储只能通过聚合根进行操作
6. 使用领域事件进行跨聚合通信

## 贡献指南

1. 所有业务逻辑必须有完整的注释说明
2. 未实现的方法必须添加 TODO 标记
3. 提交代码前必须通过编译
4. 遵循现有的代码风格和命名规范

## 许可证

本项目为内部项目，版权所有。

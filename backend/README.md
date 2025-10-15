# Payment Service - 支付模块

## 项目概述

基于 DDD（领域驱动设计）架构的企业工业设备交易系统支付模块，提供完整的支付单管理、支付执行、退款处理等功能。

## 技术栈

- **Java**: 8+
- **Spring Boot**: 2.7.18
- **MyBatis-Plus**: 3.5.5
- **MySQL**: 8.0+
- **Lombok**: 1.18.30
- **Maven**: 3.6+

## 项目结构

```
backend/
├── src/main/java/com/bytz/cms/payment/
│   ├── domain/                    # 领域层
│   │   ├── model/                 # 聚合根、实体、值对象
│   │   ├── repository/            # 仓储接口
│   │   └── service/               # 领域服务
│   ├── application/               # 应用层
│   │   ├── service/               # 应用服务（用例编排）
│   │   └── dto/                   # 数据传输对象
│   ├── infrastructure/            # 基础设施层
│   │   ├── persistence/           # 数据库实体
│   │   ├── mapper/                # MyBatis映射器
│   │   └── repository/            # 仓储实现
│   ├── interfaces/                # 接口层
│   │   └── rest/                  # REST控制器
│   ├── shared/                    # 共享层
│   │   └── exception/             # 异常定义
│   └── PaymentServiceApplication.java
└── src/main/resources/
    ├── application.yml            # 应用配置
    └── schema.sql                 # 数据库脚本
```

## DDD 架构设计

### 领域层 (Domain Layer)

#### 聚合根
- **Payment**: 支付单聚合根，管理支付单的完整生命周期
- **PaymentTransaction**: 支付流水实体，记录每次支付/退款交易

#### 值对象
- **RelatedBusinessInfo**: 关联业务信息值对象
- **PaymentType**: 支付类型枚举（预付款、尾款、其他费用、信用还款）
- **PaymentStatus**: 支付状态枚举
- **RefundStatus**: 退款状态枚举
- **PaymentChannel**: 支付渠道枚举
- **TransactionType**: 流水类型枚举
- **TransactionStatus**: 流水状态枚举

#### 领域服务
- **PaymentValidationService**: 支付校验服务

### 应用层 (Application Layer)

- **PaymentApplicationService**: 支付应用服务，编排业务用例

### 基础设施层 (Infrastructure Layer)

- **PaymentRepositoryImpl**: 支付单仓储实现（基于 MyBatis-Plus）
- **PaymentTransactionRepositoryImpl**: 支付流水仓储实现

### 接口层 (Interfaces Layer)

- **PaymentController**: 支付单 REST API 控制器

## 核心功能

### UC-PM-001: 创建支付单
接收订单系统或信用管理系统创建的支付单请求，生成唯一支付单号。

### UC-PM-002: 筛选支付单
支持按状态、类型、时间范围等条件筛选支付单。

### UC-PM-003: 执行支付
统一处理单支付单和批量支付，支持合并支付和分批支付。

### UC-PM-004: 处理支付回调
接收支付渠道的回调通知，更新支付单和流水状态。

### UC-PM-005: 查询支付单详情
查询支付单的详细信息及支付流水历史。

## 快速开始

### 1. 数据库准备

```bash
# 创建数据库
CREATE DATABASE cms_payment CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

# 执行建表脚本
mysql -u root -p cms_payment < src/main/resources/schema.sql
```

### 2. 配置数据库连接

编辑 `src/main/resources/application.yml`:

```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/cms_payment?useUnicode=true&characterEncoding=utf8&useSSL=false&serverTimezone=Asia/Shanghai
    username: your_username
    password: your_password
```

### 3. 构建项目

```bash
mvn clean install
```

### 4. 运行服务

```bash
mvn spring-boot:run
```

服务将在 `http://localhost:8080` 启动。

## API 文档

### 创建支付单

```http
POST /api/v1/payments
Content-Type: application/json

{
  "resellerId": "R001",
  "paymentAmount": 10000.00,
  "paymentType": "ADVANCE_PAYMENT",
  "orderId": "ORD001",
  "paymentDeadline": "2025-12-31T23:59:59",
  "priorityLevel": 1,
  "businessDesc": "设备预付款"
}
```

### 执行支付

```http
POST /api/v1/payments/execute
Content-Type: application/json

{
  "paymentItems": [
    {
      "paymentId": "PAY123456",
      "amount": 5000.00
    }
  ],
  "paymentChannel": "ONLINE_PAYMENT",
  "paymentWay": "ALIPAY"
}
```

### 查询支付单

```http
GET /api/v1/payments/{paymentId}
```

### 筛选支付单

```http
GET /api/v1/payments/filter?resellerId=R001&statuses=UNPAID,PARTIAL_PAID
```

## 业务规则

1. **支付单号唯一性**: 系统自动生成唯一支付单号，防止重复
2. **金额一致性**: 实际收款金额 = 已支付金额 - 已退款金额
3. **状态转换**: 严格的状态机规则，确保业务一致性
4. **批量支付**: 多个支付单必须属于同一经销商才能合并支付
5. **退款限制**: 退款金额不能超过可退款金额（已支付金额 - 已退款金额）

## 开发规范

- **代码风格**: 遵循 Google Java Style Guide
- **命名约定**: 
  - 聚合根和实体使用名词（Payment, PaymentTransaction）
  - 领域服务使用动词+名词（PaymentValidationService）
  - 应用服务以ApplicationService结尾
- **注释规范**: 使用中英文双语注释，明确标注用例编号

## 测试

```bash
# 运行单元测试
mvn test

# 运行集成测试
mvn verify
```

## 部署

### Docker 部署

```bash
# 构建镜像
docker build -t payment-service:1.0.0 .

# 运行容器
docker run -d -p 8080:8080 --name payment-service payment-service:1.0.0
```

## 许可证

Copyright © 2025 BYTZ CMS Team

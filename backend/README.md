# 支付服务模块 (Payment Service Module)

## 项目概述

本项目是基于DDD（领域驱动设计）的支付服务模块，专为B2B工业设备交易系统设计。系统采用严格的五层架构，支持复杂的支付场景，包括批量支付、部分支付、退款管理和信用还款等功能。

## 技术栈

| 技术 | 版本 | 说明 |
|------|------|------|
| Spring Boot | 2.7.18 | 主框架 |
| MyBatis-Plus | 3.5.5 | ORM框架 |
| Lombok | 1.18.30 | 代码生成 |
| MapStruct | 1.5.5 | DTO转换 |
| MySQL | 8.x | 数据库 |
| JDK | 1.8 | Java版本 |

## 项目结构

```
backend/
├── src/main/java/com/bytz/cms/payment/
│   ├── PaymentServiceApplication.java          # Spring Boot启动类
│   ├── interfaces/                             # 接口层
│   │   ├── controller/                         # REST控制器
│   │   └── model/                              # DTO（*RO请求对象，*VO响应对象）
│   ├── application/                            # 应用层
│   │   ├── service/                            # 应用服务（用例编排）
│   │   └── assembler/                          # MapStruct装配器
│   ├── domain/                                 # 领域层
│   │   ├── model/                              # 聚合根（*Aggregate）
│   │   ├── entity/                             # 实体（*Entity）
│   │   ├── valueobject/                        # 值对象（*ValueObject）
│   │   ├── enums/                              # 枚举类型
│   │   ├── command/                            # 命令对象（*Command）
│   │   ├── repository/                         # 仓储接口（I*Repository）
│   │   └── *DomainService.java                 # 领域服务
│   ├── infrastructure/                         # 基础设施层
│   │   ├── dataobject/                         # 数据对象（*DO）
│   │   ├── mapper/                             # MyBatis-Plus Mapper
│   │   ├── repository/                         # 仓储实现（*RepositoryImpl）
│   │   ├── channel/                            # 支付渠道接口实现
│   │   └── config/                             # 配置类
│   └── shared/                                 # 共享层
│       ├── exception/                          # 业务异常
│       └── model/                              # 领域事件（*Event）
├── src/main/resources/
│   ├── application.yml                         # 应用配置
│   └── db/                                     # 数据库脚本
│       └── init.sql                            # 初始化脚本
└── pom.xml                                     # Maven配置
```

## 核心功能

### 1. 支付单管理
- **支付单创建**：接收订单系统或信用管理系统的支付单创建请求
- **支付单查询**：支持多条件筛选查询支付单信息
- **支付单状态管理**：跟踪支付单完整生命周期

### 2. 支付执行
- **统一支付流程**：单支付单和多支付单合并支付使用统一处理逻辑
- **部分支付**：支持对支付单进行多次部分支付
- **支付渠道集成**：支持线上支付、钱包支付、电汇支付、信用账户等多种渠道

### 3. 退款管理
- **退款执行**：接收订单系统审批后的退款指令
- **流水选择**：支持指定流水退款、最新流水退款、多流水分摊退款
- **退款状态跟踪**：完整的退款流程状态管理

### 4. 信用还款
- **统一处理**：信用还款作为支付类型的一种，遵循统一支付流程
- **业务关联**：通过关联业务ID和类型关联信用记录

### 5. 状态补偿
- **补偿查询**：支持主动查询支付渠道状态
- **状态同步**：自动同步本地状态与渠道状态

## 数据库设计

### 支付单表 (cms_payment)

| 字段 | 类型 | 说明 |
|------|------|------|
| id | VARCHAR(32) | 支付单号（主键） |
| order_id | VARCHAR(32) | 关联订单号 |
| reseller_id | VARCHAR(32) | 经销商ID |
| payment_amount | DECIMAL(20,6) | 支付金额 |
| paid_amount | DECIMAL(20,6) | 已支付金额 |
| refunded_amount | DECIMAL(20,6) | 已退款金额 |
| actual_amount | DECIMAL(20,6) | 实际收款金额 |
| payment_type | VARCHAR(20) | 支付类型（枚举） |
| payment_status | VARCHAR(20) | 支付状态（枚举） |
| refund_status | VARCHAR(20) | 退款状态（枚举） |
| ... | ... | 其他字段见init.sql |

### 支付流水表 (cms_payment_transaction)

| 字段 | 类型 | 说明 |
|------|------|------|
| id | VARCHAR(32) | 流水号（主键） |
| payment_id | VARCHAR(32) | 支付单号 |
| transaction_type | VARCHAR(20) | 流水类型（PAYMENT/REFUND） |
| transaction_status | VARCHAR(20) | 流水状态（枚举） |
| transaction_amount | DECIMAL(20,6) | 交易金额 |
| payment_channel | VARCHAR(50) | 支付渠道（枚举） |
| channel_transaction_number | VARCHAR(64) | 渠道交易号 |
| ... | ... | 其他字段见init.sql |

## 快速开始

### 1. 环境准备

确保已安装：
- JDK 1.8+
- Maven 3.6+
- MySQL 8.0+

### 2. 数据库初始化

```bash
# 创建数据库并执行初始化脚本
mysql -u root -p < src/main/resources/db/init.sql
```

### 3. 配置数据库连接

编辑 `src/main/resources/application.yml`：

```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/cms_payment?useUnicode=true&characterEncoding=utf8&serverTimezone=Asia/Shanghai&useSSL=false
    username: your_username
    password: your_password
```

### 4. 编译项目

```bash
cd backend
mvn clean install
```

### 5. 运行项目

```bash
mvn spring-boot:run
```

或直接运行主类：
```bash
java -jar target/payment-service-1.0.0-SNAPSHOT.jar
```

项目启动后访问：http://localhost:8080/payment-service

## 领域模型

### 核心聚合根

#### PaymentAggregate（支付单聚合根）
- **职责**：管理支付单完整生命周期
- **核心行为**：
  - `create()` - 创建支付单
  - `markPaying()` - 标记为支付中
  - `applyPayment()` - 应用支付成功
  - `applyRefund()` - 应用退款
  - `validatePayable()` - 验证可支付性
  - `validateRefundable()` - 验证可退款性

#### PaymentTransactionEntity（支付流水实体）
- **职责**：记录每笔支付/退款操作
- **核心行为**：
  - `start()` - 创建流水记录
  - `success()` - 标记成功
  - `fail()` - 标记失败

### 支付类型枚举

- `ADVANCE_PAYMENT` - 预付款
- `FINAL_PAYMENT` - 尾款
- `OTHER_PAYMENT` - 其他费用
- `CREDIT_REPAYMENT` - 信用还款

### 支付状态枚举

- `UNPAID` - 未支付
- `PAYING` - 支付中
- `PARTIAL_PAID` - 部分支付
- `PAID` - 已支付
- `FAILED` - 支付失败
- `STOPPED` - 已停止
- `FROZEN` - 已冻结

### 退款状态枚举

- `NO_REFUND` - 未退款
- `REFUNDING` - 退款中
- `PARTIAL_REFUNDED` - 部分退款
- `FULL_REFUNDED` - 全额退款
- `REFUND_FAILED` - 退款失败

## 支付渠道

系统支持以下支付渠道（框架已实现，具体逻辑需补充）：

| 渠道 | 类型 | 说明 | 实现类 |
|------|------|------|--------|
| 线上支付 | ONLINE_PAYMENT | 银联、网银等 | OnlinePaymentChannelServiceImpl |
| 钱包支付 | WALLET_PAYMENT | 企业资金账户 | WalletPaymentChannelServiceImpl |
| 电汇支付 | WIRE_TRANSFER | 银行转账 | （TODO：待实现） |
| 信用账户 | CREDIT_ACCOUNT | 信用额度 | （TODO：待实现） |

**注意**：当前渠道实现为框架代码，包含TODO标记，需要根据实际业务需求补充具体逻辑。

## API示例（TODO）

目前项目已完成领域层、基础设施层的核心实现，接口层（Controller）和应用层（ApplicationService）需要进一步补充。

预期API端点：
- `POST /api/payments` - 创建支付单
- `GET /api/payments/{id}` - 查询支付单详情
- `POST /api/payments/execute` - 执行支付
- `POST /api/payments/refund` - 执行退款
- `GET /api/payments/filter` - 筛选支付单

## 开发指南

### 代码规范

1. **命名约定**：
   - 聚合根：`*Aggregate`
   - 实体：`*Entity`
   - 值对象：`*ValueObject`
   - 命令：`*Command`
   - 仓储接口：`I*Repository`
   - 仓储实现：`*RepositoryImpl`
   - DTO：`*RO`（请求）/ `*VO`（响应）

2. **注释规范**：
   - 所有public方法必须添加Javadoc注释
   - 关键业务逻辑添加行内注释
   - TODO标记未实现的功能

3. **DDD原则**：
   - 业务逻辑必须在聚合根中实现
   - 领域层不依赖基础设施层
   - 使用领域事件解耦模块

### 扩展开发

#### 添加新的支付渠道

1. 实现`IPaymentChannelService`接口
2. 在`infrastructure/channel/impl`包下创建实现类
3. 添加`@Service`注解注册到Spring容器

#### 添加新的领域服务

1. 在`domain`包下创建服务类
2. 注入必要的Repository
3. 实现跨聚合的业务逻辑

## 待完成事项

### 高优先级
- [ ] 实现应用层服务（Application Services）
- [ ] 实现MapStruct装配器（Assemblers）
- [ ] 实现REST控制器（Controllers）
- [ ] 实现领域服务（Domain Services）
- [ ] 补充支付渠道实现逻辑

### 中优先级
- [ ] 添加单元测试
- [ ] 添加集成测试
- [ ] 完善异常处理
- [ ] 添加日志记录
- [ ] 实现全局异常处理器

### 低优先级
- [ ] 添加API文档（Swagger）
- [ ] 性能优化
- [ ] 添加监控指标
- [ ] 实现缓存机制

## 参考文档

- [支付模块需求设计文档](../支付模块需求设计.md)
- [支付模块用例模型文档](../docs/支付模块用例模型.md)
- [全局术语表](../docs/Glossary.md)
- [DDD设计YAML规范](../docs/payment.yml)

## 联系方式

如有问题，请联系DDD设计团队。

## 许可证

Copyright © 2025 DDD设计团队。保留所有权利。

# 支付模块 Payment Module

## 项目简介

本项目是企业工业设备交易系统的支付模块，采用领域驱动设计（DDD）架构，处理B2B工业设备交易中的复杂支付场景。

## 技术栈

- **Java**: 1.8
- **Spring Boot**: 2.7.18
- **MyBatis-Plus**: 3.5.5
- **Lombok**: 1.18.30
- **MapStruct**: 1.5.5.Final
- **MySQL**: 8.0.33

## 项目结构

```
com.bytz.modules.cms.payment/
├── interfaces/                      # 接口层 - REST API接口
│   ├── controller/                  # 控制器
│   │   └── PaymentController        # 支付单REST控制器
│   └── model/                       # DTO对象
│       ├── PaymentCreateRO          # 创建支付单请求对象
│       ├── PaymentVO                # 支付单响应对象
│       └── PaymentTransactionVO     # 支付流水响应对象
├── application/                     # 应用层 - 用例协调
│   ├── PaymentApplicationService    # 支付单应用服务
│   ├── PaymentQueryService          # 支付单查询服务接口
│   ├── impl/                        # 应用服务实现
│   │   └── PaymentQueryServiceImpl  # 查询服务实现
│   └── assembler/                   # MapStruct转换器
│       └── PaymentAssembler         # 支付单对象转换器
├── domain/                          # 领域层 - 核心业务逻辑
│   ├── model/                       # 聚合根和领域对象
│   │   ├── PaymentAggregate         # 支付单聚合根
│   │   └── PaymentTransaction       # 支付流水领域对象
│   ├── enums/                       # 枚举类
│   │   ├── PaymentStatus            # 支付状态
│   │   ├── PaymentType              # 支付类型
│   │   ├── PaymentChannel           # 支付渠道
│   │   ├── RefundStatus             # 退款状态
│   │   ├── TransactionType          # 流水类型
│   │   ├── TransactionStatus        # 流水状态
│   │   └── RelatedBusinessType      # 关联业务类型
│   ├── command/                     # 命令对象
│   │   ├── CreatePaymentCommand     # 创建支付单命令
│   │   ├── ExecutePaymentCommand    # 执行支付命令
│   │   └── ExecuteRefundCommand     # 执行退款命令
│   ├── repository/                  # 仓储接口
│   │   └── IPaymentRepository       # 支付单仓储接口
│   └── PaymentDomainService         # 支付领域服务
├── infrastructure/                  # 基础设施层 - 技术实现
│   ├── entity/                      # 数据库实体
│   │   ├── PaymentEntity            # 支付单数据库实体
│   │   └── PaymentTransactionEntity # 支付流水数据库实体
│   ├── mapper/                      # MyBatis-Plus Mapper
│   │   ├── PaymentMapper            # 支付单Mapper
│   │   └── PaymentTransactionMapper # 支付流水Mapper
│   ├── repository/                  # 仓储实现
│   │   └── PaymentRepositoryImpl    # 支付单仓储实现
│   ├── assembler/                   # 基础设施层转换器
│   │   └── InfrastructureAssembler  # 领域对象与数据库实体转换
│   └── channel/                     # 支付渠道实现
│       ├── IPaymentChannelService   # 支付渠道接口
│       └── impl/                    # 渠道实现类
│           ├── OnlinePaymentChannelService    # 线上支付
│           ├── WalletPaymentChannelService    # 钱包支付
│           ├── WireTransferChannelService     # 电汇支付
│           └── CreditAccountChannelService    # 信用账户
└── shared/                          # 共享层 - 公共组件
    ├── exception/                   # 业务异常
    │   ├── BusinessException        # 业务异常基类
    │   └── PaymentException         # 支付异常
    └── model/                       # 领域事件
        ├── PaymentCreatedEvent      # 支付单已创建事件
        ├── PaymentExecutedEvent     # 支付已执行事件
        └── RefundExecutedEvent      # 退款已执行事件
```

## 核心功能实现

### 1. 支付单管理
- ✅ 创建支付单（支持预付款、尾款、其他费用、信用还款）
- ✅ 查询支付单（按支付单号、订单号、经销商ID、关联业务ID）
- ✅ 支付单状态管理（UNPAID、PAYING、PARTIAL_PAID、PAID、FAILED、STOPPED、FROZEN）

### 2. 领域模型
- ✅ PaymentAggregate - 支付单聚合根，封装核心业务逻辑
- ✅ PaymentTransaction - 支付流水领域对象
- ✅ 7个枚举类型，支持业务编码和自动转换（@EnumValue）
- ✅ 3个命令对象，封装业务参数

### 3. 数据持久化
- ✅ MyBatis-Plus集成，支持Lambda查询
- ✅ 数据库实体与领域对象分离
- ✅ InfrastructureAssembler实现领域对象与数据库实体转换
- ✅ 仓储模式实现（接口在domain，实现在infrastructure）

### 4. 支付渠道框架
- ✅ IPaymentChannelService - 统一的支付渠道接口
- 🔄 OnlinePaymentChannelService - 线上支付（待对接）
- 🔄 WalletPaymentChannelService - 钱包支付（待对接）
- 🔄 WireTransferChannelService - 电汇支付（待对接）
- 🔄 CreditAccountChannelService - 信用账户（待对接）

### 5. 应用服务
- ✅ PaymentApplicationService - 支付单创建和管理
- ✅ PaymentQueryService - 支付单查询服务
- ✅ MapStruct自动对象转换

### 图例说明
- ✅ 已实现
- 🔄 接口已定义，待具体实现
- ❌ 未实现

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

### 1. 创建支付单

**接口**: `POST /payment/api/v1/payments`

**请求示例**:
```json
{
  "orderId": "ORDER20231201001",
  "resellerId": "RESELLER001",
  "paymentAmount": 10000.00,
  "paymentType": "ADVANCE_PAYMENT",
  "businessDesc": "预付款",
  "paymentDeadline": "2023-12-31T23:59:59"
}
```

**响应示例**:
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
  "createTime": "2023-12-01T10:00:00",
  "transactions": []
}
```

### 2. 查询支付单

**接口**: `GET /payment/api/v1/payments/{id}`

**说明**: 根据支付单ID查询支付单详情

### 3. 按订单号查询支付单

**接口**: `GET /payment/api/v1/payments/by-order/{orderId}`

**说明**: 查询指定订单的所有支付单

### 4. 按经销商ID查询支付单

**接口**: `GET /payment/api/v1/payments/by-reseller/{resellerId}`

**说明**: 查询指定经销商的所有支付单

### 5. 按关联业务ID查询支付单

**接口**: `GET /payment/api/v1/payments/by-business/{businessId}`

**说明**: 用于信用还款场景，查询关联到特定业务的支付单

## 待实现功能清单

### 领域层
1. **PaymentAggregate业务方法增强**
   - 创建支付单时的完整参数验证
   - 信用还款订单号与信用记录绑定验证
   - 支付回调处理的完整逻辑
   - 退款回调处理的完整逻辑

2. **PaymentDomainService领域服务**
   - 批量支付的完整验证和处理
   - 支付流水选择策略实现
   - 渠道可用性验证

### 基础设施层
1. **支付渠道具体实现**
   - OnlinePaymentChannelService - 对接第三方线上支付平台
   - WalletPaymentChannelService - 对接钱包支付系统
   - WireTransferChannelService - 实现电汇支付处理
   - CreditAccountChannelService - 对接信用账户系统

2. **仓储优化**
   - 优化支付单号和流水号生成策略（分布式ID）

### 应用层
1. **支付执行应用服务**
   - PaymentExecutionApplicationService - 支付执行编排
   - 单支付单支付
   - 合并支付（多支付单批量处理）
   - 部分支付

2. **退款应用服务**
   - RefundApplicationService - 退款业务编排
   - 接收退款指令
   - 选择支付流水
   - 执行退款操作

3. **事件机制**
   - 实现事件发布机制（Spring Events或消息队列）
   - 事件监听器实现

### 接口层
1. **支付执行接口**
   - 单支付单支付接口
   - 批量支付接口
   - 支付回调接口

2. **退款接口**
   - 发起退款接口
   - 退款回调接口

3. **全局处理**
   - 全局异常处理器（@ControllerAdvice）
   - 统一响应格式

## DDD架构设计原则

### 1. 分层职责

#### 接口层（interfaces）
- 处理HTTP请求和响应
- 使用RO（Request Object）接收请求
- 使用VO（View Object）返回响应
- 不包含任何业务逻辑
- 通过应用服务协调业务

#### 应用层（application）
- 协调用例流程
- 调用领域服务和聚合根
- 使用MapStruct进行对象转换（RO → Command → Aggregate → VO）
- 不包含业务逻辑，仅负责编排
- 事务边界控制（@Transactional）

#### 领域层（domain）
- **核心业务逻辑所在层**
- 聚合根（Aggregate）：封装核心业务行为和不变性约束
- 领域服务（DomainService）：处理跨聚合的业务逻辑
- 仓储接口（Repository）：定义数据访问契约
- 命令对象（Command）：封装超过3个参数的方法调用
- 枚举类型（Enums）：业务状态和类型定义

#### 基础设施层（infrastructure）
- 技术实现层
- 数据库实体（Entity）：仅用于数据持久化，不包含业务逻辑
- Mapper：MyBatis-Plus数据访问
- 仓储实现（RepositoryImpl）：实现领域层的仓储接口
- 支付渠道：第三方接口对接

#### 共享层（shared）
- 业务异常定义
- 领域事件定义
- 跨模块共享的工具和常量

### 2. 对象转换规则

```
请求流程：
RO (Request Object) 
  → Command (命令对象) 
    → Aggregate (聚合根)
      → Entity (数据库实体)

响应流程：
Entity (数据库实体)
  → Aggregate (聚合根)
    → VO (View Object)
```

**转换工具**：
- 接口层 ↔ 应用层：使用 `PaymentAssembler` (MapStruct)
- 领域层 ↔ 基础设施层：使用 `InfrastructureAssembler` (MapStruct)

### 3. 枚举类型设计

本项目使用MyBatis-Plus的`@EnumValue`注解实现枚举自动转换：

```java
public enum PaymentStatus {
    UNPAID("UNPAID", "未支付", "Unpaid"),
    PAYING("PAYING", "支付中", "Paying"),
    // ...
    
    @EnumValue  // 标记用于数据库存储的字段
    private final String code;
    private final String description;
    private final String englishName;
}
```

**优势**：
- 类型安全，编译时检查
- 自动完成数据库字符串与枚举对象的转换
- 统一的业务编码管理

### 4. 开发约束

1. **严格分层**：不允许跨层调用，必须逐层调用
2. **业务逻辑位置**：所有业务逻辑必须在领域层实现
3. **实体分离**：领域对象与数据库实体必须分离
4. **命令模式**：参数超过3个时使用Command对象封装
5. **仓储模式**：只能通过聚合根操作数据，不直接操作实体
6. **领域事件**：使用事件进行跨聚合通信

## 开发指南

### 代码规范

1. **注释要求**
   - 所有类必须有完整的中英文注释
   - 所有公共方法必须有功能说明
   - 未实现的方法必须添加 `// TODO:` 标记并说明需求

2. **命名规范**
   - 聚合根：`*Aggregate` 后缀
   - 领域实体：`*Entity` 后缀（infrastructure层的数据库实体也使用此后缀）
   - 命令对象：`*Command` 后缀
   - 仓储接口：`I*Repository` 前缀
   - 仓储实现：`*RepositoryImpl` 后缀
   - 请求对象：`*RO` 后缀
   - 响应对象：`*VO` 后缀
   - 领域事件：`*Event` 后缀

3. **代码质量**
   - 提交代码前必须通过编译：`mvn clean compile`
   - 遵循现有的代码风格
   - 使用Lombok减少样板代码（@Data, @Builder, @Slf4j等）
   - 合理使用MapStruct进行对象转换

### 本地开发环境搭建

1. **环境要求**
   - JDK 1.8+
   - Maven 3.6+
   - MySQL 8.0+

2. **数据库初始化**
   ```bash
   # 创建数据库
   mysql -u root -p < src/main/resources/db/schema.sql
   ```

3. **配置修改**
   
   修改 `src/main/resources/application.yml`：
   ```yaml
   spring:
     datasource:
       url: jdbc:mysql://localhost:3306/cms_payment?useUnicode=true&characterEncoding=utf8&useSSL=false&serverTimezone=Asia/Shanghai
       username: your_username
       password: your_password
   ```

4. **编译运行**
   ```bash
   # 编译项目
   mvn clean compile
   
   # 运行项目
   mvn spring-boot:run
   
   # 或打包后运行
   mvn clean package
   java -jar target/payment-module-1.0.0-SNAPSHOT.jar
   ```

5. **验证**
   
   访问 http://localhost:8080/payment/api/v1/payments (需要先实现测试接口)

### 调试技巧

1. **查看SQL日志**
   
   在 `application.yml` 中配置：
   ```yaml
   mybatis-plus:
     configuration:
       log-impl: org.apache.ibatis.logging.stdout.StdOutImpl
   ```

2. **使用日志**
   ```java
   @Slf4j
   public class YourService {
       public void method() {
           log.info("处理支付单: {}", paymentId);
           log.error("支付失败", exception);
       }
   }
   ```

### 常见问题

**Q: MapStruct生成的转换类找不到？**

A: 确保已配置annotation processor，执行 `mvn clean compile` 重新编译。

**Q: 枚举类型存储报错？**

A: 确保枚举类的code字段标注了 `@EnumValue` 注解。

**Q: 如何添加新的支付类型？**

A: 在 `PaymentType` 枚举中添加新的类型，然后更新相关业务逻辑。

## 许可证

本项目为内部项目，版权所有。

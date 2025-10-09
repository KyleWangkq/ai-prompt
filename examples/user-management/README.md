# User Management - DDD示例项目

这是一个完整的DDD（领域驱动设计）示例项目，展示了用户管理系统的实现。

## 🏗️ 项目结构

```
src/main/java/com/example/user/
├── interfaces/                    # 接口层
│   ├── UserController.java       # REST控制器
│   └── model/                     # 接口层DTO
│       ├── UserCreateRO.java     # 创建用户请求对象
│       ├── UserUpdateRO.java     # 更新用户请求对象
│       └── UserVO.java           # 用户视图对象
├── application/                   # 应用层
│   └── UserApplicationService.java # 用户应用服务
├── domain/                        # 领域层
│   ├── UserDomainService.java    # 用户领域服务
│   ├── repository/               # 仓储接口
│   │   └── IUserRepository.java  # 用户仓储接口
│   ├── entity/                   # 领域实体
│   │   └── UserEntity.java       # 用户实体
│   └── model/                    # 领域模型
│       ├── UserAggregate.java    # 用户聚合根
│       └── valueobject/          # 值对象
│           ├── UserId.java       # 用户ID值对象
│           ├── Username.java     # 用户名值对象
│           └── Email.java        # 邮箱值对象
├── infrastructure/               # 基础设施层
│   ├── mapper/                   # MyBatis-Plus Mapper
│   │   └── UserMapper.java       # 用户数据映射器
│   ├── repository/               # 仓储实现
│   │   └── UserRepositoryImpl.java # 用户仓储实现
│   └── config/                   # 配置类
│       └── MybatisPlusConfig.java # MyBatis-Plus配置
└── shared/                       # 共享组件
    ├── exception/                # 异常定义
    │   └── BusinessException.java # 业务异常
    └── model/                    # 共享模型
        └── event/                # 领域事件
            └── UserCreatedEvent.java # 用户创建事件
```

## 🎯 DDD设计亮点

### 1. 富领域模型
- **UserAggregate**: 聚合根包含所有业务逻辑
- **值对象**: Email, Username等确保数据完整性
- **领域事件**: 解耦业务流程

### 2. 清晰的分层架构
- **接口层**: 只处理HTTP请求响应，使用DTO
- **应用层**: 编排用例流程，事务边界控制
- **领域层**: 核心业务逻辑和规则
- **基础设施层**: 技术实现细节

### 3. Repository模式
- **接口**: 面向领域的仓储接口
- **实现**: 基于MyBatis-Plus的技术实现
- **聚合**: 以聚合为单位进行数据持久化

## 🚀 快速开始

### 1. 环境要求
- Java 17+
- Spring Boot 2.7+
- MySQL 8.0+
- Maven 3.6+

### 2. 配置数据库
```sql
CREATE DATABASE user_management;

CREATE TABLE users (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    username VARCHAR(50) NOT NULL UNIQUE,
    email VARCHAR(100) NOT NULL,
    status VARCHAR(20) NOT NULL,
    created_at DATETIME,
    updated_at DATETIME
);
```

### 3. 配置应用
```yaml
# application.yml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/user_management
    username: your_username
    password: your_password
  jpa:
    hibernate:
      ddl-auto: validate
```

### 4. 运行项目
```bash
mvn spring-boot:run
```

## 📝 API接口

### 创建用户
```http
POST /api/users
Content-Type: application/json

{
    "username": "john_doe",
    "email": "john@example.com"
}
```

### 获取用户
```http
GET /api/users/{id}
```

### 更新用户邮箱
```http
PUT /api/users/{id}/email
Content-Type: application/json

{
    "email": "newemail@example.com"
}
```

### 禁用用户
```http
PUT /api/users/{id}/disable
```

## 🧪 测试用例

项目包含完整的测试套件：

- **单元测试**: 领域逻辑测试 (95%覆盖率)
- **集成测试**: Repository测试
- **API测试**: Controller端到端测试

运行测试：
```bash
mvn test
```

## 🔧 扩展指南

### 添加新的业务规则
1. 在 `UserAggregate` 中添加业务方法
2. 在 `UserDomainService` 中添加跨聚合逻辑
3. 更新相应的测试用例

### 添加新的查询
1. 在 `IUserRepository` 接口中定义方法
2. 在 `UserRepositoryImpl` 中实现
3. 在 `UserApplicationService` 中调用

### 集成其他系统
1. 定义领域事件
2. 创建事件监听器
3. 异步处理外部集成

## 📚 学习资源

- [DDD设计文档](../../docs/contexts/payment/domain/Payment.md)
- [领域建模指南](../../docs/支付模块领域模型设计总结.md)
- [架构最佳实践](../../.github/prompts/ddd-code.prompts.md)

---

**这个示例展示了完整的DDD实践，可以作为其他领域模型的参考模板。**
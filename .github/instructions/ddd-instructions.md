---
description: ''
applyTo: 'backend/**/*.java, backend/**/*.kt'
---

# DDD Spring Boot 代码生成助手

## 角色定位
是一位资深Java架构师，专门负责将DDD（领域驱动设计）文档转换为可编译运行的Spring Boot项目骨架。

### 技术栈约束（严格遵守）
- 框架组合：Spring Boot 2.7 + MyBatis-Plus 3.x + Lombok
- 架构风格：DDD分层架构（简化实用版本）
- 数据层设计：Entity与Model分离，Repository接口/实现分离
- 代码质量：所有功能的接口必须生成完整，其他功能要尽量生成完整，并进行测试。未生成完整的功能，或者生成但测试失败的功能，必须在代码中用TODO标记

### 交付标准（必须满足）
1. 完整性：提供项目完整文件树结构
2. 可编译性：每个Java文件包含完整源码，能直接编译
3. 配置完备：包含必要的配置文件（数据库、MyBatis-Plus等）
4. 可扩展性：关键业务逻辑用TODO标记，便于后续开发

## 输入格式
```yaml
项目根包：com.example.project
聚合：
  - EntityName（聚合根）
    字段：
      - fieldName: Type (描述)
    业务行为：
      - methodName(): 方法描述
业务约束：
  - 约束描述
```

## 📁 项目结构规范

### 核心架构原则
- Entity与Model分离：entity包存放数据对象，model包存放聚合根
- Repository接口/实现分离：domain定义接口，infrastructure提供实现
- 五层架构：interfaces、application、domain、infrastructure、shared

### 标准包结构
```
com.example.project
├─ interfaces               # 对外接口层
│  ├─ *Controller          # REST 控制器
│  └─ model                # 接口层数据传输对象
│     ├─ *RO               # 请求对象（如：UserCreateRO）
│     └─ *VO               # 响应对象（如：UserVO）
├─ application             # 应用服务层
│  └─ *ApplicationService  # Application Services（用例编排）
├─ domain                  # 领域层
│  ├─ *DomainService       # 领域服务（如：PaymentDomainService）
│  ├─ repository           # 仓储接口包
│  │  └─ I*Repository      # 仓储接口（如：IUserRepository）
│  ├─ entity               # 领域实体包
│  │  └─ *Entity           # 实体类（如：UserEntity、OrderEntity）
│  └─ model                # 领域模型对象
│     ├─ *Aggregate        # 聚合根（如：UserAggregate）
│     └─ *ValueObject      # 值对象（如：MoneyValueObject）
├─ infrastructure          # 基础设施层
│  ├─ mapper               # MyBatis-Plus Mapper包
│  │  ├─ *Mapper           # Mapper接口（如：UserMapper）
│  │  └─ xml               # Mapper XML文件
│  │     └─ *Mapper.xml    # XML映射文件（如：UserMapper.xml）
│  ├─ repository           # 仓储实现包
│  │  └─ *RepositoryImpl   # 仓储实现（如：UserRepositoryImpl）
│  └─ *Config              # 配置类（如：MybatisPlusConfig）
└─ shared                  # 共享组件
   ├─ *Exception           # 异常定义（如：BusinessException）
   └─ model                # 共享数据对象
      └─ *Event            # 领域事件（如：UserCreatedEvent）
```

### 核心分层职责
- interfaces: 对外接口层，处理HTTP请求/响应
- application: 应用服务层，编排用例流程，不包含业务逻辑
- domain: 领域层，包含核心业务逻辑和规则
- infrastructure: 基础设施层，实现技术细节
- shared: 共享组件

## 🔧 命名与技术规范（必须遵守）

### 仓储模式（接口/实现分离）
- 仓储接口：使用 I 前缀，如 IUserRepository，位于 domain/repository/ 包
- 仓储实现：去掉 I，加 Impl 后缀，如 UserRepositoryImpl，位于 infrastructure/repository/ 包
- 继承关系：仓储接口继承 IService<Entity>；实现类继承 ServiceImpl<Mapper, Entity> 并实现对应接口

### MyBatis-Plus规范  
- Mapper接口：继承 BaseMapper<T>，位于 infrastructure/mapper/ 包
- Mapper XML：放在 infrastructure/mapper/xml/ 下，仅在自定义SQL时生成
- 实体注解：使用 @TableName、@TableId、@TableField（必要时）
- 仓储实现优先使用Lambda模式：继承 ServiceImpl<Mapper, Entity> 后使用内置的 lambdaQuery()、lambdaUpdate() 等方法进行CRUD操作
- 避免手写SQL：充分利用MyBatis-Plus的条件构造器和Lambda表达式

### Lombok使用规范
- 实体类：@Data, @NoArgsConstructor, @AllArgsConstructor（按需）
- Controller：@RestController、@RequestMapping
- Service：@Service

### 数据传输对象规范
- RO/VO与领域实体解耦：避免在Controller中直接暴露实体
- Controller层：接收 RO，返回 VO
- Application Service：负责 RO ↔ Domain ↔ VO 转换

---

## 📤 输出要求

### 必须生成的文件类型
1. 领域层：聚合根、实体、值对象、领域服务接口
2. 基础设施层：PO实体、Mapper接口、仓储实现
3. 应用层：Application Service
4. 接口层：Controller、DTO（RO/VO）
5. 配置文件：MyBatis-Plus配置、数据库配置

### 代码质量要求
- 所有类必须包含完整的包声明和必要的import
- 使用适当的注解（@Entity、@Service、@RestController等）
- 为所有待实现方法添加TODO注释
- 保持代码可编译性

### 输出格式
```
文件树结构
├─ 文件路径
└─ 每个文件的完整源码
```

---

## 💡 代码模板示例

### 纯数据Entity对象 (domain/entity/UserEntity.java)
```java
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("users")
public class UserEntity {
    @TableId
    private Long id;
    private String username;
    private String email;
    private String status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    // Entity只包含数据字段，无领域方法
}
```

### 聚合根 (domain/model/UserAggregate.java)
```java
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserAggregate {
    private Long id;
    private String username;
    private String email;
    private String status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    // 所有领域方法都在聚合根中
    public void disable() {
        this.status = "DISABLED";
        this.updatedAt = LocalDateTime.now();
        // TODO: 实现其他禁用业务逻辑
    }
    public void enable() {
        this.status = "ACTIVE";
        this.updatedAt = LocalDateTime.now();
        // TODO: 实现其他启用业务逻辑
    }
    public void changeEmail(String newEmail) {
        // TODO: 添加邮箱格式验证等业务规则
        this.email = newEmail;
        this.updatedAt = LocalDateTime.now();
    }
    public boolean isActive() {
        return "ACTIVE".equals(this.status);
    }
    // 从RO转换
    public static UserAggregate fromCreateRO(UserCreateRO ro) {
        return UserAggregate.builder()
                .username(ro.getUsername())
                .email(ro.getEmail())
                .status("ACTIVE")
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
    }
    // 转换为Entity
    public UserEntity toEntity() {
        return UserEntity.builder()
                .id(this.id)
                .username(this.username)
                .email(this.email)
                .status(this.status)
                .createdAt(this.createdAt)
                .updatedAt(this.updatedAt)
                .build();
    }
    // 从Entity转换
    public static UserAggregate fromEntity(UserEntity entity) {
        return UserAggregate.builder()
                .id(entity.getId())
                .username(entity.getUsername())
                .email(entity.getEmail())
                .status(entity.getStatus())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }
}
```

### 仓储接口 (domain/repository/IUserRepository.java)
```java
public interface IUserRepository extends IService<UserEntity> {
    UserEntity findByUsername(String username);
    UserAggregate findAggregateById(Long id);
    void saveAggregate(UserAggregate aggregate);
}
```

### 仓储实现 (infrastructure/repository/UserRepositoryImpl.java)
```java
@Service
public class UserRepositoryImpl extends ServiceImpl<UserMapper, UserEntity> implements IUserRepository {
    @Override
    public UserEntity findByUsername(String username) {
        return this.lambdaQuery()
                .eq(UserEntity::getUsername, username)
                .one();
    }
    @Override
    public UserAggregate findAggregateById(Long id) {
        UserEntity entity = this.getById(id);
        return entity == null ? null : UserAggregate.fromEntity(entity);
    }
    @Override
    public void saveAggregate(UserAggregate aggregate) {
        UserEntity entity = aggregate.toEntity();
        if (entity.getId() == null) {
            this.save(entity);
            aggregate.setId(entity.getId());
        } else {
            this.updateById(entity);
        }
    }
    // Lambda模式示例：条件查询
    public List<UserEntity> findActiveUsers() {
        return this.lambdaQuery()
                .eq(UserEntity::getStatus, "ACTIVE")
                .list();
    }
    // Lambda模式示例：分页查询
    public Page<UserEntity> findUsersByStatusPage(String status, int pageNum, int pageSize) {
        Page<UserEntity> page = new Page<>(pageNum, pageSize);
        return this.lambdaQuery()
                .eq(UserEntity::getStatus, status)
                .orderByDesc(UserEntity::getCreatedAt)
                .page(page);
    }
    // Lambda模式示例：批量更新
    public void batchUpdateUserStatus(List<Long> userIds, String newStatus) {
        this.lambdaUpdate()
                .set(UserEntity::getStatus, newStatus)
                .set(UserEntity::getUpdatedAt, LocalDateTime.now())
                .in(UserEntity::getId, userIds)
                .update();
    }
    // Lambda模式示例：复杂条件查询
    public List<UserEntity> findUsersByConditions(String username, String email, String status) {
        return this.lambdaQuery()
                .like(StringUtils.hasText(username), UserEntity::getUsername, username)
                .eq(StringUtils.hasText(email), UserEntity::getEmail, email)
                .eq(StringUtils.hasText(status), UserEntity::getStatus, status)
                .orderByDesc(UserEntity::getCreatedAt)
                .list();
    }
    // Lambda模式示例：统计查询
    public long countActiveUsers() {
        return this.lambdaQuery()
                .eq(UserEntity::getStatus, "ACTIVE")
                .count();
    }
    // TODO: 其他领域查询方法实现
}
```

### Mapper接口 (infrastructure/mapper/UserMapper.java)
```java
@Mapper
public interface UserMapper extends BaseMapper<UserEntity> {
    // MyBatis-Plus会自动实现基本CRUD
    // 复杂查询可在此定义，对应XML文件
}
```

### Mapper XML (infrastructure/mapper/xml/UserMapper.xml)
```xml
<?xml version="1.0" encoding="UTF-8"?>
<!DOCTYPE mapper PUBLIC "-//mybatis.org//DTD Mapper 3.0//EN" "http://mybatis.org/dtd/mybatis-3-mapper.dtd">
<mapper namespace="com.example.project.infrastructure.mapper.UserMapper">
    <!-- 自定义查询示例 -->
    <!-- 
    <select id="findUsersByStatus" resultType="com.example.project.domain.entity.UserEntity">
        SELECT * FROM users WHERE status = #{status}
    </select>
    -->
</mapper>
```

### Application Service (application/UserApplicationService.java)
```java
@Service
public class UserApplicationService {
    @Autowired
    private IUserRepository userRepository;
    @Autowired
    private UserDomainService userDomainService;
    public UserVO createUser(UserCreateRO ro) {
        // RO -> Aggregate
        UserAggregate aggregate = UserAggregate.fromCreateRO(ro);
        // 领域服务处理
        userDomainService.validateAndCreateUser(aggregate);
        // 保存
        userRepository.saveAggregate(aggregate);
        // Aggregate -> VO
        return UserVO.builder()
                .id(aggregate.getId())
                .username(aggregate.getUsername())
                .email(aggregate.getEmail())
                .status(aggregate.getStatus())
                .build();
    }
    public UserVO updateUserEmail(Long userId, String newEmail) {
        // 获取聚合根
        UserAggregate aggregate = userRepository.findAggregateById(userId);
        if (aggregate == null) {
            throw new BusinessException("用户不存在");
        }
        // 领域方法处理
        aggregate.changeEmail(newEmail);
        // 领域服务验证
        userDomainService.validateEmailChange(aggregate);
        // 保存
        userRepository.saveAggregate(aggregate);
        // 返回VO
        return UserVO.builder()
                .id(aggregate.getId())
                .username(aggregate.getUsername())
                .email(aggregate.getEmail())
                .status(aggregate.getStatus())
                .build();
    }
}
```

### 领域服务 (domain/UserDomainService.java)
```java
@Service
public class UserDomainService {
    @Autowired
    private IUserRepository userRepository;
    public void validateAndCreateUser(UserAggregate aggregate) {
        // 用户名唯一性检查
        UserEntity existingUser = userRepository.findByUsername(aggregate.getUsername());
        if (existingUser != null) {
            throw new BusinessException("用户名已存在");
        }
        // TODO: 其他业务验证规则
    }
    public void validateEmailChange(UserAggregate aggregate) {
        // TODO: 邮箱变更业务验证
        if (aggregate.getEmail() == null || !aggregate.getEmail().contains("@")) {
            throw new BusinessException("邮箱格式不正确");
        }
    }
    // TODO: 其他领域服务方法
}
```

---

## ⚙️ 可选配置参数

在提供DDD文档时，可声明以下参数：
* db: mysql / postgresql（影响实体注解和建表语法）
* generateMapperXml: true / false（是否为复杂查询生成XML文件）
* useSoftDelete: true / false（是否添加软删除字段）
* auditFields: true / false（是否自动生成审计字段：createdBy, createdAt, updatedBy, updatedAt）

**使用示例**：
```
配置: { db: mysql, generateMapperXml: true, useSoftDelete: true, auditFields: true }
```

## 使用说明

**提供你的DDD设计文档，我会基于该文档直接生成完整的项目代码骨架（文件树 + 每个Java文件内容）。**

---

## ✅ 验收清单

确保生成的代码满足以下要求：
1. 完整性检查：所有聚合必须生成实体、仓储接口、Mapper、仓储实现、Application Service、Controller、RO/VO
2. 命名规范：包路径、类名必须与输入的根包一致
3. 事件处理：领域事件若在文档中声明，则生成 shared 包下的事件类
4. 注解完整：必要的注解完整（@TableName、@TableId、@RestController、@Service、Lombok 注解等）
5. 解耦要求：RO/VO 与领域实体要解耦（不得在 Controller 中直接暴露实体）
6. 待实现标记：对于每个待实现的方法使用 // TODO: 注释标注

---

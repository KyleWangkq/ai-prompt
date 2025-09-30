# 优化版提示词：将 DDD 设计文档转为可编译代码骨架（Java / Spring Boot 2.7 / MyBatis-Plus）

下面的提示词模板可直接用于让 AI（或自动化脚本）把你的 DDD 设计文档转换成可用的 Java 项目骨架。把你的 DDD 文档粘贴到 "输入文档" 区域，然后运行即可。提示词包含明确的包结构、命名规范、输出文件模板与可选参数，便于生成高质量、一致性强的代码。

---

## 1. 目标（对 AI 的简短说明）

你是一名资深的 Java 架构师和 DDD 实践专家。根据我提供的 DDD 设计文档，生成完整的项目代码骨架，满足以下技术与架构要求：

* Spring Boot 2.7
* MyBatis-Plus（`IService` / `ServiceImpl` / `BaseMapper`）
* 关系型数据库（MySQL / PostgreSQL）
* Lombok（用于简化 POJO）
* 遵循 DDD 分层（interfaces / application / domain / infrastructure / event / config）与本提示中定义的包规范

输出以文件树和伴随的 Java 类/接口/Mapper/XML 内容为主（只生成骨架与必要注解与注释），便于开发者进一步填充业务实现。

---

## 2. 输入要求（你必须传给 AI 的内容）

把下面内容作为输入文档直接粘贴：

1. 项目根包名（例如：`com.example.project`）
2. 聚合与上下文清单：每个聚合名、聚合根、聚合内实体与值对象
3. 每个实体的字段（字段名、类型、是否主键、数据库列名、是否索引、约束说明）
4. 领域行为 / 业务规则（方法名、输入/输出、触发条件、事务边界）
5. 需要的领域事件（事件名、载荷字段、何时发布）
6. 额外说明（是否需要 Mapper XML、自定义 SQL、软删除、乐观锁、审计字段）

示例格式（可选）:

```
ProjectRoot: com.example.project
Aggregate: User
  AggregateRoot: User
    fields:
      - id: Long (pk)
      - username: String
      - email: String
      - status: Enum(UserStatus)
    behaviors:
      - register(username, email) -> UserCreatedEvent
      - disable() -> change status
Events:
  - UserCreatedEvent(payload: userId, username)
Infrastructure: use MySQL, need XML for complex query
```

---

## 3. 包结构（必须严格生成）

```
com.example.project
├─ interfaces
│  ├─ controller         # REST 控制器
│  └─ model              # RO / VO
│     ├─ ro
│     └─ vo
├─ application
│  └─ service            # Application Services（用例）
├─ event                 # 领域事件、集成事件 DTO
├─ domain
│  ├─ entity             # 聚合根 / 实体 / 值对象
│  ├─ repository         # 仓储接口（I 前缀）
│  └─ service            # 领域服务
├─ infrastructure
│  ├─ mapper             # MyBatis-Plus Mapper 接口
│  │   └─ xml            # Mapper XML（仅在需要时生成）
│  └─ repositoryImpl     # 仓储实现（Impl 后缀）
└─ config                # Spring 与 MyBatis 配置
```

---

## 4. 命名与约定（必须遵守）

* 仓储接口使用 `I` 前缀：`IUserRepository`；实现类去掉 `I`，加 `Impl`：`UserRepositoryImpl`。
* 仓储接口继承 `IService<T>`；实现类继承 `ServiceImpl<Mapper, Entity>` 并实现接口。
* Mapper 接口继承 `BaseMapper<T>`；Mapper XML 放在 `mapper/xml` 下，仅在自定义 SQL 时生成。
* 实体使用 MyBatis-Plus 注解（`@TableName`、`@TableId`）；字段使用 `@TableField`（必要时）。
* 使用 Lombok 注解：`@Data`, `@NoArgsConstructor`, `@AllArgsConstructor`, `@Builder`（按需）。
* Controller 使用 `@RestController`、`@RequestMapping`；Application Service 使用 `@Service`。
* RO/VO 与领域实体解耦：Controller 接收 `RO`，Application Service 将 RO 转为领域 `Entity` 或领域 DTO，再执行业务，最后返回 `VO`。

---

## 5. 输出格式（AI 要生成的内容）

对于每个聚合，生成以下文件与代码骨架：

1. `domain/entity/{X}.java`：实体/聚合根/值对象类（含字段、注释、基本构造器、必要的领域方法签名）
2. `domain/repository/I{X}Repository.java`：仓储接口（继承 `IService<{X}>`），包括需要的领域查询方法签名
3. `infrastructure/mapper/{X}Mapper.java`：Mapper 接口（继承 `BaseMapper<{X}>`）
4. `infrastructure/mapper/xml/{X}Mapper.xml`（可选）：自定义 SQL
5. `infrastructure/repositoryImpl/{X}RepositoryImpl.java`：仓储实现（`ServiceImpl<{X}Mapper, {X}>`，实现 `I{X}Repository`）
6. `application/service/{X}ApplicationService.java`：Application Service，封装用例流程，转换 RO↔Entity↔VO
7. `interfaces/model/ro/{X}CreateRO.java`、`interfaces/model/vo/{X}VO.java`：RO / VO
8. `interfaces/controller/{X}Controller.java`：REST Controller（示例 CRUD 与关键用例）
9. `event/{X}Event.java`：领域事件类（如有）
10. `config/MybatisPlusConfig.java`：基础配置（如全局策略、分页插件注入说明）

每个文件应包含必要的 import、类注释、关键注解与 TODO 标记，方便后续实现。

---

## 6. 模板示例（以 `User` 聚合为例）

> 注意：下面只是示意模板，AI 在生成实际文件时应替换包名与字段，并生成完整的类文件。

**domain/entity/User.java**

```java
package com.example.project.domain.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@TableName("users")
public class User {
    @TableId
    private Long id;
    private String username;
    private String email;
    // 领域方法示例
    public void disable() {
        // TODO: 添加业务校验
    }
}
```

**domain/repository/IUserRepository.java**

```java
package com.example.project.domain.repository;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.project.domain.entity.User;

public interface IUserRepository extends IService<User> {
    // 自定义领域查询方法签名示例
    User findByUsername(String username);
}
```

**infrastructure/mapper/UserMapper.java**

```java
package com.example.project.infrastructure.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.project.domain.entity.User;

public interface UserMapper extends BaseMapper<User> {}
```

**infrastructure/repositoryImpl/UserRepositoryImpl.java**

```java
package com.example.project.infrastructure.repositoryImpl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;
import com.example.project.infrastructure.mapper.UserMapper;
import com.example.project.domain.entity.User;
import com.example.project.domain.repository.IUserRepository;

@Service
public class UserRepositoryImpl extends ServiceImpl<UserMapper, User> implements IUserRepository {
    // TODO: 添加自定义方法实现
}
```

**application/service/UserApplicationService.java**

```java
package com.example.project.application.service;

import org.springframework.stereotype.Service;
import com.example.project.domain.repository.IUserRepository;
import com.example.project.interfaces.model.ro.UserCreateRO;
import com.example.project.interfaces.model.vo.UserVO;

@Service
public class UserApplicationService {
    private final IUserRepository userRepository;

    public UserApplicationService(IUserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public UserVO createUser(UserCreateRO ro) {
        // TODO: RO -> Entity 转换，调用领域逻辑，持久化，返回 VO
        return null;
    }
}
```

**interfaces/controller/UserController.java**

```java
package com.example.project.interfaces.controller;

import org.springframework.web.bind.annotation.*;
import com.example.project.application.service.UserApplicationService;
import com.example.project.interfaces.model.ro.UserCreateRO;
import com.example.project.interfaces.model.vo.UserVO;

@RestController
@RequestMapping("/api/users")
public class UserController {
    private final UserApplicationService userAppService;

    public UserController(UserApplicationService userAppService) {
        this.userAppService = userAppService;
    }

    @PostMapping
    public UserVO create(@RequestBody UserCreateRO ro) {
        return userAppService.createUser(ro);
    }
}
```

---

## 7. 可选参数（在请求中声明）

* `db`: `mysql` / `postgresql`（决定实体注解、建表注释）
* `generateMapperXml`: `true` / `false`（是否为复杂查询生成 XML）
* `useSoftDelete`: `true` / `false`（是否在实体上添加软删除字段与注解）
* `auditFields`: `createdBy, createdAt, updatedBy, updatedAt`（是否自动生成）

示例：`{ db: mysql, generateMapperXml: true, useSoftDelete: true }`

---

## 8. 验收清单（AI 必须满足）

1. 所有聚合必须生成：实体、仓储接口、Mapper、仓储实现、Application Service、Controller、RO/VO。
2. 包路径、类名必须与输入的根包一致。
3. 领域事件若在文档中声明，则生成 `event` 包下的事件类。
4. 必要的注解（`@TableName`、`@TableId`、`@RestController`、`@Service`、Lombok 注解等）必须存在。
5. RO/VO 与领域实体要解耦（不得在 Controller 中直接暴露实体）。
6. 对于每个待实现的方法使用 `// TODO:` 注释标注，说明需要业务实现的地方。

---

## 9. 交互范例（用户如何调用）

用户粘贴 DDD 文档后，AI 输出：

* 一个文件树（显示文件路径）
* 每个文件的完整 Java 源代码骨架
* 如果 `generateMapperXml: true`，生成对应 `XMapper.xml`（带示例 SQL）

---

## 10. 额外注意事项与最佳实践建议

* 如果某个聚合过大，建议拆成子聚合或将复杂查询放到 `repositoryImpl` 并在 `mapper/xml` 中实现。
* 对于强一致性的操作，标注事务边界（`@Transactional`）并放在 Application Service 层。
* 对于跨聚合操作，使用领域事件解耦，事件类放在 `event` 包。
* 生成的代码应保持可编译（必要 import、包声明、类签名存在），但业务方法可保留 TODO。

---

## 11. Checklist（供复制）

* [ ] 我已包含项目根包名
* [ ] 我已列出聚合及其实体/值对象与字段
* [ ] 我已列出业务行为与事件
* [ ] 我已声明是否需要 Mapper XML、自定义 SQL 或审计字段

---

> 现在：把你的 DDD 设计文档按上面“输入要求”的格式粘贴到回复中，或告诉我你要生成的根包名与几条核心聚合信息。我会基于该文档直接生成完整的项目代码骨架（文件树 + 每个 Java 文件内容）。

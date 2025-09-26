你是一名资深的 Java 架构师和 DDD 实践专家。  
我将提供一份 DDD 设计文档，请你基于该文档生成对应的代码实现。  
技术栈要求如下：
- 框架：Spring Boot 2.7
- 持久化框架：MyBatis-Plus
- 数据库：关系型数据库（如 MySQL 或 PostgreSQL）
- 遵循 DDD 分层架构和自定义包结构规范

---

### 包结构规范

- `com.example.project.interfaces`
  - `controller` → REST API Controller 实现类
  - `model` → 接口层对象（RO/VO）
    - **RO (Request Object)** → Controller 接收的请求对象
    - **VO (View Object)** → Controller 返回的响应对象

- `com.example.project.application`
  - `service` → 应用服务，封装用例逻辑，调用领域层

- `com.example.project.event`
  - 存放领域事件或集成事件对象

- `com.example.project.domain`
  - `entity` → 领域实体、聚合根、值对象
  - `repository` → 数据接口，命名规则：
    - 接口用 `I` 前缀，例如 `IUserRepository`
    - 接口继承 MyBatis-Plus 的 `IService<T>`，例如：
      ```java
      public interface IUserRepository extends IService<User> {
          // 可添加自定义领域方法
      }
      ```
  - `service` → 领域服务，封装复杂业务逻辑

- `com.example.project.infrastructure`
  - `repositoryImpl` → 数据接口实现类，命名规则：
    - 去掉 `I` 前缀，加 `Impl` 后缀，例如 `UserRepositoryImpl`
    - **继承 MyBatis-Plus 的 `ServiceImpl<Mapper, Entity>` 并实现接口**，例如：
      ```java
      @Service
      public class UserRepositoryImpl extends ServiceImpl<UserMapper, User> implements IUserRepository {
          // 可添加自定义方法
      }
      ```
  - `mapper` → MyBatis-Plus Mapper 接口
    - `xml` → MyBatis 对应的 Mapper XML 文件

- `com.example.project.config`
  - Spring Boot 和 MyBatis 配置类

---

### 代码生成要求

1. **领域层 (Domain)**
   - 按文档定义的聚合、实体和值对象生成类，放在 `entity` 包下
   - 在 `repository` 包下生成 MyBatis-Plus 接口 (`IUserRepository`)，继承 `IService<T>`
   - 如果存在复杂逻辑，放入 `service` 包下作为领域服务

2. **基础设施层 (Infrastructure)**
   - 在 `repositoryImpl` 包下生成接口实现类 (`UserRepositoryImpl`)，继承 `ServiceImpl<Mapper, Entity>` 并实现接口
   - 在 `mapper` 包下生成 Mapper 接口（继承 `BaseMapper<T>`）
   - 在 `mapper/xml` 包下生成 XML 文件（仅自定义 SQL 时）

3. **应用层 (Application)**
   - 在 `application.service` 包下生成 Application Service 类，封装用例逻辑，调用领域层

4. **接口层 (Interfaces)**
   - 在 `controller` 包下生成 Controller 实现类（使用 `@RestController`、`@RequestMapping`）
   - 在 `model` 包下生成 RO/VO 对象：
     - **RO**：Controller 接收的请求对象（如 `UserCreateRO`）
     - **VO**：Controller 返回的响应对象（如 `UserVO`）

5. **事件层 (Event)**
   - 在 `event` 包下生成领域事件或集成事件对象

6. **附加要求**
   - 使用 Lombok 简化 getter/setter/构造器
   - 实体类使用 MyBatis-Plus 注解：`@TableName`、`@TableId`
   - Application Service 与 Controller 层之间通过 **RO → Entity → VO** 转换，不直接暴露领域对象

---

### 输入文档
下面是我设计的 DDD 文档，请你基于该文档生成完整的项目代码骨架：


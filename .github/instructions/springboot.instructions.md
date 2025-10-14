---
description: 'Guidelines for building Spring Boot base applications'
applyTo: 'backend/**/*.java, backend/**/*.kt'
---

# Spring Boot Development

## General Instructions

- Make only high confidence suggestions when reviewing code changes.
- Write code with good maintainability practices, including comments on why certain design decisions were made.
- Handle edge cases and write clear exception handling.
- For libraries or external dependencies, mention their usage and purpose in comments.

## Spring Boot Instructions

### Dependency Injection

- Use constructor injection for all required dependencies.
- Declare dependency fields as `private final`.

### Configuration

- Use YAML files (`application.yml`) for externalized configuration.
- Environment Profiles: Use Spring profiles for different environments (dev, test, prod)
- Configuration Properties: Use @ConfigurationProperties for type-safe configuration binding
- Secrets Management: Externalize secrets using environment variables or secret management systems

### Code Organization

- Package Structure: Organize by feature/domain rather than by layer
- Separation of Concerns: Keep controllers thin, services focused, and repositories simple
- Utility Classes: Make utility classes final with private constructors

### Service Layer

- Place business logic in `@Service`-annotated classes.
- Services should be stateless and testable.
- Inject repositories via the constructor.
- Service method signatures should use domain IDs or DTOs, not expose repository entities directly unless necessary.

### Logging

- Use  Lombok `@Slf4j` for logging
- Do not use concrete implementations (Logback, Log4j2) or `System.out.println()` directly.
- Use parameterized logging: `log.info("User {} logged in", userId);`.

### Data Access Layer (MyBatis-Plus)

- Use MyBatis-Plus for database operations and repository implementations.
- Extend `BaseMapper<T>` for basic CRUD operations.
- Use `@Mapper` annotation on mapper interfaces.
- Leverage MyBatis-Plus built-in methods: `selectById()`, `insert()`, `updateById()`, `deleteById()`.
- Use `LambdaQueryWrapper` and `LambdaUpdateWrapper` for type-safe queries.
- For complex queries, use `@Select`, `@Update`, `@Insert`, `@Delete` annotations or XML mappers.
- Use Page<T> for pagination with `selectPage()` method.

```java
// Example Mapper Interface
@Mapper
public interface UserMapper extends BaseMapper<UserEntity> {
    // Custom query using annotation
    @Select("SELECT * FROM users WHERE status = #{status} AND age > #{minAge}")
    List<UserEntity> findActiveUsersByAge(@Param("status") String status, @Param("minAge") Integer minAge);
    
    // Using LambdaQueryWrapper (recommended for dynamic queries)
    default List<UserEntity> findUsersByCondition(String name, Integer age) {
        LambdaQueryWrapper<UserEntity> wrapper = new LambdaQueryWrapper<>();
        wrapper.like(StringUtils.isNotBlank(name), UserEntity::getName, name)
               .ge(age != null, UserEntity::getAge, age);
        return selectList(wrapper);
    }
}
```

### Entity Mapping

- Use `@TableName` to specify table names.
- Use `@TableId` for primary key fields with appropriate ID generation strategy.
- Use `@TableField` for field mapping and to exclude non-database fields.
- Use `@TableLogic` for logical deletion.
- Follow naming conventions: entity class names in PascalCase, table names in snake_case.

```java
@Data
@TableName("t_user")
public class UserEntity {
    @TableId(type = IdType.AUTO)
    private Long id;
    
    @TableField("user_name")
    private String name;
    
    @TableField("email_address")
    private String email;
    
    @TableLogic
    private Integer deleted;
    
    @TableField(exist = false)
    private String tempField; // Not persisted to database
}
```

### Security & Input Handling

- MyBatis-Plus automatically handles SQL injection prevention through parameterized queries.
- Always use wrapper conditions or parameterized annotations (`@Param`) for dynamic SQL.
- Validate request bodies and parameters using JSR-380 (`@NotNull`, `@Size`, etc.) annotations and `BindingResult`

## Build and Verification

- After adding or modifying code, verify the project continues to build successfully.
- If the project uses Maven, run `mvn clean install`.
- If the project uses Gradle, run `./gradlew build` (or `gradlew.bat build` on Windows).
- Ensure all tests pass as part of the build.

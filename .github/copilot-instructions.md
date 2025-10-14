# Copilot Instructions for DDD Spring Boot Workspace

This workspace is a comprehensive Domain-Driven Design (DDD) development environment that generates enterprise Spring Boot applications using AI-driven code generation and automation workflows.

## Architecture Overview

This codebase follows a **five-layer DDD architecture** with strict separation of concerns:

```
interfaces/     # REST API controllers, DTOs, GraphQL endpoints
application/    # Use case orchestration, application services, commands
domain/         # Business logic, aggregates, entities, value objects, domain services
infrastructure/ # Repository implementations, database mappers, external integrations
shared/         # Cross-cutting concerns, exceptions, utilities
```

**Key Design Principles:**
- **Rich Domain Models**: Business logic lives in aggregates, not anemic services
- **Repository Pattern**: Domain interfaces, infrastructure implementations using MyBatis-Plus
- **Domain Events**: Aggregate-to-aggregate communication via event sourcing
- **Bounded Contexts**: Clear context boundaries with explicit integration patterns

## Core Development Patterns

### Aggregate Design
- **Aggregate Roots** control business invariants and serve as consistency boundaries
- **Value Objects** ensure data integrity (e.g., `Email`, `UserId`, `Money`)
- **Domain Events** are collected in aggregates and published after transactions
- Example: See `examples/user-management/src/main/java/com/example/user/domain/model/UserAggregate.java`

### Repository Pattern
```java
// Domain layer - interface definition
public interface UserRepository {
    Optional<UserAggregate> findById(UserId id);
    void save(UserAggregate user);
}

// Infrastructure layer - MyBatis-Plus implementation
@Repository
public class UserRepositoryImpl implements UserRepository {
    // Implementation using MyBatis-Plus
}
```

### Application Services
- Orchestrate business workflows across aggregates
- Manage transaction boundaries
- Handle domain event publishing
- Convert between DTOs and domain objects

## AI Agent System

### Available Commands
The workspace includes a sophisticated Copilot Agent (`.github/copilot/agent.yml`) with these commands:

- `/ddd-generate` - Generate complete DDD code from specifications
- `/ddd-validate` - Validate architecture compliance and best practices
- `/ddd-test` - Generate comprehensive test suites (unit, integration, API)
- `/ddd-refactor` - Analyze and suggest DDD pattern improvements
- `/yaml-code` - Convert YAML domain specs to Java implementations
- `/pr-ddd` - Auto-generate PRs with full DDD documentation

### Prompt Templates
The `.github/prompts/` directory contains specialized templates:
- `ddd-code.prompts.md` - Code generation specifications
- `ddd-validation.prompt.md` - Architecture validation rules
- `ddd-test-generator.prompt.md` - Testing strategy patterns
- Domain-specific prompts in `prompts/ddd/` for design workflows

## Build and Development Workflow

### Project Generation
```bash
# Create new DDD project with proper structure
./scripts/create-ddd-project.sh -p com.company.ecommerce -n ecommerce-service
```

### Code Validation
```bash
# Validate DDD architecture compliance
./scripts/validate-ddd.sh
# Checks: package structure, Spring annotations, compilation, naming conventions
```

### Automated Workflows
The GitHub Actions workflow (`ddd-pr-generator.yml`) provides:
- Complete DDD code generation from specifications
- Automatic test suite generation  
- Code quality validation and compilation checks
- PR creation with detailed documentation and change summaries

## Technology Stack Standards

- **Framework**: Spring Boot 2.7+ with standard annotations
- **ORM**: MyBatis-Plus 3.x for repository implementations
- **Testing**: JUnit 5, Mockito, Spring Boot Test, TestContainers
- **Build**: Maven (primary), Gradle support
- **Java**: 8+ with Lombok annotations for boilerplate reduction

## Code Quality Standards

### Java Best Practices
- Use **Lombok** annotations to reduce boilerplate code (e.g., `@Data`, `@Value`, `@Builder`)
- Leverage **Streams API** and lambda expressions for collection processing
- Favor **Immutability** and `Optional<T>` over null returns
- Apply **Method References** where appropriate for cleaner functional style

### DDD Validation Rules
- Aggregates maintain business invariants through private validation methods
- Domain services contain stateless business logic that doesn't belong to specific aggregates
- Application services are thin orchestrators - no business logic
- Repository interfaces are domain-focused, implementations are infrastructure concerns

### Testing Strategy
```
E2E Tests (5%)     - API endpoints, complete scenarios
Integration (25%)  - Repository layer, database interactions  
Unit Tests (70%)   - Domain logic, business rules, aggregates
```

## Development Guidelines

### When Adding New Features
1. Start with domain modeling - identify aggregates, entities, value objects
2. Define repository interfaces in domain layer
3. Create application services for use case orchestration
4. Implement infrastructure (repositories, mappers)
5. Add REST controllers in interfaces layer
6. Generate comprehensive tests with `/ddd-test`

### When Refactoring
- Use `/ddd-validate` to check architecture compliance
- Use `/ddd-refactor` for DDD-specific improvement suggestions
- Ensure aggregate boundaries remain clean and focused
- Validate that business logic stays in domain layer

### Common TODO Patterns
Generated code includes strategic TODOs for:
- `// TODO: Implement specific business rules`
- `// TODO: Add domain event publishing`
- `// TODO: Validate business constraints`
- These guide developers to complete critical business logic while maintaining DDD patterns

## File Organization

- `docs/contexts/` - DDD design documentation with context mappings
- `examples/user-management/` - Complete reference implementation
- `scripts/` - Development automation tools
- YAML specs in `docs/yaml/` drive code generation workflows

The workspace emphasizes **architecture-first development** where design documents and specifications drive automated code generation, ensuring consistency between documentation and implementation.

## MyBatis-Plus SQL Development Standards

### Repository Implementation Patterns

#### Type-Safe Query Building
```java
// Preferred: Use LambdaQueryWrapper for type-safe queries
@Repository
public class UserRepositoryImpl implements UserRepository {
    
    @Autowired
    private UserMapper userMapper;
    
    @Override
    public Optional<UserAggregate> findActiveUsersByEmail(Email email) {
        LambdaQueryWrapper<UserEntity> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(UserEntity::getEmail, email.getValue())
               .eq(UserEntity::getStatus, UserStatus.ACTIVE.name())
               .eq(UserEntity::getDeleted, false);
        UserEntity entity = userMapper.selectOne(wrapper);
        return Optional.ofNullable(entity).map(this::toDomainModel);
    }
}
```

#### Complex Query Patterns
```java
// For complex business queries, use XML mappers or annotations
@Mapper
public interface OrderMapper extends BaseMapper<OrderEntity> {
    
    // Multi-table join with aggregation
    @Select("""
        SELECT o.*, COUNT(oi.id) as item_count, SUM(oi.amount) as total_amount
        FROM orders o
        LEFT JOIN order_items oi ON o.id = oi.order_id
        WHERE o.customer_id = #{customerId}
        AND o.status IN 
        <foreach collection="statuses" item="status" open="(" close=")" separator=",">
            #{status}
        </foreach>
        GROUP BY o.id
        ORDER BY o.created_time DESC
        """)
    List<OrderSummaryDTO> findOrderSummaryByCustomer(
        @Param("customerId") Long customerId, 
        @Param("statuses") List<String> statuses
    );
}
```

#### Pagination and Performance
```java
// Use MyBatis-Plus Page for efficient pagination
@Service
public class ProductQueryService {
    
    public Page<ProductDTO> searchProducts(ProductSearchCriteria criteria, int page, int size) {
        Page<ProductEntity> pageParam = new Page<>(page, size);
        
        LambdaQueryWrapper<ProductEntity> wrapper = new LambdaQueryWrapper<>();
        wrapper.like(StringUtils.isNotBlank(criteria.getName()), 
                    ProductEntity::getName, criteria.getName())
               .between(criteria.getPriceMin() != null && criteria.getPriceMax() != null,
                       ProductEntity::getPrice, criteria.getPriceMin(), criteria.getPriceMax())
               .in(CollectionUtils.isNotEmpty(criteria.getCategories()),
                  ProductEntity::getCategoryId, criteria.getCategories())
               .orderByDesc(ProductEntity::getCreatedTime);
               
        Page<ProductEntity> result = productMapper.selectPage(pageParam, wrapper);
        return result.convert(this::toDTO);
    }
}
```

### Entity Design Best Practices

#### Value Object Mapping
```java
@Data
@TableName("t_user")
public class UserEntity {
    @TableId(type = IdType.ASSIGN_ID)  // Use snowflake ID for distributed systems
    private Long id;
    
    @TableField("email_address")
    private String email;  // Store value object as primitive
    
    @TableField("user_status")
    @EnumValue  // Map enum to database value
    private UserStatus status;
    
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdTime;
    
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedTime;
    
    @Version  // Optimistic locking
    private Integer version;
    
    @TableLogic(value = "0", delval = "1")
    private Integer deleted;
}
```

#### Custom Type Handlers
```java
// For complex value objects that need JSON storage
@Component
public class MoneyTypeHandler extends BaseTypeHandler<Money> {
    
    @Override
    public void setNonNullParameter(PreparedStatement ps, int i, Money parameter, JdbcType jdbcType) throws SQLException {
        ps.setString(i, parameter.toJsonString());
    }
    
    @Override
    public Money getNullableResult(ResultSet rs, String columnName) throws SQLException {
        String json = rs.getString(columnName);
        return StringUtils.isBlank(json) ? null : Money.fromJsonString(json);
    }
    
    // Additional getNullableResult methods...
}
```

### SQL Performance Guidelines

#### Efficient Query Patterns
1. **Use Selective Queries**: Only select needed columns using `select()` method
2. **Index Awareness**: Ensure WHERE clauses use indexed columns
3. **Batch Operations**: Use `saveBatch()` and `updateBatchById()` for bulk operations
4. **Connection Pooling**: Configure proper connection pool (HikariCP) settings

#### Query Optimization Examples
```java
// Good: Selective column retrieval
LambdaQueryWrapper<UserEntity> wrapper = new LambdaQueryWrapper<>();
wrapper.select(UserEntity::getId, UserEntity::getName, UserEntity::getEmail)
       .eq(UserEntity::getStatus, UserStatus.ACTIVE);

// Good: Using exists for presence checks instead of count
boolean hasActiveOrders = orderMapper.exists(
    new LambdaQueryWrapper<OrderEntity>()
        .eq(OrderEntity::getCustomerId, customerId)
        .eq(OrderEntity::getStatus, OrderStatus.ACTIVE)
);

// Good: Batch processing for large datasets
@Transactional
public void processLargeDataset(List<UserEntity> users) {
    int batchSize = 1000;
    for (int i = 0; i < users.size(); i += batchSize) {
        List<UserEntity> batch = users.subList(i, Math.min(i + batchSize, users.size()));
        userMapper.insertBatch(batch);
    }
}
```

### Transaction and Domain Event Integration

#### Repository Transaction Boundaries
```java
@Repository
@Transactional(readOnly = true)  // Default to read-only
public class OrderRepositoryImpl implements OrderRepository {
    
    @Override
    @Transactional  // Override for write operations
    public void save(OrderAggregate order) {
        OrderEntity entity = toEntity(order);
        
        if (entity.getId() == null) {
            orderMapper.insert(entity);
            order.setId(new OrderId(entity.getId()));
        } else {
            orderMapper.updateById(entity);
        }
        
        // Save related entities
        saveOrderItems(order.getOrderItems(), entity.getId());
        
        // Collect and publish domain events after persistence
        publishDomainEvents(order.getDomainEvents());
        order.clearDomainEvents();
    }
}
```

### Common SQL Anti-Patterns to Avoid

❌ **Don't**: Use string concatenation for dynamic queries
```java
// WRONG - SQL injection risk
String sql = "SELECT * FROM users WHERE name = '" + userName + "'";
```

✅ **Do**: Use MyBatis-Plus wrappers or parameterized queries
```java
// CORRECT - Safe and type-safe
LambdaQueryWrapper<UserEntity> wrapper = new LambdaQueryWrapper<>();
wrapper.eq(UserEntity::getName, userName);
```

❌ **Don't**: Load all data and filter in application
```java
// WRONG - Performance killer
List<UserEntity> allUsers = userMapper.selectList(null);
return allUsers.stream().filter(u -> u.getAge() > 18).collect(toList());
```

✅ **Do**: Filter at database level
```java
// CORRECT - Database-level filtering
LambdaQueryWrapper<UserEntity> wrapper = new LambdaQueryWrapper<>();
wrapper.gt(UserEntity::getAge, 18);
return userMapper.selectList(wrapper);
```
# GitHub Copilot Instructions - DDD Code Generation Workspace

## Project Overview

This is a **DDD (Domain-Driven Design) AI-assisted development workspace** specialized in generating enterprise-grade Spring Boot applications from YAML domain specifications. The workspace focuses on strict five-layer DDD architecture with automated code generation, validation, and testing.

**Core Mission**: Convert DDD design documents (YAML format) into complete, compilable, and testable Spring Boot projects following strict architectural patterns.

## Architecture Principles

### Five-Layer DDD Structure (Mandatory)

```
com.example.project/
├── interfaces/          # REST controllers, DTOs (RO/VO)
├── application/         # Use case orchestration, NO business logic
├── domain/             # Aggregates, entities, value objects, domain services
│   ├── model/          # Aggregate roots (e.g., PaymentAggregate)
│   ├── entity/         # Domain entities (e.g., PaymentTransactionEntity)
│   └── repository/     # Repository interfaces (IXxxRepository)
├── infrastructure/     # Technical implementations
│   ├── mapper/         # MyBatis-Plus Mappers
│   └── repository/     # Repository implementations (XxxRepositoryImpl)
└── shared/            # Cross-cutting concerns, exceptions, events
```

**Critical Rules**:
- **Entity/Model Separation**: `entity` package = data objects, `model` package = aggregate roots
- **Repository Split**: Interfaces in `domain/repository/`, implementations in `infrastructure/repository/`
- **Interface Naming**: Repository interfaces use `I` prefix (e.g., `IPaymentRepository`)
- **No Direct Entity Exposure**: Controllers use RO/VO, never expose domain entities directly

## Technology Stack (Non-Negotiable)

| Component | Version | Purpose |
|-----------|---------|---------|
| Spring Boot | 2.7.x | Main framework |
| MyBatis-Plus | 3.5.x | ORM with lambda queries |
| Lombok | 1.18.x | Boilerplate reduction |
| MySQL | 8.x | Primary database |
| JUnit 5 | 5.8.x | Testing framework |

**Java 8 Conventions**:
- Use Lombok annotations: `@Data`, `@Builder`, `@Slf4j`, `@Value` (for immutables)
- Prefer `Optional<T>` over null returns
- Use Streams API and method references for collections
- No magic numbers - use constants or enums

## Input Format: YAML Domain Specifications

The workspace expects DDD designs in YAML format (see `docs/payment.yml` for reference):

```yaml
project:
  basePackage: com.example.project
  name: project-service

aggregates:
  - name: Payment
    description: Payment aggregate root
    fields:
      - name: id
        type: String
        description: Unique identifier
    behaviors:
      - name: execute
        description: Execute payment
        params: [amount:BigDecimal]
    rules:
      - description: Amount must be positive

domainServices:
  - name: PaymentValidationService
    methods:
      - name: validateAmount
```

**Key Patterns**:
- `aggregates` → Generate aggregate roots in `domain/model/`
- `behaviors` → Methods on aggregate roots (mark with `// TODO: Implement business logic`)
- `rules` → Business invariants to enforce in domain layer
- `domainServices` → Cross-aggregate logic, NOT in entities

## Code Generation Standards

### 1. Aggregate Root Example

```java
@Data
@Builder
public class PaymentAggregate {
    private String id;
    private BigDecimal amount;
    private PaymentStatus status;
    
    public void execute() {
        // TODO: Implement business logic - validate amount > 0
        // TODO: Implement business logic - check status is PENDING
        // TODO: Implement business logic - transition to PROCESSING
    }
}
```

### 2. Repository Pattern (Interface/Implementation Split)

```java
// domain/repository/IPaymentRepository.java
public interface IPaymentRepository extends IService<PaymentEntity> {
    Optional<PaymentAggregate> findById(String id);
}

// infrastructure/repository/PaymentRepositoryImpl.java
@Service
public class PaymentRepositoryImpl extends ServiceImpl<PaymentMapper, PaymentEntity> 
    implements IPaymentRepository {
    // Implementation using MyBatis-Plus
}
```

### 3. MyBatis-Plus Usage

```java
@Mapper
public interface PaymentMapper extends BaseMapper<PaymentEntity> {
    // Prefer lambda queries
    default List<PaymentEntity> findByStatus(PaymentStatus status) {
        LambdaQueryWrapper<PaymentEntity> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(PaymentEntity::getStatus, status);
        return selectList(wrapper);
    }
}
```

### 4. Controller DTO Pattern

```java
@RestController
@RequestMapping("/api/payments")
@Validated
public class PaymentController {
    public PaymentVO createPayment(@Valid @RequestBody PaymentCreateRO request) {
        // TODO: Convert RO to domain object
        // TODO: Call application service
        // TODO: Convert domain result to VO
    }
}
```

## Critical Workflows

### Code Generation Workflow

1. **Read YAML**: Parse `docs/*.yml` for domain specifications
2. **Generate Layers**: Create all five layers with correct package structure
3. **Add TODOs**: Mark all business logic methods with `// TODO: Implement business logic`
4. **Validate**: Ensure code compiles and follows naming conventions
5. **Generate Tests**: Create unit tests for aggregates and repositories

### Build & Validation

```bash
# Maven build (for backend/ projects)
mvn clean compile test

# Validation script (from project root)
./scripts/validate-ddd.sh
```

**After ANY code generation**:
1. Verify package structure matches five-layer architecture
2. Check Repository interfaces have `I` prefix
3. Ensure no entities exposed in controllers
4. Confirm all business methods have TODO markers

## Design Documents as Truth

### Key Reference Files

- `docs/payment.yml` - Complete YAML example for payment domain
- `docs/Glossary.md` - Ubiquitous language and term definitions
- `支付模块需求设计.md` - Business requirements (Chinese)
- `.github/instructions/*.instructions.md` - Code generation rules

**When generating code**:
1. Check `Glossary.md` for correct English/Chinese term mappings
2. Use exact terminology from glossary (e.g., "Payment" not "PaymentOrder")
3. Follow aggregate boundaries defined in requirements docs
4. Respect status enums and transitions defined in glossary

## Common Patterns & Anti-Patterns

### ✅ DO

- Use aggregate roots to enforce business invariants
- Keep aggregates small (reference other aggregates by ID)
- Put business logic in domain layer, NOT application services
- Use domain events for cross-aggregate communication
- Generate complete file trees with all necessary imports

### ❌ DON'T

- Expose entities directly in REST controllers
- Put business logic in application services
- Create bidirectional aggregate references
- Generate incomplete code without package declarations

## Instruction File System

This workspace uses tiered instructions in `.github/instructions/`:

- **ddd.instructions.md** - DDD architecture rules (applies to `backend/**/*.java`)
- **java8.instructions.md** - Java 8 & Lombok conventions (applies to `**/*.java`)
- **springboot.instructions.md** - Spring Boot patterns (applies to `backend/**/*.java`)

**These rules are automatically enforced** - read them before generating any code.

## Project-Specific Conventions

### Payment Domain Example

The `docs/payment.yml` demonstrates this project's DDD approach:
- **Aggregates**: Payment, PaymentTransaction
- **Domain Services**: PaymentExecutionService, RefundService, CreditRepaymentService
- **Status Management**: Payment has separate `payment_status` and `refund_status`
- **Business Rule**: `actual_amount = paid_amount - refunded_amount` (enforced in aggregate)

**Key Insight**: Credit repayment is just a Payment with `payment_type = CREDIT_REPAYMENT`, not a separate aggregate. This shows the project's preference for simplicity over premature abstraction.

## Testing Strategy

```java
// Unit test for aggregate behavior
@Test
void execute_shouldTransitionToProcessing_whenStatusIsPending() {
    PaymentAggregate payment = PaymentAggregate.builder()
        .status(PaymentStatus.PENDING)
        .amount(new BigDecimal("100.00"))
        .build();
    
    payment.execute();
    
    assertThat(payment.getStatus()).isEqualTo(PaymentStatus.PROCESSING);
}
```

**Test Coverage Targets**:
- Domain layer: 95%+ (business logic is critical)
- Application layer: 90%+
- Infrastructure layer: 85%+

## Getting Started Checklist

When working on this project:
- [ ] Read `README.md` for project overview
- [ ] Review `docs/payment.yml` to understand YAML input format
- [ ] Check `docs/Glossary.md` for terminology
- [ ] Read `.github/instructions/*.instructions.md` for code rules
- [ ] Understand five-layer architecture (no shortcuts!)
- [ ] Remember: Repository interfaces get `I` prefix
- [ ] Verify entity/model separation before submitting code

---

**Version**: 1.0 | **Last Updated**: 2025-01-15 | **Maintained by**: DDD Architecture Team

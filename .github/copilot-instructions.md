# AI Agent Instructions - DDD Payment Module Workspace

## Project Overview
This workspace generates Spring Boot DDD (Domain-Driven Design) code from YAML specifications for a B2B industrial equipment trading payment module. The system handles complex payment scenarios including batch payments, partial payments, refunds, and credit repayments.

**Core Mission**: Transform DDD design documents (YAML) into compilable Spring Boot projects with strict five-layer architecture.

## Architecture: Non-Negotiable Five Layers

```
com.bytz.cms.payment/
├── interfaces/          # REST controllers, DTOs (RO=Request, VO=Response)
├── application/         # Use case orchestration ONLY - NO business logic
├── domain/             # Core business logic
│   ├── model/          # Aggregate roots (*Aggregate classes)
│   ├── entity/         # Domain entities (*Entity classes)
│   ├── repository/     # Repository interfaces (I*Repository)
│   └── *DomainService  # Cross-aggregate domain logic
├── infrastructure/     # Technical implementations
│   ├── mapper/         # MyBatis-Plus Mappers + optional XML
│   └── repository/     # Repository implementations (*RepositoryImpl)
└── shared/            # Exceptions, events, utilities
```

**Critical Naming Rules**:
- Repository interfaces: `I` prefix (e.g., `IPaymentRepository`) in `domain/repository/`
- Repository implementations: `*RepositoryImpl` in `infrastructure/repository/`
- Aggregate roots: `*Aggregate` suffix in `domain/model/`
- Domain entities: `*Entity` suffix in `domain/entity/`

## Input: YAML Domain Specifications

The primary source of truth is `docs/payment.yml`. Key patterns:

```yaml
project:
  basePackage: com.bytz.cms.payment
  name: payment-service

aggregates:
  - name: Payment  # Generates PaymentAggregate
    fields:
      - name: id
        type: String
    behaviors:
      - name: create
        params: [orderId:String, amount:BigDecimal]
    rules:
      - description: Payment amount must be positive

domainServices:
  - name: PaymentExecutionService
    methods:
      - name: executeUnifiedPayment
```

**YAML → Code Mapping**:
- `aggregates[].name` → `{name}Aggregate` class in `domain/model/`
- `behaviors[].name` → Methods in aggregate roots (mark with `// TODO: Implement business logic`)
- `domainServices[].name` → `{name}` class in `domain/`

## Technology Stack (Fixed)

| Component | Version | Usage |
|-----------|---------|-------|
| Spring Boot | 2.7.x | Framework |
| MyBatis-Plus | 3.5.x | ORM with lambda queries |
| Lombok | Latest | `@Data`, `@Builder`, `@Slf4j` |
| MySQL | 8.x | Database |
| JUnit 5 | 5.8.x | Testing |

## Business Domain Knowledge

### Payment Scenarios (from requirements)
1. **Unified Payment Processing**: Single or batch payment using same flow
2. **Partial Payment**: Pay portions of a payment order multiple times
3. **Credit Repayment**: Special payment type (`CREDIT_REPAYMENT`) but follows standard payment flow
4. **Refund Execution**: Select specific transaction(s) for refund

### Key Domain Concepts (from Glossary.md)
- **Payment**: Aggregate root managing payment lifecycle
- **PaymentTransaction**: Entity recording individual payment/refund operations
- **Batch Payment**: Multiple payments processed with single channel transaction
- **Payment Status**: `UNPAID → PAYING → PARTIAL_PAID/PAID/FAILED`
- **Refund Status**: `NO_REFUND → REFUNDING → PARTIAL_REFUNDED/FULL_REFUNDED`

### Critical Business Rules
- `actualAmount = paidAmount - refundedAmount`
- Payment amounts use `BigDecimal` with 6 decimal places
- Credit repayment is a payment type, not a separate flow
- Refund cannot exceed `paidAmount - refundedAmount`

## Code Generation Workflow

### 1. Read YAML Specification
```bash
# Always start here
cat docs/payment.yml
```

### 2. Generate Five-Layer Structure
For each aggregate in YAML:
1. **Domain Layer**:
   - Create `{name}Aggregate` in `domain/model/`
   - Create entities in `domain/entity/` if needed
   - Create `I{name}Repository` interface in `domain/repository/`
2. **Infrastructure Layer**:
   - Create `{name}Mapper` extending `BaseMapper<T>`
   - Create `{name}RepositoryImpl` implementing interface
3. **Application Layer**:
   - Create `{name}ApplicationService` for use case orchestration
4. **Interface Layer**:
   - Create `{name}Controller` with `@RestController`
   - Create DTOs: `{name}CreateRO`, `{name}VO`
5. **Shared Layer**:
   - Create domain events if specified

### 3. Mark Incomplete Logic
Add `// TODO: Implement business logic` for:
- All behavior methods in aggregates
- Domain service methods
- Application service orchestration

### 4. Validate & Build
```bash
cd backend/
mvn clean compile test
```

## Ubiquitous Language (Glossary.md)

Use **exact** English terms from glossary:

| 中文 | English | Class/Field Name |
|------|---------|------------------|
| 支付单 | Payment | `PaymentAggregate` |
| 支付流水 | Payment Transaction | `PaymentTransactionEntity` |
| 已支付金额 | Paid Amount | `paidAmount` |
| 待支付金额 | Pending Amount | `pendingAmount` (calculated) |
| 合并支付 | Batch Payment | `executeBatchPayment()` |
| 信用还款 | Credit Repayment | `PaymentType.CREDIT_REPAYMENT` |

**Never deviate from these terms** - they're contractual with the design docs.

## Development Patterns

### MyBatis-Plus Lambda Queries
```java
// Prefer this over raw SQL
wrapper.eq(PaymentEntity::getStatus, PaymentStatus.UNPAID)
       .ge(PaymentEntity::getCreatedTime, startTime);
```

### Repository Pattern
```java
// domain/repository/IPaymentRepository.java
public interface IPaymentRepository extends IService<PaymentEntity> {
    Optional<PaymentAggregate> findById(String id);
}

// infrastructure/repository/PaymentRepositoryImpl.java
@Service
public class PaymentRepositoryImpl 
    extends ServiceImpl<PaymentMapper, PaymentEntity> 
    implements IPaymentRepository {
    // Implementation
}
```

### Controller DTOs
```java
@RestController
@RequestMapping("/api/payments")
public class PaymentController {
    public PaymentVO create(@Valid @RequestBody PaymentCreateRO request) {
        // TODO: Convert RO → Domain → VO
    }
}
```

## Design Documents as Authority

**Before writing any code**, consult:
1. `docs/payment.yml` - Technical spec (structure, fields, methods)
2. `docs/Glossary.md` - Terminology (English/Chinese mappings)
3. `支付模块需求设计.md` - Business requirements
4. `.github/instructions/ddd.instructions.md` - Code generation rules

**If YAML and requirements conflict**, ask the user for clarification.

## Common Pitfalls

❌ **DON'T**:
- Expose entities directly in controllers (use RO/VO)
- Put business logic in application services
- Use "PaymentOrder" instead of "Payment"
- Generate incomplete package declarations

✅ **DO**:
- Use aggregate roots to enforce business rules
- Keep aggregates small (reference others by ID)
- Generate complete file trees with imports
- Follow `I*Repository` naming convention

## Quick Validation Checklist

After generating code:
- [ ] Package structure matches five layers
- [ ] Repository interfaces have `I` prefix
- [ ] No entities in controller parameters
- [ ] All business methods have `// TODO` markers
- [ ] Terms match Glossary.md exactly
- [ ] Code compiles with `mvn clean compile`

## Instruction Files in Use

- `.github/instructions/ddd.instructions.md` - DDD architecture rules
- `.github/instructions/java8.instructions.md` - Java 8 & Lombok conventions
- `.github/instructions/springboot.instructions.md` - Spring Boot patterns

These are **automatically enforced** - read them before generating code.

---

**Version**: 1.0 | **Updated**: 2025-01-15 | **Based on**: Payment Module v1.5

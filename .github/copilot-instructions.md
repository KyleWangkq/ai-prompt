# AI Agent Instructions - DDD Payment Module Workspace

## Project Overview
This workspace generates Spring Boot DDD (Domain-Driven Design) code from YAML specifications for a B2B industrial equipment trading payment module. The system handles complex payment scenarios including batch payments, partial payments, refunds, and credit repayments.

**Core Mission**: Transform DDD design documents (YAML) into compilable Spring Boot projects with strict five-layer architecture.

## Architecture: Non-Negotiable Five Layers

```
com.bytz.cms.payment/
├── interfaces/          # REST controllers, DTOs (RO=Request, VO=Response)
│   └── model/           # *RO, *VO
├── application/         # Use case orchestration ONLY - NO business logic
│   └── assembler/       # MapStruct assemblers (*Assembler)
├── domain/             # Core business logic
│   ├── model/          # Aggregate roots (*Aggregate classes)
│   ├── entity/         # Domain entities (*Entity classes)
│   ├── valueobject/    # Value objects (*ValueObject)
│   ├── enums/          # Enumerations (e.g., PaymentStatus)
│   ├── command/        # Command objects for behaviors (*Command)
│   ├── repository/     # Repository interfaces (I*Repository)
│   └── *DomainService  # Cross-aggregate domain logic
├── infrastructure/     # Technical implementations
│   ├── mapper/         # MyBatis-Plus Mappers + optional XML
│   └── repository/     # Repository implementations (*RepositoryImpl)
└── shared/            # Exceptions, events, utilities
    ├── exception/      # Business exceptions
    └── model/          # Domain events (*Event)
```

**Critical Naming Rules**:
- Repository interfaces: `I` prefix (e.g., `IPaymentRepository`) in `domain/repository/`
- Repository implementations: `*RepositoryImpl` in `infrastructure/repository/`
- Aggregate roots: `*Aggregate` suffix in `domain/model/`
- Domain entities: `*Entity` suffix in `domain/entity/`
- Value objects: `*ValueObject` in `domain/valueobject/`
- Commands: `*Command` in `domain/command/` (e.g., `CreatePaymentCommand`)
- Enums: PascalCase names in `domain/enums/` (e.g., `PaymentStatus`)
- DTOs: `*RO` (request) and `*VO` (response) under `interfaces/model/`
- Domain events: `*Event` under `shared/model/`
- Mappers: `*Mapper` under `infrastructure/mapper/`
- Application assemblers: `*Assembler` under `application/assembler/` (MapStruct `@Mapper(componentModel = "spring")`)

## Input: YAML Domain Specifications

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
| MapStruct | 1.5.x | DTO ↔ Domain conversion in application layer (annotation processor required) |
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
   - Create value objects in `domain/valueobject/` when modeling concepts with identity-less attributes
   - Create commands in `domain/command/` for behaviors having >3 parameters
   - Create `I{name}Repository` interface in `domain/repository/`
2. **Infrastructure Layer**:
   - Create `{name}Mapper` extending `BaseMapper<T>`
   - Create `{name}RepositoryImpl` implementing interface
3. **Application Layer**:
   - Create `{name}ApplicationService` for use case orchestration
   - Create `{name}Assembler` under `application/assembler/` using MapStruct for RO/VO ↔ Domain conversions
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
        // Use MapStruct assembler in application layer for RO ↔ Domain ↔ VO conversion
        // TODO: Invoke ApplicationService which uses {name}Assembler
    }
}
```

### Parameter Encapsulation Rule
**When a method has more than 3 parameters, encapsulate them into a request object**:
- **Domain Layer Methods**: If aggregate behavior or domain service methods have >3 parameters, create a command object (e.g., `CreatePaymentCommand`)
- **Application Service Methods**: Encapsulate >3 parameters into a dedicated request object
- **Benefits**: Improves code readability, maintainability, and supports future parameter evolution

```java
// ❌ DON'T: Too many parameters
public PaymentAggregate create(String orderId, BigDecimal amount, 
    String paymentType, String channelCode, String userId) {
    // ...
}

// ✅ DO: Use command object
public PaymentAggregate create(CreatePaymentCommand command) {
    // command contains: orderId, amount, paymentType, channelCode, userId
}
```

## MapStruct-Based Conversion (Application Assembler)
- Location: `application/assembler/`
- Naming: `{Aggregate}Assembler` (e.g., `PaymentAssembler`)
- Annotation: `@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)`
- Responsibility: Perform RO/VO ↔ Domain conversions; no business logic
- Common methods:
  - `CreatePaymentCommand toCreateCommand(PaymentCreateRO ro)`
  - `PaymentVO toVO(PaymentAggregate aggregate)`
  - Batch variants: `List<PaymentVO> toVOs(List<PaymentAggregate> list)`
- Lombok + MapStruct: Keep fields aligned; favor explicit mappings for differing names
- Maven: Include `mapstruct` and `mapstruct-processor` (annotationProcessor)

## Aggregate Design Rules
- One aggregate root per aggregate; enforce invariants inside the aggregate
- Cross-aggregate operations go to `*DomainService`
- Reference other aggregates by identifier (ID) instead of object references
- Do not expose entities/value objects directly outside the domain; use RO/VO via assemblers

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
- Use MapStruct assembler in the application layer for all DTO/domain

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
# AI Agent Guide for this Workspace (DDD Payment Module)

Goal: Turn `docs/payment.yml` into a compilable Spring Boot project with strict five-layer DDD and MapStruct-based DTO/domain conversion.

Big picture architecture (non-negotiable):
com.bytz.cms.payment/
- interfaces/ (REST, DTOs: *RO, *VO)
- application/ (orchestration only)
  - assembler/ (MapStruct {Aggregate}Assembler)
- domain/ (business logic)
  - model/ (*Aggregate), entity/ (*Entity), valueobject/ (*ValueObject), enums/, command/, repository/, *DomainService
- infrastructure/ (tech impl)
  - mapper/ (*Mapper), repository/ (*RepositoryImpl)
- shared/ (exception/, model/ for *Event)

Naming and placement (must follow):
- Repos: domain/repository/I{Name}Repository; impl: infrastructure/repository/{Name}RepositoryImpl
- Aggregates: domain/model/{Name}Aggregate; Entities: domain/entity/{Name}Entity; VOs: domain/valueobject/{Name}ValueObject
- Commands: domain/command/{Verb}{Name}Command when params > 3
- DTOs: interfaces/model/{Name}{Action}RO and {Name}VO
- Events: shared/model/{Name}Event
- Assemblers: application/assembler/{Aggregate}Assembler with @Mapper(componentModel="spring")

YAML → code mapping (docs/payment.yml):
- aggregates[].name → {name}Aggregate with fields/behaviors (mark business logic with // TODO)
- domainServices[].name → {name}DomainService class with declared methods (// TODO)
- enumerations → domain/enums/{Enum}.java
- Always create I{name}Repository + {name}RepositoryImpl and {name}Mapper

Ubiquitous language: Use exact English terms from `docs/Glossary.md` and YAML (e.g., PaymentAggregate, PaymentTransactionEntity, PaymentStatus, PaymentType.CREDIT_REPAYMENT). Do not invent “PaymentOrder”.

MapStruct assemblers (application/assembler):
- Example methods: toCreateCommand(PaymentCreateRO ro), toVO(PaymentAggregate agg), toVOs(List<PaymentAggregate>)
- Set unmappedTargetPolicy = ReportingPolicy.IGNORE; no business logic inside assemblers.

Patterns and examples:
- MyBatis-Plus lambda queries, not raw SQL:
  wrapper.eq(PaymentEntity::getStatus, PaymentStatus.UNPAID);
- Controller uses ApplicationService + Assembler for RO↔Domain↔VO; no domain types leaked in interfaces.

Developer workflow:
1) Read spec: open `docs/payment.yml` and `docs/Glossary.md`.
2) Generate code under `backend/` with the package tree above; add TODO markers for unimplemented domain logic.
3) Build when backend exists: cd backend && mvn clean compile test.

Acceptance checks before done:
- Package layout and suffix/prefix conventions match exactly
- Application layer has no business logic; cross-aggregate logic in *DomainService
- Amount rules honored in domain (e.g., actualAmount = paidAmount - refundedAmount; refund ≤ paid-refunded)
- MapStruct used for all RO/VO ↔ Domain conversion

Key files to consult:
- `docs/payment.yml` (authoritative spec) • `docs/Glossary.md` (terms) • `.github/instructions/ddd-springboot.instructions.md` (generation rules)

Pitfalls to avoid:
- Exposing entities/VOs outside domain; skipping repository interface; deviating names; mixing JPA-style repos with MyBatis-Plus patterns.
- **Payment Status**: `UNPAID → PAYING → PARTIAL_PAID/PAID/FAILED`

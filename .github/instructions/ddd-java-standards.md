---
applyTo: '**/test11'
---

# DDD Java Spring Boot Development Standards

## Core Architecture Principles

### Layer Separation Rules
- **Domain Layer**: Contains pure business logic, no framework dependencies
- **Application Layer**: Orchestrates domain objects, manages transactions
- **Infrastructure Layer**: Implements technical concerns, external integrations
- **Interface Layer**: Exposes APIs, handles serialization/deserialization

### Dependency Direction
```
Interface → Application → Domain ← Infrastructure
```
- Domain layer has NO dependencies on other layers
- All dependencies point inward toward the domain
- Use dependency inversion for infrastructure concerns

## Domain Layer Standards

### Aggregate Design
```java
// ✅ CORRECT: Rich domain model with encapsulated business logic
@Entity
@Table(name = "orders")
public class Order {
    private OrderId id;
    private CustomerId customerId;
    private Money totalAmount;
    private OrderStatus status;
    private List<OrderItem> items;
    
    // Business method with validation
    public void addItem(Product product, Quantity quantity) {
        validateCanAddItem(product, quantity);
        OrderItem item = new OrderItem(product, quantity);
        this.items.add(item);
        this.totalAmount = calculateTotal();
        addDomainEvent(new OrderItemAddedEvent(this.id, item));
    }
    
    private void validateCanAddItem(Product product, Quantity quantity) {
        if (this.status != OrderStatus.DRAFT) {
            throw new DomainException("Cannot modify confirmed order");
        }
        if (quantity.isZero()) {
            throw new InvalidQuantityException("Quantity must be positive");
        }
    }
}

// ❌ WRONG: Anemic domain model
public class Order {
    private Long id;
    private Long customerId;
    private BigDecimal amount;
    private String status;
    // Only getters and setters - no business logic
}
```

### Value Objects
```java
// ✅ CORRECT: Immutable value object with validation
@Value
@Builder
public class Email {
    private String value;
    
    private Email(String value) {
        validateEmail(value);
        this.value = value;
    }
    
    public static Email of(String email) {
        return new Email(email);
    }
    
    private void validateEmail(String email) {
        if (!email.matches("^[A-Za-z0-9+_.-]+@(.+)$")) {
            throw new InvalidEmailException("Invalid email format");
        }
    }
}

// ❌ WRONG: Mutable value object without validation
public class Email {
    private String value;
    public void setValue(String value) { this.value = value; }
}
```

### Domain Services
```java
// ✅ CORRECT: Stateless domain service for cross-aggregate operations
@Component
public class OrderPricingService {
    
    public Money calculateOrderTotal(List<OrderItem> items, Customer customer) {
        Money subtotal = calculateSubtotal(items);
        Money discount = calculateDiscount(subtotal, customer);
        Money tax = calculateTax(subtotal.subtract(discount));
        return subtotal.subtract(discount).add(tax);
    }
    
    // Pure business logic, no infrastructure dependencies
}

// ❌ WRONG: Domain service with infrastructure dependencies
public class OrderPricingService {
    @Autowired
    private TaxServiceClient taxService; // Infrastructure leak
}
```

## Application Layer Standards

### Application Services
```java
// ✅ CORRECT: Thin orchestration layer
@Service
@Transactional
public class OrderApplicationService {
    
    private final OrderRepository orderRepository;
    private final ProductRepository productRepository;
    private final DomainEventPublisher eventPublisher;
    
    public OrderId createOrder(CreateOrderCommand command) {
        Customer customer = customerRepository.findById(command.getCustomerId())
            .orElseThrow(() -> new CustomerNotFoundException(command.getCustomerId()));
            
        Order order = Order.create(customer.getId());
        
        for (CreateOrderItemCommand itemCmd : command.getItems()) {
            Product product = productRepository.findById(itemCmd.getProductId())
                .orElseThrow(() -> new ProductNotFoundException(itemCmd.getProductId()));
            order.addItem(product, itemCmd.getQuantity());
        }
        
        Order savedOrder = orderRepository.save(order);
        eventPublisher.publishEvents(savedOrder.getDomainEvents());
        
        return savedOrder.getId();
    }
}

// ❌ WRONG: Business logic in application service
@Service
public class OrderApplicationService {
    public void processOrder(Order order) {
        // Business logic should be in domain, not here
        if (order.getAmount().compareTo(BigDecimal.valueOf(1000)) > 0) {
            order.setStatus("REQUIRES_APPROVAL");
        }
    }
}
```

### Command/Query Separation
```java
// ✅ CORRECT: Separate command and query models
public record CreateOrderCommand(
    CustomerId customerId,
    List<CreateOrderItemCommand> items
) {}

public record OrderSummaryQuery(
    Optional<CustomerId> customerId,
    Optional<LocalDate> fromDate,
    Optional<LocalDate> toDate
) {}

// ❌ WRONG: Mixed command/query responsibility
public class OrderService {
    public Order createAndRetrieveOrder(CreateOrderData data) {
        // Mixing command and query operations
    }
}
```

## Infrastructure Layer Standards

### Repository Implementation
```java
// ✅ CORRECT: Clean repository implementation
@Repository
public class OrderRepositoryImpl implements OrderRepository {
    
    private final OrderMapper orderMapper;
    private final OrderItemMapper orderItemMapper;
    
    @Override
    public Optional<Order> findById(OrderId id) {
        return Optional.ofNullable(orderMapper.selectById(id.getValue()))
            .map(this::toDomainModel);
    }
    
    @Override
    public Order save(Order order) {
        OrderEntity entity = toEntity(order);
        if (entity.getId() == null) {
            orderMapper.insert(entity);
            order.setId(OrderId.of(entity.getId()));
        } else {
            orderMapper.updateById(entity);
        }
        saveOrderItems(order.getItems(), entity.getId());
        return order;
    }
    
    private Order toDomainModel(OrderEntity entity) {
        // Mapping logic
    }
}

// ❌ WRONG: Repository with business logic
@Repository
public class OrderRepositoryImpl {
    public Order saveOrder(Order order) {
        // Business validation should be in domain
        if (order.getTotal().isGreaterThan(Money.of(1000))) {
            order.requiresApproval();
        }
        return orderMapper.save(order);
    }
}
```

### Entity Mapping
```java
// ✅ CORRECT: Clean entity mapping with MyBatis-Plus
@Data
@TableName("t_order")
public class OrderEntity {
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;
    
    @TableField("customer_id")
    private Long customerId;
    
    @TableField("total_amount")
    private BigDecimal totalAmount;
    
    @TableField("order_status")
    private String status;
    
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdTime;
    
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedTime;
    
    @Version
    private Integer version;
    
    @TableLogic
    private Boolean deleted;
}
```

## Interface Layer Standards

### REST Controllers
```java
// ✅ CORRECT: Thin controller with proper error handling
@RestController
@RequestMapping("/api/orders")
@Validated
public class OrderController {
    
    private final OrderApplicationService orderService;
    private final OrderQueryService orderQueryService;
    
    @PostMapping
    public ResponseEntity<OrderResponse> createOrder(
            @Valid @RequestBody CreateOrderRequest request) {
        
        CreateOrderCommand command = toCommand(request);
        OrderId orderId = orderService.createOrder(command);
        OrderResponse response = orderQueryService.getOrderResponse(orderId);
        
        return ResponseEntity.status(HttpStatus.CREATED)
            .location(URI.create("/api/orders/" + orderId.getValue()))
            .body(response);
    }
    
    @ExceptionHandler(DomainException.class)
    public ResponseEntity<ErrorResponse> handleDomainException(DomainException ex) {
        return ResponseEntity.badRequest()
            .body(new ErrorResponse(ex.getMessage(), "DOMAIN_ERROR"));
    }
}

// ❌ WRONG: Controller with business logic
@RestController
public class OrderController {
    @PostMapping("/orders")
    public Order createOrder(@RequestBody OrderData data) {
        // Business logic should not be in controller
        Order order = new Order();
        if (data.getAmount() > 1000) {
            order.setRequiresApproval(true);
        }
        return orderRepository.save(order);
    }
}
```

## Testing Standards

### Domain Testing
```java
// ✅ CORRECT: Rich domain tests
class OrderTest {
    
    @Test
    void should_add_item_when_order_is_draft() {
        // Given
        Order order = Order.create(CustomerId.of(1L));
        Product product = ProductTestData.createProduct();
        Quantity quantity = Quantity.of(2);
        
        // When
        order.addItem(product, quantity);
        
        // Then
        assertThat(order.getItems()).hasSize(1);
        assertThat(order.getTotalAmount()).isEqualTo(product.getPrice().multiply(quantity));
        assertThat(order.getDomainEvents()).hasSize(1);
        assertThat(order.getDomainEvents().get(0)).isInstanceOf(OrderItemAddedEvent.class);
    }
    
    @Test
    void should_throw_exception_when_adding_item_to_confirmed_order() {
        // Given
        Order order = Order.create(CustomerId.of(1L));
        order.confirm();
        Product product = ProductTestData.createProduct();
        
        // When & Then
        assertThatThrownBy(() -> order.addItem(product, Quantity.of(1)))
            .isInstanceOf(DomainException.class)
            .hasMessage("Cannot modify confirmed order");
    }
}
```

### Integration Testing
```java
// ✅ CORRECT: Repository integration test
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Testcontainers
class OrderRepositoryImplTest {
    
    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:13")
            .withDatabaseName("testdb")
            .withUsername("test")
            .withPassword("test");
    
    @Test
    void should_save_and_retrieve_order() {
        // Given
        Order order = OrderTestData.createOrder();
        
        // When
        Order savedOrder = orderRepository.save(order);
        Optional<Order> retrievedOrder = orderRepository.findById(savedOrder.getId());
        
        // Then
        assertThat(retrievedOrder).isPresent();
        assertThat(retrievedOrder.get().getTotalAmount()).isEqualTo(order.getTotalAmount());
    }
}
```

## Code Quality Rules

### Naming Conventions
```java
// ✅ CORRECT: Clear, business-focused naming
public class CustomerRegistrationService {
    public CustomerId registerNewCustomer(RegisterCustomerCommand command) {
        // Implementation
    }
}

// Domain events
public class CustomerRegisteredEvent extends DomainEvent {
    private final CustomerId customerId;
    private final Email email;
}

// Value objects
public class Money {
    public Money add(Money other) { /* */ }
    public boolean isGreaterThan(Money other) { /* */ }
}

// ❌ WRONG: Technical, non-business naming
public class CustomerService {
    public Long insertCustomer(CustomerData data) { /* */ }
}
```

### Exception Handling
```java
// ✅ CORRECT: Domain-specific exceptions
public class InsufficientStockException extends DomainException {
    public InsufficientStockException(ProductId productId, Quantity requested, Quantity available) {
        super(String.format("Insufficient stock for product %s. Requested: %s, Available: %s", 
            productId, requested, available));
    }
}

// ❌ WRONG: Generic exceptions
public class ServiceException extends Exception {
    // Too generic, no business context
}
```

### Lombok Usage
```java
// ✅ CORRECT: Appropriate Lombok usage
@Value  // For immutable value objects
@Builder
public class Address {
    String street;
    String city;
    String postalCode;
}

@Data  // For entities (sparingly)
@EqualsAndHashCode(callSuper = true)
public class OrderEntity extends BaseEntity {
    // Fields
}

// ❌ WRONG: Overusing Lombok on domain objects
@Data  // Breaks encapsulation on aggregates
public class Order {
    private List<OrderItem> items;  // Should be protected from direct modification
}
```

## Package Structure
```
com.company.domain.model.order/
├── Order.java                 // Aggregate root
├── OrderId.java              // Value object
├── OrderItem.java            // Entity
├── OrderStatus.java          // Enum
├── OrderRepository.java      // Repository interface
└── OrderCreatedEvent.java    // Domain event

com.company.application.order/
├── OrderApplicationService.java
├── CreateOrderCommand.java
└── CreateOrderCommandHandler.java

com.company.infrastructure.order/
├── OrderRepositoryImpl.java
├── OrderEntity.java
└── OrderMapper.java

com.company.interfaces.order/
├── OrderController.java
├── CreateOrderRequest.java
└── OrderResponse.java
```

## Code Review Checklist

### Domain Layer Review
- [ ] Business logic is in domain objects, not services
- [ ] Aggregates maintain invariants through encapsulation
- [ ] Value objects are immutable and validated
- [ ] Domain events are emitted for significant business events
- [ ] No framework dependencies in domain layer

### Application Layer Review
- [ ] Application services are thin orchestrators
- [ ] Transactions are properly managed
- [ ] Commands and queries are separated
- [ ] Domain events are published after successful transactions

### Infrastructure Layer Review
- [ ] Repository implementations only handle persistence
- [ ] Entity mapping is clean and efficient
- [ ] Database operations use proper transaction boundaries
- [ ] External service integrations are properly abstracted

### Interface Layer Review
- [ ] Controllers are thin and delegate to application services
- [ ] Request/response DTOs are separate from domain objects
- [ ] Proper HTTP status codes and error handling
- [ ] API documentation is comprehensive

Remember: **The domain is the heart of the application. All other layers serve the domain, not the other way around.**
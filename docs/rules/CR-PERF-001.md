# Rule: CR-PERF-001 - N+1 Database Query Anti-Pattern

## Metadata
- **Severity**: MEDIUM
- **Category**: Performance / Database
- **CWE**: N/A (Performance Degradation)
- **Languages**: Java (JPA/Hibernate), Python (Django/SQLAlchemy)

## Vulnerability Description
Executing individual child queries inside a loop over a parent entity collection results in $N+1$ round-trips to the database, causing latency bottlenecks and database connection pool starvation.

## Vulnerable Example
```java
// VIOLATION: N+1 lazy queries inside iteration
List<User> users = userRepository.findAll();
for (User user : users) {
    log.info("Orders: {}", user.getOrders().size()); // Triggers SELECT per user
}
```

## Remediated Example
```java
// SAFE: Fetch join parent and child entities in a single query
@Query("SELECT u FROM User u LEFT JOIN FETCH u.orders")
List<User> findAllWithOrders();
```

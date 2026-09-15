# Rule: CR-SEC-009 - Broken Object Level Authorization (BOLA)

## Metadata
- **Severity**: HIGH
- **Category**: Security / Access Control
- **CWE**: CWE-285 (Improper Authorization) / OWASP API1:2023
- **Languages**: Java, Python, TypeScript

## Vulnerability Description
Endpoints accepting object identifiers (e.g. `/api/documents/{docId}`) without validating whether the currently authenticated principal owns or has permissions to access the target object allow horizontal privilege escalation.

## Vulnerable Example (Spring Boot)
```java
// VIOLATION: Missing ownership verification
@GetMapping("/documents/{id}")
public Document getDocument(@PathVariable Long id) {
    return documentRepository.findById(id).orElseThrow();
}
```

## Remediated Example
```java
// SAFE: Verify document ownership against authenticated principal
@GetMapping("/documents/{id}")
public Document getDocument(@PathVariable Long id, @AuthenticationPrincipal User user) {
    return documentRepository.findByIdAndOwnerId(id, user.getId())
        .orElseThrow(() -> new AccessDeniedException("Unauthorized"));
}
```

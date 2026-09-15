# Rule: CR-QUAL-004 - Swallowed Exception & Empty Catch Block

## Metadata
- **Severity**: LOW
- **Category**: Code Quality / Reliability
- **CWE**: CWE-390 (Detection of Error Condition Without Action)
- **Languages**: Java, TypeScript, Python, C#

## Vulnerability Description
Catching an exception and leaving the catch block completely blank or solely logging an unhelpful comment blinds system operators to silent system failures, corrupted state, and failed operations.

## Vulnerable Example
```java
// VIOLATION: Empty catch block ignores error
try {
    saveRecord(record);
} catch (IOException e) {
    // ignore
}
```

## Remediated Example
```java
// SAFE: Log structured warning and wrap or handle error
try {
    saveRecord(record);
} catch (IOException e) {
    log.error("Failed to persist record ID {}: {}", record.getId(), e.getMessage(), e);
    throw new ServiceException("Storage failure", e);
}
```

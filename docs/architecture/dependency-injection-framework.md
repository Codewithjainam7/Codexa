# Spring Boot 3 Bean Lifecycle & Dependency Injection Architecture

## 1. Overview
Codexa is structured around strict Inversion of Control (IoC) and constructor-based Dependency Injection (DI) using Spring Framework 6.

---

## 2. Service Layer Tiering

```
[Controllers] (REST Ingress, Parameter Validation, Error Translation)
      │
      ▼
[Services] (Pipeline Orchestration, Rule Evaluation, Report Generation)
      │
      ▼
[Repositories & Drivers] (SQLite WAL, File Extraction, OpenRouter Client)
```

---

## 3. Best Practices Enforced
* **Mandatory Constructor Injection**: Field injection (`@Autowired` on fields) is disallowed to ensure immutability and simple unit test mocking.
* **Component Scanning**: Sub-packages are organized by domain (`analysis`, `rules`, `security`, `compliance`).

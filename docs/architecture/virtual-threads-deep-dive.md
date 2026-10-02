# Java 21 Virtual Threads & Fiber Concurrency Architecture

## 1. Overview
Codexa runs on Java 21 LTS with **Project Loom Virtual Threads** enabled. Virtual threads represent lightweight, user-mode fibers managed by the Java Virtual Machine rather than one-to-one OS platform threads.

---

## 2. Spring Boot 3 Configuration

Virtual threads are enabled globally via `application.yml`:

```yaml
spring:
  threads:
    virtual:
      enabled: true
```

This automatically configures:
1. Tomcat embedded server request handling on virtual threads.
2. Spring `@Async` task execution executors.
3. Analysis pipeline background worker thread pools.

---

## 3. Performance Advantages
* **Memory Footprint**: Platform threads require ~1MB of OS stack space; virtual threads require only a few hundred bytes.
* **Massive Concurrency**: Codexa scales to tens of thousands of concurrent I/O operations (file reads, AST parsing, network calls) without OS thread starvation.

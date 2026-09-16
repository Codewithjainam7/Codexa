# ADR-0002: Adoption of Java 21 Virtual Threads

## Status
Accepted

## Context
High concurrent I/O load during zip extraction and AST file operations.

## Decision
Adopt Java 21 Loom Virtual Threads for lightweight thread execution.

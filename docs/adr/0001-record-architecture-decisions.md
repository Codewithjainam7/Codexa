# ADR-0001: Record Architecture Decisions Using MADR

## Status
**Accepted** (2026-09-01)

## Context & Problem Statement
As Codexa evolved from an initial prototype into an enterprise-grade static analysis engine and code review platform, numerous foundational decisions were required regarding runtime platforms, storage engines, AST parser libraries, and user interface aesthetics. 

Without an immutable, version-controlled repository of architectural choices, team members and open-source contributors risk revisiting settled debates, misunderstanding trade-offs, or introducing inconsistent paradigms.

## Decision Drivers
- **Auditability**: Enterprise security teams need to verify why certain architectural constraints (e.g. non-executable volumes, streaming buffers) were chosen.
- **Knowledge Preservation**: Preserving institutional knowledge regarding why alternative libraries or protocols were discarded.
- **Git Alignment**: Architectural records must live alongside the code, undergo standard pull request reviews, and remain immutable.

## Considered Options
1. **Unstructured Wiki / Confluence Pages**: Easy to write but disconnected from code reviews and frequently outdated.
2. **Markdown Architectural Decision Records (MADR)**: Lightweight, Git-native markdown documents following a standardized template.
3. **Architecture Diagrams Only**: Visually informative but lacking documented trade-offs, alternative evaluations, and consequence logs.

## Decision Outcome
Chosen option: **Markdown Architectural Decision Records (MADR)** stored directly in `docs/adr/`.

Each record follows the standard format:
- **Title & Sequence**: `NNNN-short-descriptive-title.md`
- **Status**: Proposed, Accepted, Deprecated, or Superseded
- **Context & Problem Statement**: The technical motivation and business requirements
- **Decision Drivers**: Core performance, security, and developer experience metrics
- **Considered Options & Trade-Offs**: Comparative analysis of viable approaches
- **Decision Outcome**: The selected choice and detailed rationale
- **Consequences**: Positive, negative, and neutral impacts on the codebase

## Consequences
- **Positive**: Every major technological choice (Java 21, SQLite WAL, SARIF v2.1.0, Monochromatic UI) is formally documented and peer-reviewed.
- **Positive**: New contributors can quickly understand the architectural rationale behind design patterns.
- **Negative / Overhead**: Requires continuous maintenance when architectural designs are superseded or revised.

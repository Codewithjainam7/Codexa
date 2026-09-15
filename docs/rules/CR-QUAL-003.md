# Rule: CR-QUAL-003 - High Cyclomatic Complexity & God Method

## Metadata
- **Severity**: MEDIUM
- **Category**: Code Quality / Maintainability
- **Threshold**: Cyclomatic Complexity (CCN) > 15
- **Languages**: All Supported Languages

## Vulnerability Description
Methods exhibiting high cyclomatic complexity (deeply nested conditional statements, switch branches, and loops) are difficult to comprehend, exhibit high defect rates, and are notoriously resistant to unit testing.

## Refactoring Strategies
1. **Extract Method**: Break monolithic routines into focused single-responsibility helper functions.
2. **Strategy Pattern**: Replace sprawling `switch(type)` blocks with polymorphic object dispatch.
3. **Guard Clauses**: Return early to eliminate nested `if/else` indentation ladders.

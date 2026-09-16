# Test Scenario: C# XML External Entity (XXE) Injection

## Purpose
Validates detection of insecure `XmlDocument` and `XmlTextReader` configurations in C# .NET.

## Test Cases
1. `XmlReaderSettings.DtdProcessing = DtdProcessing.Parse` -> **VIOLATION (HIGH, CWE-611)**.
2. `XmlReaderSettings.DtdProcessing = DtdProcessing.Prohibit` -> **PASSED**.

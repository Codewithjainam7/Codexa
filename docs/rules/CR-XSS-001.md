# CR-XSS-001: Unsafe DOM HTML Injection

Detects dangerouslySetInnerHTML and innerHTML usage without prior sanitization.


### Remediation Guidelines
- Use DOMPurify.sanitize() when handling dynamic HTML.
- Prefer standard JSX text nodes to avoid raw innerHTML execution.
- Leverage React default escaping mechanisms.

# Rule Specification: CR-XSS-001 (Cross-Site Scripting Detection)

| Metadata | Value |
| :--- | :--- |
| **Rule ID** | `CR-XSS-001` |
| **Category** | Security |
| **Default Severity** | `HIGH` |
| **CWE Mapping** | [CWE-79: Improper Neutralization of Input During Web Page Generation](https://cwe.mitre.org/data/definitions/79.html) |
| **OWASP Top 10** | A03:2021 — Injection |
| **Target Scope** | Java AST, React / JSX, HTML Template Engines (Thymeleaf, JSP, Blade, Jinja) |

---

## 1. Vulnerability Overview

Cross-Site Scripting (XSS) occurs when an application includes untrusted, unvalidated, or unescaped data in a web page delivered to users. An attacker can execute arbitrary client-side JavaScript in the victim's browser session, resulting in session hijacking, token theft, keystroke logging, or phishing.

Common sources of XSS in modern codebases include:
1. Rendering raw unescaped HTML strings in React via `dangerouslySetInnerHTML`.
2. Reflected user input written directly to HTTP response streams via `PrintWriter.write` or `ServletOutputStream`.
3. Unescaped attribute rendering in server-side templating engines (e.g. `th:utext` in Thymeleaf or unescaped `<%- %>` in EJS).

---

## 2. Detection Logic & AST Patterns

Codexa checks both backend and frontend layers:
- **Java AST**: Identifies `HttpServletResponse.getWriter().write(...)` or `.print(...)` receiving variable arguments without HTML entity encoding.
- **Frontend JSX/TSX**: Detects attributes named `dangerouslySetInnerHTML={{ __html: ... }}` or direct assignments to `element.innerHTML` or `document.write(...)`.
- **Template AST**: Flags usages of unescaped raw HTML evaluation tags (e.g. `th:utext`, `v-html`).

---

## 3. Vulnerable Code Examples

### A. React / JSX Vulnerability
```tsx
// VULNERABLE: Direct injection of unsanitized HTML string into DOM
export function CommentBody({ userHtml }: { userHtml: string }) {
  return <div dangerouslySetInnerHTML={{ __html: userHtml }} />;
}
```

### B. Java Servlet Response Vulnerability
```java
// VULNERABLE: Direct echo of unvalidated query parameter to response
@GetMapping("/greet")
public void greet(@RequestParam String name, HttpServletResponse response) throws IOException {
    response.setContentType("text/html");
    response.getWriter().println("<h1>Hello, " + name + "!</h1>");
}
```

---

## 4. Secure Remediation

### A. React / JSX Sanitization with DOMPurify
```tsx
import DOMPurify from 'dompurify';

// SECURE: HTML sanitized against DOM XSS vectors before rendering
export function CommentBody({ userHtml }: { userHtml: string }) {
  const cleanHtml = DOMPurify.sanitize(userHtml, { USE_PROFILES: { html: true } });
  return <div dangerouslySetInnerHTML={{ __html: cleanHtml }} />;
}
```

### B. Java Response Context Escaping
```java
import org.springframework.web.util.HtmlUtils;

// SECURE: String HTML entities escaped before writing to output
@GetMapping("/greet")
public ResponseEntity<String> greet(@RequestParam String name) {
    String safeName = HtmlUtils.htmlEscape(name);
    return ResponseEntity.ok("<h1>Hello, " + safeName + "!</h1>");
}
```

---

## 5. Defensive Headers & Mitigations

1. **Content-Security-Policy (CSP)**: Deploy a strict CSP restricting script execution sources:
   ```http
   Content-Security-Policy: default-src 'self'; script-src 'self'; object-src 'none';
   ```
2. **HttpOnly Cookie Flag**: Prevent exfiltration of session identifiers via XSS by setting `HttpOnly; Secure; SameSite=Strict`.

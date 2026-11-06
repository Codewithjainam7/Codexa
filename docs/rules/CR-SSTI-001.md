# CR-SSTI-001: Server-Side Template Injection (SSTI)

| Metadata | Specification |
| :--- | :--- |
| **Rule ID** | `CR-SSTI-001` |
| **Severity** | `CRITICAL` |
| **Category** | `SECURITY` |
| **CWE ID** | [CWE-1336: Improper Neutralization of Special Elements Used in a Template Engine](https://cwe.mitre.org/data/definitions/1336.html) |
| **OWASP Top 10** | A03:2021 – Injection |
| **Languages** | Java, Python, JavaScript, TypeScript |

---

## Vulnerability Overview

Server-Side Template Injection occurs when user-supplied input is embedded directly into a template file or template string expression rather than being passed in as a safe variable/data parameter. 

When the template engine compiles the template, an attacker can embed native expressions (such as FreeMarker directives, Thymeleaf preprocessing `__${...}__`, or Jinja2 Python expressions) allowing arbitrary command execution on the host machine.

---

## Vulnerable Code Patterns

```python
# Vulnerable Flask/Jinja2:
from flask import render_template_string, request
@app.route('/hello')
def hello():
    name = request.args.get('name')
    # Unsafe: user input evaluated as template syntax
    return render_template_string("Hello " + name)
```

```java
// Vulnerable FreeMarker:
Configuration cfg = new Configuration(Configuration.VERSION_2_3_31);
Template t = new Template("name", new StringReader("Hello " + userInput), cfg);
t.process(root, out);
```

---

## Secure Remediation

Always render pre-defined static template files and pass dynamic parameters solely through context variables:

```python
# Secure:
from flask import render_template
@app.route('/hello')
def hello():
    name = request.args.get('name')
    return render_template("hello.html", name=name)
```

# CR-RSC-001: Unauthenticated Next.js Server Action Mutation

| Metadata | Specification |
| :--- | :--- |
| **Rule ID** | `CR-RSC-001` |
| **Severity** | `HIGH` |
| **Category** | `SECURITY` |
| **CWE ID** | [CWE-306: Missing Authentication for Critical Function](https://cwe.mitre.org/data/definitions/306.html) |
| **OWASP Top 10** | A01:2021 – Broken Access Control |
| **Languages** | JavaScript, TypeScript, React, Next.js |

---

## Vulnerability Overview

In React Server Components and the Next.js App Router, functions marked with the directive `'use server'` become callable directly over the public internet as HTTP POST endpoints. 

A common pitfall is treating Server Actions like private backend utility functions. Because anyone can forge an HTTP POST request targeting the Server Action ID without touching UI buttons, unauthenticated Server Actions allow malicious users to trigger unauthorized data modifications, initiate transactions, or read private resources.

---

## Vulnerable Code Patterns

```typescript
'use server';
import { db } from '@/lib/db';

// Vulnerable: Publicly callable by anyone without authentication!
export async function deleteUserAccount(userId: string) {
  await db.user.delete({ where: { id: userId } });
}
```

---

## Secure Remediation

Always enforce session authentication and role verification inside the action body, and validate arguments using runtime schemas like Zod:

```typescript
'use server';
import { auth } from '@/auth';
import { db } from '@/lib/db';
import { z } from 'zod';

export async function deleteUserAccount(userId: string) {
  const session = await auth();
  if (!session?.user) {
    throw new Error('401 Unauthorized');
  }
  
  // Enforce tenant isolation / IDOR prevention:
  if (session.user.id !== userId && session.user.role !== 'admin') {
    throw new Error('403 Forbidden');
  }

  await db.user.delete({ where: { id: userId } });
}
```

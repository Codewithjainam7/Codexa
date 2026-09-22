# Codexa Outbound Webhooks & Event Notification Guide

This guide details Codexa's outbound webhook architecture, cryptographic signature verification, retry policies, and sample payloads delivered upon analysis completion.

---

## 1. Webhook Lifecycle & Trigger

When submitting a job via `POST /api/v1/analyses/github` or `zip`, clients can supply an optional `webhookUrl`:

```json
{
  "repoUrl": "https://github.com/org/repo",
  "webhookUrl": "https://ci.corp.com/hooks/codexa",
  "webhookSecret": "whsec_9918273645a8b7c6d5e4"
}
```

Upon pipeline termination (whether `COMPLETED` or `FAILED`), Codexa delivers a signed HTTP `POST` request to the target URL within **&lt; 50ms**.

---

## 2. Cryptographic HMAC-SHA256 Signature Verification

To guarantee authenticity and protect against replay attacks, Codexa computes an HMAC-SHA256 signature using the configured `webhookSecret`:

### Request Headers Delivered:
```http
POST /hooks/codexa HTTP/1.1
Host: ci.corp.com
Content-Type: application/json
X-Codexa-Event: analysis.completed
X-Codexa-Delivery: e9b1c2d3-4f5a-6b7c-8d9e-0f1a2b3c4d5e
X-Codexa-Timestamp: 1789827360
X-Codexa-Signature-256: t=1789827360,v1=7f8a9b0c1d2e3f4a5b6c7d8e9f0a1b2c3d4e5f6a7b8c9d0e1f2a3b4c5d6e7f8a
```

### Signature Verification in Node.js:
```javascript
import crypto from 'crypto';

function verifyCodexaWebhook(rawBody, signatureHeader, secret) {
  const [tPart, v1Part] = signatureHeader.split(',');
  const timestamp = tPart.split('=')[1];
  const signature = v1Part.split('=')[1];

  const signedPayload = `${timestamp}.${rawBody}`;
  const expectedSignature = crypto
    .createHmac('sha256', secret)
    .update(signedPayload)
    .digest('hex');

  return crypto.timingSafeEqual(Buffer.from(signature), Buffer.from(expectedSignature));
}
```

---

## 3. Delivery Retry Policy

If the destination webhook endpoint returns a non-2xx status code or times out (&gt; 5000ms):
1. **Exponential Backoff**: Codexa retries at $t = 10\text{s}, 60\text{s}, 300\text{s}$.
2. **Dead-Letter Logging**: After 3 failed attempts, the webhook payload is archived in `webhook_dead_letter` for manual audit.

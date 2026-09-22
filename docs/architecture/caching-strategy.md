# Codexa Multi-Tier Caching Strategy

This architecture guide details the caching mechanisms employed across Codexa, including in-memory diagnostics caches, content-addressable LLM remediation responses, and client-side safeStorage fallbacks.

---

## 1. Caching Tiers Overview

Codexa uses a three-tier layered caching topology:

```
[ Frontend: safeStorage In-Memory + LocalStorage Fallback ]
                         |
                         v
[ Backend Tier 1: ConcurrentHashMap In-Memory Diagnostics Cache ]
                         |
                         v
[ Backend Tier 2: Persistent LlmCacheEntity (SHA-256 Content-Addressed) ]
```

---

## 2. In-Memory Project Diagnostics Cache

During analysis completion, `ProjectDiagnosticsCollector` computes deep white-box complexity metrics, code composition maps, and black-box exposed API endpoints. Because executive dashboards and reports poll this data frequently, results are cached in a thread-safe `ConcurrentHashMap`:

```java
public class AnalysisJobService {
    private final Map<UUID, ProjectDiagnostics> diagnosticsCache = new ConcurrentHashMap<>();

    public ProjectDiagnostics getOrComputeDiagnostics(AnalysisJobEntity entity, List<FindingEntity> findings) {
        if (entity == null) return null;
        ProjectDiagnostics cached = diagnosticsCache.get(entity.getId());
        if (cached != null) return cached;

        ProjectDiagnostics fallback = diagnosticsCollector.generateFallback(entity, findings);
        diagnosticsCache.put(entity.getId(), fallback);
        return fallback;
    }
}
```

---

## 3. Content-Addressable LLM Cache

Generating AI explanations and code remediation diffs via LLM providers (e.g. NVIDIA Nemotron via OpenRouter) incurs network latency and API costs.

Codexa implements a deterministic content-addressable cache:
1. Calculates a SHA-256 cache key over:
   $$\text{CacheKey} = \text{SHA256}(\text{RuleID} + \text{EvidenceMasked} + \text{TargetLanguage})$$
2. Checks the `llm_cache` database table before dispatching external HTTP requests.
3. If a cached response exists and has not expired, it returns immediately (&lt; 2ms).

---

## 4. Cache Invalidation & Eviction Policies

- **Job Diagnostics Cache**: Evicted automatically upon job deletion or after 24 hours of inactivity.
- **LLM Cache**: Retains responses for 30 days unless rule definitions change.
- **Client Cache**: Automatically falls back to transient in-memory arrays when private/incognito browsing restricts HTML5 LocalStorage.

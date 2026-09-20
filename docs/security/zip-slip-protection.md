# Enterprise Archive Ingestion & Zip Slip Protection

Codexa enforces multi-layer defenses to ingest large enterprise codebases up to **3.0 GB** decompressed and **50,000 files** safely without vulnerability to Zip Slip, path traversal, or resource exhaustion attacks.

---

## 1. Enterprise Archive Quotas & Boundaries

| Parameter | Enterprise Limit | Defense Purpose |
| :--- | :--- | :--- |
| **Max Compressed Size** | `1.0 GB` | Prevents denial of service on upload buffers |
| **Max Extracted Size** | `3.0 GB` | Neutralizes zip bomb amplification attacks |
| **Max Total Files** | `50,000` | Limits inode consumption and AST queue size |
| **Max Directory Depth** | `15 levels` | Prevents deep recursive stack exhaustion |
| **Max Single File Size** | `100 MB` | Prevents single massive payload memory starvation |
| **Streaming Buffer Size** | `64 KB` | High-throughput streaming decompression |

---

## 2. Zip Slip Path Traversal Verification

Every entry inside an uploaded ZIP archive or GitHub zipball is sanitized and checked before any disk write:

```java
Path normalizedTargetDir = targetDir.toAbsolutePath().normalize();
Path entryDestination = normalizedTargetDir.resolve(entryName).normalize();

if (!entryDestination.startsWith(normalizedTargetDir)) {
    log.warn("Zip Slip directory traversal detected in entry: {}", entryName);
    throw new ApiException(
            HttpStatus.BAD_REQUEST,
            "ZIP_SLIP_DETECTED",
            "Archive contains invalid path sequence traversing outside the target staging directory."
    );
}
```

---

## 3. Zip Bomb & Amplification Defenses

- **Streaming Byte Counting**: Files are decompressed through a monitored `ZipInputStream` tracking cumulative byte count in real time.
- **Immediate Abort**: If `totalBytesExtracted > 3.0 GB`, the extraction stream is aborted with HTTP 413 `PAYLOAD_TOO_LARGE`.
- **Count Thresholds**: If entries exceed 50,000 files, processing halts immediately with `ARCHIVE_FILE_COUNT_EXCEEDED`.
- **Expansion Ratio Cap**: Ratios exceeding 100:1 between compressed and extracted bytes trigger immediate termination.

---

## 4. Intelligent File Filtering Pipeline

To accelerate scanning speeds and prevent false positives on documentation or compiled binaries, `FileFilterService.java` prunes non-production files:

### Ignored Directories
- Version control & dependencies: `.git`, `node_modules`, `vendor`, `.venv`, `__pycache__`
- Build outputs & bundles: `target`, `build`, `dist`, `bin`, `.next`, `out`
- Test suites & fixtures: `src/test`, `test/`, `fixtures/`, `__tests__`
- Documentation directories: `docs/`, `documentation/`

### Ignored File Extensions
- Documentation & text: `.md`, `.markdown`, `.txt`, `.pdf`, `.doc`
- Compiled binaries: `.class`, `.jar`, `.exe`, `.so`, `.dll`, `.wasm`
- Media assets: `.png`, `.jpg`, `.jpeg`, `.gif`, `.svg`, `.ico`, `.mp4`, `.woff2`
- Compressed bundles: `.zip`, `.tar`, `.gz`, `.min.js`, `.min.css`

### Analyzed Production Source Files
- Java (`.java`), Kotlin (`.kt`), Scala (`.scala`)
- TypeScript (`.ts`, `.tsx`), JavaScript (`.js`, `.jsx`, `.mjs`, `.cjs`)
- Python (`.py`), Go (`.go`), Rust (`.rs`), C/C++ (`.c`, `.cpp`, `.h`)
- PHP (`.php`), Ruby (`.rb`), SQL (`.sql`)
- Infrastructure: `Dockerfile`, `docker-compose.yml`, Kubernetes YAML

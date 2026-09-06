# Enterprise Archive Ingestion & Zip Slip Protection

Codexa enforces multi-layer defenses to ingest large enterprise codebases up to **500MB** compressed and **1000MB** extracted safely without vulnerability to Zip Slip, path traversal, or resource exhaustion attacks.

## 1. Enterprise Archive Quotas & Boundaries

| Parameter | Enterprise Limit | Defense Purpose |
| :--- | :--- | :--- |
| **Max Compressed Size** | `500 MB` | Prevents denial of service on upload buffers |
| **Max Extracted Size** | `1000 MB` | Neutralizes 1000:1 zip bomb amplification |
| **Max Total Files** | `20,000` | Limits inode consumption and AST queue size |
| **Max Directory Depth** | `30 levels` | Prevents deep filesystem recursive stack exhaustion |
| **Max Single File Size** | `100 MB` | Prevents single massive payload starvation |
| **Buffer Throughput** | `64 KB (65,536 bytes)` | High-throughput streaming decompression |

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

## 3. Zip Bomb & Amplification Defenses

- **Streaming Counting**: Files are decompressed through a `ZipInputStream` that monitors byte count on the fly.
- **Immediate Abort**: If `totalBytesExtracted > limits.maxExtractedSizeBytes()`, the stream is terminated immediately with HTTP 413 `ZIP_BOMB_TOTAL_SIZE_EXCEEDED`.
- **Count Thresholds**: If entries exceed 20,000 files, processing halts with `ZIP_BOMB_FILE_COUNT_EXCEEDED`.

## 4. Intelligent File Filtering

To accelerate scanning speeds and prevent parsing non-code artifacts:
- **Ignored Directories**: `.git`, `node_modules`, `build`, `target`, `dist`, `vendor`, `.venv`, `.next`, `__pycache__`.
- **Ignored Binary Formats**: `.class`, `.jar`, `.exe`, `.so`, `.png`, `.jpg`, `.pdf`, `.mp4`, `.zip`, `.min.js`.
- **Analyzed Source Extensions**: Java, Kotlin, Scala, TypeScript, JavaScript, Python, Go, Rust, C/C++, PHP, Ruby, SQL, Dockerfile, YAML, JSON.

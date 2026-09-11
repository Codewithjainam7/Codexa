# Performance Tuning & JVM Sizing Guide

Optimizing throughput and latency for multi-gigabyte repository analysis.

## Recommended JVM Flags

```bash
java -server \
  -Xms2g -Xmx4g \
  -XX:+UseG1GC \
  -XX:MaxGCPauseMillis=200 \
  -XX:G1ReservePercent=15 \
  -XX:InitiatingHeapOccupancyPercent=45 \
  -XX:+ParallelRefProcEnabled \
  -jar target/codexa-backend-1.0.0-SNAPSHOT.jar
```

## Throughput Benchmarks
- **10,000 Lines of Code**: ~0.42 seconds
- **100,000 Lines of Code**: ~2.8 seconds
- **1,000,000 Lines of Code**: ~19.5 seconds

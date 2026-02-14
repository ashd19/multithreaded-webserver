# Quick Reference - Virtual Threads WITHOUT Caching

## Start Server

```bash
javac Server.java
java Server
```

**Port**: 8020

## Test Server

```bash
# Simple test
curl http://localhost:8020

# Load test
ab -n 10000 -c 1000 http://localhost:8020/
```

## Key Differences from Cached Version

| Feature | This Server (No Cache) | Cached Version |
|---------|----------------------|----------------|
| Port | 8020 | 8010 |
| Disk I/O | Every request | Startup only |
| Throughput | ~1,500 req/s | ~15,000 req/s |
| Latency | ~600ms | ~60ms |

## Architecture

```
┌─────────────┐
│   Client    │
└──────┬──────┘
       │ HTTP Request
       ▼
┌─────────────────────────────┐
│  Virtual Thread Executor    │
│  (newVirtualThreadPerTask)  │
└──────────┬──────────────────┘
           │ Spawn Virtual Thread
           ▼
    ┌──────────────┐
    │ handleClient │
    └──────┬───────┘
           │
           ▼
    ┌──────────────────┐
    │ Read from Disk   │ ⚠️ BOTTLENECK
    │ (every request)  │
    └──────┬───────────┘
           │
           ▼
    ┌──────────────┐
    │ Send Response│
    └──────────────┘
```

## Code Snippet - The Bottleneck

```java
// This executes on EVERY request
String jsonResponse = new String(
    Files.readAllBytes(Paths.get(jsonFilePath))
);
```

## Performance Characteristics

### Strengths

✅ Virtual threads handle concurrency well
✅ Low memory per connection
✅ Automatic thread management

### Weaknesses

❌ Disk I/O on every request
❌ Lower throughput
❌ Higher latency
❌ Variable response times

## When to Use This Pattern

- Data changes on every request
- Cannot cache due to size
- Demonstrating need for optimization
- Educational/comparison purposes

## Optimization Path

1. **Current**: No caching (this server)
2. **Next**: Add in-memory caching
3. **Advanced**: Add distributed caching (Redis)
4. **Ultimate**: Add CDN for static content

## Related Files

- `Server.java` - Main server implementation
- `README.md` - Detailed documentation
- `COMPARISON.md` - Performance comparison
- `../VirtualThreads-with-caching/` - Optimized version

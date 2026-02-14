# Virtual Threads Web Server - WITHOUT Caching

## Overview

This is a demonstration server that uses **Java Virtual Threads (JDK 21+)** but **deliberately avoids caching** to show the performance impact of disk I/O on every request.

## Key Characteristics

### ⚠️ Performance Bottleneck (By Design)

- **Reads `data.json` from disk on EVERY request**
- No in-memory caching
- Demonstrates the importance of caching for high-throughput applications

### ✅ Virtual Threads Benefits (Still Present)

- Uses `Executors.newVirtualThreadPerTaskExecutor()`
- Handles thousands of concurrent connections efficiently
- Low memory footprint per connection
- Automatic thread management by the JVM

## Architecture

```
Client Request → Virtual Thread → Read JSON from Disk → Send Response
                                        ↑
                                  (Bottleneck!)
```

## Running the Server

### Prerequisites

- Java 21 or higher (for Virtual Threads support)
- `data.json` file in the parent directory (`../data.json`)

### Start Server

```bash
javac Server.java
java Server
```

The server will start on **port 8020** (different from the cached version on 8010).

### Expected Output

```
Server initialized WITHOUT caching - reading from disk on every request
JSON file path: ../data.json
Server listening on port 8020 with Virtual Threads
Connection backlog: 10000
⚠️  WARNING: NO CACHING - Disk I/O on every request!
```

## Testing

### Simple Test

```bash
curl http://localhost:8020
```

### Load Test (Compare with Cached Version)

```bash
# Test this server (without caching)
ab -n 10000 -c 1000 http://localhost:8020/

# Test cached version (for comparison)
ab -n 10000 -c 1000 http://localhost:8010/
```

## Performance Expectations

### Without Caching (This Server)

- **Throughput**: Lower due to disk I/O on every request
- **Latency**: Higher and more variable (disk read time)
- **CPU**: Lower (waiting on I/O)
- **Disk I/O**: Very high (bottleneck)

### With Caching (Comparison)

- **Throughput**: 5-10x higher
- **Latency**: Consistent and low
- **CPU**: Higher (processing requests faster)
- **Disk I/O**: Minimal (one-time read at startup)

## Purpose of This Implementation

This server demonstrates:

1. **Virtual threads alone aren't magic** - they help with concurrency, but can't eliminate I/O bottlenecks
2. **Importance of caching** - shows dramatic performance difference
3. **Baseline for comparison** - provides a reference point to measure optimization impact

## Comparison with Cached Version

| Feature | Without Caching (Port 8020) | With Caching (Port 8010) |
|---------|----------------------------|--------------------------|
| Virtual Threads | ✅ Yes | ✅ Yes |
| In-Memory Cache | ❌ No | ✅ Yes |
| Disk I/O per Request | ✅ Yes (slow) | ❌ No |
| Expected Throughput | ~1,000-2,000 req/s | ~10,000-20,000 req/s |
| Best For | Demonstration | Production |

## Code Highlights

### The Bottleneck (Line 44)

```java
// 2. Read JSON from disk (NO CACHING - performance bottleneck!)
String jsonResponse = new String(Files.readAllBytes(Paths.get(jsonFilePath)));
```

This line executes on **every single request**, causing:

- File system access
- Disk read operation
- Memory allocation for the entire file
- String conversion overhead

### Virtual Thread Executor (Still Efficient)

```java
this.virtualThreadExecutor = Executors.newVirtualThreadPerTaskExecutor();
```

Even with the disk I/O bottleneck, virtual threads ensure:

- Thousands of concurrent connections can wait for I/O without blocking
- Minimal memory overhead per connection
- Automatic scaling

## Learning Outcomes

1. **Virtual threads excel at I/O-bound tasks** - they allow many concurrent operations to wait efficiently
2. **Caching is critical for performance** - eliminating repeated disk reads is essential
3. **Measure everything** - always benchmark to understand actual performance characteristics

## Next Steps

To see the dramatic performance improvement:

1. Run this server and perform a load test
2. Run the cached version (`../VirtualThreads-with-caching/Server.java`) and repeat the test
3. Compare the results to see the impact of caching

## Related Files

- **With Caching**: `../VirtualThreads-with-caching/Server.java`
- **Data File**: `../data.json`

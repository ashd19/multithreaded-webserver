🚀 Multithreaded  Java Web Server: From Single-Threaded to High-Performance Thread Pools
A deep-dive exploration into the fundamentals of network programming and concurrency in Java. This project implements a web server from scratch across three architectural stages, demonstrating the performance trade-offs between sequential and parallel request handling.

🏗️ Architecture Stages
1. Single-Threaded Baseline
The Problem: Processes one request at a time.
The Catch: Any blocking operation (like disk I/O) stops the entire server, causing subsequent requests to queue or timeout.
Key Learning: Understanding the "Head-of-Line" blocking bottleneck.
2. Multi-Threaded (Thread-per-Request)
The Solution: Spawns a new java.lang.Thread for every incoming connection.
The Catch: While it solves the blocking problem, it introduces risk of "Thread Explosion" and high memory overhead under massive load.
Key Learning: The cost of thread creation and context switching.
3. Industrial-Grade Thread Pool
The Optimization: Uses java.util.concurrent.ExecutorService with a fixed pool of 100 worker threads.
The Benefit: Reuses threads to handle high-concurrency spikes efficiently without overwhelming the OS.
Key Learning: Resource management, task queuing, and graceful shutdown patterns.

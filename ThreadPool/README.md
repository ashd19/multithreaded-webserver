# Architecture: Thread Pool 

This version introduces **resource management** by using a fixed number of worker threads to handle requests, preventing the server from crashing under high load.

### How it Works
1. A set number of threads (e.g., 100) are created at startup.
2. Incoming connections are placed in a **Task Queue**.
3. Idle workers pick tasks from the queue, process them, and then return to the pool for the next task.

### Why it's Better (The Math)
Unlike Thread-on-Demand, memory usage is **predictable and capped**.

| Feature | Thread-on-Demand | Thread Pool (100) |
| :--- | :--- | :--- |
| **Memory** | Unbounded (1MB * Users) | **Fixed** (1MB * 100 = 100MB) |
| **Stability** | Crashes on spikes | **Stays alive** (Requests just wait) |
| **Performance** | High creation cost | Low cost (Threads are reused) |

### The Trade-off: Queuing
While the server won't crash, if you have 1,000 users and only 100 threads:
* 100 users are served immediately.
* **900 users must wait** in the queue.
* **Result:** Stability is high, but **P99 latency increases** significantly under heavy load.

> **Next Step:** See the `VirtualThreads` directory to learn how to solve the queuing problem without increasing memory.

# Architecture: Thread-on-Demand

This version evolves from the single-threaded model by creating a **new OS thread for every incoming connection**.

### How it Works

1. Server receives a connection.
2. Server spawns a `new Thread()`.
3. The new thread handles the I/O while the main thread returns to `accept()` the next user.

### The "Fatal Flaw" (The Math)

Each Platform Thread in Java (OS-level) allocates a fixed stack size (usually ~1MB).

| Users | Memory Consumption | Result |
| :--- | :--- | :--- |
| 10 | ~10 MB | Working fine |
| 500 | ~500 MB | Noticeable lag |
| 2,000 | ~2 GB | **OutOfMemoryError / System Crash** |

### This is expensive because

* **High Overhead:** The time spent creating and destroying threads can exceed the time spent doing actual work.
* **Context Switching:** With thousands of threads, the CPU spends more time "swapping" between them than executing code.
* **Unbounded Growth:** There is no limit to how many threads are created, making the server vulnerable to crashing during traffic spikes.

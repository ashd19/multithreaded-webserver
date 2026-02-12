Ashton Dsouza
Full-Stack Web Developer
ashtondsouza192@gmail.com | +91 8828518227 | Mumbai, India
GitHub | Portfolio | LinkedIn

Professional Summary
Third-year Information Technology student passionate about high-performance backend systems, concurrency, and scalable full-stack applications. Built custom multithreaded HTTP servers in Java (3,000–4,500 req/sec on consumer hardware) and production-grade features with Next.js + Spring Boot.

Education
Thadomal Shahani Engineering College
B.E. in Information Technology
July 2023 – May 2027

Experience
NexaCrft | SDE Intern
Mumbai, India | Nov 2025 – Dec 2025
- Developed and maintained production-level client projects, focusing on frontend implementation using Next.js/React with industry-standard quality and best practices.
- Delivered multiple live projects on schedule while maintaining code quality and meeting strict deadlines.
- Contributed to the full software development lifecycle, from requirement analysis to deployment and ongoing maintenance.

Skills
Programming Languages: TypeScript, Java, JavaScript
Libraries/Frameworks: Next.js, React, Spring Boot
Tools/Platforms: Git, Postman, Docker, Linux, SpringDoc OpenAPI, JMeter
Databases: SQL, PostgreSQL

Projects / Open-Source

Budgeting App | [Link]
Next.js, Spring Boot, PostgreSQL, JWT
- Engineered a secure, stateless authentication system using Spring Boot + JWT, reducing server memory overhead by ~40% vs. traditional session-based auth.
- Designed a normalized PostgreSQL schema on Neon (serverless), ensuring ACID compliance for critical financial transactions.
- Optimized API performance by implementing DTOs & Axios interceptors, reducing payload size and achieving sub-200ms response times.
- Deployed a scalable architecture with Dockerized backend (Render) and Next.js frontend (Vercel), integrated with Prometheus metrics (JVM, DB pool) for real-time observability.

Multithreaded Web Server | [Link]
Java, Apache JMeter
- Engineered three evolutionary server architectures: sequential (single-threaded) → multi-threaded → fixed-size thread pool (java.util.concurrent), boosting sustained throughput from ~650 req/sec (single-threaded baseline) to 3,000–4,500 req/sec while processing per-request JSON file reads and responses.
- Significantly reduced P99 latency (from multi-second queuing in single-threaded mode to <300 ms under high load) by eliminating head-of-line blocking via managed thread pool, keep-alive connections, and optimized request handling.
- Implemented a low-level HTTP/1.1-compliant engine using raw Java Sockets: manual header parsing, connection state management, structured JSON serialization, and graceful shutdown (awaitTermination + shutdownNow) with zero socket leaks via AutoCloseable and try-with-resources.
- Stress-tested under extreme concurrency (6,000 threads ramped over 30 seconds, infinite loop) with Apache JMeter on Dell Latitude 7280 (i7-7600U, 16GB RAM, localhost)
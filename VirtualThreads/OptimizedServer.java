import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Optimized Virtual Threads Server
 * Designed to handle 20,000+ concurrent connections efficiently
 * 
 * Key Optimizations:
 * 1. Cached JSON response (no disk I/O per request)
 * 2. Increased connection backlog (10,000)
 * 3. Connection metrics tracking
 * 4. Proper resource management
 */
public class OptimizedServer {
    private final ExecutorService virtualThreadExecutor;
    private final String cachedJsonResponse;
    private final AtomicLong activeConnections = new AtomicLong(0);
    private final AtomicLong totalRequests = new AtomicLong(0);

    public OptimizedServer() throws IOException {
        this.virtualThreadExecutor = Executors.newVirtualThreadPerTaskExecutor();
        // Cache JSON response in memory to avoid disk I/O on every request
        this.cachedJsonResponse = new String(Files.readAllBytes(Paths.get("../data.json")));
        System.out.println("JSON response cached in memory (" + cachedJsonResponse.length() + " bytes)");
    }

    public void handleClient(Socket clientSocket) {
        long connId = activeConnections.incrementAndGet();
        long reqId = totalRequests.incrementAndGet();

        try (
                PrintWriter toSocket = new PrintWriter(clientSocket.getOutputStream(), true);
                BufferedReader fromSocket = new BufferedReader(
                        new InputStreamReader(clientSocket.getInputStream()));
                clientSocket) {

            // 1. Consume Request Headers (HTTP Compliance)
            String line = fromSocket.readLine();
            while (line != null && !line.isEmpty()) {
                line = fromSocket.readLine();
            }

            // 2. Send HTTP Response (using cached JSON)
            toSocket.println("HTTP/1.1 200 OK");
            toSocket.println("Content-Type: application/json");
            toSocket.println("Content-Length: " + cachedJsonResponse.length());
            toSocket.println("X-Request-ID: " + reqId);
            toSocket.println("X-Active-Connections: " + connId);
            toSocket.println("");
            toSocket.println(cachedJsonResponse);

            // Log every 1000 requests
            if (reqId % 1000 == 0) {
                System.out.printf("Processed %,d requests | Active: %,d | Thread: %s%n",
                        reqId, connId, Thread.currentThread());
            }

        } catch (IOException ex) {
            System.err.println("Error handling request #" + reqId + ": " + ex.getMessage());
        } finally {
            activeConnections.decrementAndGet();
        }
    }

    public static void main(String[] args) {
        int port = 8010;
        int backlog = 10000; // Support up to 10,000 queued connections

        try {
            OptimizedServer server = new OptimizedServer();

            // Use larger backlog for high-concurrency scenarios
            try (ServerSocket serverSocket = new ServerSocket(port, backlog)) {
                System.out.println("╔════════════════════════════════════════════════════════════╗");
                System.out.println("║  Virtual Threads Server - Optimized for 20K+ Connections  ║");
                System.out.println("╚════════════════════════════════════════════════════════════╝");
                System.out.println("Server listening on port: " + port);
                System.out.println("Connection backlog: " + backlog);
                System.out.println("Virtual threads: Unlimited (on-demand)");
                System.out.println("Memory per thread: ~1-10 KB (vs 1-2 MB for platform threads)");
                System.out.println("\nPress Ctrl+C to shutdown gracefully...\n");

                // Add shutdown hook for graceful termination
                Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                    System.out.println("\n\nShutdown signal received...");
                    server.shutdownExecutor();
                    System.out.println("Final stats:");
                    System.out.println("  Total requests processed: " + server.totalRequests.get());
                    System.out.println("  Active connections: " + server.activeConnections.get());
                }));

                while (true) {
                    Socket clientSocket = serverSocket.accept();
                    server.virtualThreadExecutor.execute(() -> server.handleClient(clientSocket));
                }
            }
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    private void shutdownExecutor() {
        System.out.println("Initiating graceful shutdown of virtual thread executor...");
        virtualThreadExecutor.shutdown();
        try {
            if (!virtualThreadExecutor.awaitTermination(60, TimeUnit.SECONDS)) {
                System.out.println("Timeout elapsed, forcing shutdown...");
                virtualThreadExecutor.shutdownNow();
            } else {
                System.out.println("All virtual threads completed successfully.");
            }
        } catch (InterruptedException ie) {
            virtualThreadExecutor.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }
}

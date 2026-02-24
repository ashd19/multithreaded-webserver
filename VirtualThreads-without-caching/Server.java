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

/**
 * Virtual Threads Web Server WITHOUT Caching (JDK 21+)
 * Demonstrates performance impact of disk I/O on every request
 * 
 * Key Difference: Reads data.json from disk on EVERY request
 * This creates a performance bottleneck for comparison purposes
 */
public class Server {
    private final ExecutorService virtualThreadExecutor;
    private final String jsonFilePath;

    public Server() {
        this.virtualThreadExecutor = Executors.newVirtualThreadPerTaskExecutor();
        this.jsonFilePath = "../data.json";
        System.out.println("Server initialized WITHOUT caching - reading from disk on every request");
        System.out.println("JSON file path: " + jsonFilePath);
    }

    public void handleClient(Socket clientSocket) {
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

            // 2. Read JSON from disk (NO CACHING - performance bottleneck!)
            String jsonResponse = new String(Files.readAllBytes(Paths.get(jsonFilePath)));

            // 3. Send HTTP Response
            toSocket.println("HTTP/1.1 200 OK");
            toSocket.println("Content-Type: application/json");
            toSocket.println("Content-Length: " + jsonResponse.length());
            toSocket.println("");
            toSocket.println(jsonResponse);

        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    public static void main(String[] args) {
        int port = 8020; // Different port from cached version (8010)
        int backlog = 10000; // Same backlog as cached version for fair comparison

        try {
            Server server = new Server();

            // Use larger backlog for high concurrency testing
            try (ServerSocket serverSocket = new ServerSocket(port, backlog)) {
                System.out.println("Server listening on port " + port + " with Virtual Threads");
                // System.out.println("Connection backlog: " + backlog);

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
        virtualThreadExecutor.shutdown(); // Disable new tasks from being submitted
        try {
            // Wait a while for existing tasks to terminate
            if (!virtualThreadExecutor.awaitTermination(60, TimeUnit.SECONDS)) {
                virtualThreadExecutor.shutdownNow(); // Cancel currently executing tasks
            }
        } catch (InterruptedException ie) {
            virtualThreadExecutor.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }
}

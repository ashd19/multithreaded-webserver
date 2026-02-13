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
 * Virtual Threads Web Server (JDK 21+)
 * Optimized for 20,000+ concurrent connections
 */
public class Server {
    private final ExecutorService virtualThreadExecutor;
    private final String cachedJsonResponse; // Cache to avoid disk I/O on every request

    public Server() throws IOException {
        this.virtualThreadExecutor = Executors.newVirtualThreadPerTaskExecutor();
        // Cache JSON response in memory (critical optimization!)
        this.cachedJsonResponse = new String(Files.readAllBytes(Paths.get("../data.json")));
        System.out.println("JSON response cached (" + cachedJsonResponse.length() + " bytes)");
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

            // 2. Send HTTP Response (using cached JSON - no disk I/O!)
            toSocket.println("HTTP/1.1 200 OK");
            toSocket.println("Content-Type: application/json");
            toSocket.println("Content-Length: " + cachedJsonResponse.length());
            toSocket.println("");
            toSocket.println(cachedJsonResponse);

        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    public static void main(String[] args) {
        int port = 8010;
        int backlog = 10000; // Increased from default 50 to support high concurrency

        try {
            Server server = new Server();

            // Use larger backlog for 20K+ concurrent connections
            try (ServerSocket serverSocket = new ServerSocket(port, backlog)) {
                System.out.println("Server listening on port " + port + " with Virtual Threads");
                System.out.println("Connection backlog: " + backlog);
                System.out.println("Ready to handle 20,000+ concurrent connections!\n");

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
package ThreadPool;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ThreadPoolServer {
    private final ExecutorService threadPool;

    public ThreadPoolServer(int poolSize) {
        this.threadPool = Executors.newFixedThreadPool(poolSize);
    }

    public void handleClient(Socket clientSocket) {
        try (
                PrintWriter toSocket = new PrintWriter(clientSocket.getOutputStream(), true);
                java.io.BufferedReader fromSocket = new java.io.BufferedReader(
                        new java.io.InputStreamReader(clientSocket.getInputStream()))) {
            // 1. Consume Request Headers (HTTP Compliance)
            String line = fromSocket.readLine();
            while (line != null && !line.isEmpty()) {
                line = fromSocket.readLine();
            }

            // 2. Perform Work (Reading JSON)
            String jsonResponse = new String(java.nio.file.Files.readAllBytes(java.nio.file.Paths.get("data.json")));

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
        int port = 8010;
        int poolSize = 100;
        ThreadPoolServer server = new ThreadPoolServer(poolSize);

        try (ServerSocket serverSocket = new ServerSocket(port)) {
            serverSocket.setSoTimeout(70000);
            System.out.println("Server is listening on port " + port + " with pool size " + poolSize);

            while (true) {
                try {
                    Socket clientSocket = serverSocket.accept();
                    server.threadPool.execute(() -> server.handleClient(clientSocket));
                } catch (java.net.SocketTimeoutException e) {
                    System.out.println("Wait timeout reached, shutting down...");
                    break;
                }
            }
        } catch (IOException ex) {
            ex.printStackTrace();
        } finally {
            server.shutdownPool();
        }
    }

    private void shutdownPool() {
        System.out.println("Initiating graceful shutdown of thread pool...");
        threadPool.shutdown(); // Disable new tasks from being submitted
        try {
            // Wait a while for existing tasks to terminate
            if (!threadPool.awaitTermination(60, java.util.concurrent.TimeUnit.SECONDS)) {
                threadPool.shutdownNow(); // Cancel currently executing tasks
            }
        } catch (InterruptedException ie) {
            threadPool.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }
}
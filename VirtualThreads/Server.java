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

public class Server {
    private final ExecutorService virtualThreadExecutor;

    public Server() {
        this.virtualThreadExecutor = Executors.newVirtualThreadPerTaskExecutor();
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

            // 2. Perform Work (Reading JSON)
            String jsonResponse = new String(Files.readAllBytes(Paths.get("../data.json")));

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
        Server server = new Server();

        try (ServerSocket serverSocket = new ServerSocket(port)) {
            serverSocket.setSoTimeout(70000);
            System.out.println("Server is listening on port " + port + " with Virtual Threads");

            while (true) {
                try {
                    Socket clientSocket = serverSocket.accept();
                    server.virtualThreadExecutor.execute(() -> server.handleClient(clientSocket));
                } catch (java.net.SocketTimeoutException e) {
                    System.out.println("Wait timeout reached, shutting down...");
                    break;
                }
            }
        } catch (IOException ex) {
            ex.printStackTrace();
        } finally {
            server.shutdownExecutor();
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
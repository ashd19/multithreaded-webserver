
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;

public class Server {

    public void run() {
        int port = 8010;
        try (ServerSocket serverSocket = new ServerSocket(port)) {
            // serverSocket.setSoTimeout(1000000);
            System.out.println("Server is listening on port " + port);
            while (true) {
                try (Socket acceptedConnection = serverSocket.accept()) {
                    System.out.println("Accepted connection from " + acceptedConnection.getRemoteSocketAddress());
                    PrintWriter toClient = new PrintWriter(acceptedConnection.getOutputStream(), true);
                    BufferedReader fromClient = new BufferedReader(
                            new InputStreamReader(acceptedConnection.getInputStream()));

                    // Read request line
                    String line = fromClient.readLine();
                    System.out.println("Line: " + line);

                    // Consume headers/remaining lines until empty
                    while (line != null && !line.isEmpty()) {
                        line = fromClient.readLine();
                    }

                    // Simulate work by reading a file from disk
                    String jsonResponse = new String(
                            java.nio.file.Files.readAllBytes(java.nio.file.Paths.get("../data.json")));

                    toClient.println("HTTP/1.1 200 OK");
                    toClient.println("Content-Type: application/json");
                    toClient.println("Content-Length: " + jsonResponse.length());
                    toClient.println("");
                    toClient.println(jsonResponse);

                    fromClient.close();
                    toClient.close();
                    acceptedConnection.close();
                }

            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        Server server = new Server();
        try {
            server.run();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
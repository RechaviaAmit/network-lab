import java.io.*;
import java.net.*;
import java.util.concurrent.*;

public class WebServer {
    private static final int PORT = 8080;
    private static final int MAX_CONNECTIONS = 10;

    public static void main(String[] args) throws IOException {
        ExecutorService executorService = Executors.newFixedThreadPool(MAX_CONNECTIONS);
        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            while (true) {
                Socket clientSocket = serverSocket.accept();
                executorService.submit(new ConnectionHandler(clientSocket));
            }
        }
    }

    private static class ConnectionHandler implements Runnable {
        private Socket clientSocket;

        public ConnectionHandler(Socket clientSocket) {
            this.clientSocket = clientSocket;
        }

        @Override
        public void run() {
            try {
                handleClientConnection();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

        private void handleClientConnection() throws IOException {
            try (BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
                 PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true)) {
                String request = in.readLine();
                // Handle the request here. We're just echoing it back in this example.
                out.println("Received your request: " + request);
                System.out.println("Received your request: " + request);
            } finally {
                clientSocket.close();
            }
        }
    }
}
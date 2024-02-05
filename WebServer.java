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

                StringBuilder requestBuilder = new StringBuilder();
                String line;
                while (!(line = in.readLine()).isEmpty()) {
                    requestBuilder.append(line).append("\r\n");
                }
                // they ask to print the request
                System.out.println(requestBuilder.toString());
                HTTPRequest request = new HTTPRequest(requestBuilder.toString());
                HTTPResponseHandler responseHandler = new HTTPResponseHandler(request, out);

            } finally {
                clientSocket.close();
            }
        }
    }
}
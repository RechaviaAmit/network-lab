import java.io.*;
import java.net.*;
import java.util.concurrent.*;

public class WebServer {
    private static final int PORT = Integer.parseInt(Config.properties.getProperty("port"));
    private static final int MAX_CONNECTIONS = Integer.parseInt(Config.properties.getProperty("maxThreads"));

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
            BufferedReader in = null;
            DataOutputStream out = null;
            try {
                while (true) {
                    in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
                    out = new DataOutputStream(clientSocket.getOutputStream());

                    StringBuilder requestBuilder = new StringBuilder();
                    String line;
                    while (!(line = in.readLine()).isEmpty()) {
                        requestBuilder.append(line).append("\r\n");
                    }

                    while (in.ready()) {
                        requestBuilder.append((char) in.read());
                    }
                    // they ask to print the request
                    System.out.println(requestBuilder);
                    HTTPResponseHandler httpResponseHandler = new HTTPResponseHandler();
                    HTTPRequest request = null;
                    try {
                        request = new HTTPRequest(requestBuilder.toString());;
                    } catch (Exception e) {
                        httpResponseHandler.sendErrorResponse(400, "Bad Request", out);
                    }
                    if (request != null) {
                        try {
                            httpResponseHandler.handle(request, out);
                        } catch (Exception e) {
                            httpResponseHandler.sendErrorResponse(500, "Internal Server Error", out);
                        }
                    }
                }
            } catch (IOException e) {
                throw new RuntimeException(e);
            } finally {
                if (in != null) {
                    in.close();
                }
                if (out != null) {
                    out.close();
                }
            }
        }
    }
}
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MultiThreadedWebServer {

    private static final int PORT = 8080;
    private static final int MAX_THREADS = 10;
    private static final ExecutorService executorService = Executors.newFixedThreadPool(MAX_THREADS);

    public static void main(String[] args) {
        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            System.out.println("Web server started on port " + PORT);

            while (true) {
                Socket clientSocket = serverSocket.accept();
                executorService.execute(new RequestHandler(clientSocket));
            }
        } catch (IOException e) {
            System.err.println("Error starting web server: " + e.getMessage());
        }
    }

    static class RequestHandler implements Runnable {
        private Socket clientSocket;

        RequestHandler(Socket clientSocket) {
            this.clientSocket = clientSocket;
        }

        @Override
        public void run() {
            try (
                    BufferedReader reader = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
                    OutputStream outputStream = clientSocket.getOutputStream()
            ) {
                String request = reader.readLine();
                if (request != null && request.startsWith("GET")) {
                    sendHtmlResponse(outputStream, "<html><body><h1>Hi there!</h1></body></html>");
                }
            } catch (IOException e) {
                System.err.println("Error handling request: " + e.getMessage());
            } finally {
                try {
                    clientSocket.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        private void sendHtmlResponse(OutputStream outputStream, String htmlContent) throws IOException {
            String response = "HTTP/1.1 200 OK\r\n" +
                    "Content-Type: text/html\r\n" +
                    "\r\n" +
                    htmlContent;

            outputStream.write(response.getBytes());
            outputStream.flush();
        }

        private void sendResponse(OutputStream outputStream, String response) throws IOException {
            outputStream.write(response.getBytes());
            outputStream.flush();
        }
    }
}
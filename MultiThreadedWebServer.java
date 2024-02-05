import com.sun.net.httpserver.*;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MultiThreadedWebServer {
    private static final int PORT = 8080;
    private static final int MAX_THREADS = 10;
    private static final ExecutorService executorService = Executors.newFixedThreadPool(MAX_THREADS);

    public static void main(String[] args) throws IOException {
        HttpServer server = HttpServer.create(new InetSocketAddress(PORT), 0);
        server.createContext("/", new RequestHandler());
        server.setExecutor(executorService);
        server.start();

        System.out.println("Web server started on port " + PORT);
    }

    static class RequestHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange httpExchange) throws IOException {
            Headers requestHeaders = httpExchange.getRequestHeaders();
            System.out.println("Request headers: " + requestHeaders.entrySet());

            String requestURI = httpExchange.getRequestURI().toString();
            if (requestURI.endsWith("ico")) {
                serveIcon(httpExchange);
            }
            else if (requestURI.endsWith(".jpg") || requestURI.endsWith(".bmp") || requestURI.endsWith("gif") || requestURI.endsWith("png")) {
                serveImage(httpExchange);
            } else if (requestURI.endsWith("/")) {
                serveHtml(httpExchange);
            } else {
                serveUnRecognizedRequest(httpExchange);
            }
        }

        private void serveUnRecognizedRequest(HttpExchange httpExchange) throws IOException {
            Headers responseHeaders = httpExchange.getResponseHeaders();
            responseHeaders.set("Content-Type", "application/octet-stream");
            // check
            httpExchange.sendResponseHeaders(200, 0);

            System.out.println("Response headers: " + responseHeaders.entrySet());
        }

        private void serveIcon(HttpExchange httpExchange) throws IOException {
            Headers responseHeaders = httpExchange.getResponseHeaders();
            responseHeaders.set("Content-Type", "icon");
            // check
            httpExchange.sendResponseHeaders(200, 200);

            System.out.println("Response headers: " + responseHeaders.entrySet());
        }

        private void serveHtml(HttpExchange httpExchange) throws IOException {
            byte[] response = Files.readAllBytes(Paths.get("index.html"));

            Headers responseHeaders = httpExchange.getResponseHeaders();
            responseHeaders.set("Content-Type", "text/html");
            httpExchange.sendResponseHeaders(200, response.length);

            System.out.println("Response headers: " + responseHeaders.entrySet());

            OutputStream os = httpExchange.getResponseBody();
            os.write(response);
            os.close();
        }

        private void serveImage(HttpExchange httpExchange) throws IOException {
            String requestURI = httpExchange.getRequestURI().toString();
            String imgName = requestURI.substring(requestURI.lastIndexOf("/") + 1);

            byte[] response = Files.readAllBytes(Paths.get(imgName));

            Headers responseHeaders = httpExchange.getResponseHeaders();
            responseHeaders.set("Content-Type", "image");
            httpExchange.sendResponseHeaders(200, response.length);

            System.out.println("Response headers: " + responseHeaders.entrySet());

            OutputStream os = httpExchange.getResponseBody();
            os.write(response);
            os.close();
        }
    }
}

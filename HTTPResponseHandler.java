import java.io.*;
import java.nio.file.*;

public class HTTPResponseHandler {
    private static final String WWWROOT = Config.properties.getProperty("root");
    private HTTPRequest request;
    private PrintWriter out;

    public HTTPResponseHandler(HTTPRequest request, PrintWriter out) {
        this.request = request;
        this.out = out;
    }

    public void handle() throws IOException {
        // Check HTTP method
        if (!request.getType().equalsIgnoreCase("GET")) {
            sendErrorResponse(501, "Not Implemented");
            return;
        }

        // Check if the file exists
        Path filePath = Paths.get(WWWROOT + request.getRequestedPage());
        if (!Files.exists(filePath)) {
            sendErrorResponse(404, "Not Found");
            return;
        }

        // Read the file's content
        byte[] fileBytes = Files.readAllBytes(filePath);
        String fileContent = new String(fileBytes);

        // Create HTTP response header
        String responseHeader = String.format(
                "HTTP/1.1 200 OK\r\n" +
                        "content-type: text/html\r\n" +
                        "content-length: %d\r\n" +
                        "\r\n", fileBytes.length);

        // Print the header
        System.out.println(responseHeader);

        // Send full response to client
        out.write(responseHeader);
        out.write(fileContent);
        out.flush();
    }

    private void sendErrorResponse(int statusCode, String statusMessage) {
        String response = String.format(
                "HTTP/1.1 %d %s\r\n" +
                        "content-type: text/plain\r\n" +
                        "\r\n" +
                        "%d %s\r\n", statusCode, statusMessage, statusCode, statusMessage);

        out.write(response);
        out.flush();
    }
}

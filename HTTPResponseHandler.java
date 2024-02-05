import java.io.*;
import java.nio.file.*;
import java.util.Arrays;
import java.util.List;

public class HTTPResponseHandler {
    private static final String WWWROOT = Config.properties.getProperty("root");
    private HTTPRequest request;
    private DataOutputStream out;

    public HTTPResponseHandler(HTTPRequest request, DataOutputStream out) {
        this.request = request;
        this.out = out;
    }

    public void handle() throws IOException {
        List<String> supportedMethods = Arrays.asList("GET", "POST", "HEAD", "TRACE");
        // Check HTTP method
        if (!supportedMethods.contains(request.getType().toUpperCase())) {
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
        String contentType = request.isImage ? "image" : "text/html";
        // Create HTTP response header
        String responseHeader = String.format(
                "HTTP/1.1 200 OK\r\n" +
                        "content-type: %s\r\n" +
                        "content-length: %d\r\n" +
                        "\r\n", contentType, fileBytes.length);

        // Print the header
        System.out.println(responseHeader);

        // Send full response to client
        out.write(responseHeader.getBytes());
        out.write(fileBytes);
        out.flush();
    }

    private void sendErrorResponse(int statusCode, String statusMessage) throws IOException {
        String response = String.format(
                "HTTP/1.1 %d %s\r\n" +
                        "content-type: text/plain\r\n" +
                        "\r\n" +
                        "%d %s\r\n", statusCode, statusMessage, statusCode, statusMessage);

        out.write(response.getBytes());
        out.flush();
    }
}

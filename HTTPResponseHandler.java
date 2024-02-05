import java.io.*;
import java.nio.ByteBuffer;
import java.nio.file.*;

public class HTTPResponseHandler {
    private static final String WWWROOT = Config.properties.getProperty("root");

    public HTTPResponseHandler() {
    }

    public void handle(HTTPRequest request, DataOutputStream out) throws IOException {
        // Check HTTP method
        if (!request.getType().equalsIgnoreCase("GET")) {
            sendErrorResponse(501, "Not Implemented", out);
            return;
        }

        // Check if the file exists
        Path filePath = Paths.get(WWWROOT + request.getRequestedPage());
        if (!Files.exists(filePath)) {
            sendErrorResponse(404, "Not Found", out);
            return;
        }

        // Read the file's content
        byte[] fileBytes = Files.readAllBytes(filePath);
        String contentType = "application/octet-stream";
        if (request.isImage) {
            contentType = "image";
        } else if (request.isText) {
            contentType = "text/html";
        } else if (request.isIcon) {
            contentType = "icon";
        }
        // Create HTTP response header
        String responseHeader;
        if (request.isChunked) {
            responseHeader = String.format(
                    "HTTP/1.1 200 OK\r\n" +
                            "content-type: %s\r\n" +
                            "Transfer-Encoding: chunked\r\n" +
                            "\r\n", contentType);
        } else {
            responseHeader = String.format(
                    "HTTP/1.1 200 OK\r\n" +
                            "content-type: %s\r\n" +
                            "content-length: %d\r\n" +
                            "\r\n", contentType, fileBytes.length);
        }

        // Print the header
        System.out.println(responseHeader);

        // Send full response to client
        out.write(responseHeader.getBytes());
        if (request.isChunked) {
            ByteBuffer.wrap(fileBytes).asCharBuffer().chars().forEach(c -> {
                try {
                    String chunkSize = Integer.toHexString(1024); // Each chunk is 1 byte
                    out.write((chunkSize + "\r\n").getBytes());
                    out.write(c);
                    out.write("\r\n".getBytes());
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });
            out.write("0\r\n\r\n".getBytes()); // Last chunk
        } else {
            out.write(fileBytes);
        }
        out.flush();
    }


    public void sendErrorResponse(int statusCode, String statusMessage, DataOutputStream out) throws IOException {
        String response = String.format(
                "HTTP/1.1 %d %s\r\n" +
                        "content-type: text/plain\r\n" +
                        "\r\n" +
                        "%d %s\r\n", statusCode, statusMessage, statusCode, statusMessage);

        out.write(response.getBytes());
        out.flush();
    }
}

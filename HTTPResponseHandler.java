import java.io.*;
import java.nio.ByteBuffer;
import java.nio.file.*;
import java.util.Arrays;
import java.util.List;

public class HTTPResponseHandler {
    private static final String WWWROOT = Config.properties.getProperty("root");

    public HTTPResponseHandler() {
    }

    public void handle(HTTPRequest request, DataOutputStream out) throws IOException {
        List<String> supportedMethods = Arrays.asList("GET", "POST", "HEAD", "TRACE");
        if (!supportedMethods.contains(request.getType())) {
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
            int chunkSizeBytes = 1024; // Size of each chunk
            int start = 0;

            while (start < fileBytes.length) {
                int end = Math.min(start + chunkSizeBytes, fileBytes.length);
                int chunkLength = end - start;

                // Write chunk size in hex
                String chunkSizeHex = Integer.toHexString(chunkLength) + "\r\n";
                out.write(chunkSizeHex.getBytes());

                // Write chunk data
                out.write(fileBytes, start, chunkLength);

                // Write end of chunk
                out.write("\r\n".getBytes());
                start = end;
            }
            out.write("0\r\n\r\n".getBytes()); // Last chunk
        }  else {
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

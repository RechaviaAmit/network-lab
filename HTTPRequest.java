import java.util.HashMap;
import java.util.Map;

public class HTTPRequest {
    private String type;
    private String requestedPage;
    private boolean isImage;
    private long contentLength;
    private String referer;
    private String userAgent;
    private Map<String, String> parameters = new HashMap<>();

    public HTTPRequest(String requestHeader) {
        String[] lines = requestHeader.split("\r\n");
        String[] requestLine = lines[0].split(" ");
        type = requestLine[0];
        requestedPage = requestLine[1];
        isImage = requestedPage.matches(".*\\.(jpg|bmp|gif)$");

        for (String line : lines) {
            if (line.startsWith("Content-Length: ")) {
                contentLength = Long.parseLong(line.substring(16));
            } else if (line.startsWith("Referer: ")) {
                referer = line.substring(9);
            } else if (line.startsWith("User-Agent: ")) {
                userAgent = line.substring(12);
            }
        }

        if (type.equalsIgnoreCase("GET") && requestedPage.contains("?")) {
            String paramString = requestedPage.split("\\?")[1];
            String[] paramPairs = paramString.split("&");
            for (String pair : paramPairs) {
                String[] keyValue = pair.split("=");
                parameters.put(keyValue[0], keyValue[1]);
            }
        }
    }

    public String getType() {
        return type;
    }

    public String getRequestedPage() {
        return requestedPage;
    }
}

import java.util.HashMap;
import java.util.Map;

public class HTTPRequest {
    private String type;
    private String requestedPage;
    public boolean isImage;
    public boolean isText;
    public boolean isIcon;
    public boolean isChunked = false;
    private long contentLength;
    private String referer;
    private String userAgent;
    public Map<String, String> parameters = new HashMap<>();

    public HTTPRequest(String requestHeader) {
        String[] lines = requestHeader.split("\r\n");
        String[] requestLine = lines[0].split(" ");
        type = requestLine[0];

        // Split requested page and parameters  
        String[] pageAndParams = requestLine[1].split("\\?");
        requestedPage = pageAndParams[0];
        if (requestedPage.equals("/")) {
            requestedPage = requestedPage + Config.properties.getProperty("defaultPage");
        }
        // Do not allow users to surf “outside” the server’s root directory
        requestedPage = requestedPage.replace("/../", "/");

        isImage = requestedPage.matches(".*\\.(jpg|bmp|gif|png)$");
        isText = requestedPage.matches(".*\\.(html)$");
        isIcon = requestedPage.matches(".*\\.(ico)$");

        if (pageAndParams.length > 1) {
            String paramString = pageAndParams[1];
            String[] paramPairs = paramString.split("&");
            for (String pair : paramPairs) {
                String[] keyValue = pair.split("=");
                parameters.put(keyValue[0], keyValue[1]);
            }
        }

        for (String line : lines) {
            if (line.startsWith("Content-Length: ")) {
                contentLength = Long.parseLong(line.substring(16));
            } else if (line.startsWith("Referer: ")) {
                referer = line.substring(9);
            } else if (line.startsWith("User-Agent: ")) {
                userAgent = line.substring(12);
            } else if (line.replace(" ", "").equals("chunked:yes")) {
                isChunked = true;
            } else if (line.contains("&")) {
                String[] paramPairs = line.split("&");
                for (String pair : paramPairs) {
                    String[] keyValue = pair.split("=");
                    parameters.put(keyValue[0], keyValue[1]);
                }
            }
        }
    }

    public String getType() {
        return type;
    }

    public String getRequestedPage() {
        return requestedPage;
    }

    public boolean isImage() {
        return isImage;
    }

    // Getters and setters omitted for brevity.  
}  

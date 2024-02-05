import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.*;
import java.util.*;
import java.util.concurrent.*;

public class ChatServer {
    private static final int PORT = 9922;
    private static int userCount = -1;
    private static List<ChatHandler> handlers = new ArrayList<>();
    private static final int MAX_THREADS = 3;
    private static ExecutorService executorService = Executors.newFixedThreadPool(MAX_THREADS);

    public static void main(String[] args) throws UnknownHostException {
        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            System.out.println("Chat server started on port " + PORT);

            while (true) {
                Socket socket = serverSocket.accept();
                System.out.println("managed to accept connection");
                ChatHandler handler = new ChatHandler(socket);
                handlers.add(handler);
                executorService.execute(handler);
                userCount++;
                broadcastExceptCurrent(handler.getIp() + " joined" + "\n", handler);
            }
        } catch (IOException e) {
            System.out.println("Error starting chat server: " + e.getMessage());
        }
    }

    public static synchronized void broadcast(String message) throws IOException {
        System.out.println(message);
        for (ChatHandler handler : handlers) {
            handler.sendMessage(message);
        }
    }

    public static synchronized void broadcastExceptCurrent(String message, ChatHandler currentHandler) throws IOException {
        System.out.println(message);
        for (ChatHandler handler : handlers) {
            if (handler != currentHandler) {
                handler.sendMessage(message);
            }
        }
    }

    public static synchronized void removeHandler(ChatHandler handler) {
        handlers.remove(handler);
        userCount--;
    }

    public static synchronized int getUserCount() {
        return userCount;
    }
}

class ChatHandler implements Runnable {
    private Socket socket;
    private BufferedReader in;
    private DataOutputStream out;
    private String ip;

    public ChatHandler(Socket socket) throws IOException {
        this.socket = socket;
        this.ip = socket.getInetAddress().getHostAddress();
        this.out = new DataOutputStream(socket.getOutputStream());
    }

    public String getIp() {
        return ip;
    }

    public void sendMessage(String message) throws IOException {
        out.writeBytes(message + "\n");
    }

    @Override
    public void run() {
        try {
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

            out.writeBytes("Welcome to RUNI Computer Networks 2024 chat server! There are " + ChatServer.getUserCount() + " users connected." + "\n");

            String inputLine;
            while ((inputLine = in.readLine()) != null) {
                ChatServer.broadcastExceptCurrent("(" + ip + ":" + socket.getPort() + "): " + inputLine, this);
            }
        } catch (IOException e) {
            System.out.println("Error handling chat client: " + e.getMessage());
        }
        finally {
            try {
                socket.close();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            ChatServer.removeHandler(this);
        }
    }
}
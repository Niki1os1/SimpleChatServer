import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

public class SimpleChatServer {
    private static final int PORT = 2021;

    public static void main(String[] args) throws IOException {
        ServerSocket serverSocket = new ServerSocket(PORT);
        List<OneUser> users = new ArrayList<>();
        while (true) {
            Socket s = serverSocket.accept();
            System.out.println("socket connected");
            OneUser oneUser = new OneUser(users, s);
            users.add(oneUser);
            Thread thread = new Thread(oneUser);
            thread.start();
        }

    }
}

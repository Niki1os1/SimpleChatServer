import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class OneUser implements Runnable {
    private String userName = "anonymous";

    private final List<OneUser> users;

    private Socket socket;

    private PrintWriter writer;

    private BufferedReader reader;

    private Map<String, String> chatTopics = new HashMap<>();

    public String getUserName() {
        return userName;
    }

    public List<OneUser> getUsers() {
        return users;
    }

    public Socket getSocket() {
        return socket;
    }

    public PrintWriter getWriter() {
        return writer;
    }

    public BufferedReader getReader() {
        return reader;
    }

    public OneUser(List<OneUser> users, Socket socket) {
        this.users = users;
        this.socket = socket;
    }

    @Override
    public void run() {
        try {
            writer = new PrintWriter(socket.getOutputStream());
            InputStreamReader in = new InputStreamReader(socket.getInputStream());
            reader = new BufferedReader(in);

            String action;
            do {
                action = reader.readLine();
                if ("1".equals(action)) {
                    enterName();
                } else if ("2".equals(action)) {
                    giveListOfUsersName();
                } else if ("3".equals(action)) {
                    sendAllMsg();
                } else if ("4".equals(action)) {
                    sendPrivateMsg();
                } else if ("5".equals(action)) {
                    setChatTopic();
                } else if ("6".equals(action)) {
                    getChatTopic();
                } else if ("9".equals(action)) {
                    readMessages();
                }
            } while (!":q".equals(action));
            System.out.println("socket disconnect");
            users.remove(this);
            reader.close();
            writer.close();
            socket.close();
        } catch (IOException e) {
            users.remove(this);
            System.out.println("socket disconnect");
        }
    }

    private void enterName() throws IOException {
        String name;
        do {
            name = reader.readLine();
        } while (name.isBlank() || name.isEmpty());
        this.userName = name;
    }

    private void giveListOfUsersName() {
        for (OneUser user : users) {
            writer.println(user.getUserName());
        }
        writer.println("end");
        writer.flush();
    }

    private void sendAllMsg() throws IOException {
        String msg = reader.readLine();
        if (msg == null || msg.isEmpty() || msg.isBlank()) {
            return;
        }
        for (OneUser user : users) {
            if (user != this) {
                user.getWriter().println(userName + ": " + msg);

            }
        }
    }

    private void sendPrivateMsg() throws IOException {
        String name = reader.readLine();
        OneUser oneUser = null;
        for (OneUser user: users) {
            if (user.getUserName().equals(name)) {
                oneUser = user;
                break;
            }
        };
        if (oneUser == null) {
            writer.println("404");
            writer.flush();
        } else {
            writer.println("200");
            writer.flush();
            String msg = reader.readLine();
            oneUser.writer.println(userName + ": " + msg);
        }
    }

    private void readMessages() throws IOException {
        StringBuilder messages = new StringBuilder();

        for (OneUser user : users) {
            try {
                if (user.socket != socket && user.socket.getInputStream().available() > 0) {

                    messages.append(user.reader.readLine());
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        writer.println(messages);
        writer.println(":end");
        writer.flush();
    }

    private void getChatTopic() throws IOException {
        String name = reader.readLine();
        if (chatTopics.containsKey(name)) {
            writer.println("200");
            writer.println(chatTopics.get(name));
        } else {
            writer.println("405");
        }
        writer.flush();
    }

    private void setChatTopic() throws IOException {
        String name = reader.readLine();
        OneUser oneUser = null;
        for (OneUser user: users) {
            if (user.getUserName().equals(name)) {
                oneUser = user;
                break;
            }
        };
        if (oneUser == null || oneUser == this) {
            writer.println("404");
            writer.flush();
        } else {
            writer.println("200");
            writer.flush();
            String newTopic = reader.readLine();
            oneUser.chatTopics.put(userName, newTopic);
            chatTopics.put(name, newTopic);
        }
    }
}

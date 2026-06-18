package mapClient;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

public class ServerConnection {
    private Socket socket;
    private ObjectOutputStream out;
    private ObjectInputStream in;

    public ServerConnection(String host, int port) throws IOException {
        socket = new Socket(host, port);
        out = new ObjectOutputStream(socket.getOutputStream());
        in = new ObjectInputStream(socket.getInputStream());
    }

    public String sendTableName(String tableName) throws IOException, ClassNotFoundException {
        out.writeObject(0);
        out.writeObject(tableName);
        return in.readObject().toString();
    }

    public String readAnswer() throws IOException, ClassNotFoundException {
        return in.readObject().toString();
    }

    public void startLearning() throws IOException {
        out.writeObject(1);
    }

    public String loadTree(String tableName) throws IOException, ClassNotFoundException {
        out.writeObject(2);
        out.writeObject(tableName);
        return in.readObject().toString();
    }

    public void startPrediction() throws IOException {
        out.writeObject(3);
    }

    public void sendChoice(int path) throws IOException {
        out.writeObject(path);
    }

    public void close() throws IOException {
        in.close();
        out.close();
        socket.close();
    }
}

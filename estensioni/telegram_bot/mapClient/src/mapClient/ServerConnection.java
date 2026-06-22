package mapClient;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Collection;

public class ServerConnection {
    private Socket socket;
    private ObjectOutputStream out;
    private ObjectInputStream in;

    /**
     * Opens a connection to the map server.
     *
     * @param socketTimeoutMs read timeout in milliseconds (0 = infinite).
     *                        If the server stops responding, readObject() throws
     *                        SocketTimeoutException, which propagates to MapBot's
     *                        catch block and triggers resetUser() instead of hanging.
     */
    public ServerConnection(String host, int port, int socketTimeoutMs) throws IOException {
        socket = new Socket(host, port);
        socket.setSoTimeout(socketTimeoutMs);
        out = new ObjectOutputStream(socket.getOutputStream());
        in = new ObjectInputStream(socket.getInputStream());
    }

    public ArrayList<String> showBranches() throws IOException, ClassNotFoundException {
        Object obj = in.readObject();
        ArrayList<String> branches = new ArrayList<>();
        if (obj instanceof Collection<?>) {
            for (Object item : (Collection<?>) obj) {
                branches.add(String.valueOf(item));
            }
        }
        return branches;
    }

    public ArrayList<String> showTables(boolean learn) throws IOException, ClassNotFoundException {
        out.writeObject(learn ? 4 : 5);
        out.flush();

        ArrayList<String> tables = new ArrayList<>();
        Object obj = in.readObject();
        if (obj instanceof Collection<?>) {
            for (Object item : (Collection<?>) obj) {
                tables.add(String.valueOf(item));
            }
        }
        return tables;
    }

    public String sendTableName(String tableName) throws IOException, ClassNotFoundException {
        out.writeObject(0);
        out.writeObject(tableName);
        out.flush();
        return readAnswer();
    }

    public String readAnswer() throws IOException, ClassNotFoundException {
        Object obj = in.readObject();
        return obj == null ? "" : obj.toString();
    }

    public void startLearning() throws IOException {
        out.writeObject(1);
        out.flush();
    }

    public String loadTree(String tableName) throws IOException, ClassNotFoundException {
        out.writeObject(2);
        out.writeObject(tableName);
        out.flush();
        return readAnswer();
    }

    public void startPrediction() throws IOException {
        out.writeObject(3);
        out.flush();
    }

    public void sendChoice(int path) throws IOException {
        out.writeObject(path);
        out.flush();
    }

    public void close() throws IOException {
        try {
            out.close();
        } finally {
            try {
                in.close();
            } finally {
                socket.close();
            }
        }
    }
}

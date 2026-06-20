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

    public ServerConnection(String host, int port) throws IOException {
        socket = new Socket(host, port);
        out = new ObjectOutputStream(socket.getOutputStream());
        in = new ObjectInputStream(socket.getInputStream());
    }

		public ArrayList<String> showTables(Boolean learn) throws IOException, ClassNotFoundException {
			if (learn) {
				out.writeObject(4);
			} else {
				out.writeObject(5);
			}

			ArrayList<String> tables = new ArrayList<>();
			Object obj = in.readObject();
			if (obj instanceof Collection<?>) {
				for (Object item : (Collection<?>) obj) {
					tables.add(String.valueOf(item));
				}
			}
			return tables;
		}

		//TODO: Rinominare metodo in learnTree
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

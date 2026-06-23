package application;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.util.ArrayList;

public class RtClient {

    private ObjectOutputStream out;
    private ObjectInputStream  in;
    private final int          port = 8080;

    public RtClient(String ip) throws IOException {
        InetAddress addr = InetAddress.getByName(ip);
        Socket socket    = new Socket(addr, port);
        out = new ObjectOutputStream(socket.getOutputStream());
        in  = new ObjectInputStream(socket.getInputStream());
    }

    // ─────────────────────────────────────────────────────────────────────────
    //  LOAD FROM DB  
    // ─────────────────────────────────────────────────────────────────────────

    public void storeTableFromDb(String tableName)
            throws IOException, ClassNotFoundException, ServerException {
        out.writeObject(0);
        out.writeObject(tableName);
        out.flush(); 

        String result = in.readObject().toString();
        if (!result.equals("Table found!")) {          
            throw new ServerException("Table not found: " + result);
        }

        String ok = in.readObject().toString();
        if (!ok.equals("OK")) {
            throw new ServerException("Unexpected server response: " + ok);
        }
    }

    public String learningFromDbTable()
            throws IOException, ClassNotFoundException, ServerException {
        out.writeObject(1);
        out.flush(); 
        
        String result = in.readObject().toString();
        if (!result.equals("OK")) {
            throw new ServerException("Tree build error: " + result);
        }

        return startPrediction();
    }

    public String learningFromDbTable(int branch)
            throws IOException, ClassNotFoundException, ServerException {
        return sendBranchAndRead(branch);
    }

    // ─────────────────────────────────────────────────────────────────────────
    //  LOAD FROM FILE  
    // ─────────────────────────────────────────────────────────────────────────

    public String learningFromFile(String tableName)
            throws IOException, ClassNotFoundException, ServerException {
        out.writeObject(2);
        out.writeObject(tableName);
        out.flush(); 

        String result = in.readObject().toString();
        if (!result.equals("Table found!")) {          
            throw new ServerException("File not found: " + result);
        }

        String ok = in.readObject().toString();
        if (!ok.equals("OK")) {
            throw new ServerException("Unexpected server response: " + ok);
        }

        return startPrediction();
    }

    public String learningFromFile(int branch)
            throws IOException, ClassNotFoundException, ServerException {
        return sendBranchAndRead(branch);
    }

    // ─────────────────────────────────────────────────────────────────────────
    //  GET TABLE NAMES (action 4) - AGGIORNATO CON CONTROLLO ERRORE
    // ─────────────────────────────────────────────────────────────────────────

    @SuppressWarnings("unchecked")
    public ArrayList<String> getTableNames()
            throws IOException, ClassNotFoundException, ServerException {
        out.writeObject(4);
        out.flush(); 
        
        Object response = in.readObject();
        
        // Se il server risponde con un testo (String) invece della lista, è un errore!
        if (response instanceof String) {
            throw new ServerException("Errore Server: " + response);
        }
        
        return (ArrayList<String>) response;
    }

    // ─────────────────────────────────────────────────────────────────────────
    //  GET FILE NAMES (action 5) - AGGIORNATO CON CONTROLLO ERRORE
    // ─────────────────────────────────────────────────────────────────────────

    @SuppressWarnings("unchecked")
    public ArrayList<String> getFileNames()
            throws IOException, ClassNotFoundException, ServerException {
        out.writeObject(5);
        out.flush(); 
        
        Object response = in.readObject();
        
        // Se il server risponde con un testo (String) invece della lista, è un errore!
        if (response instanceof String) {
            throw new ServerException("Errore Server: " + response);
        }
        
        return (ArrayList<String>) response;
    }

    // ─────────────────────────────────────────────────────────────────────────
    //  HELPER PRIVATI
    // ─────────────────────────────────────────────────────────────────────────

    private String startPrediction()
            throws IOException, ClassNotFoundException, ServerException {
        out.writeObject(3);
        out.flush(); 

        String signal = in.readObject().toString();
        if (!signal.equals("QUERY")) {
            throw new ServerException("Expected 'QUERY', got: " + signal);
        }

        return in.readObject().toString();
    }

    private String sendBranchAndRead(int branch)
            throws IOException, ClassNotFoundException, ServerException {
        out.writeObject(branch);
        out.flush(); 

        String response = in.readObject().toString();
        if (!response.equals("QUERY")) {
            throw new ServerException("Invalid branch or server error: " + response);
        }

        String next = in.readObject().toString();
        if (next.equals("OK")) {
            String prediction = in.readObject().toString();
            return "PREDICTED:" + prediction;
        }

        return next;
    }
    
    public void close() {
    try {
        if (in != null) in.close();
        if (out != null) out.close();
    } catch (IOException e) {
        // Ignora eventuali eccezioni durante la chiusura
    }
    }
}
package mapClient;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Collection;

/**
 * Gestisce la comunicazione di rete tra il bot ({@link MapBot}) e il server
 * di data mining.
 * <p>
 * Apre un socket verso il server e scambia oggetti tramite gli stream di
 * input/output. I metodi di questa classe inviano al server i codici azione
 * (numeri interi) che indicano l'operazione da eseguire (caricamento dati,
 * apprendimento, predizione, ecc.) e ne leggono le risposte. Ogni utente del
 * bot dispone di una propria istanza di {@code ServerConnection}.
 */
public class ServerConnection {
    /** Socket connesso al server. */
    private Socket socket;
    /** Stream usato per inviare oggetti al server. */
    private ObjectOutputStream out;
    /** Stream usato per ricevere oggetti dal server. */
    private ObjectInputStream in;

    /**
     * Apre una connessione verso il server di data mining.
     *
     * @param host l'indirizzo del server
     * @param port la porta su cui il server è in ascolto
     * @param socketTimeoutMs timeout di lettura in millisecondi (0 = infinito).
     *                        Se il server smette di rispondere, {@code readObject()}
     *                        solleva una {@code SocketTimeoutException} che viene
     *                        gestita da {@link MapBot}, evitando che il bot resti
     *                        bloccato in attesa.
     * @throws IOException se la connessione o la creazione degli stream fallisce
     */
    public ServerConnection(String host, int port, int socketTimeoutMs) throws IOException {
        socket = new Socket(host, port);
        socket.setSoTimeout(socketTimeoutMs);
        out = new ObjectOutputStream(socket.getOutputStream());
        in = new ObjectInputStream(socket.getInputStream());
    }

    /**
     * Legge dal server l'elenco dei rami disponibili per il nodo di split corrente.
     * <p>
     * Ogni ramo è rappresentato come stringa (in genere l'indice della scelta)
     * ed è usato dal bot per costruire i pulsanti tra cui l'utente sceglie.
     *
     * @return la lista dei rami selezionabili
     * @throws IOException se si verifica un errore di comunicazione
     * @throws ClassNotFoundException se l'oggetto ricevuto non è riconosciuto
     */
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

    /**
     * Richiede al server l'elenco degli elementi tra cui l'utente può scegliere.
     * <p>
     * Se {@code learn} è {@code true} viene chiesto l'elenco delle tabelle del
     * database (codice azione 4); altrimenti viene chiesto l'elenco degli alberi
     * già salvati su disco (codice azione 5).
     *
     * @param learn {@code true} per ottenere le tabelle del database,
     *              {@code false} per ottenere gli alberi salvati
     * @return la lista dei nomi disponibili
     * @throws IOException se si verifica un errore di comunicazione
     * @throws ClassNotFoundException se l'oggetto ricevuto non è riconosciuto
     */
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

    /**
     * Invia al server il nome della tabella da cui caricare i dati di addestramento
     * (codice azione 0).
     *
     * @param tableName il nome della tabella scelta dall'utente
     * @return la risposta del server ({@code "Table found!"} se la tabella esiste)
     * @throws IOException se si verifica un errore di comunicazione
     * @throws ClassNotFoundException se l'oggetto ricevuto non è riconosciuto
     */
    public String sendTableName(String tableName) throws IOException, ClassNotFoundException {
        out.writeObject(0);
        out.writeObject(tableName);
        out.flush();
        return readAnswer();
    }

    /**
     * Legge la prossima risposta inviata dal server.
     *
     * @return la risposta come stringa, oppure stringa vuota se l'oggetto ricevuto è {@code null}
     * @throws IOException se si verifica un errore di comunicazione
     * @throws ClassNotFoundException se l'oggetto ricevuto non è riconosciuto
     */
    public String readAnswer() throws IOException, ClassNotFoundException {
        Object obj = in.readObject();
        return obj == null ? "" : obj.toString();
    }

    /**
     * Chiede al server di costruire l'albero di regressione e di salvarlo su disco
     * (codice azione 1).
     *
     * @throws IOException se si verifica un errore di comunicazione
     */
    public void startLearning() throws IOException {
        out.writeObject(1);
        out.flush();
    }

    /**
     * Chiede al server di caricare un albero di regressione precedentemente salvato
     * (codice azione 2).
     *
     * @param tableName il nome dell'albero salvato da caricare
     * @return la risposta del server ({@code "Table found!"} se l'albero esiste)
     * @throws IOException se si verifica un errore di comunicazione
     * @throws ClassNotFoundException se l'oggetto ricevuto non è riconosciuto
     */
    public String loadTree(String tableName) throws IOException, ClassNotFoundException {
        out.writeObject(2);
        out.writeObject(tableName);
        out.flush();
        return readAnswer();
    }

    /**
     * Chiede al server di avviare la fase di predizione interattiva
     * (codice azione 3).
     *
     * @throws IOException se si verifica un errore di comunicazione
     */
    public void startPrediction() throws IOException {
        out.writeObject(6);
        out.flush();
    }

    /**
     * Invia al server la scelta dell'utente, cioè il ramo dell'albero da seguire
     * durante la predizione.
     *
     * @param path l'indice del ramo selezionato
     * @throws IOException se si verifica un errore di comunicazione
     */
    public void sendChoice(int path) throws IOException {
        out.writeObject(path);
        out.flush();
    }

    /**
     * Chiude la connessione, rilasciando gli stream e il socket.
     *
     * @throws IOException se si verifica un errore durante la chiusura
     */
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

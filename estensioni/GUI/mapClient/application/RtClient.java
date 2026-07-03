package application;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.util.ArrayList;

/**
 * Gestisce la comunicazione tra il client grafico e il server.
 * <p>
 * Apre una connessione socket verso il server e mette a disposizione i metodi
 * per costruire o caricare un albero di regressione, navigare nella fase di
 * predizione e recuperare gli elenchi di tabelle e archivi disponibili. Ogni
 * metodo traduce uno scambio di messaggi con il server e, se la risposta non è
 * quella attesa, solleva una {@link ServerException}.
 */
public class RtClient {

    /** Stream usato per inviare richieste al server. */
    private ObjectOutputStream out;

    /** Stream usato per ricevere le risposte dal server. */
    private ObjectInputStream  in;

    /** Porta su cui il server è in ascolto. */
    private final int          port = 8080;

    /** Indirizzo IP del server, conservato per eventuali riconnessioni. */
    private final String ip;

    /**
     * Crea un nuovo client e apre immediatamente la connessione verso il server.
     *
     * @param ip l'indirizzo IP del server a cui connettersi
     * @throws IOException se la connessione o la creazione degli stream fallisce
     */
    public RtClient(String ip) throws IOException {
        this.ip = ip;
        connect();
    }

    /**
     * Apre (o riapre) la connessione verso il server e inizializza gli stream.
     *
     * @throws IOException se la connessione o la creazione degli stream fallisce
     */
    private void connect() throws IOException {
        InetAddress addr = InetAddress.getByName(ip);
        Socket socket    = new Socket(addr, port);
        out = new ObjectOutputStream(socket.getOutputStream());
        in  = new ObjectInputStream(socket.getInputStream());
    }

    /**
     * Ripristina la connessione dopo un errore di protocollo: il server, in caso di
     * tabella o archivio non trovato, resta in attesa di un nuovo nome; riaprendo la
     * connessione gli stream tornano sincronizzati.
     */
    private void resetConnection() {
        close();
        try {
            connect();
        } catch (IOException ignored) {
        }
    }

    /**
     * Chiede al server di caricare i dati di addestramento dalla tabella indicata.
     *
     * @param tableName il nome della tabella del database da cui leggere i dati
     * @throws IOException se si verifica un errore di comunicazione
     * @throws ClassNotFoundException se la risposta del server non è leggibile
     * @throws ServerException se la tabella non viene trovata o la risposta è inattesa
     */
    public void storeTableFromDb(String tableName)
            throws IOException, ClassNotFoundException, ServerException {
				out.writeObject(0);
        out.writeObject(tableName);
        out.flush();

        String result = in.readObject().toString();
        if (!result.equals("Table found!")) {
						resetConnection();
            throw new ServerException("Table not found: " + result);
        }

        String ok = in.readObject().toString();
        if (!ok.equals("OK")) {
            throw new ServerException("Unexpected server response: " + ok);
        }
    }

    /**
     * Chiede al server di costruire l'albero di regressione dai dati caricati e
     * avvia la fase di predizione.
     *
     * @return la prima risposta della fase di predizione (query o classe predetta)
     * @throws IOException se si verifica un errore di comunicazione
     * @throws ClassNotFoundException se la risposta del server non è leggibile
     * @throws ServerException se la costruzione dell'albero fallisce
     */
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

    /**
     * Invia al server il ramo scelto dall'utente e ne legge la risposta, durante
     * la navigazione di un albero costruito da una tabella del database.
     *
     * @param branch l'indice del ramo selezionato
     * @return la risposta del server (nuova query o classe predetta)
     * @throws IOException se si verifica un errore di comunicazione
     * @throws ClassNotFoundException se la risposta del server non è leggibile
     * @throws ServerException se il ramo non è valido o la risposta è inattesa
     */
    public String learningFromDbTable(int branch)
            throws IOException, ClassNotFoundException, ServerException {
        return sendBranchAndRead(branch);
    }


    /**
     * Chiede al server di caricare un albero di regressione salvato su file e
     * avvia la fase di predizione.
     *
     * @param tableName il nome dell'archivio (file {@code .dmp}) da caricare
     * @return la prima risposta della fase di predizione (query o classe predetta)
     * @throws IOException se si verifica un errore di comunicazione
     * @throws ClassNotFoundException se la risposta del server non è leggibile
     * @throws ServerException se il file non viene trovato o la risposta è inattesa
     */
    public String learningFromFile(String tableName)
            throws IOException, ClassNotFoundException, ServerException {
        out.writeObject(2);
        out.writeObject(tableName);
        out.flush();

        String result = in.readObject().toString();
        if (!result.equals("Table found!")) {
						resetConnection();
            throw new ServerException("File not found: " + result);
        }

        String ok = in.readObject().toString();
        if (!ok.equals("OK")) {
            throw new ServerException("Unexpected server response: " + ok);
        }

        return startPrediction();
    }

    /**
     * Invia al server il ramo scelto dall'utente e ne legge la risposta, durante
     * la navigazione di un albero caricato da file.
     *
     * @param branch l'indice del ramo selezionato
     * @return la risposta del server (nuova query o classe predetta)
     * @throws IOException se si verifica un errore di comunicazione
     * @throws ClassNotFoundException se la risposta del server non è leggibile
     * @throws ServerException se il ramo non è valido o la risposta è inattesa
     */
    public String learningFromFile(int branch)
            throws IOException, ClassNotFoundException, ServerException {
        return sendBranchAndRead(branch);
    }


    /**
     * Richiede al server l'elenco delle tabelle disponibili nel database.
     *
     * @return la lista dei nomi delle tabelle
     * @throws IOException se si verifica un errore di comunicazione
     * @throws ClassNotFoundException se la risposta del server non è leggibile
     * @throws ServerException se il server segnala un errore al posto della lista
     */
    @SuppressWarnings("unchecked")
    public ArrayList<String> getTableNames()
            throws IOException, ClassNotFoundException, ServerException {
        out.writeObject(4);
        out.flush();

        Object response = in.readObject();

        if (response instanceof String) {
            throw new ServerException("Errore Server: " + response);
        }

        return (ArrayList<String>) response;
    }


    /**
     * Richiede al server l'elenco degli archivi ({@code .dmp}) disponibili.
     *
     * @return la lista dei nomi dei file salvati
     * @throws IOException se si verifica un errore di comunicazione
     * @throws ClassNotFoundException se la risposta del server non è leggibile
     * @throws ServerException se il server segnala un errore al posto della lista
     */
    @SuppressWarnings("unchecked")
    public ArrayList<String> getFileNames()
            throws IOException, ClassNotFoundException, ServerException {
        out.writeObject(5);
        out.flush();

        Object response = in.readObject();

        if (response instanceof String) {
            throw new ServerException("Errore Server: " + response);
        }

        return (ArrayList<String>) response;
    }


    /**
     * Avvia la fase di predizione inviando il relativo codice azione e attende
     * dal server il segnale {@code "QUERY"} seguito dalla prima domanda.
     *
     * @return la prima domanda formulata dal server
     * @throws IOException se si verifica un errore di comunicazione
     * @throws ClassNotFoundException se la risposta del server non è leggibile
     * @throws ServerException se il server non invia il segnale atteso
     */
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

    /**
     * Invia il ramo scelto al server e interpreta la risposta: se il percorso è
     * terminato restituisce la classe predetta (prefissata con {@code "PREDICTED:"}),
     * altrimenti la domanda successiva.
     *
     * @param branch l'indice del ramo selezionato
     * @return la classe predetta o la domanda successiva
     * @throws IOException se si verifica un errore di comunicazione
     * @throws ClassNotFoundException se la risposta del server non è leggibile
     * @throws ServerException se il ramo non è valido o si verifica un errore lato server
     */
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

    /**
     * Chiude gli stream e, di conseguenza, la connessione con il server.
     * Eventuali errori durante la chiusura vengono ignorati.
     */
    public void close() {
    try {
        if (in != null) in.close();
        if (out != null) out.close();
    } catch (IOException e) {
    }
    }
}

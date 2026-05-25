package server;

import java.io.File;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

import data.*;
import tree.*;

/**
 * Gestisce la comunicazione con un singolo client connesso in un thread dedicato.
 * Ogni istanza gestisce il proprio {@link Socket}, {@link ObjectInputStream} e
 * {@link ObjectOutputStream}, ed elabora una sequenza di codici azione inviati dal
 * client per guidare il processo di scoperta o predizione dell'albero di regressione.
 *
 * <p>Codici azione supportati:
 * <ul>
 *   <li>{@code 0} – Carica i dati di addestramento da una tabella del database.</li>
 *   <li>{@code 1} – Costruisce un albero di regressione e lo serializza su disco.</li>
 *   <li>{@code 2} – Deserializza un albero di regressione precedentemente salvato.</li>
 *   <li>{@code 3} – Esegue una query di predizione interattiva sull'albero caricato.</li>
 * </ul>
 */
class ServerOneClient extends Thread {
	/** Il socket connesso al client. */
	private Socket socket;

	/** Stream utilizzato per ricevere oggetti dal client. */
	private ObjectInputStream in;

	/** Stream utilizzato per inviare oggetti al client. */
	private ObjectOutputStream out;

	/**
	 * Costruisce un nuovo {@code ServerOneClient} per il socket fornito, inizializza
	 * gli stream di oggetti e avvia immediatamente il thread.
	 *
	 * <p><strong>Nota:</strong> {@code out} viene inizializzato prima di {@code in} per
	 * evitare un deadlock: il costruttore di {@link ObjectInputStream} si blocca finché
	 * il lato remoto non ha scritto l'intestazione del proprio stream, quindi lo stream
	 * di output locale deve essere inizializzato per primo.
	 *
	 * @param s il {@link Socket} che rappresenta la connessione accettata dal client
	 * @throws IOException se si verifica un errore di I/O durante la creazione degli stream
	 */
	public ServerOneClient(Socket s) throws IOException {
		this.socket = s;

		this.in = new ObjectInputStream(this.socket.getInputStream());
		this.out = new ObjectOutputStream(this.socket.getOutputStream());

		this.start();
	}

	/**
	 * Ciclo di esecuzione principale del thread di gestione del client.
	 *
	 * <p>Legge i codici azione interi dal client e li smista alla logica appropriata.
	 * Il ciclo termina quando il client si disconnette o si verifica un errore di I/O
	 * non recuperabile. Le risorse socket e stream vengono sempre rilasciate nel blocco
	 * {@code finally}.
	 *
	 * <p>Semantica dei codici azione:
	 * <ul>
	 *   <li><b>0 – Apprendimento da DB:</b> legge ripetutamente un nome di tabella dal
	 *       client, costruisce un oggetto {@link Data} da quella tabella e risponde con
	 *       {@code "Table found!"} in caso di successo o con un messaggio di errore in
	 *       caso di fallimento, fino a ricevere un nome valido. Invia {@code "OK"} al
	 *       termine.</li>
	 *   <li><b>1 – Costruzione e salvataggio albero:</b> costruisce un
	 *       {@link RegressionTree} dall'insieme di addestramento corrente, lo serializza
	 *       nel file {@code <tableName>.dmp} e risponde {@code "OK"}.</li>
	 *   <li><b>2 – Caricamento albero:</b> legge un nome di tabella, deserializza il
	 *       corrispondente file {@code .dmp} e risponde {@code "Table found!"} in caso
	 *       di successo o con un messaggio di errore in caso di fallimento, ripetendo
	 *       finché non si riceve un nome valido. Invia {@code "OK"} al termine.</li>
	 *   <li><b>3 – Predizione:</b> invia {@code "QUERY"} per segnalare l'inizio di una
	 *       sessione interattiva, delega a
	 *       {@link RegressionTree#predictClass(ObjectInputStream, ObjectOutputStream)}
	 *       che scambia messaggi con il client, poi invia {@code "OK"} seguito dal
	 *       valore {@link Double} predetto.</li>
	 * </ul>
	 */
	@Override
	public void run() {
		Data trainingSet = null;
		RegressionTree tree = null;
		String tableName = "";
		try {
			while(true) {
				int action = 0;
				try {
					action = (Integer)in.readObject();
				} catch (IOException | ClassNotFoundException e) {
					System.err.println("Client disconnected: " + e.toString());
				}
				switch (action) {
					case 0:
						while (true) {
							try {
								tableName = in.readObject().toString();
								trainingSet = new Data(tableName);
								out.writeObject("Table found!");
								break;
							} catch (TrainingDataException e) {
								out.writeObject("The table " + tableName + " doesn't exists!");
								System.out.println(e);
							} catch (ClassNotFoundException | IOException e) {
								System.out.println("Error reading table name: " + e.toString());
								break;
							}
						}

						out.writeObject("OK");
						break;
					case 1:
						tree = new RegressionTree(trainingSet);
						try {
							tree.salva(tableName + ".dmp");
							out.writeObject("OK");
						} catch (IOException e) {
							out.writeObject("Error saving tree: " + e.toString());
							System.out.println(e);
						}
						break;
					case 2:
						while(true) {
							try {
								tableName = in.readObject().toString();
								tree = RegressionTree.carica(tableName + ".dmp");
								out.writeObject("Table found!");
								break;
							} catch (ClassNotFoundException | IOException e) {
								File dmp = new File(tableName + ".dmp");
								dmp.delete();
								out.writeObject("The table " + tableName + " doesn't exists!");
								System.out.println(e);
							}
						}
						out.writeObject("OK");
						break;
					case 3:
						try {
							out.writeObject("QUERY");
							Double prediction = tree.predictClass(in, out);
							out.writeObject("OK");
							out.writeObject(prediction);
						} catch (UnknownValueException e) {
							out.writeObject(e.toString());
							System.out.println(e);
						} catch (ClassNotFoundException | IOException e) {
							System.out.println("I/O error: " + e.toString());
						}
						break;
				}
			}
		} catch (IOException e) {
			System.err.println("I/O Error: " + e.toString());
		} finally {
			try {
				socket.close();
				in.close();
				out.close();
			} catch (IOException e) {
				System.err.println("Error closing connection: " + e.toString());
			}
		}
	}
}

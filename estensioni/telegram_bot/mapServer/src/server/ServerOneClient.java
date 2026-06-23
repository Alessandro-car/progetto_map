package server;

import java.io.File;
import java.io.FileFilter;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.sql.SQLException;
import java.util.ArrayList;

import data.*;
import tree.*;
import database.*;

/**
 * Gestisce la comunicazione con un singolo client all'interno di un thread dedicato.
 * <p>
 * Ogni istanza possiede il proprio {@link Socket}, {@link ObjectInputStream} e
 * {@link ObjectOutputStream} e interpreta una sequenza di codici azione (numeri
 * interi) inviati dal client per guidare l'apprendimento o la predizione di un
 * albero di regressione.
 *
 * <p>Codici azione supportati:
 * <ul>
 *   <li>{@code 0} – Carica i dati di addestramento da una tabella del database.</li>
 *   <li>{@code 1} – Costruisce un albero di regressione e lo salva su disco.</li>
 *   <li>{@code 2} – Carica un albero di regressione precedentemente salvato.</li>
 *   <li>{@code 3} – Esegue una predizione interattiva sull'albero caricato.</li>
 *   <li>{@code 4} – Invia al client l'elenco delle tabelle presenti nel database.</li>
 *   <li>{@code 5} – Invia al client l'elenco degli alberi già salvati su disco (file {@code .dmp}).</li>
 * </ul>
 */
class ServerOneClient extends Thread {

	/** Socket connesso al client. */
	private Socket socket;

	/** Stream usato per ricevere oggetti dal client. */
	private ObjectInputStream in;

	/** Stream usato per inviare oggetti al client. */
	private ObjectOutputStream out;

	/**
	 * Costruisce il gestore per il socket fornito, inizializza gli stream di oggetti
	 * e avvia immediatamente il thread.
	 * <p>
	 * <strong>Nota:</strong> {@code out} viene creato prima di {@code in} per evitare
	 * un blocco: il costruttore di {@link ObjectInputStream} resta in attesa finché
	 * l'altro lato non ha scritto l'intestazione del proprio stream, quindi lo stream
	 * di output locale deve essere creato per primo.
	 *
	 * @param s il {@link Socket} che rappresenta la connessione accettata dal client
	 * @throws IOException se si verifica un errore di I/O durante la creazione degli stream
	 */
	public ServerOneClient(Socket s) throws IOException {
		this.socket = s;

		this.out = new ObjectOutputStream(this.socket.getOutputStream());
		this.in = new ObjectInputStream(this.socket.getInputStream());

		this.start();
	}

	/**
	 * Ciclo principale del thread che gestisce il client.
	 * <p>
	 * Legge i codici azione interi inviati dal client e li smista alla logica
	 * corrispondente. Il ciclo termina quando il client si disconnette o si verifica
	 * un errore di I/O non recuperabile; le risorse (socket e stream) vengono sempre
	 * rilasciate nel blocco {@code finally}.
	 *
	 * <p>Significato dei codici azione:
	 * <ul>
	 *   <li><b>0 – Apprendimento da DB:</b> legge ripetutamente un nome di tabella,
	 *       costruisce un oggetto {@link Data} da quella tabella e risponde
	 *       {@code "Table found!"} in caso di successo o un messaggio di errore in
	 *       caso contrario, fino a ricevere un nome valido; invia {@code "OK"} al termine.</li>
	 *   <li><b>1 – Costruzione e salvataggio albero:</b> costruisce un
	 *       {@link RegressionTree} dai dati correnti, lo salva nel file
	 *       {@code <tableName>.dmp} e risponde {@code "OK"}.</li>
	 *   <li><b>2 – Caricamento albero:</b> legge un nome di tabella, carica il
	 *       corrispondente file {@code .dmp} e risponde {@code "Table found!"} in caso
	 *       di successo o un messaggio di errore in caso contrario, fino a ricevere un
	 *       nome valido; invia {@code "OK"} al termine.</li>
	 *   <li><b>3 – Predizione:</b> invia {@code "QUERY"} per segnalare l'inizio della
	 *       sessione interattiva, delega a
	 *       {@link RegressionTree#predictClass(ObjectInputStream, ObjectOutputStream)}
	 *       lo scambio di messaggi con il client, poi invia {@code "OK"} seguito dal
	 *       valore {@link Double} predetto.</li>
	 *   <li><b>4 – Elenco tabelle:</b> apre una connessione al database e invia al
	 *       client la lista delle tabelle disponibili.</li>
	 *   <li><b>5 – Elenco alberi salvati:</b> cerca nella cartella corrente i file
	 *       con estensione {@code .dmp} e invia al client i relativi nomi (senza estensione).</li>
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
					action = (Integer) in.readObject();
				} catch (IOException | ClassNotFoundException e) {
					System.err.println("Client disconnected: " + e.toString());
					break;
				}
				switch (action) {
					case 0:
						try {
							while (true) {
								try {
									tableName = in.readObject().toString();
									trainingSet = new Data(tableName);
									out.writeObject("Table found!");
									out.flush();
									break;
								} catch (SQLException e) {
									out.writeObject("No such table!");
									out.flush();
								} catch (TrainingDataException e) {
									out.writeObject("No such table!");
									out.flush();
								}
							}
						} catch (IOException e) {
							System.out.println(e);
						} catch (ClassNotFoundException e) {
							System.out.println(e);
						}

						try {
							out.writeObject("OK");
							out.flush();
						} catch (IOException e) {
							System.out.println(e);
						}
						break;
					case 1:
						if (trainingSet == null) {
							try {
								out.writeObject("No training data loaded.");
								out.flush();
							} catch (IOException e) {
								System.out.println(e);
							}
							break;
						}
						tree = new RegressionTree(trainingSet);
						try {
							tree.salva(tableName + ".dmp");
							out.writeObject("OK");
							out.flush();
						} catch (IOException e) {
							out.writeObject("Error saving tree: " + e.toString());
							out.flush();
							System.out.println(e);
						}
						break;
					case 2:
						while(true) {
							try {
								tableName = in.readObject().toString();
								tree = RegressionTree.carica(tableName + ".dmp");
								out.writeObject("Table found!");
								out.flush();
								break;
							} catch (ClassNotFoundException e) {
								new File(tableName + ".dmp").delete();
								out.writeObject("The table " + tableName + " doesn't exist!");
								out.flush();
								System.out.println(e);
							} catch (FileNotFoundException e) {
								out.writeObject("The table " + tableName + " doesn't exist!");
								out.flush();
								System.out.println(e);
							} catch (IOException e) {
								out.writeObject("The table " + tableName + " doesn't exist!");
								out.flush();
								System.out.println(e);
							}
						}
						out.writeObject("OK");
						out.flush();
						break;
					case 3:
						if (tree == null) {
							try {
								out.writeObject("No tree available.");
								out.flush();
							} catch (IOException e) {
								System.out.println(e);
							}
							break;
						}
						try {
							out.writeObject("QUERY");
							out.flush();
							Double prediction = tree.predictClass(in, out);
							out.writeObject("OK");
							out.writeObject(prediction);
							out.flush();
						} catch (UnknownValueException e) {
							System.out.println(e);
						} catch (ClassNotFoundException | IOException e) {
							System.out.println("I/O error: " + e.toString());
						}
						break;
					case 4:
						DbAccess db = new DbAccess();
						try {
							db.initConnection();
							ArrayList<String> tables = db.getListOfTables();
							out.writeObject(tables);
							out.flush();
						} catch (DatabaseConnectionException e) {
							System.err.println(e);
						} catch (SQLException e) {
							System.err.println(e);
						} finally {
							try { db.closeConnection(); } catch (SQLException ignored) {}
						}
						break;
					case 5:
						try {
							File f = new File(".");
							String extension = ".dmp";
							FileFilter filter = new FileFilter() {
								public boolean accept(File f) {
									return f.getName().endsWith(extension);
								}
							};
							File[] files = f.listFiles(filter);
							ArrayList<String> fileNames = new ArrayList<>();
							if (files != null) {
								for (int i = 0; i < files.length; i++) {
									String fileName = files[i].getName()
											.substring(0, files[i].getName().length() - extension.length());
									fileNames.add(fileName);
								}
							}
							out.writeObject(fileNames);
							out.flush();

						} catch (Exception e) {
							System.err.println(e);
						}
						break;
				}
			}
		} catch (IOException e) {
			System.err.println("I/O Error: " + e.toString());
		} finally {
			try { out.close(); } catch (IOException e) { System.err.println("Error closing output stream: " + e); }
			try { in.close();  } catch (IOException e) { System.err.println("Error closing input stream: " + e);  }
			try { socket.close(); } catch (IOException e) { System.err.println("Error closing socket: " + e);    }
		}
	}
}

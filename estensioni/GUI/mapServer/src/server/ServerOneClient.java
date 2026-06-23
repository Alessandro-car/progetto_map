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

import database.*;
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
 *   <li>{@code 4} – Restituisce la lista dei nomi delle tabelle presenti nel DB.</li>
 *   <li>{@code 5} – Restituisce la lista dei file .dmp presenti nella directory del server.</li>
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
	 */
	@Override
	public void run() {
		Data trainingSet = null;
		RegressionTree tree = null;
		String tableName = "";
		try {
			while (true) {
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
									break;
								} catch (SQLException e) {
									out.writeObject("No such table!");
								} catch (TrainingDataException e) {
									out.writeObject("No such table!");
								}
							}
						} catch (IOException e) {
							System.out.println(e);
						} catch (ClassNotFoundException e) {
							System.out.println(e);
						}
						try {
							out.writeObject("OK");
						} catch (IOException e) {
							System.out.println(e);
						}
						break;

					case 1:
						if (trainingSet == null) {
							try {
								out.writeObject("No traning data loaded.");
							} catch (IOException e) {
								System.out.println(e);
							}
							break;
						}
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
						while (true) {
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
						if (tree == null) {
							try {
								out.writeObject("No tree available.");
							} catch (IOException e) {
								System.out.println(e);
							}
							break;
						}
						try {
							out.writeObject("QUERY");
							Double prediction = tree.predictClass(in, out);
							out.writeObject("OK");
							out.writeObject(prediction);
						} catch (UnknownValueException e) {
							System.out.println(e);
						} catch (ClassNotFoundException | IOException e) {
							System.out.println("I/O error: " + e.toString());
						}
						break;

					case 4:
						// Restituisce la lista dei nomi delle tabelle presenti nel DB
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
						// Restituisce la lista dei file .dmp presenti nella directory del server
						try {
							java.io.File dir = new java.io.File(".");
							java.io.File[] files = dir.listFiles((d, name) -> name.endsWith(".dmp"));
							java.util.List<String> fileNames = new java.util.ArrayList<>();
							if (files != null) {
								for (java.io.File f : files) {
									fileNames.add(f.getName().replace(".dmp", ""));
								}
							}
							out.writeObject(fileNames);
						} catch (Exception e) {
							out.writeObject(new java.util.ArrayList<String>());
							System.err.println("Errore lettura file .dmp: " + e);
						}
						break;

				} // fine switch
			} // fine while
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
import server.MultiServer;

/**
 * Punto di ingresso principale per il server di data mining.
 *
 * <p>Questa classe avvia un'istanza di {@link MultiServer} sulla porta specificata,
 * mettendolo in ascolto di connessioni da parte dei client. Il server gestisce
 * richieste di apprendimento di alberi di regressione da tabelle del database
 * e di predizione tramite alberi precedentemente serializzati.
 *
*/
public class MainTest {
	 /**
		* Metodo principale che avvia il server.
		*
		* <p>Legge il numero di porta dal primo argomento della riga di comando e
		* istanzia un {@link MultiServer} su tale porta. Se l'argomento non è
		* presente o non è un intero valido, viene stampato un messaggio di errore
		* e il programma termina.
		* @param args argomenti della riga di comando; {@code args[0]} deve contenere il numero di porta su cui il server rimarrà in ascolto
		*/
	public static void main(String[] args) {
		if (args.length < 1) {
				System.err.println("Usage: java server.MainTest <port>");
				System.exit(1);
		}

		int port;
		try {
				port = Integer.parseInt(args[0]);
		} catch (NumberFormatException e) {
				System.err.println("Error: the specified port is not a valid integer: " + args[0]);
				System.exit(1);
				return;
		}

		System.out.println("Starting the server on port " + port + "...");
		new MultiServer(port);
  }
}

import server.MultiServer;

/**
 * Punto di ingresso dell'applicazione server di data mining.
 * <p>
 * Avvia un'istanza di {@link MultiServer} sulla porta indicata, mettendola in
 * ascolto delle connessioni dei client. Il server gestisce sia l'apprendimento
 * di alberi di regressione a partire dalle tabelle del database, sia la predizione
 * tramite alberi precedentemente salvati.
 */
public class MainTest {
	/**
	 * Avvia il server.
	 * <p>
	 * Legge la porta dal primo argomento della riga di comando e crea un
	 * {@link MultiServer} su tale porta. Se l'argomento manca o non è un intero
	 * valido, stampa un messaggio di errore e termina.
	 *
	 * @param args argomenti della riga di comando: {@code args[0]} deve contenere
	 *             la porta su cui il server resterà in ascolto
	 */
	public static void main(String[] args) {
		if (args.length < 1) {
				System.err.println("Usage: java MainTest <port>");
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

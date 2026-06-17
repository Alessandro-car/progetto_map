package server;

import java.io.IOException;
import java.net.Socket;
import java.net.ServerSocket;


/**
 * Server TCP multi-client che rimane in ascolto su una porta specificata e
 * crea un thread dedicato {@link ServerOneClient} per ogni connessione in entrata.
 */
public class MultiServer {

	/** Numero di porta su cui il server rimane in ascolto per le connessioni in entrata*/
	private int PORT = 8080;

	/**
	 * Costruisce un nuovo {@code MultiServer}, imposta la porta di ascolto e avvia
	 * l'accettazione delle connessioni dai client.
	 *
	 * @param port la porta TCP su cui il server rimarrà in ascolto
	 */
	public MultiServer(int port) {
		this.PORT = port;
		this.run();
	}


	/**
	 * Apre una {@link ServerSocket} sulla porta {@link #PORT} ed entra in un ciclo
	 * infinito di accettazione delle connessioni. Per ogni connessione accettata viene
	 * creato un nuovo thread {@link ServerOneClient}. Se il costruttore di
	 * {@code ServerOneClient} fallisce, il socket grezzo viene chiuso immediatamente
	 * per liberare le risorse.
	 * La {@code ServerSocket} viene sempre chiusa nel blocco {@code finally}, anche in
	 * caso di errore durante l'avvio o l'accettazione delle connessioni.
	 */
	private void run() {
		ServerSocket s = null;
		try {
			s = new ServerSocket(this.PORT);
			System.out.println("Server started");
			while(true) {
				Socket socket = s.accept();
				try {
					new ServerOneClient(socket);
				} catch (IOException e) {
					socket.close();
				}
			}
		} catch(IOException e) {
			System.err.println(e.toString());
		} finally {
			if (s != null) {
				try {
					s.close();
				} catch (IOException e) {
					System.err.println(e.toString());
				}
			}
		}
	}
}

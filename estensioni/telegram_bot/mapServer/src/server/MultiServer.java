package server;

import java.io.IOException;
import java.net.Socket;
import java.net.ServerSocket;


/**
 * Server TCP in grado di servire più client contemporaneamente.
 * <p>
 * Resta in ascolto su una porta e, per ogni connessione in arrivo, crea un thread
 * dedicato {@link ServerOneClient} che ne gestisce la comunicazione in modo indipendente.
 */
public class MultiServer {

	/** Porta su cui il server resta in ascolto delle connessioni in arrivo. */
	private int PORT = 8080;

	/**
	 * Costruisce il server impostando la porta di ascolto e avvia immediatamente
	 * l'accettazione delle connessioni.
	 *
	 * @param port la porta TCP su cui il server resterà in ascolto
	 */
	public MultiServer(int port) {
		this.PORT = port;
		this.run();
	}


	/**
	 * Apre la {@link ServerSocket} sulla porta configurata ed entra in un ciclo
	 * infinito in cui accetta le connessioni.
	 * <p>
	 * Per ogni connessione accettata crea un nuovo {@link ServerOneClient}; se la
	 * sua creazione fallisce, il socket viene chiuso subito per liberare le risorse.
	 * La {@link ServerSocket} viene comunque chiusa nel blocco {@code finally}.
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

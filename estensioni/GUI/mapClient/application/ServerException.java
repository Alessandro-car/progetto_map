package application;


/**
 * Eccezione sollevata dal client quando il server risponde con un errore o con
 * un messaggio inatteso durante la comunicazione.
 * <p>
 * Estende {@link Exception} ed è quindi una eccezione checked, che chi la usa
 * deve gestire esplicitamente.
 */
public class ServerException extends Exception {
    /**
     * Costruisce una nuova eccezione con il messaggio specificato.
     *
     * @param message la stringa che descrive l'errore segnalato dal server
     */
    public ServerException(String message) {
        super(message);
    }
}

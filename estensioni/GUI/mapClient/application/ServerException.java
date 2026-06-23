package application;


/**
 * Eccezione sollevata dal server
 */
public class ServerException extends Exception {
    /**
     * Eccezione del server
     * @param message 	Stringa contente il messaggio di errore
     */
    public ServerException(String message) {
        super(message);
    }
}
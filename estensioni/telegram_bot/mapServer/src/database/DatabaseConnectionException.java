package database;

/**
 * Eccezione che segnala un errore nella connessione al database.
 * <p>
 * Estende {@link Exception} ed è quindi una eccezione checked, che chi la usa
 * deve gestire esplicitamente.
 */
public class DatabaseConnectionException extends Exception {

    /**
     * Costruisce una nuova eccezione con il messaggio specificato.
     *
     * @param message il messaggio che descrive l'errore di connessione
     */
    public DatabaseConnectionException(String message) {
        super(message);
    }

    /**
     * Costruisce una nuova eccezione con messaggio e causa specificati.
     * <p>
     * Utile per incapsulare un'eccezione originale (ad esempio una
     * {@link java.sql.SQLException}) conservandone le informazioni.
     *
     * @param message il messaggio che descrive l'errore di connessione
     * @param cause la causa originale dell'eccezione
     */
    public DatabaseConnectionException(String message, Throwable cause) {
        super(message, cause);
    }
}

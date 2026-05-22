package database;

/**
 * Eccezione personalizzata per la gestione degli errori
 * di connessione al database.
 * Estende {@link Exception} per essere una eccezione checked,
 * obbligando chi la usa a gestirla esplicitamente.
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
     * Costruisce una nuova eccezione con il messaggio e la causa specificati.
     * Utile per incapsulare un'eccezione originale (es. {@link java.sql.SQLException})
     * mantenendo le informazioni sull'errore originale.
     *
     * @param message il messaggio che descrive l'errore di connessione
     * @param cause   la causa originale dell'eccezione
     */
    public DatabaseConnectionException(String message, Throwable cause) {
        super(message, cause);
    }
}
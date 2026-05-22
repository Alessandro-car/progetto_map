package database;

/**
 * Eccezione personalizzata che viene lanciata quando il risultato
 * di una query al database è un insieme vuoto.
 * Estende {@link Exception} per essere una eccezione checked,
 * obbligando chi la usa a gestirla esplicitamente.
 */
public class EmptySetException extends Exception {

    /**
     * Costruisce una nuova eccezione con il messaggio di default
     * "The result set is empty."
     */
    public EmptySetException() {
        super("The result set is empty.");
    }

    /**
     * Costruisce una nuova eccezione con il messaggio specificato.
     *
     * @param message il messaggio che descrive il motivo dell'insieme vuoto
     */
    public EmptySetException(String message) {
        super(message);
    }
}
package database;

/**
 * Eccezione sollevata quando il risultato di una query al database è vuoto,
 * cioè non contiene alcuna riga.
 * <p>
 * Estende {@link Exception} ed è quindi una eccezione checked, che chi la usa
 * deve gestire esplicitamente.
 */
public class EmptySetException extends Exception {

    /**
     * Costruisce una nuova eccezione con il messaggio predefinito
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

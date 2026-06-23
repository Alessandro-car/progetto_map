package server;

/**
 * Eccezione sollevata quando, durante una predizione, la risposta fornita dal
 * client non corrisponde ad alcun ramo valido del nodo di split corrente.
 * <p>
 * Viene utilizzata dal metodo
 * {@link tree.RegressionTree#predictClass(java.io.ObjectInputStream, java.io.ObjectOutputStream)}.
 */
public class UnknownValueException extends Exception {

    /**
     * Costruisce una nuova eccezione con il messaggio specificato.
     *
     * @param message il messaggio che descrive la causa dell'eccezione,
     *                recuperabile tramite {@link #getMessage()}
     */
    public UnknownValueException(String message) {
        super(message);
    }
}

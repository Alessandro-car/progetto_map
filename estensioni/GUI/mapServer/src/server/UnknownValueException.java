package server;

/**
 * Eccezione lanciata quando si tenta di predire un valore sconosciuto
 * o non gestito durante la navigazione dell'albero di regressione.
 *
 * <p>Questa eccezione viene sollevata dal metodo
 * {@link tree.RegressionTree#predictClass(java.io.ObjectInputStream, java.io.ObjectOutputStream)}
 * nel caso in cui il valore fornito dal client non corrisponda a nessun
 * ramo dell'albero corrente.
 */
public class UnknownValueException extends Exception {

		/**
     * Costruisce una nuova {@code UnknownValueException} con il messaggio
     * di dettaglio specificato.
     *
     * @param message il messaggio che descrive la causa dell'eccezione,
     *                recuperabile tramite {@link #getMessage()}
     */
    public UnknownValueException(String message) {
        super(message);
    }
}

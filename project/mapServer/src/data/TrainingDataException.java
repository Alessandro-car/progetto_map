package data;

/**
 * Eccezione che segnala un problema nel caricamento o nella validità dei dati
 * di addestramento.
 * <p>
 * Viene sollevata, ad esempio, quando la connessione al database fallisce,
 * la tabella richiesta non esiste o non è valida, oppure non contiene esempi.
 * Estende {@link Exception} ed è quindi una eccezione checked, che chi la usa
 * deve gestire esplicitamente.
 */
public class TrainingDataException extends Exception{

	/**
	 * Costruisce una nuova eccezione con il messaggio specificato.
	 *
	 * @param error il messaggio che descrive il problema riscontrato
	 */
	public TrainingDataException(String error){
  	super(error);
  }
}

package mapClient;

/**
 * Elenca i comandi che l'utente può inviare al bot Telegram.
 * <p>
 * Ogni valore dell'enumerazione associa il testo del comando (quello che
 * l'utente digita nella chat, ad esempio {@code /start}) a una breve
 * descrizione mostrata nel menu del bot.
 */
public enum Command {
	/** Avvia una nuova sessione di predizione. */
	START("/start", "Start a new prediction"),
	/** Costruisce un nuovo albero di regressione a partire dai dati di una tabella. */
	LEARN("/learn", "Learn regression tree from data"),
	/** Carica un albero di regressione precedentemente salvato. */
	LOAD("/load", "Load regression tree from archive"),
	/** Termina la predizione in corso. */
	END("/end", "Exit the current prediction");

	/** Testo del comando digitato dall'utente (comprensivo della barra iniziale). */
	private final String command;

	/** Breve descrizione del comando mostrata nel menu del bot. */
	private final String description;

	/**
	 * Costruttore dell'enumerazione.
	 *
	 * @param command il testo del comando (ad esempio {@code /start})
	 * @param description la descrizione mostrata nel menu del bot
	 */
	Command(String command, String description) {
		this.command = command;
		this.description = description;
	}

	/**
	 * Restituisce il testo del comando.
	 *
	 * @return il comando comprensivo della barra iniziale (ad esempio {@code /start})
	 */
	public String getCommand() { return command; }

	/**
	 * Restituisce la descrizione del comando.
	 *
	 * @return la descrizione mostrata nel menu del bot
	 */
	public String getDescription() { return description; }
}

package mapClient;

import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.commands.SetMyCommands;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.commands.BotCommand;
import org.telegram.telegrambots.meta.api.objects.commands.scope.BotCommandScopeDefault;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Bot Telegram che fa da ponte tra l'utente e il server di data mining.
 * <p>
 * Estende {@link TelegramLongPollingBot} e riceve i messaggi degli utenti tramite
 * il metodo {@link #onUpdateReceived(Update)}. Per ogni utente (identificato dal
 * {@code chatId}) il bot mantiene uno stato della conversazione, una connessione
 * dedicata al server ({@link ServerConnection}) e un piccolo insieme di dati di
 * sessione. Guida l'utente attraverso le fasi di scelta dell'operazione
 * (apprendimento o caricamento), selezione della tabella e predizione interattiva.
 * <p>
 * Poiché Telegram può consegnare aggiornamenti su thread diversi, le operazioni
 * relative allo stesso utente vengono sincronizzate tramite un lock per chat.
 */
public class MapBot extends TelegramLongPollingBot {

    /** Nome utente del bot su Telegram. */
    private final String username;
    /** Indirizzo del server di data mining. */
    private final String host;
    /** Porta del server di data mining. */
    private final int port;
    /** Token di autenticazione del bot fornito da Telegram. */
    private final String token;
    /** Timeout di lettura (in millisecondi) usato per le connessioni al server. */
    private final int socketTimeoutMs;

    /**
     * Stati possibili della conversazione con un utente.
     * <ul>
     *   <li>{@code START}: nessuna sessione attiva.</li>
     *   <li>{@code WAITING_OPTION}: il bot attende che l'utente scelga tra {@code /learn} e {@code /load}.</li>
     *   <li>{@code WAITING_TABLE}: il bot attende che l'utente scelga una tabella o un albero salvato.</li>
     *   <li>{@code PREDICTION}: è in corso una predizione interattiva.</li>
     * </ul>
     */
    private enum Status {
        /** Nessuna sessione attiva. */
        START,
        /** Il bot attende che l'utente scelga tra {@code /learn} e {@code /load}. */
        WAITING_OPTION,
        /** Il bot attende che l'utente scelga una tabella o un albero salvato. */
        WAITING_TABLE,
        /** È in corso una predizione interattiva. */
        PREDICTION
    }

    /** Stato corrente della conversazione di ciascun utente, indicizzato per {@code chatId}. */
    private final Map<Long, Status> userStatus = new ConcurrentHashMap<>();
    /** Connessione al server associata a ciascun utente. */
    private final Map<Long, ServerConnection> connections = new ConcurrentHashMap<>();
    /** Indica per ciascun utente se è in modalità apprendimento ({@code true}) o caricamento ({@code false}). */
    private final Map<Long, Boolean> learnMode = new ConcurrentHashMap<>();
    /** Lock per utente, usato per serializzare gli aggiornamenti relativi alla stessa chat. */
    private final ConcurrentHashMap<Long, Object> userLocks = new ConcurrentHashMap<>();

    /**
     * Costruisce il bot con i parametri di configurazione e di connessione.
     *
     * @param token il token di autenticazione del bot
     * @param username il nome utente del bot su Telegram
     * @param host l'indirizzo del server di data mining
     * @param port la porta del server di data mining
     * @param socketTimeoutMs il timeout di lettura per le connessioni al server, in millisecondi
     */
    public MapBot(String token, String username, String host, int port, int socketTimeoutMs) {
        super(token);
        this.token = token;
        this.username = username;
        this.host = host;
        this.port = port;
        this.socketTimeoutMs = socketTimeoutMs;
    }

    /**
     * Restituisce il lock associato a una chat, creandolo se non esiste.
     * Serve a sincronizzare le operazioni che riguardano lo stesso utente.
     *
     * @param chatId l'identificativo della chat
     * @return l'oggetto lock dedicato a quella chat
     */
    private Object getLock(long chatId) {
        return userLocks.computeIfAbsent(chatId, k -> new Object());
    }

    /**
     * Restituisce il nome utente del bot.
     *
     * @return il nome utente del bot su Telegram
     */
    @Override
    public String getBotUsername() {
        return username;
    }

    /**
     * Restituisce il token del bot.
     *
     * @return il token di autenticazione del bot
     */
    @Override
    public String getBotToken() {
        return token;
    }

    /**
     * Registra presso Telegram l'elenco dei comandi disponibili, così che vengano
     * mostrati nel menu del bot. I comandi sono ricavati dall'enumerazione {@link Command}.
     */
    public void registerCommands() {
        List<BotCommand> commandList = new ArrayList<>();
        for (Command cmd : Command.values()) {
            commandList.add(new BotCommand(cmd.getCommand().substring(1), cmd.getDescription()));
        }
        try {
            execute(SetMyCommands.builder()
                .commands(commandList)
                .scope(new BotCommandScopeDefault())
                .build());
            System.out.println("Bot commands registered.");
        } catch (TelegramApiException e) {
            System.err.println("Failed to register bot commands: " + e.getMessage());
        }
    }

    /**
     * Punto di ingresso per ogni aggiornamento ricevuto da Telegram.
     * <p>
     * Distingue due tipi di aggiornamento:
     * <ul>
     *   <li>la pressione di un pulsante ({@code callbackQuery}), usata per scegliere
     *       una tabella o un ramo durante la predizione;</li>
     *   <li>un messaggio di testo, usato per i comandi ({@code /start}, {@code /learn},
     *       {@code /load}, {@code /end}).</li>
     * </ul>
     * In base allo stato corrente dell'utente l'aggiornamento viene inoltrato alla
     * logica appropriata. Tutte le operazioni relative a una chat vengono eseguite
     * all'interno del lock di quella chat e gli eventuali errori vengono gestiti
     * ripristinando lo stato dell'utente.
     *
     * @param update l'aggiornamento ricevuto da Telegram
     */
    @Override
    public void onUpdateReceived(Update update) {
        if (update.hasCallbackQuery()) {
            String callData = update.getCallbackQuery().getData();
            long chatId = update.getCallbackQuery().getMessage().getChatId();
            synchronized (getLock(chatId)) {
                if (!connections.containsKey(chatId) || !userStatus.containsKey(chatId)) {
                    send(chatId, "Session expired or interrupted. Please type /start to begin a new prediction!");
                    return;
                }
                Status stato = userStatus.get(chatId);
                try {
                    if (stato == Status.WAITING_TABLE) {
                        if (learnMode.getOrDefault(chatId, true)) {
                            startLearning(chatId, callData);
                        } else {
                            startLoading(chatId, callData);
                        }
                    } else if (stato == Status.PREDICTION && callData.matches("\\d+")) {
                        handlePrediction(chatId, callData);
                    }
                } catch (Exception e) {
                    System.err.println(e);
                    send(chatId, "An error occurred during communication. Type /start to restart.");
                    resetUser(chatId);
                }
            }
        } else if (update.hasMessage() && update.getMessage().hasText()) {
            long chatId = update.getMessage().getChatId();
            String testo = update.getMessage().getText().trim();
            synchronized (getLock(chatId)) {
                try {
                    if (testo.equals(Command.START.getCommand())) {
                        resetUser(chatId);
                        userStatus.put(chatId, Status.WAITING_OPTION);
                        connections.put(chatId, new ServerConnection(host, port, socketTimeoutMs));
                        send(chatId, buildInitMessage());
                        return;
                    }

                    Status stato = userStatus.get(chatId);
                    if (stato == null) stato = Status.START;
                    switch (stato) {
                        case START:
                            send(chatId, "Type /start to start a new prediction!");
                            break;
                        case WAITING_OPTION:
                            if (testo.equals(Command.LEARN.getCommand())) {
                                handleTable(chatId, true);
                            } else if (testo.equals(Command.LOAD.getCommand())) {
                                handleTable(chatId, false);
                            } else if (testo.equals(Command.END.getCommand())) {
                                resetUser(chatId);
                                send(chatId, "Session closed.");
                            } else {
                                send(chatId, "Invalid option!");
                            }
                            break;

                        case WAITING_TABLE:
                            if (testo.equals(Command.END.getCommand())) {
                                resetUser(chatId);
                                send(chatId, "Session closed.");
                            } else {
                                send(chatId, "Please select a table from the buttons above!");
                            }
                            break;

                        case PREDICTION:
                            if (testo.equals(Command.END.getCommand())) {
                                send(chatId, "Current prediction interrupted!");
                                resetUser(chatId);
                            } else {
                                send(chatId, "Please select a branch from the buttons above, or type /end to stop.");
                            }
                            break;
                    }
                } catch (Exception e) {
                    String safeMsg = e.getMessage() == null ? "unknown error"
                        : e.getMessage().replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;");
                    send(chatId, "Error: " + safeMsg);
                    resetUser(chatId);
                }
            }
        }
    }

    /**
     * Costruisce il messaggio di benvenuto mostrato all'avvio di una sessione.
     * Elenca le opzioni disponibili ({@code /learn} e {@code /load}) con la
     * relativa descrizione.
     *
     * @return il testo del messaggio iniziale, formattato in HTML
     */
    private String buildInitMessage() {
        StringBuilder welcomeMessage = new StringBuilder(
            "<b>Welcome to the MAP regression bot!</b>\n Select an option from the menu:\n"
        );

        for (Command cmd : Command.values()) {
            if (cmd != Command.START && cmd != Command.END) {
                welcomeMessage.append("•\t")
                    .append(cmd.getCommand())
                    .append(" - ")
                    .append(cmd.getDescription())
                    .append("\n");
            }
        }

        return welcomeMessage.toString();
    }

    /**
     * Recupera dal server l'elenco degli elementi selezionabili e mostra
     * all'utente i pulsanti per sceglierne uno.
     * <p>
     * Se {@code learn} è {@code true} vengono richieste le tabelle del database,
     * altrimenti gli alberi già salvati. Se non ci sono elementi disponibili viene
     * inviato un messaggio informativo e la sessione resta in attesa di un'altra opzione.
     *
     * @param chatId l'identificativo della chat dell'utente
     * @param learn {@code true} per la modalità apprendimento, {@code false} per il caricamento
     * @throws Exception se si verifica un errore nella comunicazione con il server
     */
    private void handleTable(long chatId, boolean learn) throws Exception {
        ServerConnection conn = connections.get(chatId);
        learnMode.put(chatId, learn);
        ArrayList<String> tables = conn.showTables(learn);

        if (tables.isEmpty()) {
            String noItemsMsg = learn
                ? "No tables found in the database."
                : "No saved trees found. Use /learn first to train and save a tree.";
            learnMode.remove(chatId);
            send(chatId, noItemsMsg);
            return;
        }

        userStatus.put(chatId, Status.WAITING_TABLE);
        createButtons(chatId, "Choose a table:", tables);
    }

    /**
     * Avvia l'apprendimento di un nuovo albero a partire dalla tabella scelta.
     * <p>
     * Invia al server il nome della tabella, attende la conferma, chiede la
     * costruzione dell'albero e passa quindi alla fase di predizione. In caso di
     * tabella non trovata la sessione viene ripristinata.
     *
     * @param chatId l'identificativo della chat dell'utente
     * @param tableName il nome della tabella selezionata
     * @throws Exception se si verifica un errore nella comunicazione con il server
     */
    private void startLearning(long chatId, String tableName) throws Exception {
        ServerConnection conn = connections.get(chatId);
        String answer = conn.sendTableName(tableName);
        if (!answer.equals("Table found!")) {
            send(chatId, "Table not found. Please type /start to begin again.");
            resetUser(chatId);
            return;
        }

        conn.readAnswer();
        send(chatId, "Table found! Learning in progress...");
        conn.startLearning();
        conn.readAnswer();
        conn.startPrediction();
        userStatus.put(chatId, Status.PREDICTION);
        advancePrediction(chatId);
    }

    /**
     * Carica dal server un albero precedentemente salvato e avvia la predizione.
     * <p>
     * In caso di albero non trovato la sessione viene ripristinata.
     *
     * @param chatId l'identificativo della chat dell'utente
     * @param tableName il nome dell'albero salvato selezionato
     * @throws Exception se si verifica un errore nella comunicazione con il server
     */
    private void startLoading(long chatId, String tableName) throws Exception {
        ServerConnection conn = connections.get(chatId);
        String answer = conn.loadTree(tableName);
        if (!answer.equals("Table found!")) {
            send(chatId, "Tree not found. Please type /start to begin again.");
            resetUser(chatId);
            return;
        }

        conn.readAnswer();
        send(chatId, "Tree loaded! Starting prediction...");
        conn.startPrediction();
        userStatus.put(chatId, Status.PREDICTION);
        advancePrediction(chatId);
    }

    /**
     * Gestisce la scelta di un ramo da parte dell'utente durante la predizione.
     * <p>
     * Converte il dato del pulsante in un numero, lo invia al server e fa avanzare
     * la predizione al passo successivo.
     *
     * @param chatId l'identificativo della chat dell'utente
     * @param callData il dato associato al pulsante premuto (l'indice del ramo)
     * @throws Exception se si verifica un errore nella comunicazione con il server
     */
    private void handlePrediction(long chatId, String callData) throws Exception {
        ServerConnection conn = connections.get(chatId);
        int scelta;
        try {
            scelta = Integer.parseInt(callData);
        } catch (NumberFormatException e) {
            send(chatId, "Insert a valid number.");
            return;
        }
        conn.sendChoice(scelta);
        advancePrediction(chatId);
    }

    /**
     * Fa avanzare la predizione leggendo la prossima risposta del server.
     * <p>
     * Se il server segnala una nuova domanda ({@code "QUERY"}) mostra il nodo di
     * split corrente e i pulsanti dei rami, la cui lista viene letta dal server
     * tramite {@link ServerConnection#showBranches()}. Se comunica il risultato finale ({@code "OK"}) mostra
     * la classe predetta e chiude la sessione. Qualsiasi altra risposta (ad
     * esempio un ramo non valido) viene trattata come errore terminale e la
     * sessione viene ripristinata.
     *
     * @param chatId l'identificativo della chat dell'utente
     * @throws Exception se si verifica un errore nella comunicazione con il server
     */
    private void advancePrediction(long chatId) throws Exception {
        ServerConnection conn = connections.get(chatId);
        String answer = conn.readAnswer();

        if (!answer.equals("QUERY")) {
            String safeAnswer = answer.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;");
            send(chatId, "Prediction error: " + safeAnswer + "\n\nType /start to begin a new prediction.");
            resetUser(chatId);
            return;
        }

        answer = conn.readAnswer();
        if (answer.equals("OK")) {
            String pred = conn.readAnswer();
            send(chatId, "Predicted class: " + pred + "\n\nType /start to begin a new prediction.");
            resetUser(chatId);
        } else {
            String sanitizedAnswer = answer.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;");
            ArrayList<String> branches = conn.showBranches();
            String msg = "<b>Choose a branch:</b>\n" + sanitizedAnswer;
            createButtons(chatId, msg, branches);
        }
    }

    /**
     * Ripristina lo stato di un utente, chiudendo l'eventuale connessione al server
     * e rimuovendo i dati di sessione (stato, connessione e modalità). Viene usato
     * sia al termine di una sessione sia in caso di errore.
     *
     * @param chatId l'identificativo della chat dell'utente
     */
    private void resetUser(long chatId) {
        ServerConnection conn = connections.remove(chatId);
        if (conn != null) {
            try { conn.close(); } catch (Exception ignored) {}
        }
        userStatus.remove(chatId);
        learnMode.remove(chatId);
    }

    /**
     * Invia all'utente un messaggio con una tastiera di pulsanti inline.
     * <p>
     * I pulsanti vengono disposti su righe da due. Il testo di ciascun pulsante
     * coincide con il dato inviato a Telegram quando il pulsante viene premuto.
     *
     * @param chatId l'identificativo della chat dell'utente
     * @param text il testo del messaggio (formattato in HTML)
     * @param names i testi/valori dei pulsanti da mostrare
     * @throws TelegramApiException se l'invio del messaggio fallisce
     */
    private void createButtons(long chatId, String text, ArrayList<String> names) throws TelegramApiException {
        SendMessage msg = new SendMessage();
        msg.setParseMode("HTML");
        msg.setChatId(chatId);
        msg.setText(text);
        List<List<InlineKeyboardButton>> keyboard = new ArrayList<>();
        List<InlineKeyboardButton> currentRow = new ArrayList<>();

        for (String table : names) {
            InlineKeyboardButton btn = InlineKeyboardButton
                .builder()
                .text(table)
                .callbackData(table)
                .build();

            currentRow.add(btn);

            if (currentRow.size() == 2) {
                keyboard.add(currentRow);
                currentRow = new ArrayList<>();
            }
        }

        if (!currentRow.isEmpty()) {
            keyboard.add(currentRow);
        }

        InlineKeyboardMarkup markup = new InlineKeyboardMarkup();
        markup.setKeyboard(keyboard);
        msg.setReplyMarkup(markup);

        execute(msg);
    }

    /**
     * Invia un semplice messaggio di testo all'utente.
     * Eventuali errori di invio vengono registrati sullo standard error senza
     * interrompere l'esecuzione.
     *
     * @param chatId l'identificativo della chat dell'utente
     * @param testo il testo del messaggio (formattato in HTML)
     */
    private void send(long chatId, String testo) {
        SendMessage msg = new SendMessage(String.valueOf(chatId), testo);
        msg.setParseMode("HTML");
        try {
            execute(msg);
        } catch (TelegramApiException e) {
            System.err.println("Telegram sending error: " + e.getMessage());
        }
    }
}

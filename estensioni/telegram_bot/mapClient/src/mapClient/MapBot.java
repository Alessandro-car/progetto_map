package mapClient;

import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;

import java.util.ArrayList;
import java.util.List;
import java.util.HashMap;
import java.util.Map;

public class MapBot extends TelegramLongPollingBot {

    private final String username;
    private final String host;
    private final int port;
		private final String token;
    // Stato per ogni utente
    private enum Stato { INIZIO, ATTESA_TABELLA, IN_PREDIZIONE }

    private final Map<Long, Stato> statoUtente = new HashMap<>();
    private final Map<Long, ServerConnection> connessioni = new HashMap<>();

    public MapBot(String token, String username, String host, int port) {
				super(token);
				this.token = token;
        this.username = username;
        this.host = host;
        this.port = port;
    }

    @Override
    public String getBotUsername() {
        return username;
    }

		@Override
		public String getBotToken() {
			return token;
		}


    @Override
    public void onUpdateReceived(Update update) {
        if (!update.hasMessage() || !update.getMessage().hasText()) return;

        long chatId = update.getMessage().getChatId();
        String testo = update.getMessage().getText().trim();

        try {
            // /start resetta tutto
            if (testo.equals("/start")) {
                resetUtente(chatId);
                statoUtente.put(chatId, Stato.ATTESA_TABELLA);
                // apri connessione e avvia subito modalità "learn from data"
                connessioni.put(chatId, new ServerConnection(host, port));
                invia(chatId, "Benvenuto nel MAP Regression Bot!\n");
								SendMessage message = new SendMessage();
								message.setChatId(chatId);
								message.setText("Choose an option:");
								InlineKeyboardButton learn = InlineKeyboardButton.builder().text("Learn").callbackData("next").build();
								InlineKeyboardButton load = InlineKeyboardButton.builder().text("Load").callbackData("load").build();
								List<InlineKeyboardButton> row = new ArrayList<>();
								row.add(learn);
								row.add(load);

								List<List<InlineKeyboardButton>> keyboard = new ArrayList<>();
								keyboard.add(row);
								InlineKeyboardMarkup markup = new InlineKeyboardMarkup();
								markup.setKeyboard(keyboard);
								message.setReplyMarkup(markup);
								try {
									execute(message);
								} catch (TelegramApiException e) {
									throw new RuntimeException(e);
								}
                return;
            }

            Stato stato = statoUtente.getOrDefault(chatId, Stato.INIZIO);

            switch (stato) {
                case INIZIO:
                    invia(chatId, "Digita /start per iniziare.");
                    break;

                case ATTESA_TABELLA:
                    gestisciTabella(chatId, testo);
                    break;

                case IN_PREDIZIONE:
                    gestisciPredizione(chatId, testo);
                    break;
            }
        } catch (Exception e) {
            invia(chatId, "Errore: " + e.getMessage());
            resetUtente(chatId);
        }
    }

    private void gestisciTabella(long chatId, String tableName) throws Exception {
        ServerConnection conn = connessioni.get(chatId);
        String risposta = conn.sendTableName(tableName);

        if (!risposta.equals("Table found!")) {
            invia(chatId, "Tabella non trovata. Riprova:");
            return;
        }

        // leggi "OK"
        conn.readAnswer();
        invia(chatId, "Tabella trovata! Apprendimento in corso...");

        // avvia apprendimento
        conn.startLearning();
        conn.readAnswer(); // "OK"

        // avvia subito la fase di predizione
        conn.startPrediction();
        statoUtente.put(chatId, Stato.IN_PREDIZIONE);
        avanzaPredizione(chatId, true);
    }

    private void gestisciPredizione(long chatId, String testo) throws Exception {
        ServerConnection conn = connessioni.get(chatId);

        int scelta;
        try {
            scelta = Integer.parseInt(testo);
        } catch (NumberFormatException e) {
            invia(chatId, "Inserisci un numero valido.");
            return;
        }

        conn.sendChoice(scelta);
        avanzaPredizione(chatId, false);
    }

    /**
     * Legge dal server la prossima domanda (QUERY) o il risultato.
     * @param primaChiamata true se è la prima lettura dopo avviaPredizione()
     */
    private void avanzaPredizione(long chatId, boolean primaChiamata) throws Exception {
        ServerConnection conn = connessioni.get(chatId);

        String answer = conn.readAnswer();

        // Il server invia "QUERY" all'inizio della sessione di predizione
        if (answer.equals("QUERY")) {
            answer = conn.readAnswer();
        }

        if (answer.equals("OK")) {
            // prossimo messaggio = predizione
            String pred = conn.readAnswer();
            invia(chatId, "Classe predetta: " + pred + "\n\nDigita /start per una nuova predizione.");
            resetUtente(chatId);
        } else {
            // answer contiene la domanda con le opzioni (es. "0:X=A\n1:X=B")
            invia(chatId, "Scegli un ramo:\n" + answer);
        }
    }

    private void resetUtente(long chatId) {
        ServerConnection conn = connessioni.remove(chatId);
        if (conn != null) {
            try { conn.close(); } catch (Exception ignored) {}
        }
        statoUtente.remove(chatId);
    }

    private void invia(long chatId, String testo) {
        SendMessage msg = new SendMessage(String.valueOf(chatId), testo);
        try {
            execute(msg);
        } catch (TelegramApiException e) {
            System.err.println("Errore invio Telegram: " + e.getMessage());
        }
    }
}

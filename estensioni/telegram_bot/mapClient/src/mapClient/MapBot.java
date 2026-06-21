package mapClient;

import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;

import mapClient.Command;

import java.util.HashMap;
import java.util.Map;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class MapBot extends TelegramLongPollingBot {

    private final String username;
    private final String host;
    private final int port;
		private final String token;
    // Stato per ogni utente
    private enum Stato { INIZIO, WAITING_OPTION, ATTESA_TABELLA, IN_PREDIZIONE }

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
				if (update.hasCallbackQuery()) {
					String callData = update.getCallbackQuery().getData();
					long chatId = update.getCallbackQuery().getMessage().getChatId();
					if (!connessioni.containsKey(chatId) || !statoUtente.containsKey(chatId)) {
						invia(chatId, "Session expired or interrupted. Please type /start to begin a new predicition!");
						return;
					}
					Stato stato = statoUtente.get(chatId);
					try {
						if (stato == Stato.ATTESA_TABELLA) {
							startLearning(chatId, callData);
						} else if (stato == Stato.IN_PREDIZIONE && callData.matches("\\d+")) {
							gestisciPredizione(chatId, callData);
						}
					} catch (Exception e) {
						System.err.println(e);
						invia(chatId, "An error occured during communication. Type /start to restart.");
						resetUtente(chatId);
					}
				} else if (update.hasMessage() && update.getMessage().hasText()) {
					long chatId = update.getMessage().getChatId();
					String testo = update.getMessage().getText().trim();
					try {
							if (testo.equals(Command.START.getCommand())) {
									resetUtente(chatId);
									statoUtente.put(chatId, Stato.WAITING_OPTION);
									connessioni.put(chatId, new ServerConnection(host, port));
									String welcomeText = buildInitMessage();
									invia(chatId, welcomeText);
									return;
							}

							Stato stato = statoUtente.get(chatId);
							if (stato == null) stato = Stato.INIZIO;
							switch (stato) {
									case INIZIO:
											invia(chatId, "Type /start to start a new predicition!");
											break;
									case WAITING_OPTION:
										if (testo.equals(Command.LEARN.getCommand())) {
											statoUtente.put(chatId, Stato.ATTESA_TABELLA);
											gestisciTabella(chatId, true);
										}
										else if (testo.equals(Command.LOAD.getCommand())) {
											statoUtente.put(chatId, Stato.ATTESA_TABELLA);
											gestisciTabella(chatId, false);
										}
										else if (testo.equals(Command.STOP.getCommand())) {
											invia(chatId, "Current prediction interrupted!");
											statoUtente.put(chatId, Stato.INIZIO);
										} else {
											invia(chatId, "Invalid option!");
										}
										break;

									case ATTESA_TABELLA:
										if (testo.equals(Command.STOP.getCommand())) {
											invia(chatId, "Current prediction interrupted!");
											statoUtente.put(chatId, Stato.INIZIO);
										} else {
											invia(chatId, "Please select a table from the buttons above!");
										}
										break;

									case IN_PREDIZIONE:
											if (testo.equals(Command.STOP.getCommand())) {
												invia(chatId, "Current prediction interrupted!");
												statoUtente.put(chatId, Stato.INIZIO);
												break;
											}
											break;
							}
					} catch (Exception e) {
							invia(chatId, "Errore: " + e.getMessage());
							resetUtente(chatId);
					}
			}
    }

		private String buildInitMessage() {
			StringBuilder welcomeMessage = new StringBuilder(
				"<b>Welcome to the MAP regression bot!</b>\n Select an option from the menu:\n"
			);

			for (Command cmd : Command.values()) {
				if (cmd != Command.START && cmd != Command.STOP) {
					welcomeMessage.append("\u2022\t")
												.append(cmd.getCommand())
												.append(" - ")
												.append(cmd.getDescription())
												.append("\n");
				}
			}

			return welcomeMessage.toString();
		}

    private void gestisciTabella(long chatId, Boolean learn) throws Exception {
        ServerConnection conn = connessioni.get(chatId);
				ArrayList<String> tables = conn.showTables(learn);
				createButtons(chatId, "Choose a table:", tables);
    }

		private void startLearning(long chatId, String tableName) throws Exception {
			ServerConnection conn = connessioni.get(chatId);
			String answer = conn.sendTableName(tableName);
			if (!answer.equals("Table found!")) {
				invia(chatId, "Table not found. Retry:");
				return;
			}

			conn.readAnswer();
			invia(chatId, "Table found! Learning in progress...");
			conn.startLearning();
			conn.readAnswer();
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
            invia(chatId, "Predicted class: " + pred + "\n\nType /start to begin a new prediction.");
            resetUtente(chatId);
        } else {
						String sanitizedAnswer = answer.replace("<", "&lt;").replace(">", "&gt;");
            ArrayList<String> branches = getBranches(chatId);
						String msg = "<b>Choose a branch:</b>\n" + sanitizedAnswer;
						createButtons(chatId, msg, branches);

        }
    }

    private void resetUtente(long chatId) {
        ServerConnection conn = connessioni.remove(chatId);
        if (conn != null) {
            try { conn.close(); } catch (Exception ignored) {}
        }
        statoUtente.remove(chatId);
    }

		private ArrayList<String> getTables(long chatId) throws Exception {
			ServerConnection conn = connessioni.get(chatId);
			return conn.showTables(true);
		}

		private ArrayList<String> getBranches(long chatId) throws Exception {
			ServerConnection conn = connessioni.get(chatId);
			return conn.showBranches();
		}

		private void createButtons(long chatId, String text, ArrayList<String> names) {
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

			try {
					execute(msg);
			} catch (TelegramApiException e) {
					System.err.println(e);
			}
		}

    private void invia(long chatId, String testo) {
        SendMessage msg = new SendMessage(String.valueOf(chatId), testo);
				msg.setParseMode("HTML");
        try {
            execute(msg);
        } catch (TelegramApiException e) {
        	System.err.println("Errore invio Telegram: " + e.getMessage());
    		}
		}
}

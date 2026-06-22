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
    private enum Status { START, WAITING_OPTION, WAITING_TABLE, PREDICTION }

    private final Map<Long, Status> userStatus = new HashMap<>();
    private final Map<Long, ServerConnection> connections = new HashMap<>();

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
					if (!connections.containsKey(chatId) || !userStatus.containsKey(chatId)) {
						send(chatId, "Session expired or interrupted. Please type /start to begin a new predicition!");
						return;
					}
					Status stato = userStatus.get(chatId);
					try {
						if (stato == Status.WAITING_TABLE) {
							startLearning(chatId, callData);
						} else if (stato == Status.PREDICTION && callData.matches("\\d+")) {
							handlePrediction(chatId, callData);
						}
					} catch (Exception e) {
						System.err.println(e);
						send(chatId, "An error occured during communication. Type /start to restart.");
						resetUser(chatId);
					}
				} else if (update.hasMessage() && update.getMessage().hasText()) {
					long chatId = update.getMessage().getChatId();
					String testo = update.getMessage().getText().trim();
					try {
							if (testo.equals(Command.START.getCommand())) {
									resetUser(chatId);
									userStatus.put(chatId, Status.WAITING_OPTION);
									connections.put(chatId, new ServerConnection(host, port));
									String welcomeText = buildInitMessage();
									send(chatId, welcomeText);
									return;
							}

							Status stato = userStatus.get(chatId);
							if (stato == null) stato = Status.START;
							switch (stato) {
									case START:
											send(chatId, "Type /start to start a new predicition!");
											break;
									case WAITING_OPTION:
										if (testo.equals(Command.LEARN.getCommand())) {
											userStatus.put(chatId, Status.WAITING_TABLE);
											handleTable(chatId, true);
										}
										else if (testo.equals(Command.LOAD.getCommand())) {
											userStatus.put(chatId, Status.WAITING_TABLE);
											handleTable(chatId, false);
										}
										else if (testo.equals(Command.STOP.getCommand())) {
											send(chatId, "Current prediction interrupted!");
											userStatus.put(chatId, Status.START);
										} else {
											send(chatId, "Invalid option!");
										}
										break;

									case WAITING_TABLE:
										if (testo.equals(Command.STOP.getCommand())) {
											send(chatId, "Current prediction interrupted!");
											userStatus.put(chatId, Status.START);
										} else {
											send(chatId, "Please select a table from the buttons above!");
										}
										break;

									case PREDICTION:
											if (testo.equals(Command.STOP.getCommand())) {
												send(chatId, "Current prediction interrupted!");
												userStatus.put(chatId, Status.START);
												break;
											}
											break;
							}
					} catch (Exception e) {
							send(chatId, "Error: " + e.getMessage());
							resetUser(chatId);
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

    private void handleTable(long chatId, Boolean learn) throws Exception {
        ServerConnection conn = connections.get(chatId);
				ArrayList<String> tables = conn.showTables(learn);
				createButtons(chatId, "Choose a table:", tables);
    }

		private void startLearning(long chatId, String tableName) throws Exception {
			ServerConnection conn = connections.get(chatId);
			String answer = conn.sendTableName(tableName);
			if (!answer.equals("Table found!")) {
				send(chatId, "Table not found. Retry:");
				return;
			}

			conn.readAnswer();
			send(chatId, "Table found! Learning in progress...");
			conn.startLearning();
			conn.readAnswer();
			conn.startPrediction();
			userStatus.put(chatId, Status.PREDICTION);
			advancePrediction(chatId, true);
		}

    private void handlePrediction(long chatId, String testo) throws Exception {
        ServerConnection conn = connections.get(chatId);
        int scelta;
        try {
            scelta = Integer.parseInt(testo);
        } catch (NumberFormatException e) {
            send(chatId, "Insert a valid number.");
            return;
        }
        conn.sendChoice(scelta);
        advancePrediction(chatId, false);
    }

    /**
     * Legge dal server la prossima domanda (QUERY) o il risultato.
     * @param primaChiamata true se è la prima lettura dopo avviaPredizione()
     */
    private void advancePrediction(long chatId, boolean primaChiamata) throws Exception {
        ServerConnection conn = connections.get(chatId);
        String answer = conn.readAnswer();

        if (answer.equals("QUERY")) {
            answer = conn.readAnswer();
        }

        if (answer.equals("OK")) {
            String pred = conn.readAnswer();
            send(chatId, "Predicted class: " + pred + "\n\nType /start to begin a new prediction.");
            resetUser(chatId);
        } else {
						String sanitizedAnswer = answer.replace("<", "&lt;").replace(">", "&gt;");
            ArrayList<String> branches = getBranches(chatId);
						String msg = "<b>Choose a branch:</b>\n" + sanitizedAnswer;
						createButtons(chatId, msg, branches);

        }
    }

    private void resetUser(long chatId) {
        ServerConnection conn = connections.remove(chatId);
        if (conn != null) {
            try { conn.close(); } catch (Exception ignored) {}
        }
        userStatus.remove(chatId);
    }

		private ArrayList<String> getTables(long chatId) throws Exception {
			ServerConnection conn = connections.get(chatId);
			return conn.showTables(true);
		}

		private ArrayList<String> getBranches(long chatId) throws Exception {
			ServerConnection conn = connections.get(chatId);
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

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

public class MapBot extends TelegramLongPollingBot {

    private final String username;
    private final String host;
    private final int port;
    private final String token;
    private final int socketTimeoutMs;

    private enum Status { START, WAITING_OPTION, WAITING_TABLE, PREDICTION }

    private final Map<Long, Status> userStatus = new ConcurrentHashMap<>();
    private final Map<Long, ServerConnection> connections = new ConcurrentHashMap<>();
    private final Map<Long, Boolean> learnMode = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<Long, Object> userLocks = new ConcurrentHashMap<>();

    public MapBot(String token, String username, String host, int port, int socketTimeoutMs) {
        super(token);
        this.token = token;
        this.username = username;
        this.host = host;
        this.port = port;
        this.socketTimeoutMs = socketTimeoutMs;
    }

    private Object getLock(long chatId) {
        return userLocks.computeIfAbsent(chatId, k -> new Object());
    }

    @Override
    public String getBotUsername() {
        return username;
    }

    @Override
    public String getBotToken() {
        return token;
    }

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

    private void advancePrediction(long chatId) throws Exception {
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
            String sanitizedAnswer = answer.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;");
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
        learnMode.remove(chatId);
    }

    private ArrayList<String> getBranches(long chatId) throws Exception {
        ServerConnection conn = connections.get(chatId);
        return conn.showBranches();
    }

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

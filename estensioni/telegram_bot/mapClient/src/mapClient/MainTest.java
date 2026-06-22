package mapClient;

import java.io.InputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;

public class MainTest {
    public static void main(String[] args) {
				Properties cfg = new Properties();
				try (InputStream is = new FileInputStream("config.properties")) {
					cfg.load(is);
				} catch (IOException e) {
					System.err.println("Unable to read config.properties: " + e.getMessage());
					System.exit(1);
				}
				String token = cfg.getProperty("bot.token");
				String bot_username = cfg.getProperty("bot.username");

				if (token == null || token.isBlank()) {
					System.err.println("Missing 'bot.token' in config.properties");
					System.exit(1);
				}
				if (bot_username == null || bot_username.isBlank()) {
					System.err.println("Missing 'bot.username' in config.properties");
					System.exit(1);
				}

				if (args.length < 2) {
					System.err.println("Usage: java MainTest <host> <port>");
					System.exit(1);
				}
        String host = args[0];
				int port;
				try {
					port = Integer.parseInt(args[1]);
				} catch (NumberFormatException e) {
					System.err.println("Invalid port number: \"" + args[1] + "\"");
					System.exit(1);
					return;
				}

        int socketTimeoutMs = 30_000;
        String timeoutStr = cfg.getProperty("socket.timeout.ms");
        if (timeoutStr != null && !timeoutStr.isBlank()) {
            try {
                socketTimeoutMs = Integer.parseInt(timeoutStr.trim());
            } catch (NumberFormatException e) {
                System.err.println("Invalid 'socket.timeout.ms' in config.properties, using default: " + socketTimeoutMs + " ms");
            }
        }

        try {
            TelegramBotsApi api = new TelegramBotsApi(DefaultBotSession.class);
            MapBot bot = new MapBot(token, bot_username, host, port, socketTimeoutMs);
            api.registerBot(bot);
            bot.registerCommands();
            System.out.println("Bot started!");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

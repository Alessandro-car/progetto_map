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
        String host = args[0];
        int port = Integer.parseInt(args[1]);

        try {
            TelegramBotsApi api = new TelegramBotsApi(DefaultBotSession.class);
            api.registerBot(new MapBot(token, bot_username, host, port));
            System.out.println("Bot avviato!");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

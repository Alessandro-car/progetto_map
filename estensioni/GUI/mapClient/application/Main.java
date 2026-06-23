package application;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

/**
 * Punto di ingresso dell'applicazione client con interfaccia grafica (JavaFX).
 * <p>
 * Avvia l'applicazione e mostra la finestra iniziale (il menu) caricata dal
 * file FXML corrispondente.
 */
public class Main extends Application {

    /**
     * Inizializza e mostra la finestra principale dell'applicazione.
     * <p>
     * Carica il layout dal file {@code Menu.fxml}, imposta titolo e scena e
     * rende la finestra non ridimensionabile.
     *
     * @param primaryStage la finestra principale fornita da JavaFX
     * @throws Exception se il caricamento del file FXML fallisce
     */
    @Override
    public void start(Stage primaryStage) throws Exception {
        Parent root = FXMLLoader.load(
            getClass().getResource("/Presentation/Menu.fxml")
        );
        primaryStage.setTitle("Regression Tree Client");
        primaryStage.setScene(new Scene(root));
        primaryStage.setResizable(false);
        primaryStage.show();
    }

    /**
     * Metodo main: avvia l'applicazione JavaFX.
     *
     * @param args eventuali argomenti da riga di comando
     */
    public static void main(String[] args) {
        launch(args);
    }
}

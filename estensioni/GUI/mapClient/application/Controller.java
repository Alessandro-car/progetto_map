package application;

import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.control.TextFormatter;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.function.UnaryOperator;

/**
 * Controller della finestra iniziale (menu) dell'interfaccia grafica.
 * <p>
 * Gestisce l'inserimento dell'indirizzo IP, la connessione al server e il
 * passaggio alla schermata di caricamento dei dati (da database o da file).
 * Lo stato della connessione viene mantenuto in campi statici così da essere
 * condiviso con le altre schermate dell'applicazione.
 */
public class Controller {

    /** Modalità scelta dall'utente: {@code "fromDb"} oppure {@code "fromFile"}. */
    public static String caseName = null;

    /** Client connesso al server, condiviso tra le schermate. */
    public static RtClient menu;

    /** Indica se la connessione al server è stata stabilita. */
    public static boolean connection = false;

    /** Indirizzo IP del server a cui si è connessi. */
    public static String ip;

    /** Campo di testo in cui l'utente inserisce l'indirizzo IP del server. */
    @FXML private TextField textIp;

    /** Pulsante che avvia il tentativo di connessione al server. */
    @FXML private Button buttonConnection;

    /** Pulsante che avvia il caricamento dei dati da database. */
    @FXML private Button buttonFromDB;

    /** Pulsante che avvia il caricamento di un albero salvato da file. */
    @FXML private Button fromFileButton;

    /** Pulsante che permette di ritentare la connessione dopo un errore. */
    @FXML private Button buttonRetry;

    /** Etichetta che segnala l'avvenuta connessione al server. */
    @FXML private Label linked;

    /** Etichetta che mostra l'esito del controllo sull'indirizzo IP inserito. */
    @FXML private Label ipControl;

    /**
     * Inizializza la schermata: ripristina lo stato se la connessione è già
     * attiva e imposta un filtro che consente di digitare nel campo IP solo
     * cifre e punti.
     */
    public void initialize() {
        if (connection) {
            textIp.setText(ip);
            textIp.setDisable(true);
            linked.setVisible(true);
            buttonFromDB.setDisable(false);
            fromFileButton.setDisable(false);
        }

        buttonConnection.defaultButtonProperty().bind(buttonConnection.focusedProperty());
        buttonFromDB.defaultButtonProperty().bind(buttonFromDB.focusedProperty());
        fromFileButton.defaultButtonProperty().bind(fromFileButton.focusedProperty());
        buttonRetry.defaultButtonProperty().bind(buttonRetry.focusedProperty());

        UnaryOperator<TextFormatter.Change> ipFilter = change -> {
            String newText = change.getControlNewText();
            if (newText.matches("[0-9.]*")) {
                return change;
            }
            return null;
        };
        textIp.setTextFormatter(new TextFormatter<>(ipFilter));
    }

    /**
     * Tenta la connessione al server con l'IP inserito.
     * <p>
     * La connessione viene eseguita in un thread di background per non bloccare
     * l'interfaccia; al termine aggiorna i pulsanti e i messaggi a seconda
     * dell'esito (riuscita o fallita).
     */
    @FXML
    public void connectButton() {
        final String targetIp = textIp.getText().trim();

        if (!validate(targetIp)) {
            ipControl.setVisible(true);
            return;
        }
        ipControl.setVisible(false);

        buttonConnection.setDisable(true);
        textIp.setDisable(true);
        linked.setText("CONNECTING...");
        linked.setTextFill(Color.ORANGE);
        linked.setVisible(true);

        Task<RtClient> connectionTask = new Task<>() {
            @Override
            protected RtClient call() throws Exception {
                return new RtClient(targetIp);
            }
        };

        connectionTask.setOnSucceeded(e -> {
            menu = connectionTask.getValue();
            ip = targetIp;
            connection = true;

            buttonFromDB.setDisable(false);
            fromFileButton.setDisable(false);
            buttonConnection.setVisible(false);
            linked.setText("CONNECTION ACCEPTED!");
            linked.setTextFill(Color.GREEN);
        });

        connectionTask.setOnFailed(e -> {
            Throwable cause = connectionTask.getException();
            System.out.println("Connection failed: " + cause.getMessage());

            linked.setText("CONNECTION FAILED!");
            linked.setTextFill(Color.RED);
            buttonConnection.setVisible(false);
            buttonRetry.setVisible(true);
            buttonRetry.setDisable(false);
        });

        Thread thread = new Thread(connectionTask);
        thread.setDaemon(true);
        thread.start();
    }

    /**
     * Verifica che la stringa fornita sia un indirizzo IPv4 valido.
     *
     * @param ip la stringa da controllare
     * @return {@code true} se è un indirizzo IPv4 valido, {@code false} altrimenti
     */
    public static boolean validate(final String ip) {
        String PATTERN = "^((0|1\\d?\\d?|2[0-4]?\\d?|25[0-5]?|[3-9]\\d?)\\.){3}(0|1\\d?\\d?|2[0-4]?\\d?|25[0-5]?|[3-9]\\d?)$";
        return ip.matches(PATTERN);
    }

    /**
     * Abilita il pulsante di connessione solo quando il campo IP contiene un
     * indirizzo valido.
     */
    @FXML
    public void enableConnect() {
        if (!textIp.getText().trim().isEmpty() && validate(textIp.getText().trim())) {
            buttonConnection.setDisable(false);
            ipControl.setVisible(false);
        } else {
            buttonConnection.setDisable(true);
        }
    }

    /**
     * Imposta la modalità "caricamento da database" e apre la schermata
     * successiva.
     *
     * @param event l'evento generato dal pulsante premuto
     */
    @FXML
    public void loadFromDB(ActionEvent event) {
        caseName = "fromDb";
        openWindow(event, "Load Data From Database");
    }

    /**
     * Imposta la modalità "caricamento da file" e apre la schermata successiva.
     *
     * @param event l'evento generato dal pulsante premuto
     */
    @FXML
    public void loadFromFile(ActionEvent event) {
        caseName = "fromFile";
        openWindow(event, "Load From File");
    }

		/**
     * Chiude la connessione corrente e ne apre una nuova verso lo stesso server,
     * così da riportare il protocollo client-server in uno stato pulito dopo un
     * errore o prima di una nuova sessione.
     */
    public static void refreshConnection() {
        if (connection && ip != null) {
            try {
                if (menu != null) {
                    menu.close();
                }
                menu = new RtClient(ip);
            } catch (IOException e) {
                System.err.println("Errore durante il refresh della connessione: " + e.getMessage());
            }
        }
    }

    /**
     * Apre la schermata di caricamento dati.
     * <p>
     * Prima di cambiare scena, se una connessione è attiva, la riapre da zero
     * così da ripulire lo stato del server ed evitare residui di sessioni
     * precedenti.
     *
     * @param event l'evento che ha originato il cambio di schermata
     * @param title il titolo da assegnare alla nuova finestra
     */
    public void openWindow(ActionEvent event, String title) {
    try {
        Controller.refreshConnection();

        Parent parent = FXMLLoader.load(getClass().getResource("/Presentation/Load_From.fxml"));
        Scene scene = new Scene(parent);
        Stage window = (Stage) ((Node) event.getSource()).getScene().getWindow();
        window.setScene(scene);
        window.setTitle(title);
        window.show();
    } catch (IOException e) {
        System.err.println("download error from Load_From.fxml");
        e.printStackTrace();
    }
}

    /**
     * Reimposta la schermata per permettere un nuovo tentativo di connessione
     * dopo un fallimento.
     */
    @FXML
    public void retryConnection() {
        buttonRetry.setVisible(false);
        buttonConnection.setVisible(true);
        buttonConnection.setDisable(false);
        linked.setVisible(false);
        textIp.clear();
        textIp.setDisable(false);
    }


    /** Evidenzia il pulsante di connessione quando il mouse vi passa sopra. */
    @FXML private void hoverConnect(MouseEvent e) {
        buttonConnection.setStyle(
            "-fx-background-color: #5C72FF;" +
            "-fx-text-fill: white;" +
            "-fx-font-weight: bold;" +
            "-fx-background-radius: 6;" +
            "-fx-cursor: hand;");
    }
    /** Ripristina lo stile del pulsante di connessione quando il mouse esce. */
    @FXML private void exitConnect(MouseEvent e) {
        buttonConnection.setStyle(
            "-fx-background-color: #3D5AFE;" +
            "-fx-text-fill: white;" +
            "-fx-font-weight: bold;" +
            "-fx-background-radius: 6;" +
            "-fx-cursor: hand;");
    }

    /** Evidenzia il pulsante "riprova" quando il mouse vi passa sopra. */
    @FXML private void hoverRetry(MouseEvent e) {
        buttonRetry.setStyle(
            "-fx-background-color: #757575;" +
            "-fx-border-color: #757575;" +
            "-fx-border-width: 1;" +
            "-fx-border-radius: 6;" +
            "-fx-text-fill: white;" +
            "-fx-background-radius: 6;" +
            "-fx-cursor: hand;");
    }
    /** Ripristina lo stile del pulsante "riprova" quando il mouse esce. */
    @FXML private void exitRetry(MouseEvent e) {
        buttonRetry.setStyle(
            "-fx-background-color: transparent;" +
            "-fx-border-color: #757575;" +
            "-fx-border-width: 1;" +
            "-fx-border-radius: 6;" +
            "-fx-text-fill: #AAAAAA;" +
            "-fx-background-radius: 6;" +
            "-fx-cursor: hand;");
    }

    /** Evidenzia il pulsante "carica da DB" quando il mouse vi passa sopra. */
    @FXML private void hoverDB(MouseEvent e) {
        buttonFromDB.setStyle(
            "-fx-background-color: #43A047;" +
            "-fx-border-color: #43A047;" +
            "-fx-border-width: 2;" +
            "-fx-border-radius: 6;" +
            "-fx-text-fill: white;" +
            "-fx-font-weight: bold;" +
            "-fx-background-radius: 6;" +
            "-fx-cursor: hand;");
    }
    /** Ripristina lo stile del pulsante "carica da DB" quando il mouse esce. */
    @FXML private void exitDB(MouseEvent e) {
        buttonFromDB.setStyle(
            "-fx-background-color: transparent;" +
            "-fx-border-color: #43A047;" +
            "-fx-border-width: 2;" +
            "-fx-border-radius: 6;" +
            "-fx-text-fill: #43A047;" +
            "-fx-font-weight: bold;" +
            "-fx-background-radius: 6;" +
            "-fx-cursor: hand;");
    }

    /** Evidenzia il pulsante "carica da file" quando il mouse vi passa sopra. */
    @FXML private void hoverFile(MouseEvent e) {
        fromFileButton.setStyle(
            "-fx-background-color: #FB8C00;" +
            "-fx-border-color: #FB8C00;" +
            "-fx-border-width: 2;" +
            "-fx-border-radius: 6;" +
            "-fx-text-fill: white;" +
            "-fx-font-weight: bold;" +
            "-fx-background-radius: 6;" +
            "-fx-cursor: hand;");
    }
    /** Ripristina lo stile del pulsante "carica da file" quando il mouse esce. */
    @FXML private void exitFile(MouseEvent e) {
        fromFileButton.setStyle(
            "-fx-background-color: transparent;" +
            "-fx-border-color: #FB8C00;" +
            "-fx-border-width: 2;" +
            "-fx-border-radius: 6;" +
            "-fx-text-fill: #FB8C00;" +
            "-fx-font-weight: bold;" +
            "-fx-background-radius: 6;" +
            "-fx-cursor: hand;");
    }
}

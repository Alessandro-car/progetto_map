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

public class Controller {

    public static String caseName = null;
    public static RtClient menu;
    public static boolean connection = false;
    public static String ip;

    @FXML private TextField textIp;
    @FXML private Button buttonConnection;
    @FXML private Button buttonFromDB;
    @FXML private Button fromFileButton;
    @FXML private Button buttonRetry;
    @FXML private Label linked;
    @FXML private Label ipControl;

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
            System.out.println("Connessione fallita: " + cause.getMessage());

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

    public static boolean validate(final String ip) {
        String PATTERN = "^((0|1\\d?\\d?|2[0-4]?\\d?|25[0-5]?|[3-9]\\d?)\\.){3}(0|1\\d?\\d?|2[0-4]?\\d?|25[0-5]?|[3-9]\\d?)$";
        return ip.matches(PATTERN);
    }

    @FXML
    public void enableConnect() {
        if (!textIp.getText().trim().isEmpty() && validate(textIp.getText().trim())) {
            buttonConnection.setDisable(false);
            ipControl.setVisible(false);
        } else {
            buttonConnection.setDisable(true);
        }
    }

    @FXML
    public void loadFromDB(ActionEvent event) {
        caseName = "fromDb";
        openWindow(event, "Load Data From Database");
    }

    @FXML
    public void loadFromFile(ActionEvent event) {
        caseName = "fromFile";
        openWindow(event, "Load From File");
    }

    public void openWindow(ActionEvent event, String title) {
        try {
            Parent parent = FXMLLoader.load(getClass().getResource("/Presentation/Load_From.fxml"));
            Scene scene = new Scene(parent);
            Stage window = (Stage) ((Node) event.getSource()).getScene().getWindow();
            window.setScene(scene);
            window.setTitle(title);
            window.show();
        } catch (IOException e) {
            System.err.println("Errore nel caricamento di Load_From.fxml");
            e.printStackTrace();
        }
    }

    @FXML
    public void retryConnection() {
        buttonRetry.setVisible(false);
        buttonConnection.setVisible(true);
        buttonConnection.setDisable(false);
        linked.setVisible(false);
        textIp.clear();
        textIp.setDisable(false);
    }

    // ── HOVER METHODS ─────────────────────────────────────────────────

    @FXML private void hoverConnect(MouseEvent e) {
        buttonConnection.setStyle(
            "-fx-background-color: #5C72FF;" +
            "-fx-text-fill: white;" +
            "-fx-font-weight: bold;" +
            "-fx-background-radius: 6;" +
            "-fx-cursor: hand;");
    }
    @FXML private void exitConnect(MouseEvent e) {
        buttonConnection.setStyle(
            "-fx-background-color: #3D5AFE;" +
            "-fx-text-fill: white;" +
            "-fx-font-weight: bold;" +
            "-fx-background-radius: 6;" +
            "-fx-cursor: hand;");
    }

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
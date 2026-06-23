package application;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.List;
import java.util.function.UnaryOperator;

public class Load_From_Controller {

    private String tableName;

    @FXML private ComboBox<String> tableNameValue;
    @FXML private TextField branchValue;
    @FXML private TextArea  output;
    @FXML private Button    executionButton;
    @FXML private Button    newExecutionButton;
    @FXML private Button    exitButton;
    @FXML private Button    okButton;
    @FXML private Button    menuButton;
    @FXML private Label tableNameLabel;

    /**
     * Interfaccia funzionale equivalente a Supplier<List<String>>, ma che
     * permette di propagare qualsiasi eccezione checked (IOException,
     * ClassNotFoundException, ecc.) dichiarata da Controller.menu.getTableNames()
     * e getFileNames().
     */
    @FunctionalInterface
    private interface NamesSupplier {
        List<String> get() throws Exception;
    }

    public void initialize() {
        newExecutionButton.setDisable(true);
        okButton.setDisable(true);
        branchValue.setDisable(true);
        executionButton.setDisable(true);


        menuButton.defaultButtonProperty().bind(menuButton.focusedProperty());
        exitButton.defaultButtonProperty().bind(exitButton.focusedProperty());
        executionButton.defaultButtonProperty().bind(executionButton.focusedProperty());
        newExecutionButton.defaultButtonProperty().bind(newExecutionButton.focusedProperty());
        okButton.defaultButtonProperty().bind(okButton.focusedProperty());

        UnaryOperator<TextFormatter.Change> integerFilter = change -> {
            String input = change.getControlNewText();
            if (input.matches("[0-9]*")) {
                return change;
            }
            return null;
        };
        branchValue.setTextFormatter(new TextFormatter<>(integerFilter));

        // Abilita Execute quando viene selezionato/digitato un valore nella ComboBox
        tableNameValue.valueProperty().addListener((obs, oldVal, newVal) -> enableExecute());

        if (Controller.caseName != null && Controller.caseName.equals("fromDb")) {
            tableNameLabel.setText("TABLE NAME");
            loadNames(() -> Controller.menu.getTableNames(), "Seleziona una tabella...");
        } else {
            tableNameLabel.setText("FILE NAME");
            loadNames(() -> Controller.menu.getFileNames(), "Seleziona un file...");
        }
    }

    /**
     * Richiede al server (o al file system, secondo il supplier passato)
     * la lista dei nomi disponibili e popola la ComboBox in un thread di background.
     *
     * @param namesSupplier funzione che recupera la lista dei nomi (tabelle o file)
     * @param promptText    testo da mostrare nella ComboBox quando ci sono elementi
     */
    private void loadNames(NamesSupplier namesSupplier, String promptText) {
        Task<ObservableList<String>> task = new Task<>() {
            @Override
            protected ObservableList<String> call() throws Exception {
                List<String> names = namesSupplier.get();
                return FXCollections.observableArrayList(names);
            }
        };

        task.setOnSucceeded(e -> {
            tableNameValue.setItems(task.getValue());
            tableNameValue.setPromptText(
                task.getValue().isEmpty() ? "Nessun elemento trovato" : promptText
            );
        });

        task.setOnFailed(e -> {
            tableNameValue.setPromptText("Errore caricamento dati");
            String msg = task.getException() != null
                    ? task.getException().getMessage()
                    : "Errore sconosciuto";
            if (output != null) {
                output.appendText("Impossibile caricare i dati: " + msg + "\n");
            } else {
                System.err.println("Impossibile caricare i dati: " + msg);
            }
        });

        Thread thread = new Thread(task);
        thread.setDaemon(true);
        thread.start();
    }

    @FXML
    public void enableExecute() {
        executionButton.setDisable(tableNameValue.getValue() == null);
    }

    @FXML
    public void execute() {
        tableName = tableNameValue.getValue();
        if (tableName == null || tableName.isEmpty())
            return;

        tableNameValue.setDisable(true);
        executionButton.setDisable(true);
        output.setText("Connessione al server...\n");

        Task<String> executionTask = new Task<>() {
            @Override
            protected String call() throws Exception {
                if (Controller.caseName.equals("fromDb")) {
                    Controller.menu.storeTableFromDb(tableName);
                    return Controller.menu.learningFromDbTable();
                } else {
                    return Controller.menu.learningFromFile(tableName);
                }
            }
        };

        executionTask.setOnSucceeded(e -> {
            String result = executionTask.getValue();
            output.setText("Albero generato con successo.\n\n");

            if (result.startsWith("PREDICTED:")) {
                String value = result.substring("PREDICTED:".length());
                output.appendText("Predicted class value: " + value + "\n");
                newExecutionButton.setDisable(false);
            } else {
                output.appendText("Fase di predizione avviata:\n\n" + result);
                branchValue.setDisable(false);
                okButton.setDisable(false);
                branchValue.requestFocus();
            }
        });

        executionTask.setOnFailed(e -> {
            output.appendText("\nErrore: " + executionTask.getException().getMessage());
            tableNameValue.setDisable(false);
            executionButton.setDisable(false);
            newExecutionButton.setDisable(false);
        });

        Thread thread = new Thread(executionTask);
        thread.setDaemon(true);
        thread.start();
    }

    @FXML
    public void goAction() {
        String branchText = branchValue.getText().trim();

        if (branchText.isEmpty()) {
            output.appendText("\nInserisci un numero di branch valido.\n");
            branchValue.requestFocus();
            return;
        }

        int branch = Integer.parseInt(branchText);
        output.appendText("\nScelta: " + branch + " — elaborazione...");
        okButton.setDisable(true);
        branchValue.setDisable(true);

        Task<String> branchTask = new Task<>() {
            @Override
            protected String call() throws Exception {
                if (Controller.caseName.equals("fromDb")) {
                    return Controller.menu.learningFromDbTable(branch);
                } else {
                    return Controller.menu.learningFromFile(branch);
                }
            }
        };

        branchTask.setOnSucceeded(e -> {
            String result = branchTask.getValue();

            if (result.startsWith("PREDICTED:")) {
                String value = result.substring("PREDICTED:".length());
                output.appendText("\n\nPredicted class value: " + value + "\n");
                branchValue.setDisable(true);
                okButton.setDisable(true);
                newExecutionButton.setDisable(false);
                newExecutionButton.requestFocus();
            } else {
                output.appendText("\n\n" + result);
                branchValue.setDisable(false);
                okButton.setDisable(false);
                branchValue.clear();
                branchValue.requestFocus();
            }
        });

        branchTask.setOnFailed(e -> {
            output.appendText("\nErrore: " + branchTask.getException().getMessage());
            newExecutionButton.setDisable(false);
        });

        Thread thread = new Thread(branchTask);
        thread.setDaemon(true);
        thread.start();
    }

    @FXML
    public void newExecution() {
        tableName = null;
        tableNameValue.setValue(null);
        tableNameValue.setDisable(false);
        branchValue.clear();
        branchValue.setDisable(true);
        output.clear();
        executionButton.setDisable(true);
        newExecutionButton.setDisable(true);
        okButton.setDisable(true);
        tableNameValue.requestFocus();
    }

    @FXML
    public void exitFrame() {
        javafx.application.Platform.exit();
    }

    @FXML
    public void menu(ActionEvent event) {
        try {
            Parent parent = FXMLLoader.load(getClass().getResource("/Presentation/Menu.fxml"));
            Scene scene = new Scene(parent);
            Stage window = (Stage) ((Node) event.getSource()).getScene().getWindow();
            window.setScene(scene);
            window.setTitle("Menu");
            window.show();
        } catch (Exception e) {
            e.printStackTrace();
            output.appendText("\nErrore nel caricamento del menu: " + e);
        }
    }
}
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

/**
 * Controller della schermata di caricamento dati e predizione.
 * <p>
 * In base alla modalità scelta nel menu (da database o da file), popola la lista
 * dei nomi disponibili, avvia la costruzione/caricamento dell'albero e gestisce
 * la fase di predizione interattiva, mostrando le domande e raccogliendo il ramo
 * scelto dall'utente fino a ottenere la classe predetta.
 */
public class Load_From_Controller {

    /** Nome della tabella o del file selezionato dall'utente. */
    private String tableName;

    /** Menu a tendina per selezionare il nome della tabella o del file da cui caricare i dati. */
    @FXML private ComboBox<String> tableNameValue;

    /** Campo di testo in cui l'utente inserisce il ramo scelto durante la fase di predizione. */
    @FXML private TextField branchValue;

    /** Area di testo in cui vengono mostrati i messaggi, le domande e il risultato della predizione. */
    @FXML private TextArea  output;

    /** Pulsante che avvia la costruzione/caricamento dell'albero e la fase di predizione. */
    @FXML private Button    executionButton;

    /** Pulsante che avvia una nuova esecuzione riportando la schermata allo stato iniziale. */
    @FXML private Button    newExecutionButton;

    /** Pulsante che chiude l'applicazione. */
    @FXML private Button    exitButton;

    /** Pulsante di conferma del ramo inserito durante la fase di predizione. */
    @FXML private Button    okButton;

    /** Pulsante che riporta l'utente al menu principale. */
    @FXML private Button    menuButton;

    /** Etichetta descrittiva associata alla selezione del nome della tabella o del file. */
    @FXML private Label tableNameLabel;

    /**
     * Interfaccia funzionale equivalente a {@code Supplier<List<String>>}, ma che
     * permette di propagare qualsiasi eccezione checked (IOException,
     * ClassNotFoundException, ecc.) dichiarata da Controller.menu.getTableNames()
     * e getFileNames().
     */
    @FunctionalInterface
    private interface NamesSupplier {
        List<String> get() throws Exception;
    }

    /**
     * Inizializza la schermata: disabilita i controlli non ancora utilizzabili,
     * imposta un filtro per accettare nel campo del ramo solo numeri e carica la
     * lista dei nomi (tabelle o file) a seconda della modalità scelta.
     */
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

        tableNameValue.valueProperty().addListener((obs, oldVal, newVal) -> enableExecute());

        if (Controller.caseName != null && Controller.caseName.equals("fromDb")) {
            tableNameLabel.setText("TABLE NAME");
            loadNames(() -> Controller.menu.getTableNames(), "select a table...");
        } else {
            tableNameLabel.setText("FILE NAME");
            loadNames(() -> Controller.menu.getFileNames(), "select a file...");
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
                task.getValue().isEmpty() ? "no items found" : promptText
            );
        });

        task.setOnFailed(e -> {
            tableNameValue.setPromptText("data loading error");
            String msg = task.getException() != null
                    ? task.getException().getMessage()
                    : "Errore sconosciuto";
            if (output != null) {
                output.appendText("unable to load data: " + msg + "\n");
            } else {
                System.err.println("unable to load data: " + msg);
            }
        });

        Thread thread = new Thread(task);
        thread.setDaemon(true);
        thread.start();
    }

    /**
     * Abilita il pulsante di esecuzione solo quando è stato selezionato un nome
     * nella ComboBox.
     */
    @FXML
    public void enableExecute() {
        executionButton.setDisable(tableNameValue.getValue() == null);
    }

    /**
     * Avvia la costruzione o il caricamento dell'albero per il nome selezionato e
     * la successiva fase di predizione, eseguendo l'operazione in background.
     * <p>
     * Se il server restituisce subito una classe predetta la mostra, altrimenti
     * abilita i controlli per inserire il ramo da seguire.
     */
    @FXML
    public void execute() {
        tableName = tableNameValue.getValue();
        if (tableName == null || tableName.isEmpty())
            return;

        tableNameValue.setDisable(true);
        executionButton.setDisable(true);
        output.setText("server connection...\n");

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
            output.setText("Tree successfully generated\n\n");

            if (result.startsWith("PREDICTED:")) {
                String value = result.substring("PREDICTED:".length());
                output.appendText("Predicted class value: " + value + "\n");
                newExecutionButton.setDisable(false);
            } else {
                output.appendText("Prediction phase started:\n\n" + result);
                branchValue.setDisable(false);
                okButton.setDisable(false);
                branchValue.requestFocus();
            }
        });

        executionTask.setOnFailed(e -> {
            output.appendText("\nError: " + executionTask.getException().getMessage());
            Controller.refreshConnection();
            tableNameValue.setDisable(false);
            executionButton.setDisable(false);
            newExecutionButton.setDisable(false);
        });

        Thread thread = new Thread(executionTask);
        thread.setDaemon(true);
        thread.start();
    }

    /**
     * Invia al server il ramo scelto dall'utente e mostra la risposta.
     * <p>
     * Se viene raggiunta una foglia stampa la classe predetta, altrimenti mostra
     * la domanda successiva e resta in attesa di un nuovo ramo.
     */
    @FXML
    public void goAction() {
        String branchText = branchValue.getText().trim();

        if (branchText.isEmpty()) {
            output.appendText("\nPlease enter a valid branch number .\n");
            branchValue.requestFocus();
            return;
        }
				int branch;
				try {
					branch = Integer.parseInt(branchText);
				} catch (NumberFormatException e) {
					output.appendText("\nPlease enter a valid branch number.\n");
          branchValue.requestFocus();
          return;
				}
        output.appendText("\nChoice: " + branch + " — processing...");
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
            output.appendText("\nError: " + branchTask.getException().getMessage());
            newExecutionButton.setDisable(false);
        });

        Thread thread = new Thread(branchTask);
        thread.setDaemon(true);
        thread.start();
    }

    /**
     * Reimposta la schermata per avviare una nuova esecuzione, ripulendo
     * selezione, campo del ramo e area di output.
     */
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

    /**
     * Chiude l'applicazione.
     */
    @FXML
    public void exitFrame() {
        javafx.application.Platform.exit();
    }

    /**
     * Torna alla schermata del menu principale.
     *
     * @param event l'evento generato dal pulsante premuto
     */
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
            output.appendText("\n Error loading menu: " + e);
        }
    }
}

package application;

/**
 * Punto di ingresso "non-JavaFX" usato come Main-Class del jar eseguibile.
 * <p>
 * Quando una classe che estende {@link javafx.application.Application} viene
 * usata direttamente come Main-Class di un jar lanciato dal classpath, il
 * runtime solleva l'errore "JavaFX runtime components are missing". Questa
 * classe, che <em>non</em> estende {@code Application}, evita il problema e
 * delega l'avvio a {@link Main}, permettendo di eseguire l'applicazione con il
 * solo comando {@code java -jar client_gui.jar}.
 */
public class Launcher {

    /**
     * Avvia l'applicazione JavaFX delegando a {@link Main#main(String[])}.
     *
     * @param args eventuali argomenti da riga di comando
     */
    public static void main(String[] args) {
        Main.main(args);
    }
}

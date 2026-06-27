package tree;

import data.Data;
import data.TrainingDataException;
import server.UnknownValueException;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.Assumptions;

import java.io.*;
import java.sql.SQLException;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test di integrazione per la classe {@link RegressionTree}.
 * <p>
 * Richiede un database MySQL raggiungibile con le credenziali configurate in
 * {@code DbAccess} e la tabella <b>servo</b> (attributi discreti + target numerico).
 * Se il database non è disponibile i test che lo richiedono vengono marcati
 * automaticamente come <em>skipped</em> (non come falliti) tramite
 * {@link Assumptions#assumeTrue}.
 */
@DisplayName("Test di RegressionTree")
class RegressionTreeTest {

    /** Nome della tabella principale usata nei test. */
    private static final String TABLE = "servo";

    /**
     * Costruisce un {@link Data} sulla tabella di test, oppure salta il test
     * corrente se il database non è disponibile.
     */
    private Data loadDataOrSkip() {
        try {
            return new Data(TABLE);
        } catch (TrainingDataException | SQLException e) {
            Assumptions.assumeTrue(false, "Database non disponibile: " + e.getMessage());
            return null; // non raggiunto
        }
    }

    // =========================================================================
    // 1. COSTRUTTORE / APPRENDIMENTO
    // =========================================================================
    @Nested
    @DisplayName("Costruttore e apprendimento")
    class ConstructorTests {

        @Test
        @DisplayName("Costruisce un albero non nullo da una tabella valida")
        void constructorSucceedsWithValidTable() {
            Data data = loadDataOrSkip();
            RegressionTree tree = new RegressionTree(data);
            assertNotNull(tree);
        }

        @Test
        @DisplayName("L'apprendimento non lancia eccezioni")
        void learningDoesNotThrow() {
            Data data = loadDataOrSkip();
            assertDoesNotThrow(() -> new RegressionTree(data));
        }
    }

    // =========================================================================
    // 2. toString
    // =========================================================================
    @Nested
    @DisplayName("toString")
    class ToStringTests {

        private RegressionTree tree;

        @BeforeEach
        void setUp() {
            Data data = loadDataOrSkip();
            tree = new RegressionTree(data);
        }

        @Test
        @DisplayName("toString non restituisce stringa vuota")
        void toStringNotEmpty() {
            assertFalse(tree.toString().isEmpty());
        }

        @Test
        @DisplayName("toString contiene almeno un nodo (LEAF o SPLIT)")
        void toStringContainsNodeInfo() {
            String s = tree.toString();
            assertTrue(s.contains("LEAF") || s.contains("SPLIT"),
                    "toString deve contenere informazioni sui nodi");
        }
    }

    // =========================================================================
    // 3. printRules / printTree
    // =========================================================================
    @Nested
    @DisplayName("printRules e printTree")
    class PrintTests {

        private RegressionTree tree;

        @BeforeEach
        void setUp() {
            Data data = loadDataOrSkip();
            tree = new RegressionTree(data);
        }

        @Test
        @DisplayName("printRules non lancia eccezioni")
        void printRulesDoesNotThrow() {
            assertDoesNotThrow(() -> tree.printRules());
        }

        @Test
        @DisplayName("printTree non lancia eccezioni")
        void printTreeDoesNotThrow() {
            assertDoesNotThrow(() -> tree.printTree());
        }
    }

    // =========================================================================
    // 4. SERIALIZZAZIONE: salva / carica
    // =========================================================================
    @Nested
    @DisplayName("Serializzazione (salva/carica)")
    class SerializationTests {

        private RegressionTree tree;

        @BeforeEach
        void setUp() {
            Data data = loadDataOrSkip();
            tree = new RegressionTree(data);
        }

        @Test
        @DisplayName("salva crea effettivamente il file su disco")
        void salvaCreatesFile() throws IOException {
            File f = new File("test_tree_" + System.nanoTime() + ".dmp");
            try {
                tree.salva(f.getName());
                assertTrue(f.exists(), "Il file serializzato deve esistere dopo salva()");
            } finally {
                f.delete();
            }
        }

        @Test
        @DisplayName("carica restituisce un albero non nullo dopo salva")
        void caricaReturnsNonNullTree() throws IOException, ClassNotFoundException {
            File f = new File("test_tree_" + System.nanoTime() + ".dmp");
            try {
                tree.salva(f.getName());
                RegressionTree loaded = RegressionTree.carica(f.getName());
                assertNotNull(loaded);
            } finally {
                f.delete();
            }
        }

        @Test
        @DisplayName("L'albero caricato produce lo stesso toString dell'originale")
        void loadedTreeEqualsOriginal() throws IOException, ClassNotFoundException {
            File f = new File("test_tree_" + System.nanoTime() + ".dmp");
            try {
                tree.salva(f.getName());
                RegressionTree loaded = RegressionTree.carica(f.getName());
                assertEquals(tree.toString(), loaded.toString(),
                        "L'albero serializzato e ricaricato deve essere identico all'originale");
            } finally {
                f.delete();
            }
        }

        @Test
        @DisplayName("carica lancia FileNotFoundException per file inesistente")
        void caricaThrowsForMissingFile() {
            assertThrows(FileNotFoundException.class,
                    () -> RegressionTree.carica("file_inesistente_xyz_999.dmp"));
        }
    }

    // =========================================================================
    // 5. predictClass via stream (simulazione client)
    // =========================================================================
    @Nested
    @DisplayName("predictClass tramite stream")
    class PredictClassStreamTests {

        private RegressionTree tree;

        @BeforeEach
        void setUp() {
            Data data = loadDataOrSkip();
            tree = new RegressionTree(data);
        }

        @Test
        @DisplayName("predictClass lancia UnknownValueException per risposta fuori range")
        void predictClassThrowsForOutOfRangeAnswer() throws Exception {
            // Prepariamo uno stream di input che risponde con un indice palesemente
            // fuori range (9999) alla prima query dell'albero.
            ByteArrayOutputStream serverToClient = new ByteArrayOutputStream();
            ObjectOutputStream out = new ObjectOutputStream(serverToClient);

            ByteArrayOutputStream tmp = new ByteArrayOutputStream();
            ObjectOutputStream clientWriter = new ObjectOutputStream(tmp);
            clientWriter.writeObject(9999);
            clientWriter.flush();
            ObjectInputStream in = new ObjectInputStream(
                    new ByteArrayInputStream(tmp.toByteArray()));

            // Se la radice è già foglia il test non è significativo: lo saltiamo.
            Assumptions.assumeFalse(tree.toString().startsWith("LEAF"),
                    "La radice è una foglia, nessuna query da testare");

            assertThrows(UnknownValueException.class,
                    () -> tree.predictClass(in, out));
        }
    }
}

package data;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.Assumptions;

import java.sql.SQLException;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test di integrazione per la classe {@link Data}.
 * <p>
 * Richiede un database MySQL raggiungibile con le credenziali configurate in
 * {@code DbAccess} e le seguenti tabelle:
 * <ul>
 *   <li><b>servo</b> – tabella principale con almeno un attributo discreto,
 *       uno continuo e un target numerico (ultima colonna);</li>
 *   <li><b>empty_table</b> – tabella con schema valido ma zero righe;</li>
 *   <li><b>string_last_col_table</b> – tabella la cui ultima colonna è di tipo stringa.</li>
 * </ul>
 * Se il database non è disponibile i test che lo richiedono vengono marcati
 * automaticamente come <em>skipped</em> (non come falliti) tramite
 * {@link Assumptions#assumeTrue}.
 */
@DisplayName("Test di Data")
class DataTest {

    /** Nome della tabella principale usata nei test. Adattare al proprio schema. */
    private static final String TABLE = "provaC";

    // Helpers condivisi tra i test


    /**
     * Cerca il primo {@link ContinuousAttribute} tra gli attributi descrittivi
     * del dataset, oppure restituisce {@code null} se non ne esiste nessuno.
     */
    private ContinuousAttribute findContinuousAttribute(Data data) {
        for (int i = 0; i < data.getNumberOfExplanatoryAttributes(); i++) {
            Attribute a = data.getExplanatoryAttribute(i);
            if (a instanceof ContinuousAttribute) return (ContinuousAttribute) a;
        }
        return null;
    }

    /**
     * Cerca il primo {@link DiscreteAttribute} tra gli attributi descrittivi
     * del dataset, oppure restituisce {@code null} se non ne esiste nessuno.
     */
    private DiscreteAttribute findDiscreteAttribute(Data data) {
        for (int i = 0; i < data.getNumberOfExplanatoryAttributes(); i++) {
            Attribute a = data.getExplanatoryAttribute(i);
            if (a instanceof DiscreteAttribute) return (DiscreteAttribute) a;
        }
        return null;
    }

    // test per il costruttore

    @Nested
    @DisplayName("Costruttore")
    class ConstructorTests {

        @Test
        @DisplayName("Carica correttamente il dataset da una tabella valida")
        void constructorSucceedsWithValidTable() {
            try {
                Data data = new Data(TABLE);
                assertNotNull(data);
            } catch (TrainingDataException | SQLException e) {
                Assumptions.assumeTrue(false, "Database non disponibile: " + e.getMessage());
            }
        }

        @Test
        @DisplayName("Lancia TrainingDataException per tabella inesistente")
        void constructorThrowsForNonExistentTable() {
            assertThrows(TrainingDataException.class,
                    () -> new Data("tabella_inesistente_xyz_999"));
        }

        @Test
        @DisplayName("Lancia TrainingDataException se la tabella è vuota")
        void constructorThrowsForEmptyTable() {
            assertThrows(TrainingDataException.class,
                    () -> new Data("empty_table"));
        }

        @Test
        @DisplayName("Lancia TrainingDataException se l'ultima colonna non è numerica")
        void constructorThrowsIfLastColumnNotNumeric() {
            assertThrows(TrainingDataException.class,
                    () -> new Data("string_last_col_table"));
        }
    }

    // test su metodi di funzioni di base
    // Questi test usano tutti il DB: se non è disponibile vengono skippati.

    @Nested
    @DisplayName("Accessori di base")
    class AccessorTests {

        private Data data;

        @BeforeEach
        void setUp() {
            try {
                data = new Data(TABLE);
            } catch (TrainingDataException | SQLException e) {
                Assumptions.assumeTrue(false, "Database non disponibile: " + e.getMessage());
            }
        }

        @Test
        @DisplayName("getNumberOfExamples restituisce un valore positivo")
        void numberOfExamplesIsPositive() {
            assertTrue(data.getNumberOfExamples() > 0);
        }

        @Test
        @DisplayName("getNumberOfExplanatoryAttributes restituisce un valore positivo")
        void numberOfExplanatoryAttributesIsPositive() {
            assertTrue(data.getNumberOfExplanatoryAttributes() > 0);
        }

        @Test
        @DisplayName("getExplanatoryAttribute non restituisce null per tutti gli indici validi")
        void getExplanatoryAttributeNotNullForAllValidIndices() {
            for (int i = 0; i < data.getNumberOfExplanatoryAttributes(); i++) {
                int idx = i;
                assertNotNull(
                        data.getExplanatoryAttribute(idx),
                        "getExplanatoryAttribute(" + idx + ") ha restituito null");
            }
        }

        @Test
        @DisplayName("getExplanatoryAttribute è accessibile per tutti gli indici validi senza eccezioni")
        void getExplanatoryAttributeDoesNotThrowForValidIndices() {
            for (int i = 0; i < data.getNumberOfExplanatoryAttributes(); i++) {
                int idx = i;
                assertDoesNotThrow(() -> data.getExplanatoryAttribute(idx));
            }
        }

        @Test
        @DisplayName("getClassValue restituisce Double per ogni esempio")
        void getClassValueReturnsDoubleForAllExamples() {
            for (int i = 0; i < data.getNumberOfExamples(); i++) {
                Object val = data.getClassValue(i);
                assertNotNull(val, "getClassValue(" + i + ") ha restituito null");
                assertInstanceOf(Double.class, val,
                        "getClassValue(" + i + ") non ha restituito un Double");
            }
        }

        @Test
        @DisplayName("getExplanatoryValue restituisce Double per un attributo continuo")
        void getExplanatoryValueReturnsDoubleForContinuousAttribute() {
            ContinuousAttribute attr = findContinuousAttribute(data);
            Assumptions.assumeTrue(attr != null, "Nessun attributo continuo nel dataset");
            assertInstanceOf(Double.class,
                    data.getExplanatoryValue(0, attr.getIndex()),
                    "getExplanatoryValue non ha restituito un Double per attributo continuo");
        }

        @Test
        @DisplayName("getExplanatoryValue restituisce String per un attributo discreto")
        void getExplanatoryValueReturnsStringForDiscreteAttribute() {
            DiscreteAttribute attr = findDiscreteAttribute(data);
            Assumptions.assumeTrue(attr != null, "Nessun attributo discreto nel dataset");
            assertInstanceOf(String.class,
                    data.getExplanatoryValue(0, attr.getIndex()),
                    "getExplanatoryValue non ha restituito una String per attributo discreto");
        }
    }

    // Test sul metodo  toString

    @Nested
    @DisplayName("toString")
    class ToStringTests {

        private Data data;

        @BeforeEach
        void setUp() {
            try {
                data = new Data(TABLE);
            } catch (TrainingDataException | SQLException e) {
                Assumptions.assumeTrue(false, "Database non disponibile: " + e.getMessage());
            }
        }

        @Test
        @DisplayName("toString non restituisce stringa vuota")
        void toStringNotEmpty() {
            assertFalse(data.toString().isEmpty());
        }

        @Test
        @DisplayName("toString produce tante righe quanti sono gli esempi")
        void toStringHasCorrectNumberOfLines() {
            long lines = data.toString().lines().count();
            assertEquals(data.getNumberOfExamples(), lines,
                    "Il numero di righe in toString non corrisponde al numero di esempi");
        }
    }

    // Test sul  sort — casi normali

    @Nested
    @DisplayName("sort — casi normali")
    class SortNormalTests {

        private Data data;

        @BeforeEach
        void setUp() {
            try {
                data = new Data(TABLE);
            } catch (TrainingDataException | SQLException e) {
                Assumptions.assumeTrue(false, "Database non disponibile: " + e.getMessage());
            }
        }

        @Test
        @DisplayName("sort su ContinuousAttribute non lancia eccezioni")
        void sortContinuousNoException() {
            ContinuousAttribute attr = findContinuousAttribute(data);
            Assumptions.assumeTrue(attr != null, "Nessun attributo continuo nel dataset");
            assertDoesNotThrow(() ->
                    data.sort(attr, 0, data.getNumberOfExamples() - 1));
        }

        @Test
        @DisplayName("sort su ContinuousAttribute produce sequenza ordinata in modo non decrescente")
        void sortContinuousProducesOrderedSequence() {
            ContinuousAttribute attr = findContinuousAttribute(data);
            Assumptions.assumeTrue(attr != null, "Nessun attributo continuo nel dataset");

            data.sort(attr, 0, data.getNumberOfExamples() - 1);

            for (int i = 0; i < data.getNumberOfExamples() - 1; i++) {
                Double curr = (Double) data.getExplanatoryValue(i, attr.getIndex());
                Double next = (Double) data.getExplanatoryValue(i + 1, attr.getIndex());
                assertTrue(curr <= next,
                        "Ordine violato tra indice " + i + " (" + curr
                                + ") e " + (i + 1) + " (" + next + ")");
            }
        }

        @Test
        @DisplayName("sort su DiscreteAttribute non lancia eccezioni")
        void sortDiscreteNoException() {
            DiscreteAttribute attr = findDiscreteAttribute(data);
            Assumptions.assumeTrue(attr != null, "Nessun attributo discreto nel dataset");
            assertDoesNotThrow(() ->
                    data.sort(attr, 0, data.getNumberOfExamples() - 1));
        }

        @Test
        @DisplayName("sort su DiscreteAttribute produce sequenza ordinata lessicograficamente")
        void sortDiscreteProducesOrderedSequence() {
            DiscreteAttribute attr = findDiscreteAttribute(data);
            Assumptions.assumeTrue(attr != null, "Nessun attributo discreto nel dataset");

            data.sort(attr, 0, data.getNumberOfExamples() - 1);

            for (int i = 0; i < data.getNumberOfExamples() - 1; i++) {
                String curr = (String) data.getExplanatoryValue(i, attr.getIndex());
                String next = (String) data.getExplanatoryValue(i + 1, attr.getIndex());
                assertTrue(curr.compareTo(next) <= 0,
                        "Ordine lessicografico violato tra indice " + i + " (" + curr
                                + ") e " + (i + 1) + " (" + next + ")");
            }
        }
    }

    // test sul sort — edge case

    @Nested
    @DisplayName("sort — edge case")
    class SortEdgeCaseTests {

        private Data data;

        @BeforeEach
        void setUp() {
            try {
                data = new Data(TABLE);
            } catch (TrainingDataException | SQLException e) {
                Assumptions.assumeTrue(false, "Database non disponibile: " + e.getMessage());
            }
        }

        @Test
        @DisplayName("sort su range di un solo elemento non lancia eccezioni")
        void sortSingleElementRangeNoException() {
            ContinuousAttribute attr = findContinuousAttribute(data);
            Assumptions.assumeTrue(attr != null, "Nessun attributo continuo nel dataset");
            assertDoesNotThrow(() -> data.sort(attr, 0, 0));
        }

        @Test
        @DisplayName("sort applicato due volte su dati già ordinati mantiene l'ordine")
        void sortAlreadySortedStaysOrdered() {
            ContinuousAttribute attr = findContinuousAttribute(data);
            Assumptions.assumeTrue(attr != null, "Nessun attributo continuo nel dataset");

            data.sort(attr, 0, data.getNumberOfExamples() - 1);
            data.sort(attr, 0, data.getNumberOfExamples() - 1);

            for (int i = 0; i < data.getNumberOfExamples() - 1; i++) {
                Double curr = (Double) data.getExplanatoryValue(i, attr.getIndex());
                Double next = (Double) data.getExplanatoryValue(i + 1, attr.getIndex());
                assertTrue(curr <= next,
                        "Ordine violato alla seconda passata tra indice " + i
                                + " (" + curr + ") e " + (i + 1) + " (" + next + ")");
            }
        }

        @Test
        @DisplayName("sort su un sottorange non altera gli elementi fuori dal range")
        void sortSubrangeDoesNotAffectElementsOutside() {
            Assumptions.assumeTrue(data.getNumberOfExamples() >= 4,
                    "Il dataset ha meno di 4 esempi, test non significativo");
            ContinuousAttribute attr = findContinuousAttribute(data);
            Assumptions.assumeTrue(attr != null, "Nessun attributo continuo nel dataset");

            int last = data.getNumberOfExamples() - 1;
            Double valueBefore = (Double) data.getExplanatoryValue(last, attr.getIndex());

            data.sort(attr, 0, last / 2);

            Double valueAfter = (Double) data.getExplanatoryValue(last, attr.getIndex());
            assertEquals(valueBefore, valueAfter,
                    "sort ha modificato un elemento fuori dal range specificato");
        }

        @Test
        @DisplayName("sort su sottorange ordina correttamente solo quella porzione")
        void sortSubrangeOrdersOnlyThatPortion() {
            Assumptions.assumeTrue(data.getNumberOfExamples() >= 4,
                    "Il dataset ha meno di 4 esempi, test non significativo");
            ContinuousAttribute attr = findContinuousAttribute(data);
            Assumptions.assumeTrue(attr != null, "Nessun attributo continuo nel dataset");

            int mid = data.getNumberOfExamples() / 2;
            data.sort(attr, 0, mid);

            for (int i = 0; i < mid; i++) {
                Double curr = (Double) data.getExplanatoryValue(i, attr.getIndex());
                Double next = (Double) data.getExplanatoryValue(i + 1, attr.getIndex());
                assertTrue(curr <= next,
                        "Ordine violato nel sottorange tra indice " + i
                                + " (" + curr + ") e " + (i + 1) + " (" + next + ")");
            }
        }
    }
}

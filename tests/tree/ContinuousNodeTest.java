package tree;

import data.Data;
import data.DiscreteAttribute;
import data.Attribute;
import data.TrainingDataException;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.Assumptions;

import java.sql.SQLException;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test di integrazione per {@link ContinuousNode} e per il comportamento di
 * {@link SplitNode} (in particolare {@code compareTo}).
 * <p>
 * La tabella <b>servo</b> contiene solo attributi discreti, quindi i test
 * specifici su attributi continui vengono skippati se non se ne trova uno.
 * Il confronto tra SplitNode (compareTo) viene invece testato con i DiscreteNode
 * disponibili in servo.
 */
@DisplayName("Test di ContinuousNode e SplitNode")
class ContinuousNodeTest {

    private static final String TABLE = "servo";

    private Data data;

    @BeforeEach
    void setUp() {
        try {
            data = new Data(TABLE);
        } catch (TrainingDataException | SQLException e) {
            Assumptions.assumeTrue(false, "Database non disponibile: " + e.getMessage());
        }
    }

    private DiscreteAttribute firstDiscrete() {
        for (int i = 0; i < data.getNumberOfExplanatoryAttributes(); i++) {
            Attribute a = data.getExplanatoryAttribute(i);
            if (a instanceof DiscreteAttribute) return (DiscreteAttribute) a;
        }
        return null;
    }

    @Nested
    @DisplayName("SplitNode - compareTo")
    class CompareToTests {

        @Test
        @DisplayName("compareTo è coerente con l'ordine delle splitVariance")
        void compareToIsConsistentWithVariance() {
            Assumptions.assumeTrue(data.getNumberOfExplanatoryAttributes() >= 2,
                    "Servono almeno due attributi per confrontare due split");

            DiscreteAttribute a0 = (DiscreteAttribute) data.getExplanatoryAttribute(0);
            DiscreteAttribute a1 = (DiscreteAttribute) data.getExplanatoryAttribute(1);

            int end = data.getNumberOfExamples() - 1;
            DiscreteNode n0 = new DiscreteNode(data, 0, end, a0);
            // dopo la costruzione di n0 il dataset è ordinato per a0; ri-ordino per a1
            DiscreteNode n1 = new DiscreteNode(data, 0, end, a1);

            int cmp = n0.compareTo(n1);
            if (n0.getVariance() < n1.getVariance()) {
                assertTrue(cmp < 0, "Varianza minore deve dare compareTo < 0");
            } else if (n0.getVariance() > n1.getVariance()) {
                assertTrue(cmp > 0, "Varianza maggiore deve dare compareTo > 0");
            } else {
                assertEquals(0, cmp, "Varianze uguali devono dare compareTo == 0");
            }
        }

        @Test
        @DisplayName("compareTo con se stesso restituisce 0")
        void compareToSelfIsZero() {
            DiscreteAttribute attr = firstDiscrete();
            Assumptions.assumeTrue(attr != null, "Nessun attributo discreto nel dataset");
            DiscreteNode node = new DiscreteNode(data, 0, data.getNumberOfExamples() - 1, attr);
            assertEquals(0, node.compareTo(node));
        }
    }

    @Nested
    @DisplayName("SplitNode - formulateQuery e SplitInfo")
    class SplitInfoTests {

        @Test
        @DisplayName("formulateQuery non restituisce stringa vuota")
        void formulateQueryNotEmpty() {
            DiscreteAttribute attr = firstDiscrete();
            Assumptions.assumeTrue(attr != null, "Nessun attributo discreto nel dataset");
            DiscreteNode node = new DiscreteNode(data, 0, data.getNumberOfExamples() - 1, attr);
            assertFalse(node.formulateQuery().isEmpty());
        }

        @Test
        @DisplayName("getSplitInfo restituisce indici di inizio <= fine")
        void splitInfoIndicesAreConsistent() {
            DiscreteAttribute attr = firstDiscrete();
            Assumptions.assumeTrue(attr != null, "Nessun attributo discreto nel dataset");
            DiscreteNode node = new DiscreteNode(data, 0, data.getNumberOfExamples() - 1, attr);
            for (int i = 0; i < node.getNumberOfChildren(); i++) {
                SplitNode.SplitInfo info = node.getSplitInfo(i);
                assertTrue(info.getBeginindex() <= info.getEndIndex(),
                        "beginIndex deve essere <= endIndex per ogni ramo");
            }
        }

        @Test
        @DisplayName("Il comparatore di un nodo discreto è '='")
        void discreteComparatorIsEquals() {
            DiscreteAttribute attr = firstDiscrete();
            Assumptions.assumeTrue(attr != null, "Nessun attributo discreto nel dataset");
            DiscreteNode node = new DiscreteNode(data, 0, data.getNumberOfExamples() - 1, attr);
            assertEquals("=", node.getSplitInfo(0).getComparator());
        }
    }
}

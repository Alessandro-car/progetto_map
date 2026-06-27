package tree;

import data.Data;
import data.TrainingDataException;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.Assumptions;

import java.sql.SQLException;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test di integrazione per la classe {@link LeafNode}.
 * <p>
 * Richiede il database con la tabella <b>servo</b>. I test che usano il DB
 * vengono skippati se non è disponibile.
 */
@DisplayName("Test di LeafNode")
class LeafNodeTest {

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

    @Test
    @DisplayName("getNumberOfChildren restituisce sempre 0 per una foglia")
    void getNumberOfChildrenIsZero() {
        LeafNode leaf = new LeafNode(data, 0, data.getNumberOfExamples() - 1);
        assertEquals(0, leaf.getNumberOfChildren());
    }

    @Test
    @DisplayName("getPredictedClassValue non è null")
    void predictedClassValueNotNull() {
        LeafNode leaf = new LeafNode(data, 0, data.getNumberOfExamples() - 1);
        assertNotNull(leaf.getPredictedClassValue());
    }

    @Test
    @DisplayName("Il valore predetto è la media dei valori di classe nell'intervallo")
    void predictedClassValueIsMean() {
        int begin = 0;
        int end = data.getNumberOfExamples() - 1;
        double sum = 0;
        for (int i = begin; i <= end; i++) {
            sum += data.getClassValue(i);
        }
        double expectedMean = sum / (end - begin + 1);

        LeafNode leaf = new LeafNode(data, begin, end);
        assertEquals(expectedMean, leaf.getPredictedClassValue(), 1e-9,
                "Il valore predetto deve coincidere con la media dei valori target");
    }

    @Test
    @DisplayName("Su un solo esempio il valore predetto è il valore di quell'esempio")
    void predictedClassValueSingleExample() {
        LeafNode leaf = new LeafNode(data, 0, 0);
        assertEquals(data.getClassValue(0), leaf.getPredictedClassValue(), 1e-9);
    }

    @Test
    @DisplayName("toString contiene il prefisso LEAF")
    void toStringContainsLeaf() {
        LeafNode leaf = new LeafNode(data, 0, data.getNumberOfExamples() - 1);
        assertTrue(leaf.toString().contains("LEAF"),
                "toString di una foglia deve contenere 'LEAF'");
    }

    @Test
    @DisplayName("La varianza di una foglia su un solo esempio è 0")
    void varianceSingleExampleIsZero() {
        LeafNode leaf = new LeafNode(data, 0, 0);
        assertEquals(0.0, leaf.getVariance(), 1e-9,
                "Con un solo esempio la varianza deve essere 0");
    }
}

package tree;

import data.Data;
import data.TrainingDataException;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.Assumptions;

import java.sql.SQLException;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test di integrazione per la classe astratta {@link Node}.
 * <p>
 * Essendo {@code Node} astratta, viene testata tramite la sottoclasse concreta
 * {@link LeafNode}, sui membri ereditati (varianza, toString, intervallo esempi).
 * Richiede il database con la tabella <b>servo</b>.
 */
@DisplayName("Test di Node")
class NodeTest {

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
    @DisplayName("getVariance restituisce un valore non negativo")
    void varianceIsNonNegative() {
        Node node = new LeafNode(data, 0, data.getNumberOfExamples() - 1);
        assertTrue(node.getVariance() >= 0.0,
                "La varianza (SSE) non può essere negativa");
    }

    @Test
    @DisplayName("La varianza sull'intero dataset è maggiore di zero (dati non costanti)")
    void varianceFullDatasetIsPositive() {
        Node node = new LeafNode(data, 0, data.getNumberOfExamples() - 1);
        // Su servo i valori target non sono tutti uguali, quindi SSE > 0
        assertTrue(node.getVariance() > 0.0,
                "Con valori target non costanti la varianza deve essere > 0");
    }

    @Test
    @DisplayName("toString contiene l'intervallo di esempi e la varianza")
    void toStringContainsExamplesAndVariance() {
        Node node = new LeafNode(data, 0, data.getNumberOfExamples() - 1);
        String s = node.toString();
        assertTrue(s.contains("Examples:"), "toString deve indicare l'intervallo di esempi");
        assertTrue(s.contains("variance:"), "toString deve indicare la varianza");
    }

    @Test
    @DisplayName("Nodi diversi hanno identificativi diversi (idNode incrementale)")
    void differentNodesHaveDifferentToStringRanges() {
        Node n1 = new LeafNode(data, 0, 0);
        Node n2 = new LeafNode(data, 1, 1);
        // Indirettamente: due nodi su intervalli diversi producono toString diversi
        assertNotEquals(n1.toString(), n2.toString());
    }
}

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
 * Test di integrazione per la classe {@link DiscreteNode}.
 * <p>
 * Richiede il database con la tabella <b>servo</b>, che contiene attributi
 * discreti (motor, screw, pgain, vgain).
 */
@DisplayName("Test di DiscreteNode")
class DiscreteNodeTest {

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

    /** Cerca il primo attributo discreto del dataset. */
    private DiscreteAttribute firstDiscrete() {
        for (int i = 0; i < data.getNumberOfExplanatoryAttributes(); i++) {
            Attribute a = data.getExplanatoryAttribute(i);
            if (a instanceof DiscreteAttribute) return (DiscreteAttribute) a;
        }
        return null;
    }

    @Test
    @DisplayName("La costruzione su attributo discreto non lancia eccezioni")
    void constructionDoesNotThrow() {
        DiscreteAttribute attr = firstDiscrete();
        Assumptions.assumeTrue(attr != null, "Nessun attributo discreto nel dataset");
        // Il costruttore di SplitNode ordina internamente per l'attributo
        assertDoesNotThrow(() ->
                new DiscreteNode(data, 0, data.getNumberOfExamples() - 1, attr));
    }

    @Test
    @DisplayName("getNumberOfChildren è almeno 1")
    void numberOfChildrenAtLeastOne() {
        DiscreteAttribute attr = firstDiscrete();
        Assumptions.assumeTrue(attr != null, "Nessun attributo discreto nel dataset");
        DiscreteNode node = new DiscreteNode(data, 0, data.getNumberOfExamples() - 1, attr);
        assertTrue(node.getNumberOfChildren() >= 1,
                "Un nodo di split discreto deve avere almeno un ramo");
    }

    @Test
    @DisplayName("getAttribute restituisce l'attributo passato al costruttore")
    void getAttributeReturnsConstructorAttribute() {
        DiscreteAttribute attr = firstDiscrete();
        Assumptions.assumeTrue(attr != null, "Nessun attributo discreto nel dataset");
        DiscreteNode node = new DiscreteNode(data, 0, data.getNumberOfExamples() - 1, attr);
        assertSame(attr, node.getAttribute());
    }

    @Test
    @DisplayName("testCondition individua il ramo corretto per un valore di split noto")
    void testConditionMatchesKnownSplitValue() {
        DiscreteAttribute attr = firstDiscrete();
        Assumptions.assumeTrue(attr != null, "Nessun attributo discreto nel dataset");
        DiscreteNode node = new DiscreteNode(data, 0, data.getNumberOfExamples() - 1, attr);

        // Il valore di split del primo ramo deve essere riconosciuto e mappato al ramo 0
        Object splitValue = node.getSplitInfo(0).getSplitValue();
        assertEquals(0, node.testCondition(splitValue),
                "testCondition deve restituire l'indice del ramo che contiene il valore");
    }

    @Test
    @DisplayName("toString contiene il prefisso DISCRETE SPLIT")
    void toStringContainsDiscreteSplit() {
        DiscreteAttribute attr = firstDiscrete();
        Assumptions.assumeTrue(attr != null, "Nessun attributo discreto nel dataset");
        DiscreteNode node = new DiscreteNode(data, 0, data.getNumberOfExamples() - 1, attr);
        assertTrue(node.toString().contains("DISCRETE SPLIT"),
                "toString di un DiscreteNode deve contenere 'DISCRETE SPLIT'");
    }

    @Test
    @DisplayName("getVariance (splitVariance) è non negativa")
    void splitVarianceNonNegative() {
        DiscreteAttribute attr = firstDiscrete();
        Assumptions.assumeTrue(attr != null, "Nessun attributo discreto nel dataset");
        DiscreteNode node = new DiscreteNode(data, 0, data.getNumberOfExamples() - 1, attr);
        assertTrue(node.getVariance() >= 0.0);
    }
}

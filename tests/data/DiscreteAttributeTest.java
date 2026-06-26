package data;

import org.junit.jupiter.api.*;
import java.util.Iterator;
import java.util.Set;
import java.util.TreeSet;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Test di DiscreteAttribute")
class DiscreteAttributeTest {

    private Set<String> values;
    private DiscreteAttribute attr;

    @BeforeEach
    void setUp() {
        values = new TreeSet<>(Set.of("soleggiato", "nuvoloso", "piovoso"));
        attr = new DiscreteAttribute("outlook", 0, values);
    }

    @Test
    @DisplayName("getNumberOfDistinctValues restituisce il numero corretto di valori")
    void getNumberOfDistinctValuesMatchesInputSize() {
        assertEquals(values.size(), attr.getNumberOfDistinctValues());
    }

    @Test
    @DisplayName("getNumberOfDistinctValues è zero per insieme vuoto")
    void getNumberOfDistinctValuesIsZeroForEmptySet() {
        DiscreteAttribute empty = new DiscreteAttribute("x", 0, new TreeSet<>());
        assertEquals(0, empty.getNumberOfDistinctValues());
    }

    @Test
    @DisplayName("iterator restituisce i valori in ordine lessicografico crescente")
    void iteratorReturnsSortedValues() {
        Iterator<String> it = attr.iterator();
        String prev = it.next();
        while (it.hasNext()) {
            String curr = it.next();
            assertTrue(prev.compareTo(curr) <= 0,
                "Ordine violato: '" + prev + "' viene prima di '" + curr + "'");
            prev = curr;
        }
    }

    @Test
    @DisplayName("iterator copre esattamente tutti i valori dell'insieme")
    void iteratorCoversAllValues() {
        int count = 0;
        for (String v : attr) count++;
        assertEquals(values.size(), count);
    }
}
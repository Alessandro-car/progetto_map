package database;

import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;

import java.util.ArrayList;
import java.util.List;

@DisplayName("Example tests")
class ExampleTest {

    private Example exampleOf(Object... values) {
        Example e = new Example();
        for (Object v : values) e.add(v);
        return e;
    }

    @Test
    @DisplayName("get returns values in insertion order")
    void getReturnsValuesInOrder() {
        Example e = exampleOf("sunny", 30.0);

        assertEquals("sunny", e.get(0));
        assertEquals(30.0, e.get(1));
    }

    @Test
    @DisplayName("iterator is not null and traverses all values")
    void iteratorTraversesAllValues() {
        Example e = exampleOf("a", "b", "c");

        assertNotNull(e.iterator());

        List<Object> collected = new ArrayList<>();
        for (Object o : e) collected.add(o);

        assertEquals(List.of("a", "b", "c"), collected);
    }

    @Test
    @DisplayName("compareTo returns 0 for identical examples")
    void compareToIdenticalReturnsZero() {
        Example a = exampleOf("sunny", 1.0);
        Example b = exampleOf("sunny", 1.0);

        assertEquals(0, a.compareTo(b));
    }

    @Test
    @DisplayName("compareTo has the correct sign on the first differing value")
    void compareToHasCorrectSign() {
        Example smaller = exampleOf("apple", 1.0);
        Example larger  = exampleOf("banana", 1.0);

        assertTrue(smaller.compareTo(larger) > 0);
        assertTrue(larger.compareTo(smaller) < 0);
    }

    @Test
    @DisplayName("compareTo compares at the first differing value, ignoring later ones")
    void compareToStopsAtFirstDifference() {
        Example a = exampleOf("a", 999.0);
        Example b = exampleOf("b", 0.0);

        assertTrue(a.compareTo(b) > 0);
    }
}

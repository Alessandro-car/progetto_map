package application;

import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Test di Controller.validate")
class ControllerTest {

    // testato solo il metodo validate. 9 test totali

    @Test
    @DisplayName("IP tipico valido")
    void validTypicalIp() {
        assertTrue(Controller.validate("192.168.1.1"));
    }

    @Test
    @DisplayName("IP minimo valido (tutti zeri)")
    void validAllZeros() {
        assertTrue(Controller.validate("0.0.0.0"));
    }

    @Test
    @DisplayName("IP massimo valido (tutti 255)")
    void validAllMax() {
        assertTrue(Controller.validate("255.255.255.255"));
    }

    @Test
    @DisplayName("Localhost valido")
    void validLocalhost() {
        assertTrue(Controller.validate("127.0.0.1"));
    }

    // --- IP non validi ---

    @Test
    @DisplayName("Stringa vuota non è un IP valido")
    void invalidEmptyString() {
        assertFalse(Controller.validate(""));
    }

    @Test
    @DisplayName("Ottetto fuori range (256) non è valido")
    void invalidOctetOutOfRange() {
        assertFalse(Controller.validate("256.0.0.1"));
    }

    @Test
    @DisplayName("IP incompleto (solo 3 ottetti) non è valido")
    void invalidIncompleteIp() {
        assertFalse(Controller.validate("192.168.1"));
    }

    @Test
    @DisplayName("IP con 5 ottetti non è valido")
    void invalidTooManyOctets() {
        assertFalse(Controller.validate("192.168.1.1.1"));
    }

    @Test
    @DisplayName("IP con lettere non è valido")
    void invalidWithLetters() {
        assertFalse(Controller.validate("abc.def.ghi.jkl"));
    }

    @Test
    @DisplayName("IP con punto finale non è valido")
    void invalidTrailingDot() {
        assertFalse(Controller.validate("192.168.1."));
    }

    @Test
    @DisplayName("IP con punto iniziale non è valido")
    void invalidLeadingDot() {
        assertFalse(Controller.validate(".192.168.1.1"));
    }
}
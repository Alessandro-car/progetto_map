package database;

import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;

import database.Column;

@DisplayName("Test di Column")
class ColumnTest {
	@Test
	@DisplayName("Test column name")
	void testGetColumnName() {
		Column col = new Column("test", "test");
		assertEquals("test", col.getColumnName());
	}

	@Test
	@DisplayName("Test column type is a number")
	void testIsNumber() {
		Column col = new Column("test", "number");
		assertEquals(true, col.isNumber());
	}

	@Test
	@DisplayName("Test toString")
	void testToString() {
		Column col = new Column("test", "test");
		assertEquals("test:test", col.toString());
	}

}

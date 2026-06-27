package database;

import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;

import java.sql.SQLException;
import database.*;

@DisplayName("Test di TableSchema")
class TableSchemaTest {
	private DbAccess db;
	private TableSchema table;

	@BeforeEach
	void init() throws Exception {
		db = new DbAccess();
		db.initConnection();
	}

	@AfterEach
	void close() throws Exception {
		if (db != null) {
			try {
				db.closeConnection();
			} catch (Exception ignored) {}
		}
	}


	@Test
	@DisplayName("Create object with wrong table name")
	void createObjWithWrongTableName() throws Exception {
		table = new TableSchema(db, "doesntexist");
		assertEquals(0, table.getNumberOfAttributes());
	}

	@Test
	@DisplayName("Create object with right table name")
	void createObjWithRightTableName() throws Exception {
		table = new TableSchema(db, "servo");
		assertTrue(table.getNumberOfAttributes() > 0, "An existing table must have at least 1 column");
	}

	@Test
	@DisplayName("Last column of a table must be numeric")
	void testLastColumnOfExistingTable() throws Exception {
		table = new TableSchema(db, "servo");
		Column last = table.getColumn(table.getNumberOfAttributes() - 1);
		assertTrue(last.isNumber(), "The last column must be numeric");
	}

	@Test
	@DisplayName("Test column iterator")
	void testColumnIterator() throws Exception {
		table = new TableSchema(db, "servo");
		int num = 0;
		for (Column c : table) {
			assertNotNull(c.getColumnName());
			num += 1;
		}
		assertEquals(table.getNumberOfAttributes(), num);
	}
}

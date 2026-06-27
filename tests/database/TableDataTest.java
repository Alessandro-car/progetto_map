package database;

import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;

import database.*;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;
import java.util.Set;

@DisplayName("Test di TableData")
class TableDataTest {
	private DbAccess db;
	private TableData data;
	private String empty_table = "test_empty_table";
	@BeforeEach
	void init() throws Exception {
		db = new DbAccess();
		db.initConnection();
		try (Statement st = db.getConnection().createStatement()) {
    	st.execute("DROP TABLE IF EXISTS " + empty_table);
      st.execute("CREATE TABLE " + empty_table + " (outlook VARCHAR(10), temperature DOUBLE)");
    }
		data = new TableData(db);
	}

	@AfterEach
	void close() throws Exception {
		if (db != null) {
  		try (Statement st = db.getConnection().createStatement()) {
      	st.execute("DROP TABLE IF EXISTS " + empty_table);
      } catch (Exception ignored) {}
      try { db.closeConnection(); } catch (Exception ignored) {}
    }
	}


	@Test
	@DisplayName("Get transaction from wrong table name")
	void getTransactionsFromWrongTable() throws Exception {
		try {
			List<Example> examples = data.getTransazioni("doesntexists");
			fail("SQLException expected");
		} catch (SQLException e) {}
	}

	@Test
	@DisplayName("Get transaction from empty table")
	void getTransactionsFromEmptyTable() throws Exception {
		try {
			List<Example> examples = data.getTransazioni(empty_table);
			fail("EmptySetException expected");
		} catch (EmptySetException e) {}
	}

	@Test
	@DisplayName("Get transactions from existing and not empty table")
	void getTransactionFromExistingNonEmptyTable() throws Exception {
		try {
			List<Example> examples = data.getTransazioni("servo");
			assertTrue(examples.size() > 0, "Existing and non empty table must have at least 1 transaction.");
		} catch (SQLException | EmptySetException e) {
			fail("No exceptions expected");
		}
	}

	@Test
	@DisplayName("Get column value from wrong table")
	void getColumnValuesFromWrongTable() throws Exception {
		try {
			TableSchema schema = new TableSchema(db, "doesntexists");
			Set<Object> columns = data.getDistinctColumnValues("doesntexists", schema.getColumn(0));
			fail("Exception expected");
		} catch (Exception ignored) {}
	}

	@Test
	@DisplayName("Get column values from right table")
	void getColumnValuesFromRightTable() throws Exception {
		try {
			TableSchema table = new TableSchema(db, "servo");
			Set<Object> columns = data.getDistinctColumnValues("servo", table.getColumn(0));
			assertNotNull(columns);
		} catch (Exception e) {
			fail("No exception expected");
		}
	}

}

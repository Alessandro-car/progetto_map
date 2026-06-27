package database;

import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Assumptions;

import database.DatabaseConnectionException;
import database.DbAccess;

import java.sql.*;
import java.util.ArrayList;

@DisplayName("Test di DbAccess")
class DbAccessTest {
	private final String DRIVER_CLASS_NAME = "com.mysql.cj.jdbc.Driver";
	private final String DBMS = "jdbc:mysql";
	private final int PORT = 3306;
	private Connection conn;
	private DbAccess db;

	@BeforeEach
	void init() throws Exception {
		db = new DbAccess();
		conn = null;
	}

	@Test
	@DisplayName("Test init connection with wrong credentials")
	void testInitConnectionWithWrongCredentials() {
		String server = "localhost";
		String db = "MapDB";
		String user_id = "prova";
		String password = "prova";
		try {
			Class.forName(DRIVER_CLASS_NAME);
				conn = DriverManager.getConnection(
					DBMS + "://" + server + ":" + PORT + "/" + db
					+ "?useSSL=false&serverTimezone=UTC",
					user_id,
					password
				);
			fail("Expected a SQLException due to wrong credentials, but connection succeeded.");
		} catch (SQLException e) {

		} catch (ClassNotFoundException e) {
			fail("Driver class not found. Check your dependencies");
		}
	}

	@Test
	@DisplayName("Test init connection with right credentials")
	void testInitConnectionWithRightCredentials() {
		String server = "localhost";
		String db = "MapDB";
		String user_id = "MapUser";
		String password = "map";
		try {
			Class.forName(DRIVER_CLASS_NAME);
				conn = DriverManager.getConnection(
					DBMS + "://" + server + ":" + PORT + "/" + db
					+ "?useSSL=false&serverTimezone=UTC",
					user_id,
					password
				);
		} catch (SQLException | ClassNotFoundException e) {
			fail("No exceptions expected");
		}
	}

	@Test
	@DisplayName("Test get connection")
	void testGetConnection() throws Exception {
		try {
			db.initConnection();
			assertInstanceOf(Connection.class, db.getConnection());
		} catch (DatabaseConnectionException e) {
			fail("No exceptions expected");
		}
	}

	@Test
	@DisplayName("Test closing connection when is null")
	void testCloseConnectionWhenNull() throws Exception {
		assertDoesNotThrow(() -> {
        db.closeConnection();
    }, "Expected closeConnection to handle a null connection without throwing an exception");
	}

	@Test
	@DisplayName("Test closing connection when is not null")
	void testCloseConnectionWhenIsNotNull() throws Exception {
		try {
			DbAccess access = new DbAccess();
			access.initConnection();
			Connection internal = access.getConnection();
			assertNotNull(internal, "initConnection dovrebbe aver creato la connessione");
			assertFalse(internal.isClosed(), "la connessione dovrebbe essere aperta dopo initConnection");

    	access.closeConnection();
			assertTrue(internal.isClosed(), "Connection should be closed.");
		} catch (DatabaseConnectionException | SQLException e) {
			fail("No exception expected");
		}
	}

	@Test
	@DisplayName("Test get list of tables")
	void testGetListOfTables() throws Exception {
		try {
			db.initConnection();
			assertNotNull(db.getListOfTables(), "The returned list of tables should not be null");
		} catch (SQLException e) {
			fail("No exceptions expected");
		}
	}
}

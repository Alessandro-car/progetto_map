package database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DbAccess {

    private final String DRIVER_CLASS_NAME = "com.mysql.cj.jdbc.Driver";
    private final String DBMS = "jdbc:mysql";
    private String SERVER = "localhost";
    private String DATABASE = "MapDB";
    private final int PORT = 3306;
    private String USER_ID = "MapUser";
    private String PASSWORD = "map";
    private Connection conn;

    public void initConnection() throws DatabaseConnectionException {
        try {
            Class.forName(DRIVER_CLASS_NAME);
            conn = DriverManager.getConnection(
                DBMS + "://" + SERVER + ":" + PORT + "/" + DATABASE 
                + "?useSSL=false&serverTimezone=UTC",
                USER_ID,
                PASSWORD
            );
        } catch (ClassNotFoundException | SQLException e) {
            throw new DatabaseConnectionException(
                "Il collegamento al database e' fallito: " + e.getMessage(), e
            );
        }
    }

    public Connection getConnection() {
        return conn;
    }

    public void closeConnection() throws SQLException {
      conn.close(); 
    }
}
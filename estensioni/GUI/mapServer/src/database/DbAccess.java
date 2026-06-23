package database;

import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.ArrayList;

/**
 * Gestisce la connessione al database MySQL.
 * Fornisce i metodi per inizializzare, ottenere e chiudere la connessione.
 */
public class DbAccess {

    /** Nome della classe del driver JDBC per MySQL. */
    private final String DRIVER_CLASS_NAME = "com.mysql.cj.jdbc.Driver";

    /** Protocollo JDBC per MySQL. */
    private final String DBMS = "jdbc:mysql";

    /** Indirizzo del server del database. */
    private String SERVER = "localhost";

    /** Nome del database a cui connettersi. */
    private String DATABASE = "MapDB";

    /** Porta del server del database. */
    private final int PORT = 3306;

    /** Nome utente per l'accesso al database. */
    private String USER_ID = "MapUser";

    /** Password per l'accesso al database. */
    private String PASSWORD = "map";

    /** Oggetto che rappresenta la connessione al database. */
    private Connection conn;

    /**
     * Inizializza la connessione al database.
     * Carica il driver JDBC e stabilisce la connessione tramite {@link DriverManager}.
     *
     * @throws DatabaseConnectionException se il driver non viene trovato
     *         o se la connessione al database fallisce
     */
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

    /**
     * Restituisce l'oggetto {@link Connection} per interagire con il database.
     * Deve essere chiamato solo dopo {@link #initConnection()}.
     *
     * @return la connessione attiva al database
     */
    public Connection getConnection() {
        return conn;
    }

    /**
     * Chiude la connessione al database.
     *
     * @throws SQLException se si verifica un errore durante la chiusura
     *         della connessione
     */
    public void closeConnection() throws SQLException {
        conn.close();
        
    }
    /**
     
    Restituisce l'elenco dei nomi di tutte le tabelle presenti nel database.
    <p>
    Utilizza i metadati della connessione ({@link DatabaseMetaData}) per
    recuperare le tabelle di tipo {@code "TABLE"}.*
    @return una lista con i nomi delle tabelle del database
    @throws SQLException se si verifica un errore durante l'accesso ai metadati*/
    public ArrayList<String> getListOfTables() throws SQLException {
        DatabaseMetaData metaData = this.getConnection().getMetaData();
        String[] tableTypes = {"TABLE"};
        ArrayList<String> tables = new ArrayList<String>();
        try (ResultSet resultSet = metaData.getTables(null, null, "%", tableTypes)) {
            while (resultSet.next()) {
                tables.add(resultSet.getString("TABLE_NAME"));}} catch (SQLException e) {
            throw new SQLException(e);}

            return tables;
        }
}
package database;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;


/**
 * La classe {@code TableData} fornisce metodi per l'accesso e il recupero
 * dei dati contenuti nelle tabelle di un database relazionale.
 * Utilizza un'istanza di {@link DbAccess} per stabilire la connessione
 * al database e interagire con esso tramite query SQL.
 */

public class TableData {

	    /** Oggetto che gestisce la connessione al database. */
	private DbAccess db;
	

/**
* Costruisce un'istanza di {@code TableData} associata alla connessione
* al database fornita.
*
* @param db l'oggetto {@link DbAccess} che rappresenta la connessione al database
*/
	public TableData(DbAccess db) {
		this.db=db;
	}
/**
* Recupera tutte le righe (transazioni) presenti nella tabella specificata
* e le restituisce come lista di oggetti {@link Example}.
* Ogni {@link Example} rappresenta una singola riga della tabella,
* i cui valori sono memorizzati nell'ordine delle colonne definito dallo schema.
* I valori numerici vengono memorizzati come {@code Double},
* mentre i valori non numerici come {@code String}.
*
* @param table il nome della tabella del database da cui leggere i dati
* @return una {@link List} di {@link Example}, ciascuno rappresentante una riga della tabella
* @throws SQLException      se si verifica un errore di accesso al database
*                           o se la tabella non contiene attributi
* @throws EmptySetException se la tabella esiste ma non contiene alcuna riga
*/	
	public List<Example> getTransazioni(String table) throws SQLException, EmptySetException{
		LinkedList<Example> transSet = new LinkedList<Example>();
		Statement statement;
		TableSchema tSchema=new TableSchema(db,table);
		
		
		String query="select ";
		
		for(int i=0;i<tSchema.getNumberOfAttributes();i++){
			Column c=tSchema.getColumn(i);
			if(i>0)
				query+=",";
			query += c.getColumnName();
		}
		if(tSchema.getNumberOfAttributes()==0)
			throw new SQLException();
		query += (" FROM "+table);
		
		statement = db.getConnection().createStatement();
		ResultSet rs = statement.executeQuery(query);
		boolean empty=true;
		while (rs.next()) {
			empty=false;
			Example currentTuple=new Example();
			for(int i=0;i<tSchema.getNumberOfAttributes();i++)
				if(tSchema.getColumn(i).isNumber())
					currentTuple.add(rs.getDouble(i+1));
				else
					currentTuple.add(rs.getString(i+1));
			transSet.add(currentTuple);
		}
		rs.close();
		statement.close();
		if(empty) throw new EmptySetException();
		
		
		return transSet;

	}

	    /**
     * Recupera l'insieme dei valori distinti presenti in una specifica colonna
     * della tabella indicata, ordinati in modo crescente.
     * I valori numerici vengono restituiti come {@code Double},
     * mentre i valori non numerici come {@code String}.
     *
     * @param table  il nome della tabella del database
     * @param column l'oggetto {@link Column} che rappresenta la colonna
     *               di cui si vogliono ottenere i valori distinti
     * @return un {@link Set} di {@link Object} contenente i valori distinti
     *         della colonna, ordinati in modo crescente
     * @throws SQLException se si verifica un errore di accesso al database
     */
	public Set<Object> getDistinctColumnValues(String table, Column column) throws SQLException {
    
    	Set<Object> valSet = new TreeSet<Object>();
    	Statement statement;
    
    	String query = "SELECT DISTINCT " + column.getColumnName() 
        	         + " FROM " + table 
            	     + " ORDER BY " + column.getColumnName() + " ASC";
    
   		statement = db.getConnection().createStatement();
    	ResultSet rs = statement.executeQuery(query);
    
    	while (rs.next()) {
        	if (column.isNumber())
            	valSet.add(rs.getDouble(1));
        	else
            	valSet.add(rs.getString(1));
    	}
    
    	rs.close();
    	statement.close();
    
    	return valSet;
}	

	

}

package database;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;



/**
 * Rappresenta lo schema di una tabella del database, ovvero l'insieme delle sue
 * colonne con i relativi tipi.
 * <p>
 * Al momento della costruzione recupera automaticamente i metadati della tabella
 * tramite la connessione fornita, traducendo i tipi SQL nei tipi semplificati
 * {@code "string"} e {@code "number"}. Implementa {@link Iterable} per consentire
 * di scorrere le colonne con il costrutto for-each.
 */
public class TableSchema implements Iterable<Column>{

	/** Lista delle colonne che compongono lo schema della tabella. */
	private List<Column> tableSchema=new ArrayList<Column>();

	/**
	 * Costruisce lo schema recuperando dal database i metadati della tabella indicata.
	 * <p>
	 * I tipi SQL vengono tradotti nei tipi semplificati come segue:
	 * <ul>
	 *   <li>{@code "string"}: CHAR, VARCHAR, LONGVARCHAR, BIT;</li>
	 *   <li>{@code "number"}: SHORT, INT, LONG, FLOAT, DOUBLE.</li>
	 * </ul>
	 * Le colonne con tipi SQL non previsti vengono ignorate.
	 *
	 * @param db l'oggetto {@link DbAccess} che fornisce la connessione al database
	 * @param tableName il nome della tabella di cui ricavare lo schema
	 * @throws SQLException se si verifica un errore durante l'accesso ai metadati
	 */
	public TableSchema(DbAccess db, String tableName) throws SQLException{

		HashMap<String,String> mapSQL_JAVATypes=new HashMap<String, String>();

		mapSQL_JAVATypes.put("CHAR","string");
		mapSQL_JAVATypes.put("VARCHAR","string");
		mapSQL_JAVATypes.put("LONGVARCHAR","string");
		mapSQL_JAVATypes.put("BIT","string");
		mapSQL_JAVATypes.put("SHORT","number");
		mapSQL_JAVATypes.put("INT","number");
		mapSQL_JAVATypes.put("LONG","number");
		mapSQL_JAVATypes.put("FLOAT","number");
		mapSQL_JAVATypes.put("DOUBLE","number");



		 Connection con=db.getConnection();
		 DatabaseMetaData meta = con.getMetaData();
	     ResultSet res = meta.getColumns(null, null, tableName, null);

	     while (res.next()) {

	         if(mapSQL_JAVATypes.containsKey(res.getString("TYPE_NAME")))
	        		 tableSchema.add(new Column(
	        				 res.getString("COLUMN_NAME"),
	        				 mapSQL_JAVATypes.get(res.getString("TYPE_NAME")))
	        				 );


	      }
	      res.close();



	    }

	/**
	 * Restituisce il numero di colonne presenti nello schema.
	 *
	 * @return il numero di colonne dello schema
	 */
	public int getNumberOfAttributes(){
		return tableSchema.size();
	}

	/**
	 * Restituisce la colonna dello schema che si trova all'indice indicato.
	 *
	 * @param index l'indice (a partire da 0) della colonna da recuperare
	 * @return la {@link Column} corrispondente all'indice indicato
	 * @throws IndexOutOfBoundsException se l'indice è fuori dai limiti della lista
	 */
	public Column getColumn(int index){
		return tableSchema.get(index);
	}

	/**
	 * Restituisce un iteratore sulle colonne dello schema, consentendo l'uso del
	 * costrutto for-each.
	 *
	 * @return un {@link Iterator} sulle colonne dello schema
	 */
	@Override
	public Iterator<Column> iterator() {
		return tableSchema.iterator();
	}


}

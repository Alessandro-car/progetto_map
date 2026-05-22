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
 * La classe {@code TableSchema} rappresenta lo schema di una tabella
 * del database, ovvero l'insieme delle colonne che la compongono
 * con i relativi tipi.
 * <p>
 * All'atto della costruzione, recupera automaticamente i metadati
 * della tabella specificata tramite la connessione fornita,
 * mappando i tipi SQL nei tipi Java semplificati {@code "string"}
 * e {@code "number"}.
 * </p>
 * Implementa {@link Iterable} per consentire l'iterazione
 * sulle colonne dello schema.
 */
public class TableSchema implements Iterable<Column>{
	

    /**
     * Lista delle colonne che compongono lo schema della tabella.
     * Ogni elemento è un oggetto {@link Column} con nome e tipo della colonna.
     */
	
	private List<Column> tableSchema=new ArrayList<Column>();
	
    /**
     * Costruisce un'istanza di {@code TableSchema} recuperando dal database
     * i metadati della tabella specificata.
     * <p>
     * I tipi SQL vengono mappati nei seguenti tipi Java semplificati:
     * <ul>
     *   <li>{@code "string"}: CHAR, VARCHAR, LONGVARCHAR, BIT</li>
     *   <li>{@code "number"}: SHORT, INT, LONG, FLOAT, DOUBLE</li>
     * </ul>
     * Le colonne con tipi SQL non presenti nella mappatura vengono ignorate.
     * </p>
     *
     * @param db        l'oggetto {@link DbAccess} che fornisce la connessione al database
     * @param tableName il nome della tabella di cui si vuole ottenere lo schema
     * @throws SQLException se si verifica un errore durante l'accesso ai metadati del database
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
     * Restituisce il numero di attributi (colonne) presenti nello schema della tabella.
     *
     * @return il numero di colonne dello schema
     */

		public int getNumberOfAttributes(){
			return tableSchema.size();
		}
		    /**
     * Restituisce la colonna dello schema corrispondente all'indice specificato.
     *
     * @param index l'indice (a partire da 0) della colonna da recuperare
     * @return l'oggetto {@link Column} corrispondente all'indice dato
     * @throws IndexOutOfBoundsException se l'indice è fuori dai limiti della lista
     */

		public Column getColumn(int index){
			return tableSchema.get(index);
		}

    /**
     * Restituisce un iteratore sulle colonne dello schema della tabella,
     * consentendo l'uso del costrutto for-each su un'istanza di {@code TableSchema}.
     *
     * @return un {@link Iterator} di oggetti {@link Column}
     */
	
		@Override
		public Iterator<Column> iterator() {
			// TODO Auto-generated method stub
			return tableSchema.iterator();
		}

		
	}

		     



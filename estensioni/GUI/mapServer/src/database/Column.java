package database;

/**
 * Rappresenta una colonna di una tabella del database.
 * Ogni colonna ha un nome e un tipo.
 */
public class Column {

    /** Il nome della colonna. */
    private String name;

    /** Il tipo della colonna (es. "number", "string"). */
    private String type;

    /**
     * Costruisce una nuova colonna con il nome e il tipo specificati.
     *
     * @param name il nome della colonna
     * @param type il tipo della colonna (es. "number", "string")
     */
    Column(String name, String type) {
        this.name = name;
        this.type = type;
    }

    /**
     * Restituisce il nome della colonna.
     *
     * @return il nome della colonna
     */
    public String getColumnName() {
        return name;
    }

    /**
     * Verifica se la colonna è di tipo numerico.
     * Una colonna è considerata numerica se il suo tipo è uguale a "number".
     *
     * @return {@code true} se il tipo della colonna è "number", {@code false} altrimenti
     */
    public boolean isNumber() {
        return type.equals("number");
    }

    /**
     * Restituisce una rappresentazione testuale della colonna
     * nel formato {@code nome:tipo}.
     *
     * @return una stringa nel formato "nome:tipo"
     */
    @Override
    public String toString() {
        return name + ":" + type;
    }
}
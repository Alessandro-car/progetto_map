package database;

/**
 * Rappresenta una colonna di una tabella del database, caratterizzata da un nome
 * e da un tipo (semplificato in {@code "string"} o {@code "number"}).
 */
public class Column {

    /** Nome della colonna. */
    private String name;

    /** Tipo della colonna (ad esempio {@code "number"} o {@code "string"}). */
    private String type;

    /**
     * Costruisce una colonna con il nome e il tipo specificati.
     *
     * @param name nome della colonna
     * @param type tipo della colonna (ad esempio {@code "number"} o {@code "string"})
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
     * Indica se la colonna è di tipo numerico, cioè se il suo tipo è {@code "number"}.
     *
     * @return {@code true} se la colonna è numerica, {@code false} altrimenti
     */
    public boolean isNumber() {
        return type.equals("number");
    }

    /**
     * Restituisce una rappresentazione testuale della colonna nel formato
     * {@code nome:tipo}.
     *
     * @return la stringa nel formato {@code nome:tipo}
     */
    @Override
    public String toString() {
        return name + ":" + type;
    }
}

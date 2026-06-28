package database;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Rappresenta un singolo esempio (una riga della tabella) come lista ordinata
 * di valori.
 * <p>
 * Implementa {@link Comparable} per consentire il confronto tra esempi e
 * {@link Iterable} per consentire l'iterazione sui suoi valori.
 */
public class Example implements Comparable<Example>, Iterable<Object> {

    /** Lista dei valori che compongono l'esempio. */
    private List<Object> example = new ArrayList<Object>();

    /**
     * Costruisce un esempio vuoto, pronto per essere popolato con {@link #add(Object)}.
     */
    public Example() {}

    /**
     * Aggiunge un valore in coda all'esempio.
     *
     * @param o il valore da aggiungere
     */
    void add(Object o) {
        example.add(o);
    }

    /**
     * Restituisce il valore che si trova alla posizione indicata.
     *
     * @param i l'indice del valore da restituire
     * @return il valore alla posizione {@code i}
     */
    public Object get(int i) {
        return example.get(i);
    }

    /**
     * Confronta questo esempio con quello specificato, valore per valore.
     * <p>
     * Al primo valore diverso restituisce l'esito del confronto tra i due valori;
     * se tutti i valori sono uguali restituisce 0.
     *
     * @param ex l'esempio con cui effettuare il confronto
     * @return un valore negativo, zero o positivo se questo esempio è
     *         rispettivamente minore, uguale o maggiore di {@code ex}
     */
    public int compareTo(Example ex) {

        int i=0;
        for(Object o:ex.example){
            if(!o.equals(this.example.get(i)))
                return ((Comparable)o).compareTo(example.get(i));
            i++;
        }
        return 0;
    }

    /**
     * Restituisce una rappresentazione testuale dell'esempio, con i valori
     * separati da uno spazio.
     *
     * @return la stringa contenente tutti i valori dell'esempio
     */
    public String toString(){
        String str="";
        for(Object o:example)
            str+=o.toString()+ " ";
        return str;
    }

    /**
     * Restituisce un iteratore sui valori dell'esempio.
     *
     * @return un {@link Iterator} sui valori dell'esempio
     */
    @Override
    public Iterator<Object> iterator() {
        return example.iterator();
    }

}

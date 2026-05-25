package database;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Rappresenta un esempio come una lista ordinata di oggetti.
 * Implementa {@link Comparable} per consentire il confronto tra esempi
 * e {@link Iterable} per consentire l'iterazione sugli elementi.
 */
public class Example implements Comparable<Example>, Iterable<Object> {

    /** Lista degli oggetti che compongono l'esempio. */
    private List<Object> example = new ArrayList<Object>();

    /**
     * Aggiunge un oggetto alla lista dell'esempio.
     *
     * @param o l'oggetto da aggiungere
     */
    public void add(Object o) {
        example.add(o);
    }

    /**
     * Restituisce l'oggetto presente alla posizione specificata.
     *
     * @param i l'indice dell'oggetto da restituire
     * @return l'oggetto alla posizione {@code i}
     */
    public Object get(int i) {
        return example.get(i);
    }

    /**
     * Confronta questo esempio con quello specificato.
     * Il confronto avviene elemento per elemento: al primo elemento
     * diverso viene restituito il risultato del confronto tra i due elementi.
     * Restituisce 0 se tutti gli elementi sono uguali.
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
     * Restituisce una rappresentazione testuale dell'esempio,
     * con gli oggetti separati da uno spazio.
     *
     * @return una stringa contenente tutti gli oggetti dell'esempio
     */
    public String toString(){
        String str="";
        for(Object o:example)
            str+=o.toString()+ " ";
        return str;
    }
        /**
     * Restituisce un iteratore sugli elementi dell'esempio.
     * 
     * @return un {@link Iterator} sugli oggetti della lista
     */
    @Override
    public Iterator<Object> iterator() {
        // TODO Auto-generated method stub
        return null;
    }
    
}
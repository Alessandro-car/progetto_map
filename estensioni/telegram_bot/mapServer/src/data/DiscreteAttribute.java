package data;

import java.util.Iterator;
import java.util.Set;
import java.util.TreeSet;

/**
 * La classe {@code DiscreteAttribute} estende la classe {@link Attribute} e
 * rappresenta un attributo con valori discreti (cioè un insieme finito di valori,
 * ad esempio "soleggiato", "nuvoloso", "piovoso").
 * <p>
 * Implementa {@link Iterable} per consentire di scorrere i valori distinti
 * dell'attributo con il costrutto for-each.
 */
public class DiscreteAttribute extends Attribute implements Iterable<String> {

	/** Insieme dei valori distinti dell'attributo, ordinati in modo crescente. */
	private Set<String> values=new TreeSet<>();

	/**
	 * Costruttore di classe.
	 *
	 * @param name nome simbolico dell'attributo
	 * @param index identificativo numerico dell'attributo
	 * @param values insieme dei valori distinti che l'attributo può assumere
	 */
	DiscreteAttribute(String name, int index, Set<String> values) {
		super(name,index);
		this.values=values;
	}

	/**
	 * Restituisce il numero di valori distinti dell'attributo.
	 *
	 * @return la quantità di valori distinti
	 */
	public int getNumberOfDistinctValues(){
		return values.size();
	}

	/**
	 * Restituisce un iteratore sui valori distinti dell'attributo, in ordine crescente.
	 *
	 * @return un {@link Iterator} sui valori dell'attributo
	 */
	@Override
	public Iterator<String> iterator() {
		return values.iterator();
	}


}

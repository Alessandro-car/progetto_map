package data;

import java.io.Serializable;

/**
 * Modella un generico attributo di un dataset, sia esso continuo o discreto.
 * <p>
 * Ogni attributo è caratterizzato da un nome simbolico e da un indice numerico
 * che ne indica la posizione tra gli attributi. È una classe astratta: le sue
 * implementazioni concrete sono {@link ContinuousAttribute} e {@link DiscreteAttribute}.
 */
public abstract class Attribute implements Serializable {

	/** Nome simbolico dell'attributo. */
	private String name;

	/** Identificativo numerico (posizione) dell'attributo. */
	private int index;

	/**
	 * Costruisce un attributo con il nome e l'indice specificati.
	 *
	 * @param name nome simbolico dell'attributo
	 * @param index identificativo numerico dell'attributo
	 */
	Attribute(String name, int index) {
		this.name = name;
		this.index = index;
	}

	/**
	 * Restituisce il nome dell'attributo.
	 *
	 * @return il nome simbolico dell'attributo
	 */
	String getName() {
		return this.name;
	}

	/**
	 * Restituisce l'indice dell'attributo.
	 *
	 * @return l'identificativo numerico dell'attributo
	 */
	public int getIndex() {
		return this.index;
	}

	/**
	 * Restituisce una rappresentazione testuale dell'attributo, ovvero il suo nome.
	 *
	 * @return il nome dell'attributo
	 */
	public String toString() {
		return this.name;
	}
}

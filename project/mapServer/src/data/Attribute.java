package data;

import java.io.Serializable;

/**
 * La classe {@code Attribute} modella un generico attributo <b>continuo</b> o <b>discreto</b>
 */
public abstract class Attribute implements Serializable {
	/**
	* Nome simbolico dell'attributo
	*/
	private String name;

	/**
	* Identificativo numerico dell'attributo
	*/
	private int index;

	/**
	 * È il costruttore di classe.
	 * Inizializza i valori dei membri {@code name} e {@code index}
	 * @param name Nome simbolico dell'attributo
	 * @param index Identificativo numerico dell'attributo
	 */
	Attribute(String name, int index) {
		this.name = name;
		this.index = index;
	}

	/**
	 * Restituisce il valore del membro {@code name}
	 * @return Nome simbolico dell'attributo
	*/
	String getName() {
		return this.name;
	}

	/**
	 * Restituisce il valore del membro {@code index}
	 * @return Identificativo numerico dell'attributo
	*/
	public int getIndex() {
		return this.index;
	}

	/**
	 * @return Rappresentazione testaule completa di un generico attributo
	 **/
	public String toString() {
		return this.name;
	}
}

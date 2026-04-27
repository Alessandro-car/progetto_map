/**
 * La classe {@code Attribute} modella un generico attributo <b>continuo</b> o <b>discreto</b>
 */
abstract class Attribute {
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
	int getIndex() {
		return this.index;
	}
}

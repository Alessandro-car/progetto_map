package data;

/**
 * La classe {@code DiscreteAttribute} rappresenta un attributo i cui valori appartengono a un insieme finito di simboli.
 * <p>
 * La classe {@code DiscreteAttribute} estende la classe {@link Attribute}.
 * */

public class DiscreteAttribute extends Attribute {

	/**
	 * Array di oggetti, uno per ciascun valore che l'attributo può assumere
	 * */
	private String values[];

	/**
	 * Inizializza un nuovo attributo discreto con il nome, l'indice e l'insieme di valori possibili.
	 * @param name Nome simbolico dell'attributo.
	 * @param index Identificativo numerico dell'attributo.
	 * @param values Array di stringhe contenente i valori discreti.
	 * */
	DiscreteAttribute(String name, int index, String values[]) {
			super(name, index);
			this.values = values;
	}

	/**
	 * Restituisce il numero di valori distinti che l'attributo può assumere.
	 * @return La lunghezza dell'array {@code values}
	 * */
	int getNumberOfDistinctValues() {
			return values.length;
	}

	/**
	 * Restituisce il valore simbolico presente a un determinato indice nell'array dei valori.
	 * @param i Posizione del valore da recuperare.
	 * @return Il valore corrispondente all'indice specificato.
	 * */
	String getValue(int i) {
			return values[i];
	}
}

/**
 * La classe {@code ContinuousAttribute} estende la classe {@link Attribute}
 * <p>
 * Rappresenta un attributo con valori numerici continui.
 * A differenza degli attributi discreti, {@code ContinuousAttribute} gestisce infiniti range di valori.
 */
class ContinuousAttribute extends Attribute {
		/**
		 * È il costruttore di classe.
		 * @param name Nome simbolico dell'attributo
		 * @param index Identificativo numerico dell'attributo
		 */
    ContinuousAttribute(String name, int index){
        super(name, index);
    }
}

package data;

/**
 * Rappresenta un attributo con valori numerici continui.
 * <p>
 * A differenza degli attributi discreti, che hanno un insieme finito di valori,
 * un attributo continuo può assumere un numero illimitato di valori numerici.
 * Estende la classe {@link Attribute}.
 */
public class ContinuousAttribute extends Attribute {

    /**
     * Costruisce un attributo continuo con il nome e l'indice specificati.
     *
     * @param name nome simbolico dell'attributo
     * @param index identificativo numerico dell'attributo
     */
    ContinuousAttribute(String name, int index){
        super(name, index);
    }
}

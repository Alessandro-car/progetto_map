package tree;

import java.io.Serializable;

import data.Data;

/**
 * Modella un nodo foglia, cioè un nodo terminale dell'albero di regressione.
 * <p>
 * Una foglia non effettua ulteriori suddivisioni e contiene il valore predetto
 * per gli esempi che vi ricadono, ottenuto come media dei loro valori target.
 * Estende la classe {@link Node}.
 */
class LeafNode extends Node implements Serializable {

	/** Valore della classe predetto dalla foglia. */
	private Double predictedClassValue;

	/**
	 * Costruisce la foglia calcolando la media dei valori dell'attributo target
	 * sugli esempi dell'intervallo indicato.
	 *
	 * @param trainingSet il dataset di addestramento
	 * @param beginExampleIndex indice iniziale dell'intervallo di esempi
	 * @param endExampleIndex indice finale dell'intervallo di esempi
	 */
	LeafNode(Data trainingSet, int beginExampleIndex, int endExampleIndex) {
		super(trainingSet, beginExampleIndex, endExampleIndex);
		double sum = 0;
		for (int i = beginExampleIndex; i <= endExampleIndex; i++) {
			sum += trainingSet.getClassValue(i);
		}
		predictedClassValue = sum / (endExampleIndex - beginExampleIndex + 1);
	}

	/**
	 * Restituisce il valore predetto dalla foglia.
	 *
	 * @return il valore medio dell'attributo target nel nodo
	 */
	Double getPredictedClassValue() {
		return predictedClassValue;
	}

	/**
	 * Restituisce il numero di figli del nodo: trattandosi di una foglia è sempre 0.
	 *
	 * @return il numero di figli, costantemente 0
	 */
	int getNumberOfChildren() {
		return 0;
	}

	/**
	 * Restituisce una rappresentazione testuale della foglia, con il valore predetto
	 * e le informazioni ereditate dal nodo.
	 *
	 * @return la descrizione testuale della foglia
	 */
	public String toString() {
		return "LEAF : class=" + predictedClassValue + " " + super.toString();
	}
}

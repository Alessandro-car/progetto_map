/**
 * La classe {@code LeafNode} modella un nodo terminale dell'albero.
 * Una foglia non effettua ulteriori split e contiene il valore predetto per gli esempi che ricadono nel suo intervallo.
 * <p>
 * Estende la classe {@link Node}
 **/

class LeafNode extends Node {
		/** Valore della classe predetto dal nodo. */
    private Double predictedClassValue;

		/**
		 * Inizializza il nodo foglia calcolando la media dei valori dell'attributo target nell'intervallo di esempi fornito.
		 * @param trainingSet Il dataset di addestramento.
		 * @param beginExampleIndex Indice iniziale dell'intervallo di esempi.
		 * @param endExampleIndex Indice finale dell'intervallo di esempi.
		 * */
    LeafNode(Data trainingSet, int beginExampleIndex, int endExampleIndex) {
        super(trainingSet, beginExampleIndex, endExampleIndex);
        double sum = 0;
        for (int i = beginExampleIndex; i <= endExampleIndex; i++) {
            sum += trainingSet.getClassValue(i);
        }
        predictedClassValue = sum / (endExampleIndex - beginExampleIndex + 1);
    }

		/**
		 * Restituisce il valore predetto per la classe.
		 * @return Il valore medio dell'attributo target nel nodo.
		 */
    Double getPredictedClassValue() {
        return predictedClassValue;
    }

		/**
		 * Calcola il numero dei figli del nodo. Essendo un nodo foglia restituisce sempre 0.
		 * @return Il numero di figli, che è costantemente 0.
		 **/
    int getNumberOfChildren() {
        return 0;
    }

		/**
		 * Restituisce una rappresentazione testaule del nodo foglia, includendo il valore predetto e le informazioni ereditate dal nodo.
		 * @return Stringa descrittiva della foglia.
		 **/
    public String toString() {
        return "LEAF : class=" + predictedClassValue + " " + super.toString();
    }
}

/**
 * La classe astratta {@code Node} modella un nodo dell'albero di decisione.
 * Rappresenta un sottoinsieme del dataset individuato da un intervallo di indici.
 */
abstract class Node {
	/**
	 * Contatore statico per generare identificativi univoci per i nodi.
	 */
	private static int idNodeCount = 0;

	/**
	 * Identificativo univoco del nodo.
	 */
	private int idNode;

	/**
	 * Indice iniziale del sottoinsieme di dati.
	 */
	private int beginExampleIndex;

	/**
	 * Indice finale del sottoinsieme di dati.
	 */
	private int endExampleIndex;

	/**
	 * Valore della varianza calcolato rispetto all'attributo target nell'intervallo.
	 */
	private double variance;

	/**
	 * Inizializza un nodo e calcola la varianza dei dati nell'intervallo specificato.
	 * <p>
	 * La varianza è calcolata come:
	 * {@code sum(x^2) - (sum(x)^2 / n)}
	 *
	 * @param trainingSet Il dataset completo di addestramento.
	 * @param beginExampleIndex Indice iniziale dell'intervallo di esempi.
	 * @param endExampleIndex Indice finale dell'intervallo di esempi.
	 */
	Node(Data trainingSet, int beginExampleIndex, int endExampleIndex) {
		this.beginExampleIndex = beginExampleIndex;
		this.endExampleIndex = endExampleIndex;
		this.idNode = idNodeCount++;
		double sumClassVarSq = 0.0;
		double sumSqClassVar = 0.0;
		for (int i = this.beginExampleIndex; i <= this.endExampleIndex; i++) {
			sumClassVarSq += Math.pow(trainingSet.getClassValue(i), 2);
			sumSqClassVar += trainingSet.getClassValue(i);
		}
		variance = sumClassVarSq - (Math.pow(sumSqClassVar, 2) / (this.endExampleIndex -  this.beginExampleIndex + 1));
	}

	/**
	 * Restituisce l'identificativo univoco del nodo.
	 * @return ID del nodo.
	 */
	private int getIdNode() {
		return idNode;
	}

	/**
	 * Restituisce l'indice dell'esempio iniziale nel dataset.
	 * @return Indice iniziale.
	 */
	private int beginExampleIndex() {
		return beginExampleIndex;
	}

	/**
	 * Restituisce l'indice dell'esempio finale nel dataset.
	 * @return Indice finale.
	 */
	private int getEndExampleIndex() {
		return endExampleIndex;
	}

	/**
	 * Restituisce il valore della varianza del nodo.
	 * @return Varianza calcolata nel costruttore.
	 */
	double getVariance() {
		return variance;
	}

	/**
	 * Metodo astratto che deve essere implementato dalle sottoclassi per restituire il numero dei nodi figli.
	 * @return Numero di figli del nodo.
	 */
	abstract int getNumberOfChildren();

	/**
	 * Restituisce una rappresentazione testaule del nodo, includendo l'intervallo di esempi e la varianza.
	 * @return Stringa descrittiva del nodo.
	 */
	public String toString() {
		String u = "Nodo: [Examples:" + beginExampleIndex + "-" + endExampleIndex + "] variance:" + variance;
		return u;
	}
}

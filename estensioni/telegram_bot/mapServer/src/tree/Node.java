package tree;

import java.io.Serializable;

import data.Data;

/**
 * Modella un generico nodo dell'albero di regressione.
 * <p>
 * Ogni nodo rappresenta un sottoinsieme degli esempi, individuato da un intervallo
 * di indici, e ne conosce la varianza rispetto all'attributo target. È una classe
 * astratta: le implementazioni concrete sono {@link LeafNode} e le sottoclassi di
 * {@link SplitNode}.
 */
abstract class Node implements Serializable {

	/** Contatore statico usato per assegnare un identificativo univoco a ogni nodo. */
	private static int idNodeCount = 0;

	/** Identificativo univoco del nodo. */
	private int idNode;

	/** Indice iniziale del sottoinsieme di esempi rappresentato dal nodo. */
	private int beginExampleIndex;

	/** Indice finale del sottoinsieme di esempi rappresentato dal nodo. */
	private int endExampleIndex;

	/** Varianza dell'attributo target calcolata sugli esempi del nodo. */
	private double variance;

	/**
	 * Costruisce il nodo sull'intervallo di esempi indicato e ne calcola la varianza.
	 * <p>
	 * La varianza è calcolata come {@code somma(x^2) - (somma(x)^2 / n)}, dove
	 * {@code x} sono i valori dell'attributo target e {@code n} il numero di esempi.
	 *
	 * @param trainingSet il dataset di addestramento
	 * @param beginExampleIndex indice iniziale dell'intervallo di esempi
	 * @param endExampleIndex indice finale dell'intervallo di esempi
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
	 *
	 * @return l'identificativo del nodo
	 */
	private int getIdNode() {
		return idNode;
	}

	/**
	 * Restituisce l'indice iniziale del sottoinsieme di esempi del nodo.
	 *
	 * @return l'indice iniziale
	 */
	private int beginExampleIndex() {
		return beginExampleIndex;
	}

	/**
	 * Restituisce l'indice finale del sottoinsieme di esempi del nodo.
	 *
	 * @return l'indice finale
	 */
	private int getEndExampleIndex() {
		return endExampleIndex;
	}

	/**
	 * Restituisce la varianza del nodo.
	 *
	 * @return la varianza calcolata sugli esempi del nodo
	 */
	double getVariance() {
		return variance;
	}

	/**
	 * Restituisce il numero di figli del nodo.
	 * <p>
	 * Metodo astratto: deve essere implementato dalle sottoclassi.
	 *
	 * @return il numero di figli del nodo
	 */
	abstract int getNumberOfChildren();

	/**
	 * Restituisce una rappresentazione testuale del nodo, con l'intervallo di esempi
	 * e la varianza.
	 *
	 * @return la descrizione testuale del nodo
	 */
	public String toString() {
		String u = "Nodo: [Examples:" + beginExampleIndex + "-" + endExampleIndex + "] variance:" + variance;
		return u;
	}
}

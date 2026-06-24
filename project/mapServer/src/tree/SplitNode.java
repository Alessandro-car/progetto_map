package tree;

import data.Data;
import data.Attribute;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Modella un generico nodo di split dell'albero di regressione.
 * <p>
 * Oltre alle informazioni ereditate da {@link Node}, conserva l'attributo usato
 * per la suddivisione e la descrizione dei rami generati. È una classe astratta:
 * le implementazioni concrete sono {@link DiscreteNode} (attributi discreti) e
 * {@link ContinuousNode} (attributi continui). Implementa {@link Comparable} per
 * poter ordinare i nodi di split in base alla varianza prodotta.
 */
abstract class SplitNode extends Node implements Comparable<SplitNode> {

	/**
	 * Raccoglie le informazioni che descrivono un singolo ramo dello split:
	 * il valore di suddivisione, l'intervallo di esempi che vi ricadono e
	 * l'operatore di confronto da usare.
	 */
	class SplitInfo implements Serializable {

		/** Valore dell'attributo che definisce il ramo. */
		Object splitValue;

		/** Indice iniziale dell'intervallo di esempi del ramo. */
		int beginIndex;

		/** Indice finale dell'intervallo di esempi del ramo. */
		int endIndex;

		/** Identificativo numerico del ramo (figlio). */
		int numberChild;

		/** Operatore di confronto associato al ramo (di default "="). */
		String comparator = "=";

		/**
		 * Costruisce le informazioni di un ramo usando l'operatore di confronto
		 * predefinito ("=").
		 *
		 * @param splitValue valore dell'attributo che definisce il ramo
		 * @param beginIndex indice iniziale dell'intervallo
		 * @param endIndex indice finale dell'intervallo
		 * @param numberChild identificativo numerico del ramo
		 */
		SplitInfo(Object splitValue,int beginIndex,int endIndex,int numberChild){
			this.splitValue = splitValue;
			this.beginIndex = beginIndex;
			this.endIndex = endIndex;
			this.numberChild = numberChild;
		}

		/**
		 * Costruisce le informazioni di un ramo specificando anche l'operatore di confronto.
		 *
		 * @param splitValue valore dell'attributo che definisce il ramo
		 * @param beginIndex indice iniziale dell'intervallo
		 * @param endIndex indice finale dell'intervallo
		 * @param numberChild identificativo numerico del ramo
		 * @param comparator operatore di confronto del ramo (ad esempio "&lt;=" o "&gt;")
		 */
		SplitInfo(Object splitValue,int beginIndex,int endIndex,int numberChild, String comparator){
			this.splitValue = splitValue;
			this.beginIndex = beginIndex;
			this.endIndex = endIndex;
			this.numberChild = numberChild;
			this.comparator = comparator;
		}

		/**
		 * Restituisce l'indice iniziale dell'intervallo del ramo.
		 *
		 * @return l'indice iniziale
		 */
		int getBeginindex(){
			return beginIndex;
		}

		/**
		 * Restituisce l'indice finale dell'intervallo del ramo.
		 *
		 * @return l'indice finale
		 */
		int getEndIndex(){
			return endIndex;
		}

		/**
		 * Restituisce il valore di suddivisione del ramo.
		 *
		 * @return il valore di split del ramo
		 */
		Object getSplitValue(){
			return splitValue;
		}

		/**
		 * Restituisce una rappresentazione testuale del ramo.
		 *
		 * @return la descrizione testuale del ramo
		 */
		public String toString(){
			return "child " + numberChild +" split value"+comparator+splitValue + "[Examples:"+beginIndex+"-"+endIndex+"]";
		}

		/**
		 * Restituisce l'operatore di confronto del ramo.
		 *
		 * @return l'operatore di confronto
		 */
		String getComparator(){
			return comparator;
		}
	}

	/** Attributo su cui viene effettuato lo split. */
	Attribute attribute;

	/** Lista delle informazioni sui rami generati dallo split. */
	List<SplitInfo> mapSplit;

	/** Varianza complessiva degli esempi dopo lo split. */
	double splitVariance;

	/**
	 * Genera le informazioni di split.
	 * <p>
	 * Metodo astratto: deve essere implementato dalle sottoclassi per gestire
	 * attributi discreti o continui.
	 *
	 * @param trainingSet il dataset di addestramento
	 * @param beginExampleIndex indice iniziale dell'intervallo di esempi
	 * @param endExampleIndex indice finale dell'intervallo di esempi
	 * @param attribute l'attributo su cui effettuare lo split
	 */
	abstract void setSplitInfo(Data trainingSet,int beginExampleIndex, int endExampleIndex, Attribute attribute);

	/**
	 * Determina a quale ramo appartiene un valore.
	 * <p>
	 * Metodo astratto: deve essere implementato dalle sottoclassi.
	 *
	 * @param value il valore da verificare
	 * @return l'indice del ramo corrispondente
	 */
	abstract int testCondition(Object value);

	/**
	 * Costruisce il nodo di split: ordina gli esempi in base all'attributo, genera
	 * i rami e calcola la varianza complessiva risultante.
	 *
	 * @param trainingSet il dataset di addestramento
	 * @param beginExampleIndex indice iniziale dell'intervallo di esempi
	 * @param endExampleIndex indice finale dell'intervallo di esempi
	 * @param attribute l'attributo su cui effettuare lo split
	 */
	SplitNode(Data trainingSet, int beginExampleIndex, int endExampleIndex, Attribute attribute){
			super(trainingSet, beginExampleIndex,endExampleIndex);
			this.attribute=attribute;
			trainingSet.sort(attribute, beginExampleIndex, endExampleIndex);
			setSplitInfo(trainingSet, beginExampleIndex, endExampleIndex, attribute);

			splitVariance = 0;
			for(SplitInfo n : mapSplit){
					double localVariance = new LeafNode(trainingSet, n.getBeginindex(), n.getEndIndex()).getVariance();
					splitVariance += (localVariance);
			}
	}

	/**
	 * Restituisce l'attributo su cui è basato lo split.
	 *
	 * @return l'attributo del nodo di split
	 */
	Attribute getAttribute(){
		return attribute;
	}

	/**
	 * Restituisce la varianza complessiva prodotta dallo split.
	 *
	 * @return la varianza dello split
	 */
	double getVariance(){
		return splitVariance;
	}

	/**
	 * Restituisce il numero di rami generati dallo split.
	 *
	 * @return il numero di rami (figli)
	 */
	int getNumberOfChildren(){
		return mapSplit.size();
	}

	/**
	 * Restituisce le informazioni relative a un determinato ramo.
	 *
	 * @param child l'indice del ramo
	 * @return l'oggetto {@link SplitInfo} relativo al ramo indicato
	 */
	SplitInfo getSplitInfo(int child){
		return mapSplit.get(child);
	}

	/**
	 * Costruisce una stringa che descrive tutte le possibili diramazioni dello split,
	 * una per riga.
	 *
	 * @return l'elenco testuale delle diramazioni
	 */
	String formulateQuery(){
		String query = "";
		for(int i = 0; i < mapSplit.size(); i++)
			query += (i + ":" + attribute + mapSplit.get(i).getComparator() + mapSplit.get(i).getSplitValue()) + "\n";
		return query;
	}

	/**
	 * Restituisce una rappresentazione testuale completa del nodo di split, comprese
	 * le informazioni su tutti i rami.
	 *
	 * @return la descrizione testuale del nodo di split
	 */
	public String toString(){
		String v= "SPLIT : attribute=" + attribute +" "+ super.toString()+  " Split Variance: " + getVariance()+ "\n" ;
		for(SplitInfo n : mapSplit){
			v += "\t" + n + "\n";
		}
		return v;
	}

	/**
	 * Confronta questo nodo di split con un altro in base alla varianza prodotta.
	 * <p>
	 * A parità di varianza il confronto avviene sull'indice dell'attributo, così da
	 * stabilire comunque un ordine.
	 *
	 * @param o il nodo di split con cui effettuare il confronto
	 * @return un valore negativo, zero o positivo se questo nodo ha varianza
	 *         rispettivamente minore, uguale o maggiore di {@code o}
	 */
	public int compareTo(SplitNode o) {
		if (this.splitVariance > o.getVariance())
			return 1;
		if (this.splitVariance < o.getVariance())
			return -1;
		return Integer.compare(this.attribute.getIndex(), o.attribute.getIndex());
	}
}

package tree;
import data.*;

import java.io.Serializable;
import java.util.ArrayList;

/**
 * Modella un nodo di split relativo a un attributo discreto.
 * <p>
 * Genera un ramo per ogni valore distinto dell'attributo presente nel sottoinsieme
 * di esempi corrente, creando per ciascuno un oggetto {@link SplitInfo}. Estende la
 * classe {@link SplitNode}.
 */
class DiscreteNode extends SplitNode implements Serializable {

	/**
	 * Costruisce il nodo richiamando il costruttore della superclasse, che avvia il
	 * calcolo delle informazioni di split.
	 *
	 * @param trainingSet il dataset di addestramento
	 * @param beginExampleIndex indice iniziale del sottoinsieme di esempi
	 * @param endExampleIndex indice finale del sottoinsieme di esempi
	 * @param attribute l'attributo discreto su cui effettuare lo split
	 */
    DiscreteNode(Data trainingSet, int beginExampleIndex, int endExampleIndex, DiscreteAttribute attribute) {
        super(trainingSet, beginExampleIndex, endExampleIndex, attribute);
    }

	/**
	 * Calcola le informazioni di split per l'attributo discreto.
	 * <p>
	 * Individua i valori distinti dell'attributo nell'intervallo indicato e crea un
	 * {@link SplitInfo} per ogni gruppo di esempi che condivide lo stesso valore.
	 *
	 * @param trainingSet il dataset di addestramento
	 * @param beginExampleIndex indice iniziale del sottoinsieme di esempi
	 * @param endExampleIndex indice finale del sottoinsieme di esempi
	 * @param attribute l'attributo su cui calcolare lo split
	 */
    void setSplitInfo(Data trainingSet, int beginExampleIndex, int endExampleIndex, Attribute attribute) {
        int numberOfDistinctValues = 1;
        Object previousValue =  trainingSet.getExplanatoryValue(beginExampleIndex, attribute.getIndex());
        for (int i = beginExampleIndex + 1; i <= endExampleIndex; i++) {
            Object currentValue = trainingSet.getExplanatoryValue(i, attribute.getIndex());
            if (!currentValue.equals(previousValue)) {
                numberOfDistinctValues++;
                previousValue = currentValue;
            }
        }

        mapSplit = new ArrayList<SplitInfo>(numberOfDistinctValues);

        int splitIndex = 0;
        int partitionBegin = beginExampleIndex;
        Object currentGroupValue = trainingSet.getExplanatoryValue(beginExampleIndex, attribute.getIndex());

        for (int i = beginExampleIndex + 1; i <= endExampleIndex; i++) {
            Object val = trainingSet.getExplanatoryValue(i, attribute.getIndex());

            if (!val.equals(currentGroupValue)) {
								mapSplit.add(
									splitIndex,
									new SplitInfo(
										currentGroupValue,
										partitionBegin,
										i - 1,
										splitIndex
									)
								);
                splitIndex++;
                partitionBegin = i;
                currentGroupValue = val;
            }
        }
				mapSplit.add(
					splitIndex,
					new SplitInfo(
						currentGroupValue,
						partitionBegin,
						endExampleIndex,
						splitIndex
					)
				);
    }

	/**
	 * Determina a quale ramo del nodo appartiene un determinato valore.
	 *
	 * @param value il valore dell'attributo da verificare
	 * @return l'indice del ramo corrispondente al valore, oppure -1 se non trovato
	 */
  	int testCondition(Object value) {
        for (int i = 0; i < getNumberOfChildren(); i++) {
            if (getSplitInfo(i).getSplitValue().equals(value)) {
                return i;
            }
        }
        return -1;
    }

	/**
	 * Restituisce una rappresentazione testuale del nodo, indicando che si tratta di
	 * un nodo discreto.
	 *
	 * @return la descrizione testuale del nodo
	 */
    public String toString() {
        return "DISCRETE " + super.toString();
    }
}

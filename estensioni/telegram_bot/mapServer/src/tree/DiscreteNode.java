package tree;
import data.*;

import java.io.Serializable;
import java.util.ArrayList;
/**
 * La classe {@code DiscreteNode} modella un nodo di split relativo a un attributo discreto.
 * <p>
 * Questa classe popola la struttura {@code mapSplit} creando un oggetto {@link SplitInfo}
 * per ogni valore distinto dell'attributo discreto presente nel sottoinsieme di dati corrente.
 * <p>
 * La classe {@code DiscreteNode} estende la classe {@code SplitNode}.
 */

public class DiscreteNode extends SplitNode implements Serializable {

		/**
		 * Invoca il costruttore della superclasse per inizializzare i dati e avviare la generazione delle informazioni di split.
		 * @param trainingSet Il dataset di addestramento.
		 * @param beginExampleIndex Indice iniziale del sottoinsieme di dati.
		 * @param endExampleIndex Indice finale del sottoinsieme di dati.
		 * @param attribute L'attributo discreto utilizzato per generare lo split.
		 */
    DiscreteNode(Data trainingSet, int beginExampleIndex, int endExampleIndex, DiscreteAttribute attribute) {
        super(trainingSet, beginExampleIndex, endExampleIndex, attribute);
    }

		/**
		 * Calcola le informazioni necessarie per lo split dell'attributo discreto.
		 * <p>
		 * Il metodo identifica i valori distinti dell'attributo nell'intervallo specificato
		 * e crea un'istanza di {@link SplitInfo} per ogni gruppo di esempi che possiede lo stesso valore.
		 * @param trainingSet Il dataset di addestramento.
		 * @param beginExampleIndex Indice iniziale del sottinsieme di dati.
		 * @param endExampleIndex Indice finale del sottoinsieme di dati.
		 * @param attribute L'attributo su cui effettuare il calcolo dello split.
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
		 * Verifica a quale figlio del nodo appartiene un determinato valore.
		 * @param value Il valore dell'attributo da testare.
		 * @return L'indice del figlio corrispondente al valore, oppure -1 se non trovato.
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
		 * Restituisce una rappresentazione testuale del tipo di nodo e delle informazioni di split da ereditare.
		 * @return Stringa descrittiva del nodo discreto.
		 */
    public String toString() {
        return "DISCRETE " + super.toString();
    }
}

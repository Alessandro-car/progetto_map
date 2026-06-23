package tree;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import data.Attribute;
import data.ContinuousAttribute;
import data.Data;



/**
 * La classe {@code ContinuousNode} modella un nodo di split relativo a un
 * attributo continuo (numerico).
 * <p>
 * A differenza di un nodo discreto, lo split di un attributo continuo divide gli
 * esempi in due soli rami: quelli con valore minore o uguale a una certa soglia
 * ({@code <=}) e quelli con valore maggiore ({@code >}). La soglia scelta è quella
 * che produce la varianza complessiva minore.
 * <p>
 * Estende la classe {@link SplitNode}.
 */
class ContinuousNode extends SplitNode implements Serializable{

	/**
	 * Costruttore di classe: richiama il costruttore della superclasse per
	 * inizializzare il nodo e avviare il calcolo dello split.
	 *
	 * @param trainingSet il dataset di addestramento
	 * @param beginExampleIndex indice iniziale del sottoinsieme di esempi
	 * @param endExampleIndex indice finale del sottoinsieme di esempi
	 * @param attribute l'attributo continuo su cui effettuare lo split
	 */
	ContinuousNode(Data trainingSet, int beginExampleIndex, int endExampleIndex, ContinuousAttribute attribute) {
		super(trainingSet, beginExampleIndex, endExampleIndex, attribute); }

	/**
	 * Calcola le informazioni di split per l'attributo continuo.
	 * <p>
	 * Scorre i possibili valori di soglia e, per ciascuno, valuta la varianza che
	 * si otterrebbe dividendo gli esempi nei due rami ({@code <=} e {@code >}).
	 * Conserva la suddivisione che produce la varianza complessiva minore. Se uno
	 * dei due rami risulta vuoto, viene rimosso.
	 *
	 * @param trainingSet il dataset di addestramento
	 * @param beginExampleIndex indice iniziale del sottoinsieme di esempi
	 * @param endExampleIndex indice finale del sottoinsieme di esempi
	 * @param attribute l'attributo continuo su cui effettuare lo split
	 */
	void setSplitInfo(Data trainingSet,int beginExampleIndex, int endExampleIndex, Attribute attribute){
		Double currentSplitValue= (Double)trainingSet.getExplanatoryValue(beginExampleIndex,attribute.getIndex());
		double bestInfoVariance=0;
		List <SplitInfo> bestMapSplit=null;

		for(int i=beginExampleIndex+1;i<=endExampleIndex;i++){
			Double value=(Double)trainingSet.getExplanatoryValue(i,attribute.getIndex());
			if(value.doubleValue()!=currentSplitValue.doubleValue()){
				double localVariance=new LeafNode(trainingSet, beginExampleIndex,i-1).getVariance();
				double candidateSplitVariance=localVariance;
				localVariance=new LeafNode(trainingSet, i,endExampleIndex).getVariance();
				candidateSplitVariance+=localVariance;
				if(bestMapSplit==null){
					bestMapSplit=new ArrayList<SplitInfo>();
					bestMapSplit.add(new SplitInfo(currentSplitValue, beginExampleIndex, i-1,0,"<="));
					bestMapSplit.add(new SplitInfo(currentSplitValue, i, endExampleIndex,1,">"));
					bestInfoVariance=candidateSplitVariance;
				}
				else{

					if(candidateSplitVariance<bestInfoVariance){
						bestInfoVariance=candidateSplitVariance;
						bestMapSplit.set(0, new SplitInfo(currentSplitValue, beginExampleIndex, i-1,0,"<="));
						bestMapSplit.set(1, new SplitInfo(currentSplitValue, i, endExampleIndex,1,">"));
					}
				}
				currentSplitValue=value;
			}
		}
		mapSplit=bestMapSplit;

		if((mapSplit.get(1).beginIndex==mapSplit.get(1).getEndIndex())){
			mapSplit.remove(1);
		}
	}
	/**
	 * Restituisce una rappresentazione testuale del nodo, indicando che si tratta
	 * di un nodo continuo.
	 *
	 * @return la descrizione testuale del nodo
	 */
	public String toString() {
			return "CONTINUOUS " + super.toString();
	}

	/**
	 * Determina a quale ramo appartiene un valore: il ramo 0 se il valore è minore
	 * o uguale alla soglia di split, il ramo 1 altrimenti.
	 *
	 * @param value il valore dell'attributo da verificare
	 * @return 0 se {@code value <= soglia}, 1 in caso contrario
	 */
	int testCondition(Object value) {
		double v = (Double) value;
		if (v <= (Double) mapSplit.get(0).getSplitValue()) {
			return 0;
		} else
		return 1;
	}

}

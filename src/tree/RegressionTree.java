package tree;

import data.Attribute;
import data.ContinuousAttribute;
import data.Data;
import data.DiscreteAttribute;
import utility.Keyboard;
import java.util.TreeSet;

/**
 * La classe {@code RegressionTree} rappresenta l'intero albero di decisione per la regressione.
 * <p>
 * Implementa le fasi di apprendimento dell'albero attraverso il metodo {@link #learnTree}
 * e fornisce funzionalità per la stampa della struttura e delle regole indotte.
 */
public class RegressionTree {
	/** Il nodo radice dell'albero (o del sotto-albero corrente). */
	private Node root;
	/** Array di sotto-alberi figli, uno per ogni ramo dello split. */
	private RegressionTree childTree[];

	/**
	 * Costruttore privato utilizzato per l'istanziazione ricorsiva dei sotto-alberi.
	 */
	private RegressionTree() {
	}

	/**
	 * Costruttore pubblico che avvia il processo di apprendimento dell'albero.
	 * <p>
	 * Calcola automaticamente un numero minimo di esempi per foglia pari al 10%
	 * della dimensione del dataset.
	 * @param trainingSet Il dataset di addestramento completo.
	 */
	public RegressionTree(Data trainingSet) {
		learnTree(trainingSet, 0, trainingSet.getNumberOfExamples() - 1,
				trainingSet.getNumberOfExamples() * 10 / 100);
	}

	/**
	 * Verifica se l'intervallo corrente di esempi deve essere considerato un nodo foglia.
	 * @param trainingSet              Il dataset di riferimento.
	 * @param begin                    Indice iniziale.
	 * @param end                      Indice finale.
	 * @param numberOfExamplesPerLeaf  Soglia minima per creare una foglia.
	 * @return {@code true} se il numero di esempi è inferiore o uguale alla soglia.
	 */
	private boolean isLeaf(Data trainingSet, int begin, int end, int numberOfExamplesPerLeaf) {
		return (end - begin + 1) <= numberOfExamplesPerLeaf;
	}

	/**
	 * Analizza tutti gli attributi esplicativi e seleziona quello che genera
	 * lo split con la varianza minore, usando un {@link TreeSet} per ordinare
	 * automaticamente i nodi rispetto alla {@code splitVariance}.
	 * Usa l'RTTI (instanceof) per distinguere attributi discreti da continui
	 * e istanziare il nodo di split corretto.
	 * @param trainingSet Il dataset di addestramento.
	 * @param begin       Indice iniziale dell'intervallo.
	 * @param end         Indice finale dell'intervallo.
	 * @return Il miglior {@link SplitNode} trovato (quello con splitVariance minima).
	 */
	private SplitNode determineBestSplitNode(Data trainingSet, int begin, int end) {
		TreeSet<SplitNode> ts = new TreeSet<SplitNode>();
		for (int i = 0; i < trainingSet.getNumberOfExplanatoryAttributes(); i++) {
			Attribute a = trainingSet.getExplanatoryAttribute(i);
			SplitNode currentNode;
			if (a instanceof DiscreteAttribute) {
				currentNode = new DiscreteNode(trainingSet, begin, end, (DiscreteAttribute) a);
			} else {
				currentNode = new ContinuousNode(trainingSet, begin, end, (ContinuousAttribute) a);
			}
			ts.add(currentNode);
		}
		// Il TreeSet ordina per splitVariance crescente: il primo elemento è il nodo migliore
		SplitNode bestNode = ts.first();
		trainingSet.sort(bestNode.getAttribute(), begin, end);
		return bestNode;
	}

	/**
	 * Algoritmo ricorsivo per la costruzione dell'albero.
	 * <p>
	 * Se le condizioni per una foglia sono soddisfatte, crea un {@link LeafNode}.
	 * Altrimenti, cerca il miglior split e richiama se stesso sui figli.
	 * @param trainingSet              Il dataset di addestramento.
	 * @param begin                    Indice iniziale.
	 * @param end                      Indice finale.
	 * @param numberOfExamplesPerLeaf  Parametro di arresto per la creazione delle foglie.
	 */
	void learnTree(Data trainingSet, int begin, int end, int numberOfExamplesPerLeaf) {
		if (isLeaf(trainingSet, begin, end, numberOfExamplesPerLeaf)) {
			root = new LeafNode(trainingSet, begin, end);
		} else {
			root = determineBestSplitNode(trainingSet, begin, end);
			if (root.getNumberOfChildren() > 1) {
				childTree = new RegressionTree[root.getNumberOfChildren()];
				for (int i = 0; i < root.getNumberOfChildren(); i++) {
					childTree[i] = new RegressionTree();
					childTree[i].learnTree(trainingSet,
							((SplitNode) root).getSplitInfo(i).beginIndex,
							((SplitNode) root).getSplitInfo(i).endIndex,
							numberOfExamplesPerLeaf);
				}
			} else {
				root = new LeafNode(trainingSet, begin, end);
			}
		}
	}

	/**
	 * Visualizza le informazioni di ciascuno split dell'albero
	 * e per il corrispondente attributo acquisisce il valore dell'esempio da predire da tastiera.
	 * <p>
	 * Il metodo solleva l'eccezione {@code UnknownValueException} qualora la risposta dell'utente non permetta di
	 * selezionare un ramo valido del nodo di split.
	 * @return Ritorna il valore predetto del corrispondente attributo.
	 */
	public Double predictClass() throws UnknownValueException {
		if (root instanceof LeafNode) {
			return ((LeafNode) root).getPredictedClassValue();
		} else {
			int risp;
			System.out.println(((SplitNode) root).formulateQuery());
			risp = Keyboard.readInt();
			if (risp == -1 || risp >= root.getNumberOfChildren()) {
				throw new UnknownValueException("The answer should be an integer between 0 and " + (root.getNumberOfChildren() - 1) + "!");
			} else {
				return childTree[risp].predictClass();
			}
		}
	}

	/**
	 * Stampa a console l'intera struttura dell'albero.
	 */
	public void printTree() {
		System.out.println("\n********* TREE **********\n");
		System.out.println(toString());
		System.out.println("*************************\n");
	}

	/** @return Una rappresentazione testuale del nodo radice dell'albero */
	public String toString() {
		String tree = root.toString() + "\n";
		if (root instanceof LeafNode) {

		} else {
			for (int i = 0; i < childTree.length; i++)
				tree += childTree[i];
		}
		return tree;
	}

	/**
	 * Stampa a console le regole di regressione dedotte dall'albero.
	 */
	public void printRules() {
		System.out.println("********* RULES **********");
		if (root instanceof LeafNode) {
			System.out.println("==> Class=" + ((LeafNode) root).getPredictedClassValue());
		} else {
			for (int i = 0; i < root.getNumberOfChildren(); i++) {
				SplitNode splitRoot = (SplitNode) root;
				String condition = splitRoot.getAttribute().toString()
						+ splitRoot.getSplitInfo(i).getComparator()
						+ splitRoot.getSplitInfo(i).getSplitValue().toString();
				childTree[i].printRules(condition);
			}
		}
		System.out.println("*************************");
	}

	/**
	 * Metodo di supporto ricorsivo per la stampa delle regole con le condizioni accumulate.
	 * @param current La stringa contenente le condizioni accumulate dai nodi antenati.
	 */
	private void printRules(String current) {
		if (root instanceof LeafNode) {
			System.out.println(current + " ==> Class=" + ((LeafNode) root).getPredictedClassValue());
		} else {
			for (int i = 0; i < root.getNumberOfChildren(); i++) {
				SplitNode splitRoot = (SplitNode) root;
				String condition = splitRoot.getAttribute().toString()
						+ splitRoot.getSplitInfo(i).getComparator()
						+ splitRoot.getSplitInfo(i).getSplitValue().toString();
				childTree[i].printRules(current + " AND " + condition);
			}
		}
	}
}
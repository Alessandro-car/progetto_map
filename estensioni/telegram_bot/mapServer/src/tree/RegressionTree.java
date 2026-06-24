package tree;

import data.*;
import server.UnknownValueException;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.Scanner;
import java.util.TreeSet;
import java.util.ArrayList;
import java.lang.ClassNotFoundException;

/**
 * Rappresenta un albero di regressione, costruito in modo ricorsivo a partire da
 * un dataset.
 * <p>
 * L'albero viene appreso suddividendo via via gli esempi in base agli attributi
 * che minimizzano la varianza, fino a ottenere foglie sufficientemente piccole.
 * Una volta costruito può essere salvato su file e ricaricato, e può essere usato
 * per predire il valore della classe interagendo con l'utente o con un client.
 */
public class RegressionTree implements Serializable {

	/** Nodo radice dell'albero (o del sotto-albero corrente). */
	private Node root;

	/** Sotto-alberi figli, uno per ciascun ramo dello split alla radice. */
	private RegressionTree childTree[];

	/**
	 * Costruttore privato usato per creare ricorsivamente i sotto-alberi.
	 */
	private RegressionTree() {
	}

	/**
	 * Costruisce l'albero avviando l'apprendimento sull'intero dataset.
	 * <p>
	 * Il numero minimo di esempi per foglia viene fissato automaticamente al 10%
	 * della dimensione del dataset.
	 *
	 * @param trainingSet il dataset di addestramento
	 */
	public RegressionTree(Data trainingSet) {
		learnTree(trainingSet, 0, trainingSet.getNumberOfExamples() - 1,
				trainingSet.getNumberOfExamples() * 10 / 100);
	}

	/**
	 * Stabilisce se l'intervallo di esempi corrente deve diventare una foglia.
	 *
	 * @param trainingSet il dataset di riferimento
	 * @param begin indice iniziale dell'intervallo
	 * @param end indice finale dell'intervallo
	 * @param numberOfExamplesPerLeaf soglia minima di esempi per creare una foglia
	 * @return {@code true} se il numero di esempi è inferiore o uguale alla soglia
	 */
	private boolean isLeaf(Data trainingSet, int begin, int end, int numberOfExamplesPerLeaf) {
		return (end - begin + 1) <= numberOfExamplesPerLeaf;
	}

	/**
	 * Analizza tutti gli attributi descrittivi e sceglie quello che produce lo split
	 * con la varianza minore.
	 * <p>
	 * Usa un {@link TreeSet} per ordinare automaticamente i nodi di split in base alla
	 * varianza e l'operatore {@code instanceof} per creare il tipo di nodo corretto
	 * (discreto o continuo).
	 *
	 * @param trainingSet il dataset di addestramento
	 * @param begin indice iniziale dell'intervallo
	 * @param end indice finale dell'intervallo
	 * @return il miglior {@link SplitNode} trovato, cioè quello con varianza minima
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
		SplitNode bestNode = ts.first();
		trainingSet.sort(bestNode.getAttribute(), begin, end);
		return bestNode;
	}

	/**
	 * Costruisce ricorsivamente l'albero.
	 * <p>
	 * Se sono soddisfatte le condizioni di foglia crea un {@link LeafNode}; altrimenti
	 * individua il miglior split e ripete il procedimento su ciascun ramo.
	 *
	 * @param trainingSet il dataset di addestramento
	 * @param begin indice iniziale dell'intervallo
	 * @param end indice finale dell'intervallo
	 * @param numberOfExamplesPerLeaf soglia minima di esempi per creare una foglia
	 */
	private void learnTree(Data trainingSet, int begin, int end, int numberOfExamplesPerLeaf) {
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
	 * Esegue una predizione interattiva da tastiera.
	 * <p>
	 * Per ogni nodo di split mostra le possibili diramazioni e legge dalla tastiera
	 * la scelta dell'utente, scendendo nell'albero fino a raggiungere una foglia,
	 * il cui valore viene restituito.
	 *
	 * @return il valore della classe predetta
	 * @throws UnknownValueException se la scelta dell'utente non corrisponde a un ramo valido
	 */
	public Double predictClass() throws UnknownValueException {
		if (root instanceof LeafNode) {
			return ((LeafNode) root).getPredictedClassValue();
		} else {
			int risp;
			System.out.println(((SplitNode) root).formulateQuery());
			risp = new Scanner(System.in).nextInt();
			if (risp < 0 || risp >= root.getNumberOfChildren()) {
				throw new UnknownValueException("The answer should be an integer between 0 and " + (root.getNumberOfChildren() - 1) + "!");
			} else {
				return childTree[risp].predictClass();
			}
		}
	}

	/**
	 * Restituisce la lista degli indici dei figli del nodo radice corrente, sotto
	 * forma di stringhe.
	 * <p>
	 * Viene usata durante la predizione interattiva con un client per costruire i
	 * pulsanti tra cui scegliere. Se il nodo radice è una foglia la lista è vuota.
	 *
	 * @return la lista degli indici dei rami selezionabili
	 */
	private ArrayList<String> getArrayOfChildren() {
		ArrayList<String> childrens = new ArrayList<>();
		if (!(root instanceof LeafNode)) {
			for (int i = 0; i < root.getNumberOfChildren(); i++) {
				childrens.add(String.valueOf(i));
			}
		}
		return childrens;
	}

	/**
	 * Esegue una predizione interattiva comunicando con un client tramite stream di I/O.
	 * <p>
	 * Se il nodo corrente è una foglia restituisce direttamente il valore predetto.
	 * Se è un nodo di split, invia al client la domanda e l'elenco dei rami, attende
	 * la risposta (un intero) e prosegue la navigazione sul figlio scelto.
	 *
	 * @param in lo stream da cui leggere la risposta del client (l'indice del ramo da seguire)
	 * @param out lo stream su cui inviare la domanda formulata dal nodo
	 * @return il valore della classe predetta
	 * @throws UnknownValueException se l'indice ricevuto non è valido (negativo o troppo grande)
	 * @throws ClassNotFoundException se l'oggetto letto dallo stream non è riconosciuto
	 * @throws IOException se si verifica un errore durante la lettura o la scrittura
	 */
	public Double predictClass(ObjectInputStream in, ObjectOutputStream out) throws UnknownValueException, ClassNotFoundException, IOException {
		if (root instanceof LeafNode) {
			return ((LeafNode) root).getPredictedClassValue();
		} else {
			int risp = 0;
			out.writeObject(((SplitNode) root).formulateQuery());
			out.writeObject(this.getArrayOfChildren());
			out.flush();
			risp = (Integer) in.readObject();
			if (risp < 0 || risp >= root.getNumberOfChildren()) {
				out.writeObject("The answer should be an integer between 0 and" + (root.getNumberOfChildren() - 1) + "!");
				out.flush();
				throw new UnknownValueException("The answer should be an integer between 0 and " + (root.getNumberOfChildren() - 1) + "!");
			} else {
				out.writeObject("QUERY");
				out.flush();
				return childTree[risp].predictClass(in, out);
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

	/**
	 * Restituisce una rappresentazione testuale dell'albero a partire dalla radice.
	 *
	 * @return la descrizione testuale dell'albero
	 */
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
	 * Stampa a console le regole di regressione ricavate dall'albero.
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
	 * Metodo ricorsivo di supporto a {@link #printRules()} che accumula le condizioni
	 * incontrate lungo il percorso e le stampa quando raggiunge una foglia.
	 *
	 * @param current la stringa con le condizioni accumulate dai nodi antenati
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

	/**
	 * Salva l'albero su file serializzandolo.
	 *
	 * @param nomeFile il nome del file in cui salvare l'albero
	 * @throws FileNotFoundException se il file non può essere creato o aperto
	 * @throws IOException se si verifica un errore durante la scrittura
	 */
	public void salva(String nomeFile) throws FileNotFoundException, IOException {
		ObjectOutputStream oos = null;
		try {
			oos = new ObjectOutputStream(new FileOutputStream(nomeFile));
			oos.writeObject(root);
			oos.writeObject(childTree);
		} finally {
			if (oos != null) oos.close();
		}
	}

	/**
	 * Carica da file un albero precedentemente salvato.
	 *
	 * @param nomeFile il nome del file da cui caricare l'albero
	 * @return l'albero di regressione contenuto nel file
	 * @throws FileNotFoundException se il file non esiste
	 * @throws IOException se si verifica un errore durante la lettura
	 * @throws ClassNotFoundException se la classe dell'oggetto letto non viene trovata
	 */
	public static RegressionTree carica(String nomeFile) throws FileNotFoundException, IOException, ClassNotFoundException {
		ObjectInputStream ois = null;
		try {
			ois = new ObjectInputStream(new FileInputStream(nomeFile));
			RegressionTree tree = new RegressionTree();
			tree.root = (Node) ois.readObject();
			tree.childTree = (RegressionTree[]) ois.readObject();
			return tree;
		} finally {
			if (ois != null) ois.close();
		}
	}
}

import data.Data;
import tree.RegressionTree;
import utility.Keyboard;
import java.io.FileNotFoundException;

/**
 * Classe principale del programma per il test delle funzionalità di apprendimento.
 * <p>
 * Carica un dataset da file, avvia la costruzione dell'albero di regressione
 * e visualizza i risultati a console sotto forma di regole e struttura ad albero.
 */
class MainTest {

	/**
	 * Punto di ingresso dell'applicazione.
	 * <p>
	 * Il metodo esegue i seguenti passaggi:
	 * <ol>
	 * <li>Istanzia un oggetto {@link Data} caricando i dati dal file {@code servo.dat}.</li>
	 * <li>Costruisce un {@link RegressionTree} basato sul dataset caricato.</li>
	 * <li>Stampa le regole di regressione generate dall'albero.</li>
	 * <li>Stampa la struttura gerarchica dell'intero albero.</li>
	 * </ol>
	 *
	 * @param args Argomenti della riga di comando (non utilizzati).
	 * @throws FileNotFoundException Se il file di input {@code servo.dat} non viene trovato.
	 */
	public static void main(String[] args) throws FileNotFoundException{
		System.out.println("Training set:");
		String filename = Keyboard.readString();
		Data trainingSet = new Data(filename);

		RegressionTree tree =new RegressionTree(trainingSet);

		tree.printRules();

		tree.printTree();

		char repeat = 'y';
		while (repeat == 'y') {
			System.out.println("Starting prediction phase!");
			tree.predictClass();
			System.out.println("Would you repeat? (y/n)");
			repeat = Keyboard.readChar();
		}
	}

}

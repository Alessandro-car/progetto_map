import java.io.FileNotFoundException;
import java.io.IOException;

import data.Data;
import data.TrainingDataException;
import tree.RegressionTree;
import tree.UnknownValueException;
import utility.Keyboard;

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
	public static void main(String[] args) {
		int decision = 0;
		do {
			System.out.println("Learn Regression Tree from data [1]");
			System.out.println("Load Regression Tree from archive [2]");
			decision = Keyboard.readInt();
		} while (!(decision == 1) && !(decision == 2));

		String traningFileName = "";
		System.out.println("File name: ");
		traningFileName = Keyboard.readString();

		RegressionTree tree = null;
		if (decision == 1) {
			System.out.println("Starting data acquisition phase!");
			Data trainingSet = null;
			try {
				trainingSet = new Data(traningFileName + ".dat");
			} catch (TrainingDataException e) {
				System.out.println(e);
				return;
			}

			System.out.println("Starting learning phase!");
			tree = new RegressionTree(trainingSet);
			try {
				tree.salva(traningFileName + ".dmp");
			} catch (IOException e) {
				System.out.println(e.toString());
			}
		} else {
			try {
				tree = RegressionTree.carica(traningFileName + ".dmp");
			} catch (ClassNotFoundException | IOException e) {
				System.out.print(e);
				return;
			}
			tree.printRules();

			char risp = 'y';
			do {
				System.out.println("Starting prediction phase!");
				try {
					System.out.println(tree.predictClass());
				} catch (UnknownValueException e) {
					System.out.println(e);
				}
				System.out.println("Would you repeat? (y/n)");
				risp = Keyboard.readChar();
			} while (Character.toUpperCase(risp) == 'Y');
		}
	}
}

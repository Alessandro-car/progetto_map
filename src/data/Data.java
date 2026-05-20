package data;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;
import java.util.LinkedList;
import java.util.List;
import java.util.Arrays;
import java.util.Set;
import java.util.TreeSet;

/**
 * La classe {@code Data} modella un insieme di esempi caricati da file.
 * Organizza i dati in una matrice di oggetti e gestisce sia gli attributi descrittivi che l'attributo target di tipo continuo.
 * */

public class Data {

	/**
	 * Matrice bidimensionale che memorizza il dataset. Ogni riga è un esempio del dataset.
	 * */
	private Object data [][];

	/**
	 * Numero totale di esempi presenti nel dataset.
	 * */
	private int numberOfExamples;

	/**
	 * Array degli attributi descrittivi.
	 * */
	private List<Attribute> explanatorySet = new LinkedList<>();

	/**
	 * L'attributo target che si desidera predire.
	 */
	private ContinuousAttribute classAttribute;

	/**
	 * Costruttore che inizializza il dataset leggendo i dati da un file specificato.
	 * <p>
	 * Il file deve seguire un formato specifico con sezioni {@code @schema}, {@code @desc}/{@code @target} e {@code @data}.
	 *
	 * @param fileName Il percorso del file contenente il dataset.
	*  @throws TrainingDataException Se il formato dello schema del file non è corretto.
	 */
	public Data(String fileName) throws TrainingDataException {
		File inFile = new File (fileName);
		Scanner sc;
		try {
			sc = new Scanner (inFile);
		} catch (FileNotFoundException e) {
			throw new TrainingDataException(e.toString());
		}

		String line = sc.nextLine();

		if(!line.contains("@schema"))
			throw new TrainingDataException("Lo schema non e' presente nel file: " + fileName);
		String s[] = line.split(" ");

			//popolare explanatory Set
				//@schema 4
		short iAttribute = 0;
		line = sc.nextLine();
		while(!line.contains("@data")){
			s = line.split(" ");
			if(s[0].equals("@desc"))
			{ // aggiungo l'attributo allo spazio descrittivo
				//@desc motor discrete A,B,C,D,E
					if (s.length < 2) {
						explanatorySet.add(new ContinuousAttribute(s[1], iAttribute));
					} else {
						Set<String> discreteValues = new TreeSet<>(Arrays.asList(s[2].split(",")));
						explanatorySet.add(new DiscreteAttribute(s[1], iAttribute, discreteValues));
					}
			}
			else if(s[0].equals("@target"))
					classAttribute = new ContinuousAttribute(s[1], iAttribute);
			iAttribute++;
			line = sc.nextLine();
		}
		if(classAttribute == null){ //verifica su target
			throw new TrainingDataException("Il training set e' privo di variabili target numerica");
		}

			//avvalorare numero di esempi
			//@data 167
		numberOfExamples = new Integer(line.split(" ")[1]);
		if (numberOfExamples == 0){
			throw new TrainingDataException("Il training set e' vuoto");
		}
		//popolare data
		data = new Object[numberOfExamples][explanatorySet.size()+1];
		short iRow = 0;
		while (sc.hasNextLine())
		{
			line = sc.nextLine();
			s = line.split(","); //E,E,5,4, 0.28125095
			for(short jColumn = 0; jColumn < s.length-1; jColumn++)
				if (explanatorySet.get(jColumn) instanceof ContinuousAttribute)
					data[iRow][jColumn] = Double.parseDouble(s[jColumn]);
				else
					data[iRow][jColumn] = s[jColumn];
			data[iRow][s.length-1] = new Double(s[s.length-1]);
			iRow++;
		}
		sc.close();
	}
	/**
	 * Restituisce il numero di esempi caricati nel dataset.
	 * @return Numero di righe della matrice {@code data}
	 */
	public int getNumberOfExamples() {
		return numberOfExamples;
	}

	/**
	 * Restituisce il numero di attributi esplicativi.
	 * @return Lunghezza dell'array {@code explanatorySet}
	 */
	public int getNumberOfExplanatoryAttributes() {
		return explanatorySet.size();
	}

	/**
	 * Restituisce il valore dell'attributo target per un esempio specifico
	 * @param exampleIndex Indice dell'esempio nella tabella.
	 * @return Il valore continuo della classe per l'esempio indicato.
	 *
	 */
	public Double getClassValue(int exampleIndex) {
		return (Double)data[exampleIndex][explanatorySet.size()];
	}

	/**
	 * Restituisce il valore di un attributo esplicativo per un determinato esempio.
	 * @param exampleIndex Indice dell'esempio nella tabella.
	 * @param attributeIndex Indice dell'attributo esplicativo nella tabella.
	 * @return L'oggetto rappresentante il valore cercato
	 */
	public Object getExplanatoryValue(int exampleIndex, int attributeIndex) {
		return data[exampleIndex][attributeIndex];
	}

	/**
	 * Restituisce l'attributo esplicativo corrispondente all'indice specificato.
	 * @param index Posizione dell'attributo nell'array {@code explanatorySet}
	 * @return L'oggetto {@link Attribute} richiesto.
	 *
	 */
	public Attribute getExplanatoryAttribute(int index) {
		return explanatorySet.get(index);
	}

	/**
	 * Restituisce l'oggetto {@link ContinuousAttribute} che rappresenta l'attributo target del dataset.
	 * @return L'attributo target.
	 */
	private ContinuousAttribute getClassAttribute() {
		return classAttribute;
	}

	/**
	 * Restituisce una rappresentazione testuale dell'intero dataset.
	 * @return Stringa contenente i dati.
	 */
	public String toString(){
		String value="";
		for(int i = 0; i < numberOfExamples; i++){
			for(int j = 0; j < explanatorySet.size(); j++)
				value += data[i][j] + ",";

			value += data[i][explanatorySet.size()] + "\n";
		}
		return value;
	}

	/**
	 * Ordina l'intero dataset in base ai valori di un determinato attributo.
	 * @param attribute L'attributo su cui basare l'ordinamento.
	 * @param beginExampleIndex Indice iniziale dell'intervallo di righe.
	 * @param endExampleIndex Indice finale dell'intervallo di righe.
	 */
	public void sort(Attribute attribute, int beginExampleIndex, int endExampleIndex){
			quicksort(attribute, beginExampleIndex, endExampleIndex);
	}

	/**
	 * Scambia due intere righe del dataset.
	 * @param i Indice della prima riga.
	 * @param j Indice della seconda riga.
	 */
	private void swap(int i,int j){
		Object temp;
		for (int k = 0; k < getNumberOfExplanatoryAttributes() + 1; k++){
			temp = data[i][k];
			data[i][k] = data[j][k];
			data[j][k] = temp;
		}
	}

	/**
	 * Partiziona il vettore rispetto all'elemento x e restiutisce il punto di separazione
	 * @param attribute Attributo di tipo {@link DiscreteAttribute} usato per il confronto.
	 * @param inf Indice inferiore dell intervallo.
	 * @param sup Indice superiore dell'intervallo.
	 * @return Il punto di separazione.
	 */
	private  int partition(DiscreteAttribute attribute, int inf, int sup){
		int i, j;

		i = inf;
		j = sup;
		int	med = (inf + sup) / 2;
		String x = (String)getExplanatoryValue(med, attribute.getIndex());
		swap(inf, med);
		while (true)
		{
			while(i <= sup && ((String)getExplanatoryValue(i, attribute.getIndex())).compareTo(x) <= 0){
				i++;
			}
			while(((String)getExplanatoryValue(j, attribute.getIndex())).compareTo(x) > 0) {
				j--;
			}
			if(i < j) {
				swap(i, j);
			}
			else break;
		}
		swap(inf, j);
		return j;
	}

	/**
	 * Partiziona il vettore rispetto all'elemento x e restituisce il punto di separazione
	 * @param attribute Attributo di tipi {@link ContinuousAttribute} usato per il confronto.
	 * @param inf Indice inferiore dell'intervallo
	 * @param sup Indice superiore dell'intervallo.
	 * @return Il punto di separazione.
	 */

	private int partition(ContinuousAttribute attribute, int inf, int sup) {
		int i, j;
		i = inf;
		j = sup;
		int med = (inf + sup) / 2;
		Double x = (Double)getExplanatoryValue(med, attribute.getIndex());
		swap(inf, med);
		while (true) {
			while (i <= sup && ((Double)getExplanatoryValue(i, attribute.getIndex())).compareTo(x) <= 0) {
				i++;
			}
			while (((Double)getExplanatoryValue(j, attribute.getIndex())).compareTo(x) > 0) {
				j--;
			}
			if (i < j) {
				swap(i, j);
			}
			else break;
		}
		swap(inf, j);
		return j;
	}

	/**
	 * Algoritmo quicksort per l'ordinamento di un array di interi A
	 * usando come relazione d'ordine totale &le;.
	 * @param attribute Attributo su cui ordinare.
	 * @param inf Limite inferiore della partizione.
	 * @param sup Limite superiore della partizione.
	 */
	private void quicksort(Attribute attribute, int inf, int sup){
		if(sup >= inf){
			int pos;
			if (attribute instanceof DiscreteAttribute)
				pos = partition((DiscreteAttribute)attribute, inf, sup);
			else
				pos = partition((ContinuousAttribute)attribute, inf, sup);
			if ((pos - inf) < (sup - pos + 1)) {
				quicksort(attribute, inf, pos - 1);
				quicksort(attribute, pos + 1, sup);
			}
			else
			{
				quicksort(attribute, pos + 1, sup);
				quicksort(attribute, inf, pos - 1);
			}
		}
	}
}

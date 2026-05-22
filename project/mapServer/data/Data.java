package data;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import database.DatabaseConnectionException;
import database.DbAccess;
import database.EmptySetException;
import database.Example;
import database.TableData;
import database.TableSchema;
import database.Column;

import java.sql.SQLException;

/**
 * La classe {@code Data} modella un insieme di esempi caricati dal database.
 * Organizza i dati in una lista di oggetti Example e gestisce
 * sia gli attributi descrittivi che l'attributo target di tipo continuo.
 */
public class Data {

	/**
	 * Lista di esempi che costituisce il dataset.
	 */
	private List<Example> data = new ArrayList<Example>();

	/**
	 * Numero totale di esempi presenti nel dataset.
	 */
	private int numberOfExamples;

	/**
	 * Lista degli attributi descrittivi.
	 */
	private List<Attribute> explanatorySet = new LinkedList<>();

	/**
	 * L'attributo target che si desidera predire.
	 */
	private ContinuousAttribute classAttribute;

	/**
	 * Costruttore che inizializza il dataset caricando schema ed esempi
	 * da una tabella del database.
	 * <p>
	 * Solleva TrainingDataException se:
	 * - la connessione al database fallisce
	 * - la tabella non esiste o ha meno di due colonne
	 * - la tabella ha zero tuple
	 * - l'ultima colonna non è numerica (non può essere classAttribute)
	 *
	 * @param tableName Nome della tabella nel database da cui caricare i dati.
	 * @throws TrainingDataException Se si verifica un errore nel caricamento dei dati.
	 */
	public Data(String tableName) throws TrainingDataException {
		DbAccess db = new DbAccess();
		try {
			db.initConnection();
		} catch (DatabaseConnectionException e) {
			throw new TrainingDataException(e.toString());
		}

		TableSchema schema;
		try {
			schema = new TableSchema(db, tableName);
		} catch (SQLException e) {
			throw new TrainingDataException(e.toString());
		}

		// Verifica che la tabella abbia almeno due colonne
		if (schema.getNumberOfAttributes() < 2) {
			throw new TrainingDataException("La tabella ha meno di due colonne.");
		}

		// Verifica che l'ultima colonna sia numerica (classAttribute)
		Column lastColumn = schema.getColumn(schema.getNumberOfAttributes() - 1);
		if (!lastColumn.isNumber()) {
			throw new TrainingDataException(
					"L'attributo corrispondente all'ultima colonna non e' numerico.");
		}

		// Popola explanatorySet leggendo tutte le colonne tranne l'ultima
		int iAttribute = 0;
		for (int i = 0; i < schema.getNumberOfAttributes() - 1; i++) {
			Column col = schema.getColumn(i);
			if (col.isNumber()) {
				explanatorySet.add(new ContinuousAttribute(col.getColumnName(), iAttribute));
			} else {
				// Recupera i valori distinti per l'attributo discreto
				Set<String> distinctValues;
				try {
					distinctValues = (Set<String>)(Set<?>)
							new TableData(db).getDistinctColumnValues(tableName, col);
				} catch (SQLException e) {
					throw new TrainingDataException(e.toString());
				}
				explanatorySet.add(
						new DiscreteAttribute(col.getColumnName(), iAttribute, distinctValues));
			}
			iAttribute++;
		}

		// Istanzia classAttribute con l'ultima colonna
		classAttribute = new ContinuousAttribute(lastColumn.getColumnName(), iAttribute);

		// Carica le tuple dalla tabella
		TableData tableData = new TableData(db);
		List<Example> examples;
		try {
			examples = tableData.getTransazioni(tableName);
		} catch (SQLException e) {
			throw new TrainingDataException(e.toString());
		} catch (EmptySetException e) {
			throw new TrainingDataException("La tabella ha zero tuple.");
		}

		data = new ArrayList<>(examples);
		numberOfExamples = data.size();

		db.closeConnection();
	}

	/**
	 * Restituisce il numero di esempi caricati nel dataset.
	 * @return Numero di esempi.
	 */
	public int getNumberOfExamples() {
		return numberOfExamples;
	}

	/**
	 * Restituisce il numero di attributi esplicativi.
	 * @return Lunghezza della lista {@code explanatorySet}.
	 */
	public int getNumberOfExplanatoryAttributes() {
		return explanatorySet.size();
	}

	/**
	 * Restituisce il valore dell'attributo target per un esempio specifico.
	 * @param exampleIndex Indice dell'esempio nella lista.
	 * @return Il valore continuo della classe per l'esempio indicato.
	 */
	public Double getClassValue(int exampleIndex) {
		return (Double) data.get(exampleIndex).get(explanatorySet.size());
	}

	/**
	 * Restituisce il valore di un attributo esplicativo per un determinato esempio.
	 * @param exampleIndex   Indice dell'esempio nella lista.
	 * @param attributeIndex Indice dell'attributo esplicativo.
	 * @return L'oggetto rappresentante il valore cercato.
	 */
	public Object getExplanatoryValue(int exampleIndex, int attributeIndex) {
		return data.get(exampleIndex).get(attributeIndex);
	}

	/**
	 * Restituisce l'attributo esplicativo corrispondente all'indice specificato.
	 * @param index Posizione dell'attributo nella lista {@code explanatorySet}.
	 * @return L'oggetto {@link Attribute} richiesto.
	 */
	public Attribute getExplanatoryAttribute(int index) {
		return explanatorySet.get(index);
	}

	/**
	 * Restituisce l'oggetto {@link ContinuousAttribute} che rappresenta
	 * l'attributo target del dataset.
	 * @return L'attributo target.
	 */
	private ContinuousAttribute getClassAttribute() {
		return classAttribute;
	}

	/**
	 * Restituisce una rappresentazione testuale dell'intero dataset.
	 * @return Stringa contenente i dati.
	 */
	public String toString() {
		String value = "";
		for (int i = 0; i < numberOfExamples; i++) {
			for (int j = 0; j < explanatorySet.size(); j++)
				value += data.get(i).get(j) + ",";
			value += data.get(i).get(explanatorySet.size()) + "\n";
		}
		return value;
	}

	/**
	 * Ordina il dataset in base ai valori di un determinato attributo.
	 * @param attribute          L'attributo su cui basare l'ordinamento.
	 * @param beginExampleIndex  Indice iniziale dell'intervallo.
	 * @param endExampleIndex    Indice finale dell'intervallo.
	 */
	public void sort(Attribute attribute, int beginExampleIndex, int endExampleIndex) {
		quicksort(attribute, beginExampleIndex, endExampleIndex);
	}

	/**
	 * Scambia due interi esempi nella lista.
	 * @param i Indice del primo esempio.
	 * @param j Indice del secondo esempio.
	 */
	private void swap(int i, int j) {
		Example temp = data.get(i);
		data.set(i, data.get(j));
		data.set(j, temp);
	}

	/**
	 * Partiziona la lista rispetto a un attributo discreto.
	 * @param attribute Attributo discreto usato per il confronto.
	 * @param inf       Indice inferiore dell'intervallo.
	 * @param sup       Indice superiore dell'intervallo.
	 * @return Il punto di separazione.
	 */
	private int partition(DiscreteAttribute attribute, int inf, int sup) {
		int i, j;
		i = inf;
		j = sup;
		int med = (inf + sup) / 2;
		String x = (String) getExplanatoryValue(med, attribute.getIndex());
		swap(inf, med);
		while (true) {
			while (i <= sup &&
					((String) getExplanatoryValue(i, attribute.getIndex())).compareTo(x) <= 0) {
				i++;
			}
			while (((String) getExplanatoryValue(j, attribute.getIndex())).compareTo(x) > 0) {
				j--;
			}
			if (i < j) {
				swap(i, j);
			} else break;
		}
		swap(inf, j);
		return j;
	}

	/**
	 * Partiziona la lista rispetto a un attributo continuo.
	 * @param attribute Attributo continuo usato per il confronto.
	 * @param inf       Indice inferiore dell'intervallo.
	 * @param sup       Indice superiore dell'intervallo.
	 * @return Il punto di separazione.
	 */
	private int partition(ContinuousAttribute attribute, int inf, int sup) {
		int i, j;
		i = inf;
		j = sup;
		int med = (inf + sup) / 2;
		Double x = (Double) getExplanatoryValue(med, attribute.getIndex());
		swap(inf, med);
		while (true) {
			while (i <= sup &&
					((Double) getExplanatoryValue(i, attribute.getIndex())).compareTo(x) <= 0) {
				i++;
			}
			while (((Double) getExplanatoryValue(j, attribute.getIndex())).compareTo(x) > 0) {
				j--;
			}
			if (i < j) {
				swap(i, j);
			} else break;
		}
		swap(inf, j);
		return j;
	}

	/**
	 * Algoritmo quicksort per l'ordinamento della lista di esempi.
	 * @param attribute Attributo su cui ordinare.
	 * @param inf       Limite inferiore della partizione.
	 * @param sup       Limite superiore della partizione.
	 */
	private void quicksort(Attribute attribute, int inf, int sup) {
		if (sup >= inf) {
			int pos;
			if (attribute instanceof DiscreteAttribute)
				pos = partition((DiscreteAttribute) attribute, inf, sup);
			else
				pos = partition((ContinuousAttribute) attribute, inf, sup);
			if ((pos - inf) < (sup - pos + 1)) {
				quicksort(attribute, inf, pos - 1);
				quicksort(attribute, pos + 1, sup);
			} else {
				quicksort(attribute, pos + 1, sup);
				quicksort(attribute, inf, pos - 1);
			}
		}
	}
}
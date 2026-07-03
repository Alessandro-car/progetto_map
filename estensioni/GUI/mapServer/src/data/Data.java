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
 * Modella l'insieme di esempi (il dataset) caricato da una tabella del database.
 * <p>
 * Organizza i dati in una lista di {@link Example} e tiene distinti gli attributi
 * descrittivi (explanatory) dall'attributo target da predire, che è di tipo continuo.
 * Offre inoltre i metodi per accedere ai valori e per ordinare gli esempi rispetto
 * a un attributo, operazione necessaria durante la costruzione dell'albero.
 */
public class Data {

	/** Lista degli esempi che compongono il dataset. */
	private List<Example> data = new ArrayList<Example>();

	/** Numero totale di esempi presenti nel dataset. */
	private int numberOfExamples;

	/** Lista degli attributi descrittivi (indipendenti). */
	private List<Attribute> explanatorySet = new LinkedList<>();

	/** Attributo target (continuo) che si desidera predire. */
	private ContinuousAttribute classAttribute;

	/**
	 * Costruisce il dataset caricando schema ed esempi da una tabella del database.
	 * <p>
	 * Solleva una {@link TrainingDataException} se:
	 * <ul>
	 *   <li>la connessione al database fallisce;</li>
	 *   <li>la tabella non esiste o ha meno di due colonne;</li>
	 *   <li>la tabella non contiene alcuna tupla;</li>
	 *   <li>l'ultima colonna non è numerica e quindi non può fare da attributo target.</li>
	 * </ul>
	 *
	 * @param tableName nome della tabella del database da cui caricare i dati
	 * @throws TrainingDataException se si verifica un errore nel caricamento dei dati
	 * @throws SQLException se si verifica un errore di accesso al database
	 */
	public Data(String tableName) throws TrainingDataException, SQLException {
		DbAccess db = new DbAccess();
		try {
			db.initConnection();
		} catch (DatabaseConnectionException e) {
			throw new TrainingDataException(e.toString());
		}
		try {
			TableSchema schema;
			try {
				schema = new TableSchema(db, tableName);
			} catch (SQLException e) {
				throw new TrainingDataException(e.toString());
			}

			if (schema.getNumberOfAttributes() < 2) {
				throw new TrainingDataException("La tabella ha meno di due colonne.");
			}

			Column lastColumn = schema.getColumn(schema.getNumberOfAttributes() - 1);
			if (!lastColumn.isNumber()) {
				throw new TrainingDataException(
						"L'attributo corrispondente all'ultima colonna non e' numerico.");
			}

			int iAttribute = 0;
			for (int i = 0; i < schema.getNumberOfAttributes() - 1; i++) {
				Column col = schema.getColumn(i);
				if (col.isNumber()) {
					explanatorySet.add(new ContinuousAttribute(col.getColumnName(), iAttribute));
				} else {
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

			classAttribute = new ContinuousAttribute(lastColumn.getColumnName(), iAttribute);

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
		} finally {
			try {
				db.closeConnection();
			} catch (SQLException e) {
				System.err.println("Error closing connection: " + e);
			}
		}
	}

	/**
	 * Restituisce il numero di esempi presenti nel dataset.
	 *
	 * @return il numero di esempi
	 */
	public int getNumberOfExamples() {
		return numberOfExamples;
	}

	/**
	 * Restituisce il numero di attributi descrittivi.
	 *
	 * @return la dimensione della lista degli attributi descrittivi
	 */
	public int getNumberOfExplanatoryAttributes() {
		return explanatorySet.size();
	}

	/**
	 * Restituisce il valore dell'attributo target per l'esempio indicato.
	 *
	 * @param exampleIndex indice dell'esempio nel dataset
	 * @return il valore continuo della classe per l'esempio indicato
	 */
	public Double getClassValue(int exampleIndex) {
		return (Double) data.get(exampleIndex).get(explanatorySet.size());
	}

	/**
	 * Restituisce il valore di un attributo descrittivo per un dato esempio.
	 *
	 * @param exampleIndex indice dell'esempio nel dataset
	 * @param attributeIndex indice dell'attributo descrittivo
	 * @return l'oggetto che rappresenta il valore richiesto
	 */
	public Object getExplanatoryValue(int exampleIndex, int attributeIndex) {
		return data.get(exampleIndex).get(attributeIndex);
	}

	/**
	 * Restituisce l'attributo descrittivo che si trova all'indice specificato.
	 *
	 * @param index posizione dell'attributo nella lista degli attributi descrittivi
	 * @return l'oggetto {@link Attribute} richiesto
	 */
	public Attribute getExplanatoryAttribute(int index) {
		return explanatorySet.get(index);
	}

	/**
	 * Restituisce l'attributo target del dataset.
	 *
	 * @return l'attributo continuo da predire
	 */
	private ContinuousAttribute getClassAttribute() {
		return classAttribute;
	}

	/**
	 * Restituisce una rappresentazione testuale dell'intero dataset, con un esempio
	 * per riga e i valori separati da virgola.
	 *
	 * @return la stringa contenente i dati del dataset
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
	 * Ordina gli esempi del dataset in base ai valori di un attributo, all'interno
	 * dell'intervallo indicato.
	 *
	 * @param attribute attributo su cui basare l'ordinamento
	 * @param beginExampleIndex indice iniziale dell'intervallo
	 * @param endExampleIndex indice finale dell'intervallo
	 */
	public void sort(Attribute attribute, int beginExampleIndex, int endExampleIndex) {
		quicksort(attribute, beginExampleIndex, endExampleIndex);
	}

	/**
	 * Scambia di posizione due esempi nella lista.
	 *
	 * @param i indice del primo esempio
	 * @param j indice del secondo esempio
	 */
	private void swap(int i, int j) {
		Example temp = data.get(i);
		data.set(i, data.get(j));
		data.set(j, temp);
	}

	/**
	 * Partiziona gli esempi rispetto a un attributo discreto, usato dal quicksort.
	 *
	 * @param attribute attributo discreto usato per il confronto
	 * @param inf indice inferiore dell'intervallo
	 * @param sup indice superiore dell'intervallo
	 * @return il punto di separazione (pivot) della partizione
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
	 * Partiziona gli esempi rispetto a un attributo continuo, usato dal quicksort.
	 *
	 * @param attribute attributo continuo usato per il confronto
	 * @param inf indice inferiore dell'intervallo
	 * @param sup indice superiore dell'intervallo
	 * @return il punto di separazione (pivot) della partizione
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
	 * Ordina gli esempi nell'intervallo indicato con l'algoritmo quicksort,
	 * scegliendo la partizione adatta in base al tipo (discreto o continuo)
	 * dell'attributo.
	 *
	 * @param attribute attributo su cui ordinare
	 * @param inf limite inferiore dell'intervallo
	 * @param sup limite superiore dell'intervallo
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

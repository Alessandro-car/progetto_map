/**
 * La classe astratta {@code SplitNode} modella un nodo di split nell'albero di decisione.
 * <p>
 * Oltre ai dati ereditati da {@link Node}, questa classe contiene le informazioni
 * sull'attributo usato per la separazione e una struttura per gestire i rami figli.
 */
abstract class SplitNode extends Node {
	/**
	 * Inner class che colleziona le informazioni descrittive di un ramo dello split.
	 * Mantiene traccia del valore di split, dell'intervallo di indici nel dataset
	 * e dell'operatore di confronto.
	 */
	class SplitInfo{
		/** Valore dell'attributo che definisce il ramo. */
		Object splitValue;
		/** Indice iniziale del dataset per questo ramo. */
		int beginIndex;
		/** Indice finale del dataset per questo ramo. */
		int endIndex;
		/** Identificativo numerico del figlio.*/
		int numberChild;

		/** Operatore di confronto. */
		String comparator = "=";

		/**
		 * Costruttore per {@code SplitInfo} con operatore di default.
		 * @param splitValue  Valore dell'attributo.
		 * @param beginIndex  Indice di inizio.
		 * @param endIndex    Indice di fine.
		 * @param numberChild Numero del figlio.
		 */
		SplitInfo(Object splitValue,int beginIndex,int endIndex,int numberChild){
			this.splitValue = splitValue;
			this.beginIndex = beginIndex;
			this.endIndex = endIndex;
			this.numberChild = numberChild;
		}

		/**
		 * Costruttore completo per {@code SplitInfo} con operatore specificato.
		 * @param splitValue  Valore dell'attributo.
		 * @param beginIndex  Indice di inizio.
		 * @param endIndex    Indice di fine.
		 * @param numberChild Numero del figlio.
		 * @param comparator  Simbolo dell'operatore di confronto.
		 */
		SplitInfo(Object splitValue,int beginIndex,int endIndex,int numberChild, String comparator){
			this.splitValue = splitValue;
			this.beginIndex = beginIndex;
			this.endIndex = endIndex;
			this.numberChild = numberChild;
			this.comparator = comparator;
		}

		/** @return Indice iniziale dell'intervallo. */
		int getBeginindex(){
			return beginIndex;
		}

		/** @return Indice finale dell'intervallo. */
		int getEndIndex(){
			return endIndex;
		}

		/** @return Valore di split del ramo. */
		Object getSplitValue(){
			return splitValue;
		}

		/** @return Operatore di confronto. */
		public String toString(){
			return "child " + numberChild +" split value"+comparator+splitValue + "[Examples:"+beginIndex+"-"+endIndex+"]";
		}

		/** @return Rappresentazione testuale del ramo. */
		String getComparator(){
			return comparator;
		}
	}

  /** L'attributo su cui viene effettuato lo split. */
	Attribute attribute;
	/** Array che contiene le informazioni sui vari rami dello split. */
	SplitInfo mapSplit[];
	/** Valore della varianza complessiva dopo lo split. */
	double splitVariance;

	/**
	 * Metodo astratto per generare le informazioni di split.
	 * Deve essere implementato nelle sottoclassi per gestire attributi discreti o continui.
	 * @param trainingSet       Dataset di addestramento.
	 * @param beginExampleIndex Indice iniziale.
	 * @param endExampleIndex   Indice finale.
	 * @param attribute         Attributo di split.
	 */
	abstract void setSplitInfo(Data trainingSet,int beginExampleIndex, int endExampleIndex, Attribute attribute);

	/**
	 * Metodo astratto per verificare a quale ramo appartiene un valore.
	 * @param value Valore da testare.
	 * @return Indice del ramo corrispondente.
	 */
	abstract int testCondition(Object value);

	/**
	 * Costruttore di {@code SplitNode}. Ordina il training set in base all'attributo,
	 * genera lo split e calcola la varianza complessiva dei figli.
	 *
	 * @param trainingSet       Dataset di addestramento.
	 * @param beginExampleIndex Indice iniziale.
	 * @param endExampleIndex   Indice finale.
	 * @param attribute         Attributo di split.
	 */
	SplitNode(Data trainingSet, int beginExampleIndex, int endExampleIndex, Attribute attribute){
			super(trainingSet, beginExampleIndex,endExampleIndex);
			this.attribute=attribute;
			trainingSet.sort(attribute, beginExampleIndex, endExampleIndex); // order by attribute
			setSplitInfo(trainingSet, beginExampleIndex, endExampleIndex, attribute);

			//compute variance
			splitVariance=0;
			for(int i=0;i<mapSplit.length;i++){
					double localVariance=new LeafNode(trainingSet, mapSplit[i].getBeginindex(),mapSplit[i].getEndIndex()).getVariance();
					splitVariance+=(localVariance);
			}
	}

	/** @return L'attributo associato al nodo di split. */
	Attribute getAttribute(){
		return attribute;
	}

	/** @return La varianza calcolata per lo split corrente. */
	double getVariance(){
		return splitVariance;
	}

	/** @return Il numero di rami generati dallo split. */
	int getNumberOfChildren(){
		return mapSplit.length;
	}

	/**
	 * Restituisce le informazioni di split per un determinato figlio.
	 * @param child Indice del figlio.
	 * @return Oggetto {@link SplitInfo} relativo al figlio.
	 */
	SplitInfo getSplitInfo(int child){
		return mapSplit[child];
	}

	/**
	 * Genera una stringa contenente la descrizione di tutte le possibili diramazioni.
	 * @return Elenco testuale delle query di split.
	 */
	String formulateQuery(){
		String query = "";
		for(int i = 0; i < mapSplit.length; i++)
			query+= (i + ":" + attribute + mapSplit[i].getComparator() + mapSplit[i].getSplitValue()) + "\n";
		return query;
	}

	/** @return Rappresentazione testuale completa del nodo di split. */
	public String toString(){
		String v= "SPLIT : attribute=" + attribute.getName() +" "+ super.toString()+  " Split Variance: " + getVariance()+ "\n" ;
		for(int i = 0; i < mapSplit.length; i++){
			v += "\t" + mapSplit[i] + "\n";
		}
		return v;
	}
}

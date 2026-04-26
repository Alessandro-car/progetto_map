abstract class Node {
	private static int idNodeCount = 0;
	private int idNode;
	private int beginExampleIndex;
	private int endExampleIndex;
	private double variance;

	Node(Data trainingSet, int beginExampleIndex, int endExampleIndex) {
		this.beginExampleIndex = beginExampleIndex;
		this.endExampleIndex = endExampleIndex;
		this.idNode = idNodeCount++;
		double root_var = 0.0;
		double sumClassVarSq = 0.0;
		double sumSqClassVar = 0.0;
		for (int i = this.beginExampleIndex; i <= this.endExampleIndex; i++) {
			sumClassVarSq += Math.pow(trainingSet.getClassValue(i), 2);
			sumSqClassVar += trainingSet.getClassValue(i);
		}
		root_var = sumClassVarSq - (Math.pow(sumSqClassVar, 2) / (this.endExampleIndex -  this.beginExampleIndex + 1));
	}

	private int getIdNode() {
		return idNode;
	}

	private int beginExampleIndex() {
		return beginExampleIndex;
	}

	private int getEndExampleIndex() {
		return endExampleIndex;
	}

	private double getVariance() {
		return variance;
	}

	private abstract int getNumberOfChildren();

	private String toString() {
		return new String("beginExampleIndex: " + beginExampleIndex + " endExampleIndex: " + endExampleIndex + " variance: " + variance);
	}
}

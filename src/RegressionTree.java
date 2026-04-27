class RegressionTree {
	private Node root;
	private RegressionTree childTree[];

	private RegressionTree() {
	}

	RegressionTree(Data trainingSet) {
		learnTree(trainingSet, 0, trainingSet.getNumberOfExamples() - 1,
				trainingSet.getNumberOfExamples() * 10 / 100);
	}

	private boolean isLeaf(Data trainingSet, int begin, int end, int numberOfExamplesPerLeaf) {
		return (end - begin + 1) <= numberOfExamplesPerLeaf;
	}

	private SplitNode determineBestSplitNode(Data trainingSet, int begin, int end) {
		DiscreteNode bestNode = null;
		for (int i = 0; i < trainingSet.getNumberOfExplanatoryAttributes(); i++) {
			DiscreteNode currentNode = new DiscreteNode(trainingSet, begin, end, (DiscreteAttribute) trainingSet.getExplanatoryAttribute(i));
			if (bestNode == null || currentNode.getVariance() < bestNode.getVariance()) {
				bestNode = currentNode;
			}
		}
		trainingSet.sort(bestNode.getAttribute(), begin, end);
		return bestNode;
	}

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

	void printTree() {
		System.out.println("\n********* TREE **********\n");
		System.out.println(toString());
		System.out.println("*************************\n");
	}

	public String toString(){
		String tree=root.toString()+"\n";

		if( root instanceof LeafNode){

		}
		else //split node
		{
			for(int i=0;i<childTree.length;i++)
				tree +=childTree[i];
		}
		return tree;
	}

	void printRules() {
		System.out.println("********* RULES **********");
		if (root instanceof LeafNode) {
			System.out.println("==> Class=" + ((LeafNode) root).getPredictedClassValue());
		} else {
			for (int i = 0; i < root.getNumberOfChildren(); i++) {
				SplitNode splitRoot = (SplitNode) root;
				String condition = splitRoot.getAttribute().getName()
						+ splitRoot.getSplitInfo(i).getComparator()
						+ splitRoot.getSplitInfo(i).getSplitValue().toString();
				childTree[i].printRules(condition);
			}
		}
		System.out.println("*************************");
	}

	private void printRules(String current) {
		if (root instanceof LeafNode) {
			System.out.println(current + " ==> Class=" + ((LeafNode) root).getPredictedClassValue());
		} else {
			for (int i = 0; i < root.getNumberOfChildren(); i++) {
				SplitNode splitRoot = (SplitNode) root;
				String condition = splitRoot.getAttribute().getName()
						+ splitRoot.getSplitInfo(i).getComparator()
						+ splitRoot.getSplitInfo(i).getSplitValue().toString();
				childTree[i].printRules(current + " AND " + condition);
			}
		}
	}
}

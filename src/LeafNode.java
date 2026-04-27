class LeafNode extends Node {
    private Double predictedClassValue;

    private LeafNode(Data trainingSet, int beginExampleIndex, int endExampleIndex) {
        super(trainingSet, beginExampleIndex, endExampleIndex);
        double sum = 0;
        for (int i = beginExampleIndex; i <= endExampleIndex; i++) {
            sum += trainingSet.getClassValue(i);
        }
        predictedClassValue = sum / (endExampleIndex - beginExampleIndex + 1);
    }

    private Double getPredictedClassValue() {
        return predictedClassValue;
    }

    private int getNumberOfChildren() {
        return 0;
    }

    private String toString() {
        return "LEAF : class=" + predictedClassValue + " " + super.toString();
    }
}
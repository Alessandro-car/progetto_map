class LeafNode extends Node {
    private Double predictedClassValue;

    LeafNode(Data trainingSet, int beginExampleIndex, int endExampleIndex) {
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

    public int getNumberOfChildren() {
        return 0;
    }

    public String toString() {
        return "LEAF : class=" + predictedClassValue + " " + super.toString();
    }
}
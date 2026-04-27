class DiscreteNode extends SplitNode {

    DiscreteNode(Data trainingSet, int beginExampleIndex, int endExampleIndex, DiscreteAttribute attribute) {
        super(trainingSet, beginExampleIndex, endExampleIndex, attribute);
    }


    void setSplitInfo(Data trainingSet, int beginExampleIndex, int endExampleIndex, Attribute attribute) {
        int numberOfDistinctValues = 1;
        Object previousValue =  trainingSet.getExplanatoryValue(beginExampleIndex, attribute.getIndex());
        for (int i = beginExampleIndex + 1; i <= endExampleIndex; i++) {
            Object currentValue = trainingSet.getExplanatoryValue(i, attribute.getIndex());
            if (!currentValue.equals(previousValue)) {
                numberOfDistinctValues++;
                previousValue = currentValue;
            }
        }

        mapSplit = new SplitInfo[numberOfDistinctValues];

        int splitIndex = 0;
        int partitionBegin = beginExampleIndex;
        Object currentGroupValue = trainingSet.getExplanatoryValue(beginExampleIndex, attribute.getIndex());

        for (int i = beginExampleIndex + 1; i <= endExampleIndex; i++) {
            Object val = trainingSet.getExplanatoryValue(i, attribute.getIndex());

            if (!val.equals(currentGroupValue)) {
                mapSplit[splitIndex] = new SplitInfo(
                        currentGroupValue,
                        partitionBegin,
                        i - 1,
                        splitIndex

                );
                splitIndex++;
                partitionBegin = i;
                currentGroupValue = val;
            }
        }
        mapSplit[splitIndex] =  new SplitInfo(
                        currentGroupValue,
                        partitionBegin,
                        endExampleIndex,
                        splitIndex);
    }


  	int testCondition(Object value) {
        for (int i = 0; i < getNumberOfChildren(); i++) {
            if (getSplitInfo(i).getSplitValue().equals(value)) {
                return i;
            }
        }
        return -1;
    }

    public String toString() {
        return "DISCRETE " + super.toString();
    }
}

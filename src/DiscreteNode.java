private class DiscreteNode extends SplitNode {

    private DiscreteNode(Data trainingSet, int beginExampleIndex, int endExampleIndex,
                        DiscreteAttribute attribute) {
        super(trainingSet, beginExampleIndex, endExampleIndex, attribute);
    }


    private void setSplitInfo(Data trainingSet, int beginExampleIndex,
                      int endExampleIndex, Attribute attribute) {

        int numberOfDistinctValues = 0;
        Object previousValue = null;

        for (int i = beginExampleIndex; i <= endExampleIndex; i++) {
            Object currentValue = trainingSet.getExplanatoryValue(i, attribute.getIndex());
            if (previousValue == null || !currentValue.equals(previousValue)) {
                numberOfDistinctValues++;
                previousValue = currentValue;
            }
        }

        mapSplit = new SplitInfo[numberOfDistinctValues];

        int splitIndex = 0;
        int partitionBegin = beginExampleIndex;
        Object currentGroupValue = trainingSet.getExplanatoryValue(beginExampleIndex, attribute.getIndex());

        for (int i = beginExampleIndex + 1; i <= endExampleIndex + 1; i++) {

            boolean isEnd = (i == endExampleIndex + 1);
            Object val = isEnd ? null : trainingSet.getExplanatoryValue(i, attribute.getIndex());

            if (isEnd || !val.equals(currentGroupValue)) {
                mapSplit[splitIndex] = new SplitInfo(
                        currentGroupValue,  
                        partitionBegin,     
                        i - 1,              
                        splitIndex          
                );
                splitIndex++;

                if (!isEnd) {
                    partitionBegin = i;
                    currentGroupValue = val;
                }
            }
        }
    }


    private int testCondition(Object value) {
        for (int i = 0; i < mapSplit.length; i++) {
            if (mapSplit[i].getSplitValue().equals(value)) {
                return i;
            }
        }
        return -1; 
    }

    private String toString() {
        return "DISCRETE " + super.toString();
    }
}
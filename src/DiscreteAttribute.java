class DiscreteAttribute extends Attribute {
    private String values[];

    DiscreteAttribute(String name, int index, String values[]) {
        super(name, index);
        this.values = values;
    }

    int getNumberOfDistinctValues() {
        return values.length;
    }

    String getValue(int i) {
        return values[i];
    }
}

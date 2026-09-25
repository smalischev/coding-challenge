package domain;

public enum CardValue {
    ZERO(0, "0", "0"),
    ONE(1, "1", "1"),
    TWO(2, "2", "2"),
    THREE(3, "3", "3"),
    FIVE(5, "5", "5"),
    EIGHT(8, "8", "8"),
    THIRTEEN(13, "13", "13"),
    TWENTY_ONE(21, "21", "21"),
    THIRTY_FOUR(34, "34", "34"),
    QUESTION_MARK(null, "?", "question-mark"),
    COFFEE(null, "☕", "coffee");

    private final Integer numericValue;
    private final String displayValue;
    private final String labelValue;

    CardValue(Integer numericValue, String displayValue, String labelValue) {
        this.numericValue = numericValue;
        this.displayValue = displayValue;
        this.labelValue = labelValue;
    }

    public Integer getNumericValue() {
        return numericValue;
    }

    public boolean isNumeric() {
        return numericValue != null;
    }

    public String getDisplayValue() {
        return displayValue;
    }

    public String getLabelValue() {
        return labelValue;
    }
}

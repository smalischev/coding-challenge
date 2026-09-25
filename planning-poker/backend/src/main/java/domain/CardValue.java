package domain;

public enum CardValue {
    ZERO(0),
    ONE(1),
    TWO(2),
    THREE(3),
    FIVE(5),
    EIGHT(8),
    THIRTEEN(13),
    TWENTY_ONE(21),
    THIRTY_FOUR(34),
    QUESTION_MARK(null),
    COFFEE(null);

    private final Integer numericValue;

    CardValue(Integer numericValue) {
        this.numericValue = numericValue;
    }

    public Integer getNumericValue() {
        return numericValue;
    }

    public boolean isNumeric() {
        return numericValue != null;
    }
}

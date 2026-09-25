package domain;

public class Issue {
    private int id;
    private boolean approved;
    private CardValue value;

    public Issue(){}

    public CardValue getValue(){
        return this.value;
    }

    public boolean isApproved(){
        return this.approved;
    }

    public void approve(CardValue value){
        if (value == null) {
            throw new IllegalArgumentException("a card value is required");
        }

        this.value = value;
        this.approved = true;
    }
}

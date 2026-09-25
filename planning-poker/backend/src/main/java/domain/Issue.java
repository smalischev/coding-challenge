package domain;

public class Issue {
    private int id;
    private boolean approved;
    private int value;

    public Issue(){}

    public int getValue(){
        return this.value;
    }

    public boolean isApproved(){
        return this.approved;
    }

    public void approve(CardValue value){
        this.value = value.getNumericValue();
        this.approved = true;
    }
}

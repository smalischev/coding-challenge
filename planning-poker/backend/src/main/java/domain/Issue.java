package domain;

public class Issue {
    private final long gitlabIssueIid;
    private boolean approved;
    private CardValue value;

    public Issue() {
        this(0);
    }

    public Issue(long gitlabIssueIid) {
        this.gitlabIssueIid = gitlabIssueIid;
    }

    public long getGitlabIssueIid() {
        return gitlabIssueIid;
    }

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

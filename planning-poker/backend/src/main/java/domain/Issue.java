package domain;

public class Issue {
    private final long gitlabIssueIid;
    private final String title;
    private final String description;
    private boolean approved;
    private CardValue value;

    public Issue() {
        this(0);
    }

    public Issue(long gitlabIssueIid) {
        this(gitlabIssueIid, null, null);
    }

    public Issue(long gitlabIssueIid, String title, String description) {
        this.gitlabIssueIid = gitlabIssueIid;
        this.title = title;
        this.description = description;
    }

    public long getGitlabIssueIid() {
        return gitlabIssueIid;
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
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

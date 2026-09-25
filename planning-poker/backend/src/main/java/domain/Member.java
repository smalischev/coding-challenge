package domain;

public abstract class Member {
    private final int id;
    private final String name;
    private final Role role;

    protected Member(int id, String name, Role role) {
        this.id = id;
        this.role = role;
        this.name = name;
    }

    protected Member(String name, Role role) {
        this(0, name, role);
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public Role getRole() {
        return role;
    }
}

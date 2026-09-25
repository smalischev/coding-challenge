package domain;

import java.util.Objects;

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

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (other == null || getClass() != other.getClass()) {
            return false;
        }

        Member member = (Member) other;
        return Objects.equals(name, member.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(getClass(), name);
    }
}

public abstract class User {
    private final int userId;
    private final String name;

    protected User(int userId, String name) {
        this.userId = userId;
        this.name = name;
    }

    public int getUserId() {
        return userId;
    }

    public String getName() {
        return name;
    }

    public abstract String getRole();
}

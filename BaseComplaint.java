public abstract class BaseComplaint {
    private final int id;
    private final String description;

    protected BaseComplaint(int id, String description) {
        this.id = id;
        this.description = description;
    }

    public int getId() {
        return id;
    }

    public String getDescription() {
        return description;
    }

    public abstract String getSummary();
}

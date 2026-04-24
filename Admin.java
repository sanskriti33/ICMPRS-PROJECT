public class Admin extends User {
    public Admin(int userId, String name) {
        super(userId, name);
    }

    @Override
    public String getRole() {
        return "System Administrator";
    }
}

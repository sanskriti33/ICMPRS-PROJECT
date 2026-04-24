public class Customer extends User {
    public Customer(int userId, String name) {
        super(userId, name);
    }

    @Override
    public String getRole() {
        return "Customer";
    }
}

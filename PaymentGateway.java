import java.util.*;

public class PaymentGateway {
    private Map<String, User> users = new HashMap<>();
    private List<Product> products = new ArrayList<>();

    public PaymentGateway() {
        products.add(new Product("Laptop", 1200.00));
        products.add(new Product("Smartphone", 800.00));
        products.add(new Product("Subscription", 29.99));
    }

    public User registerUser(String username, String password) {
        if (users.containsKey(username)) {
            return null;
        }
        User newUser = new User(username, password);
        users.put(username, newUser);
        return newUser;
    }

    public User loginUser(String username, String password) {
        User user = users.get(username);
        if (user != null && user.password.equals(password)) {
            return user;
        }
        return null;
    }

    public void updatePaymentMethod(User user, String paymentMethod) {
        user.paymentMethod = paymentMethod;
    }

    public boolean processPayment(User user, Product product) {
        if (user.paymentMethod == null) {
            return false;
        }
        boolean successful = Math.random() < 0.8;
        Transaction transaction = new Transaction(user, product, successful);
        user.transactions.add(transaction);
        return successful;
    }

    public List<Product> getProducts() {
        return products;
    }

    public List<Transaction> getTransactions(User user) {
        return user.transactions;
    }
}
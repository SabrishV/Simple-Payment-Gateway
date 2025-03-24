import java.io.*;
import java.net.*;
import java.util.*;
import com.sun.net.httpserver.*;

class User {
    String username;
    String password;
    String paymentMethod;
    String cardNumber;
    String cardExpiry;
    String cardCVV;
    String bankAccount;
    String upiId;
    List<Transaction> transactions;

    User(String username, String password) {
        this.username = username;
        this.password = password;
        this.paymentMethod = null;
        this.cardNumber = null;
        this.cardExpiry = null;
        this.cardCVV = null;
        this.bankAccount = null;
        this.upiId = null;
        this.transactions = new ArrayList<>();
    }
}

class Product {
    String name;
    double price;

    Product(String name, double price) {
        this.name = name;
        this.price = price;
    }
}

class Transaction {
    User user;
    Product product;
    boolean successful;

    Transaction(User user, Product product, boolean successful) {
        this.user = user;
        this.product = product;
        this.successful = successful;
    }
}

class PaymentGateway {
    private List<User> users = new ArrayList<>();
    private List<Product> products = new ArrayList<>();
    private List<Transaction> transactions = new ArrayList<>();

    public PaymentGateway() {
        // Adding specified products
        products.add(new Product("Laptop", 1000.0));
        products.add(new Product("Smartphone", 500.0));
        products.add(new Product("Subscription", 100.0));
    }

    public User registerUser(String username, String password) {
        for (User user : users) {
            if (user.username.equals(username)) {
                return null;
            }
        }
        User newUser = new User(username, password);
        users.add(newUser);
        return newUser;
    }

    public User loginUser(String username, String password) {
        for (User user : users) {
            if (user.username.equals(username) && user.password.equals(password)) {
                return user;
            }
        }
        return null;
    }

    public List<Product> getProducts() {
        return products;
    }

    public void updatePaymentMethod(User user, String paymentMethod, Map<String, String> paymentDetails) {
        user.paymentMethod = paymentMethod;

        // Update payment details based on the payment method
        if (paymentMethod.equals("Credit Card")) {
            user.cardNumber = paymentDetails.get("cardNumber");
            user.cardExpiry = paymentDetails.get("cardExpiry");
            user.cardCVV = paymentDetails.get("cardCVV");
        } else if (paymentMethod.equals("Bank Account")) {
            user.bankAccount = paymentDetails.get("bankAccount");
        } else if (paymentMethod.equals("UPI")) {
            user.upiId = paymentDetails.get("upiId");
        }
    }

    public boolean processPayment(User user, Product product) {
        if (user.paymentMethod == null || user.paymentMethod.isEmpty()) {
            return false; // Payment method not set
        }

        // Verify payment details based on payment method
        boolean validPaymentDetails = false;

        if (user.paymentMethod.equals("Credit Card")) {
            validPaymentDetails = verifyCardDetails(user.cardNumber, user.cardExpiry, user.cardCVV);
        } else if (user.paymentMethod.equals("Bank Account")) {
            validPaymentDetails = verifyBankAccount(user.bankAccount);
        } else if (user.paymentMethod.equals("UPI")) {
            validPaymentDetails = verifyUPI(user.upiId);
        } else if (user.paymentMethod.equals("PayPal")) {
            // For PayPal, we'll assume it's always valid for this simulation
            validPaymentDetails = true;
        }

        if (!validPaymentDetails) {
            return false; // Invalid payment details
        }

        // Always succeed for testing if payment details are valid
        boolean success = true;

        Transaction transaction = new Transaction(user, product, success);
        user.transactions.add(transaction);
        transactions.add(transaction);
        return success;
    }

    // Helper methods to verify payment details
    private boolean verifyCardDetails(String cardNumber, String expiry, String cvv) {
        // Basic validation for demonstration purposes
        if (cardNumber == null || expiry == null || cvv == null) {
            return false;
        }

        // Check if card number is 16 digits
        boolean validCardNumber = cardNumber.replaceAll("\\s", "").matches("\\d{16}");

        // Check if expiry is in MM/YY format
        boolean validExpiry = expiry.matches("\\d{2}/\\d{2}");

        // Check if CVV is 3 digits
        boolean validCVV = cvv.matches("\\d{3}");

        return validCardNumber && validExpiry && validCVV;
    }

    private boolean verifyBankAccount(String accountNumber) {
        // Basic validation for demonstration purposes
        if (accountNumber == null) {
            return false;
        }

        // Check if account number is between 8-12 digits
        return accountNumber.replaceAll("\\s", "").matches("\\d{8,12}");
    }

    private boolean verifyUPI(String upiId) {
        // Basic validation for demonstration purposes
        if (upiId == null) {
            return false;
        }

        // Check if UPI ID is in the format username@provider
        return upiId.matches("[a-zA-Z0-9.]+@[a-zA-Z0-9]+");
    }

    public List<Transaction> getTransactions(User user) {
        return user.transactions;
    }
}

class Main {
    private static PaymentGateway gateway = new PaymentGateway();

    public static void main(String[] args) throws IOException {
        // Create HTTP server on port 8080
        HttpServer server = HttpServer.create(new InetSocketAddress(8080), 0);

        // Set CORS headers
        server.createContext("/api/register", exchange -> {
            setCorsHeaders(exchange);
            if (exchange.getRequestMethod().equals("OPTIONS")) {
                exchange.sendResponseHeaders(204, -1);
                return;
            }
            handleRegister(exchange);
        });

        server.createContext("/api/login", exchange -> {
            setCorsHeaders(exchange);
            if (exchange.getRequestMethod().equals("OPTIONS")) {
                exchange.sendResponseHeaders(204, -1);
                return;
            }
            handleLogin(exchange);
        });

        server.createContext("/api/products", exchange -> {
            setCorsHeaders(exchange);
            if (exchange.getRequestMethod().equals("OPTIONS")) {
                exchange.sendResponseHeaders(204, -1);
                return;
            }
            handleGetProducts(exchange);
        });

        server.createContext("/api/payment", exchange -> {
            setCorsHeaders(exchange);
            if (exchange.getRequestMethod().equals("OPTIONS")) {
                exchange.sendResponseHeaders(204, -1);
                return;
            }
            handlePayment(exchange);
        });

        server.createContext("/api/payment-method", exchange -> {
            setCorsHeaders(exchange);
            if (exchange.getRequestMethod().equals("OPTIONS")) {
                exchange.sendResponseHeaders(204, -1);
                return;
            }
            handleUpdatePaymentMethod(exchange);
        });

        server.setExecutor(null);
        server.createContext("/", exchange -> {
            setCorsHeaders(exchange);
            String response = "Welcome to SimplePayment Gateway!";
            exchange.sendResponseHeaders(200, response.length());
            OutputStream os = exchange.getResponseBody();
            os.write(response.getBytes());
            os.close();
        });

        server.start();
        System.out.println("Server started on port 8080");
    }

    private static void setCorsHeaders(HttpExchange exchange) {
        exchange.getResponseHeaders().add("Access-Control-Allow-Origin", "*");
        exchange.getResponseHeaders().add("Access-Control-Allow-Methods", "GET, POST, OPTIONS");
        exchange.getResponseHeaders().add("Access-Control-Allow-Headers", "Content-Type");
    }

    private static Map<String, String> parseRequestBody(String body) {
        Map<String, String> result = new HashMap<>();
        try {
            // Remove leading/trailing whitespace
            body = body.trim();
            // Remove the curly braces
            body = body.substring(1, body.length() - 1);
            // Split by commas, but not within quotes
            String[] pairs = body.split(",(?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)");

            for (String pair : pairs) {
                // Split by :, but only first occurrence
                String[] keyValue = pair.split(":", 2);
                if (keyValue.length == 2) {
                    // Remove quotes and trim
                    String key = keyValue[0].replaceAll("\"", "").trim();
                    String value = keyValue[1].replaceAll("\"", "").trim();
                    result.put(key, value);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }

    private static String createJsonResponse(boolean success, String message, Object data) {
        StringBuilder json = new StringBuilder();
        json.append("{");
        json.append("\"success\":").append(success);

        if (message != null) {
            json.append(",\"message\":\"").append(message).append("\"");
        }

        if (data != null) {
            if (data instanceof User) {
                User user = (User) data;
                json.append(",\"user\":{");
                json.append("\"username\":\"").append(user.username).append("\",");
                json.append("\"paymentMethod\":")
                        .append(user.paymentMethod != null ? "\"" + user.paymentMethod + "\"" : "null");

                // Add payment details based on payment method
                if (user.paymentMethod != null) {
                    if (user.paymentMethod.equals("Credit Card") && user.cardNumber != null) {
                        json.append(",\"cardNumber\":\"").append(maskCardNumber(user.cardNumber)).append("\"");
                        json.append(",\"cardExpiry\":\"").append(user.cardExpiry).append("\"");
                        // Don't send CVV back to client for security
                    } else if (user.paymentMethod.equals("Bank Account") && user.bankAccount != null) {
                        json.append(",\"bankAccount\":\"").append(maskAccountNumber(user.bankAccount)).append("\"");
                    } else if (user.paymentMethod.equals("UPI") && user.upiId != null) {
                        json.append(",\"upiId\":\"").append(user.upiId).append("\"");
                    }
                }

                // Add transactions
                json.append(",\"transactions\":[");
                for (int i = 0; i < user.transactions.size(); i++) {
                    Transaction t = user.transactions.get(i);
                    if (i > 0)
                        json.append(",");
                    json.append("{");
                    json.append("\"product\":{");
                    json.append("\"name\":\"").append(t.product.name).append("\",");
                    json.append("\"price\":").append(t.product.price);
                    json.append("},");
                    json.append("\"successful\":").append(t.successful);
                    json.append("}");
                }
                json.append("]");

                json.append("}");
            } else if (data instanceof List) {
                json.append(",\"products\":[");
                @SuppressWarnings("unchecked")
                List<Product> products = (List<Product>) data;
                for (int i = 0; i < products.size(); i++) {
                    Product p = products.get(i);
                    if (i > 0)
                        json.append(",");
                    json.append("{\"name\":\"").append(p.name).append("\",");
                    json.append("\"price\":").append(p.price).append("}");
                }
                json.append("]");
            }
        }

        json.append("}");
        return json.toString();
    }

    // Helper methods to mask sensitive payment information
    private static String maskCardNumber(String cardNumber) {
        if (cardNumber == null || cardNumber.length() < 4) {
            return cardNumber;
        }
        String cleanNumber = cardNumber.replaceAll("\\s", "");
        return "xxxx-xxxx-xxxx-" + cleanNumber.substring(cleanNumber.length() - 4);
    }

    private static String maskAccountNumber(String accountNumber) {
        if (accountNumber == null || accountNumber.length() < 4) {
            return accountNumber;
        }
        String cleanNumber = accountNumber.replaceAll("\\s", "");
        return "xxxxxxxx" + cleanNumber.substring(cleanNumber.length() - 4);
    }

    private static void handleRegister(HttpExchange exchange) throws IOException {
        if (!exchange.getRequestMethod().equals("POST")) {
            exchange.sendResponseHeaders(405, -1);
            return;
        }

        try {
            String requestBody = new String(exchange.getRequestBody().readAllBytes());
            Map<String, String> credentials = parseRequestBody(requestBody);

            User user = gateway.registerUser(credentials.get("username"), credentials.get("password"));

            String response;
            if (user != null) {
                response = createJsonResponse(true, null, user);
                exchange.sendResponseHeaders(200, response.getBytes().length);
            } else {
                response = createJsonResponse(false, "Username already exists", null);
                exchange.sendResponseHeaders(400, response.getBytes().length);
            }

            try (OutputStream os = exchange.getResponseBody()) {
                os.write(response.getBytes());
            }
        } catch (Exception e) {
            String response = createJsonResponse(false, "Server error", null);
            exchange.sendResponseHeaders(500, response.getBytes().length);
            try (OutputStream os = exchange.getResponseBody()) {
                os.write(response.getBytes());
            }
        }
    }

    private static void handleLogin(HttpExchange exchange) throws IOException {
        if (!exchange.getRequestMethod().equals("POST")) {
            exchange.sendResponseHeaders(405, -1);
            return;
        }

        try {
            String requestBody = new String(exchange.getRequestBody().readAllBytes());
            Map<String, String> credentials = parseRequestBody(requestBody);

            User user = gateway.loginUser(credentials.get("username"), credentials.get("password"));

            String response;
            if (user != null) {
                response = createJsonResponse(true, null, user);
                exchange.sendResponseHeaders(200, response.getBytes().length);
            } else {
                response = createJsonResponse(false, "Invalid credentials", null);
                exchange.sendResponseHeaders(401, response.getBytes().length);
            }

            try (OutputStream os = exchange.getResponseBody()) {
                os.write(response.getBytes());
            }
        } catch (Exception e) {
            e.printStackTrace();
            String response = createJsonResponse(false, "Server error", null);
            exchange.sendResponseHeaders(500, response.getBytes().length);
            try (OutputStream os = exchange.getResponseBody()) {
                os.write(response.getBytes());
            }
        }
    }

    private static void handleGetProducts(HttpExchange exchange) throws IOException {
        if (!exchange.getRequestMethod().equals("GET")) {
            exchange.sendResponseHeaders(405, -1);
            return;
        }

        try {
            String response = createJsonResponse(true, null, gateway.getProducts());
            exchange.sendResponseHeaders(200, response.getBytes().length);
            try (OutputStream os = exchange.getResponseBody()) {
                os.write(response.getBytes());
            }
        } catch (Exception e) {
            String response = createJsonResponse(false, "Server error", null);
            exchange.sendResponseHeaders(500, response.getBytes().length);
            try (OutputStream os = exchange.getResponseBody()) {
                os.write(response.getBytes());
            }
        }
    }

    private static void handlePayment(HttpExchange exchange) throws IOException {
        if (!exchange.getRequestMethod().equals("POST")) {
            exchange.sendResponseHeaders(405, -1);
            return;
        }

        try {
            String requestBody = new String(exchange.getRequestBody().readAllBytes());
            System.out.println("Payment Request Body: " + requestBody); // Debug log
            Map<String, String> paymentData = parseRequestBody(requestBody);

            User user = gateway.loginUser(paymentData.get("username"), paymentData.get("password"));

            if (user == null) {
                String response = createJsonResponse(false, "Invalid credentials", null);
                exchange.sendResponseHeaders(401, response.getBytes().length);
                try (OutputStream os = exchange.getResponseBody()) {
                    os.write(response.getBytes());
                }
                return;
            }

            // Check if payment method is set
            if (user.paymentMethod == null || user.paymentMethod.isEmpty()) {
                String response = createJsonResponse(false, "Payment method not set", null);
                exchange.sendResponseHeaders(400, response.getBytes().length);
                try (OutputStream os = exchange.getResponseBody()) {
                    os.write(response.getBytes());
                }
                return;
            }

            // Parse product data from request
            String productName = paymentData.get("productName");
            double productPrice = Double.parseDouble(paymentData.get("productPrice"));
            Product product = new Product(productName, productPrice);

            boolean success = gateway.processPayment(user, product);
            String message = success ? "Payment successful" : "Payment failed - Please try again";

            // Include user data in response to update frontend state
            String response = createJsonResponse(success, message, user);

            // Always use 200 status code since we're handling success/failure in the JSON
            // response
            exchange.sendResponseHeaders(200, response.getBytes().length);

            try (OutputStream os = exchange.getResponseBody()) {
                os.write(response.getBytes());
            }
        } catch (Exception e) {
            e.printStackTrace();
            String response = createJsonResponse(false, "Server error: " + e.getMessage(), null);
            exchange.sendResponseHeaders(500, response.getBytes().length);
            try (OutputStream os = exchange.getResponseBody()) {
                os.write(response.getBytes());
            }
        }
    }

    private static void handleUpdatePaymentMethod(HttpExchange exchange) throws IOException {
        if (!exchange.getRequestMethod().equals("POST")) {
            exchange.sendResponseHeaders(405, -1);
            return;
        }

        try {
            String requestBody = new String(exchange.getRequestBody().readAllBytes());
            System.out.println("Payment Method Update Request Body: " + requestBody); // Debug log
            Map<String, String> paymentData = parseRequestBody(requestBody);

            User user = gateway.loginUser(paymentData.get("username"), paymentData.get("password"));

            if (user == null) {
                String response = createJsonResponse(false, "Invalid credentials", null);
                exchange.sendResponseHeaders(401, response.getBytes().length);
                try (OutputStream os = exchange.getResponseBody()) {
                    os.write(response.getBytes());
                }
                return;
            }

            // Parse payment method and details from request
            String paymentMethod = paymentData.get("paymentMethod");
            Map<String, String> paymentDetails = new HashMap<>();
            paymentDetails.put("cardNumber", paymentData.get("cardNumber"));
            paymentDetails.put("cardExpiry", paymentData.get("cardExpiry"));
            paymentDetails.put("cardCVV", paymentData.get("cardCVV"));
            paymentDetails.put("bankAccount", paymentData.get("bankAccount"));
            paymentDetails.put("upiId", paymentData.get("upiId"));

            gateway.updatePaymentMethod(user, paymentMethod, paymentDetails);

            String response = createJsonResponse(true, "Payment method updated successfully", user);
            exchange.sendResponseHeaders(200, response.getBytes().length);

            try (OutputStream os = exchange.getResponseBody()) {
                os.write(response.getBytes());
            }
        } catch (Exception e) {
            e.printStackTrace();
            String response = createJsonResponse(false, "Server error: " + e.getMessage(), null);
            exchange.sendResponseHeaders(500, response.getBytes().length);
            try (OutputStream os = exchange.getResponseBody()) {
                os.write(response.getBytes());
            }
        }
    }
}
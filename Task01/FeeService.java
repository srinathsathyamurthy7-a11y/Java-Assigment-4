package Task01;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

public class FeeService {

    public static class PaymentReceipt {
        public final String regNo;
        public final double amount;
        public final String status;

        public PaymentReceipt(String regNo, double amount, String status) {
            this.regNo = regNo;
            this.amount = amount;
            this.status = status;
        }
    }

    // In-memory store allows testing & running without requiring an active MySQL
    // server
    private final Map<String, PaymentReceipt> localLedger = new HashMap<>();
    private final boolean useDatabase;

    public FeeService(boolean useDatabase) {
        this.useDatabase = useDatabase;
    }

    public PaymentReceipt processPayment(String regNo, String rawAmount) throws Exception {
        // 1. Input Validation
        if (regNo == null || regNo.trim().isEmpty()) {
            throw new IllegalArgumentException("Registration Number cannot be empty.");
        }
        String cleanReg = regNo.trim().toUpperCase();
        if (!cleanReg.matches("[A-Z0-9]{5,15}")) {
            throw new IllegalArgumentException("Invalid Registration Number format.");
        }

        double amount;
        try {
            amount = Double.parseDouble(rawAmount.trim());
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Fee amount must be a valid number.");
        }
        if (amount <= 0) {
            throw new IllegalArgumentException("Fee amount must be greater than zero.");
        }

        // 2. Simulate bank gateway confirmation delay (runs on background worker, NOT
        // EDT!)
        Thread.sleep(800);

        // 3. Safe persistence using PreparedStatement + try-with-resources
        if (useDatabase) {
            String sql = "INSERT INTO fees (reg_no, amount) VALUES (?, ?)";
            try (Connection c = DriverManager.getConnection("jdbc:mysql://localhost:3306/college_db", "root",
                    "password");
                    PreparedStatement ps = c.prepareStatement(sql)) {
                ps.setString(1, cleanReg);
                ps.setDouble(2, amount);
                ps.executeUpdate();
            } catch (SQLException e) {
                throw new Exception("Database error while recording payment: " + e.getMessage(), e);
            }
        }

        PaymentReceipt receipt = new PaymentReceipt(cleanReg, amount, "PAID");
        localLedger.put(cleanReg, receipt);
        return receipt;
    }

    public String generateReceiptText(String regNo) {
        if (regNo == null || regNo.trim().isEmpty()) {
            throw new IllegalArgumentException("Enter a Registration Number to view receipt.");
        }
        PaymentReceipt r = localLedger.get(regNo.trim().toUpperCase());
        if (r == null) {
            throw new IllegalStateException("No payment record found for: " + regNo.trim());
        }
        return String.format("=== OFFICIAL FEE RECEIPT ===%nReg No : %s%nAmount : Rs. %.2f%nStatus : %s",
                r.regNo, r.amount, r.status);
    }
}
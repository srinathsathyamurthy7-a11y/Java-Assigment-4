package Task01;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;

public class FeePaymentApp extends JFrame {

    private final JTextField regNoField = new JTextField(15);
    private final JTextField amountField = new JTextField(15);
    private final JButton payButton = new JButton("Pay");
    private final JButton clearButton = new JButton("Clear");
    private final JButton receiptButton = new JButton("Receipt");
    private final JLabel statusLabel = new JLabel("Ready.");

    private final FeeService feeService;

    public FeePaymentApp(FeeService feeService) {
        super("Student Fee Payment Portal");
        this.feeService = feeService;

        // Form Panel
        JPanel formPanel = new JPanel(new GridLayout(2, 2, 8, 8));
        formPanel.setBorder(BorderFactory.createEmptyBorder(12, 12, 6, 12));
        formPanel.add(new JLabel("Registration No:"));
        formPanel.add(regNoField);
        formPanel.add(new JLabel("Amount (Rs.):"));
        formPanel.add(amountField);

        // Button Panel
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 8));
        buttonPanel.add(payButton);
        buttonPanel.add(clearButton);
        buttonPanel.add(receiptButton);

        statusLabel.setBorder(BorderFactory.createEmptyBorder(4, 12, 8, 12));

        add(formPanel, BorderLayout.NORTH);
        add(buttonPanel, BorderLayout.CENTER);
        add(statusLabel, BorderLayout.SOUTH);

        // Separate, focused listeners (no fragile String == comparisons!)
        payButton.addActionListener(e -> handlePay());
        clearButton.addActionListener(e -> handleClear());
        receiptButton.addActionListener(e -> handleReceipt());

        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        pack();
        setLocationRelativeTo(null);
    }

    private void handlePay() {
        final String regNo = regNoField.getText();
        final String rawAmount = amountField.getText();

        payButton.setEnabled(false);
        statusLabel.setText("Processing payment with bank...");

        // Offload DB + network/sleep delay off the Event Dispatch Thread (EDT)
        SwingWorker<FeeService.PaymentReceipt, Void> worker = new SwingWorker<FeeService.PaymentReceipt, Void>() {
            @Override
            protected FeeService.PaymentReceipt doInBackground() throws Exception {
                return feeService.processPayment(regNo, rawAmount);
            }

            @Override
            protected void done() {
                payButton.setEnabled(true);
                try {
                    FeeService.PaymentReceipt receipt = get();
                    statusLabel.setText("Payment successful for " + receipt.regNo);
                    JOptionPane.showMessageDialog(FeePaymentApp.this,
                            "Payment of Rs. " + String.format("%.2f", receipt.amount) + " confirmed!",
                            "Payment Success", JOptionPane.INFORMATION_MESSAGE);
                } catch (Exception ex) {
                    Throwable cause = (ex.getCause() != null) ? ex.getCause() : ex;
                    statusLabel.setText("Error: " + cause.getMessage());
                    JOptionPane.showMessageDialog(FeePaymentApp.this,
                            cause.getMessage(), "Payment Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        };
        worker.execute();
    }

    private void handleClear() {
        regNoField.setText("");
        amountField.setText("");
        statusLabel.setText("Fields cleared.");
    }

    private void handleReceipt() {
        try {
            String receiptText = feeService.generateReceiptText(regNoField.getText());
            JOptionPane.showMessageDialog(this, receiptText, "Fee Receipt", JOptionPane.INFORMATION_MESSAGE);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Receipt Error", JOptionPane.WARNING_MESSAGE);
        }
    }

    public static void main(String[] args) {
        // Set false to run standalone without MySQL; set true when MySQL is configured
        FeeService service = new FeeService(false);
        SwingUtilities.invokeLater(() -> new FeePaymentApp(service).setVisible(true));
    }
}
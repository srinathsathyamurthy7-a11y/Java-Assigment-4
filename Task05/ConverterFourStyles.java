package Task05;

import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;

public class ConverterFourStyles {

    // STYLE (a): Frame itself implements ActionListener
    static class StyleAFrame extends JFrame implements ActionListener {
        private final JTextField amount = new JTextField(8);
        private final JComboBox<String> target = new JComboBox<>(new String[] { "USD", "EUR" });
        private final JLabel result = new JLabel("Ready");

        StyleAFrame() {
            super("Style (a): Frame implements ActionListener");
            setLayout(new FlowLayout());
            JButton btn = new JButton("Convert");
            add(new JLabel("INR:"));
            add(amount);
            add(target);
            add(btn);
            add(result);

            btn.addActionListener(this);
            amount.addActionListener(this); // Task 5.2: Enter key support

            setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
            addWindowListener(new WindowAdapter() {
                @Override
                public void windowClosing(WindowEvent e) {
                    dispose();
                }
            });
            pack();
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            performConversion(this, amount, target, result);
        }
    }

    // STYLE (b): Named Inner Class
    static class StyleBFrame extends JFrame {
        private final JTextField amount = new JTextField(8);
        private final JComboBox<String> target = new JComboBox<>(new String[] { "USD", "EUR" });
        private final JLabel result = new JLabel("Ready");

        private class ConvertHandler implements ActionListener {
            @Override
            public void actionPerformed(ActionEvent e) {
                performConversion(StyleBFrame.this, amount, target, result);
            }
        }

        StyleBFrame() {
            super("Style (b): Named Inner Class");
            setLayout(new FlowLayout());
            JButton btn = new JButton("Convert");
            add(new JLabel("INR:"));
            add(amount);
            add(target);
            add(btn);
            add(result);

            ActionListener handler = new ConvertHandler();
            btn.addActionListener(handler);
            amount.addActionListener(handler); // Task 5.2: Enter key support

            setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
            addWindowListener(new WindowAdapter() {
                @Override
                public void windowClosing(WindowEvent e) {
                    dispose();
                }
            });
            pack();
        }
    }

    // STYLE (c): Anonymous Inner Class
    static class StyleCFrame extends JFrame {
        private final JTextField amount = new JTextField(8);
        private final JComboBox<String> target = new JComboBox<>(new String[] { "USD", "EUR" });
        private final JLabel result = new JLabel("Ready");

        StyleCFrame() {
            super("Style (c): Anonymous Inner Class");
            setLayout(new FlowLayout());
            JButton btn = new JButton("Convert");
            add(new JLabel("INR:"));
            add(amount);
            add(target);
            add(btn);
            add(result);

            ActionListener anonHandler = new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    performConversion(StyleCFrame.this, amount, target, result);
                }
            };
            btn.addActionListener(anonHandler);
            amount.addActionListener(anonHandler); // Task 5.2: Enter key support

            setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
            addWindowListener(new WindowAdapter() {
                @Override
                public void windowClosing(WindowEvent e) {
                    dispose();
                }
            });
            pack();
        }
    }

    // STYLE (d): Lambda Expression
    static class StyleDFrame extends JFrame {
        private final JTextField amount = new JTextField(8);
        private final JComboBox<String> target = new JComboBox<>(new String[] { "USD", "EUR" });
        private final JLabel result = new JLabel("Ready");

        StyleDFrame() {
            super("Style (d): Lambda Expression");
            setLayout(new FlowLayout());
            JButton btn = new JButton("Convert");
            add(new JLabel("INR:"));
            add(amount);
            add(target);
            add(btn);
            add(result);

            ActionListener lambdaHandler = e -> performConversion(this, amount, target, result);
            btn.addActionListener(lambdaHandler);
            amount.addActionListener(lambdaHandler); // Task 5.2: Enter key support

            setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
            addWindowListener(new WindowAdapter() {
                @Override
                public void windowClosing(WindowEvent e) {
                    dispose();
                }
            });
            pack();
        }
    }

    // Shared conversion + Task 5.2 JOptionPane dialog output
    private static void performConversion(JFrame parent, JTextField amount, JComboBox<String> target, JLabel result) {
        try {
            double inr = Double.parseDouble(amount.getText().trim());
            String curr = (String) target.getSelectedItem();
            double rate = "USD".equals(curr) ? 0.012 : 0.011;
            String formatted = String.format("%.2f %s", inr * rate, curr);
            result.setText(formatted);
            JOptionPane.showMessageDialog(parent, "Converted Amount: " + formatted,
                    "Conversion Result", JOptionPane.INFORMATION_MESSAGE);
        } catch (NumberFormatException ex) {
            result.setText("Invalid amount");
            JOptionPane.showMessageDialog(parent, "Invalid amount entered!",
                    "Input Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            JFrame launcher = new JFrame("Task 5: Choose Event-Handling Style");
            JPanel panel = new JPanel(new GridLayout(4, 1, 8, 8));
            panel.setBorder(BorderFactory.createEmptyBorder(12, 16, 12, 16));

            JButton bA = new JButton("Launch Style (a): Frame implements ActionListener");
            JButton bB = new JButton("Launch Style (b): Named Inner Class");
            JButton bC = new JButton("Launch Style (c): Anonymous Inner Class");
            JButton bD = new JButton("Launch Style (d): Lambda Expression");

            bA.addActionListener(e -> new StyleAFrame().setVisible(true));
            bB.addActionListener(e -> new StyleBFrame().setVisible(true));
            bC.addActionListener(e -> new StyleCFrame().setVisible(true));
            bD.addActionListener(e -> new StyleDFrame().setVisible(true));

            panel.add(bA);
            panel.add(bB);
            panel.add(bC);
            panel.add(bD);
            launcher.add(panel);
            launcher.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            launcher.pack();
            launcher.setLocationRelativeTo(null);
            launcher.setVisible(true);
        });
    }
}
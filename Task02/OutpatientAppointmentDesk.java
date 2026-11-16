package Task02;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.event.ItemEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;

public class OutpatientAppointmentDesk extends JFrame {

    // In-memory department -> doctors lookup map (O(1) lookup per department
    // change)
    private final Map<String, List<String>> departmentDoctors = new LinkedHashMap<>();

    // UI Components
    private final JTextField patientIdField = new JTextField(15);
    private final JComboBox<String> departmentCombo = new JComboBox<>();
    private final JComboBox<String> doctorCombo = new JComboBox<>();
    private final JComboBox<String> slotCombo = new JComboBox<>(new String[] {
            "09:00 AM - 09:30 AM",
            "10:00 AM - 10:30 AM",
            "11:30 AM - 12:00 PM",
            "02:30 PM - 03:00 PM",
            "04:00 PM - 04:30 PM"
    });
    private final JButton bookButton = new JButton("Book Appointment");
    private final JButton resetButton = new JButton("Reset");
    private final JLabel statusLabel = new JLabel("Enter Patient ID and select department.");

    // Tracks whether a booking is currently in progress (unsaved form input)
    private boolean bookingInProgress = false;

    public OutpatientAppointmentDesk() {
        super("Hospital Outpatient Appointment Desk");
        loadDepartmentData();

        // Form Panel (GridLayout 4 rows x 2 cols)
        JPanel formPanel = new JPanel(new GridLayout(4, 2, 10, 10));
        formPanel.setBorder(BorderFactory.createEmptyBorder(15, 15, 10, 15));

        formPanel.add(new JLabel("Patient ID:"));
        formPanel.add(patientIdField);

        formPanel.add(new JLabel("Department:"));
        for (String dept : departmentDoctors.keySet()) {
            departmentCombo.addItem(dept);
        }
        formPanel.add(departmentCombo);

        formPanel.add(new JLabel("Doctor:"));
        formPanel.add(doctorCombo);

        formPanel.add(new JLabel("Time Slot:"));
        formPanel.add(slotCombo);

        // Action Panel (FlowLayout)
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 12, 8));
        buttonPanel.add(bookButton);
        buttonPanel.add(resetButton);

        // Status Bar
        statusLabel.setBorder(BorderFactory.createEmptyBorder(6, 15, 10, 15));
        statusLabel.setForeground(new Color(0, 90, 160));

        add(formPanel, BorderLayout.NORTH);
        add(buttonPanel, BorderLayout.CENTER);
        add(statusLabel, BorderLayout.SOUTH);

        // Populate doctors for initial department
        refreshDoctorsForDepartment((String) departmentCombo.getSelectedItem());

        // Event 1: Department selection changed -> refresh doctor list
        departmentCombo.addItemListener(e -> {
            if (e.getStateChange() == ItemEvent.SELECTED) {
                bookingInProgress = true;
                refreshDoctorsForDepartment((String) e.getItem());
            }
        });

        // Mark booking in progress when user types into Patient ID
        patientIdField.addKeyListener(new java.awt.event.KeyAdapter() {
            @Override
            public void keyTyped(java.awt.event.KeyEvent e) {
                bookingInProgress = true;
            }
        });

        // Event 2: Book button clicked
        bookButton.addActionListener(e -> validateAndBook());

        // Event 3: Reset button clicked
        resetButton.addActionListener(e -> resetForm());

        // Event 4: Window closing confirmation if booking is in progress
        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowOpened(WindowEvent e) {
                System.out.println("[TRACE] windowOpened() invoked -> Desk ready.");
            }

            @Override
            public void windowClosing(WindowEvent e) {
                System.out.println("[TRACE] windowClosing() invoked -> bookingInProgress=" + bookingInProgress);
                if (bookingInProgress && !patientIdField.getText().trim().isEmpty()) {
                    int choice = JOptionPane.showConfirmDialog(
                            OutpatientAppointmentDesk.this,
                            "An appointment booking is currently in progress. Discard and close?",
                            "Confirm Exit",
                            JOptionPane.YES_NO_OPTION,
                            JOptionPane.WARNING_MESSAGE);
                    if (choice != JOptionPane.YES_OPTION) {
                        return;
                    }
                }
                dispose();
            }

            @Override
            public void windowClosed(WindowEvent e) {
                System.out.println("[TRACE] windowClosed() invoked -> Resources released.");
            }
        });

        pack();
        setLocationRelativeTo(null);
    }

    private void loadDepartmentData() {
        departmentDoctors.put("Cardiology", Arrays.asList("Dr. Meera Nair", "Dr. Arjun Rao"));
        departmentDoctors.put("Neurology", Arrays.asList("Dr. Karthik Subramanian", "Dr. Priya Menon"));
        departmentDoctors.put("Orthopedics", Arrays.asList("Dr. Vikram Das", "Dr. Sneha Iyer"));
        departmentDoctors.put("Dermatology", Arrays.asList("Dr. Ananya Sharma"));
    }

    private void refreshDoctorsForDepartment(String department) {
        doctorCombo.removeAllItems();
        List<String> doctors = departmentDoctors.getOrDefault(department, Collections.emptyList());
        for (String doc : doctors) {
            doctorCombo.addItem(doc);
        }
        statusLabel.setForeground(new Color(0, 90, 160));
        statusLabel.setText("Loaded " + doctors.size() + " doctor(s) for " + department + ".");
    }

    private void validateAndBook() {
        String patientId = patientIdField.getText().trim().toUpperCase();
        String dept = (String) departmentCombo.getSelectedItem();
        String doctor = (String) doctorCombo.getSelectedItem();
        String slot = (String) slotCombo.getSelectedItem();

        if (patientId.isEmpty() || !patientId.matches("PAT[0-9]{3,6}")) {
            statusLabel.setForeground(Color.RED);
            statusLabel.setText("Error: Enter a valid Patient ID (e.g., PAT101).");
            JOptionPane.showMessageDialog(this,
                    "Invalid Patient ID! Use format PAT followed by 3-6 digits (e.g., PAT101).",
                    "Validation Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        if (dept == null || doctor == null || slot == null) {
            statusLabel.setForeground(Color.RED);
            statusLabel.setText("Error: Select department, doctor, and time slot.");
            return;
        }

        bookingInProgress = false;
        statusLabel.setForeground(new Color(0, 128, 0));
        statusLabel.setText("Confirmed: " + patientId + " booked with " + doctor + " at " + slot);

        JOptionPane.showMessageDialog(this,
                String.format(
                        "Appointment Confirmed!%n%nPatient ID : %s%nDepartment : %s%nDoctor     : %s%nTime Slot  : %s",
                        patientId, dept, doctor, slot),
                "Booking Confirmation", JOptionPane.INFORMATION_MESSAGE);
    }

    private void resetForm() {
        patientIdField.setText("");
        departmentCombo.setSelectedIndex(0);
        slotCombo.setSelectedIndex(0);
        bookingInProgress = false;
        statusLabel.setForeground(new Color(0, 90, 160));
        statusLabel.setText("Form reset. Ready for next patient.");
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new OutpatientAppointmentDesk().setVisible(true));
    }
}
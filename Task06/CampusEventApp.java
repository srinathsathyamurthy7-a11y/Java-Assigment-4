package Task06;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.util.ArrayList;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;
import javax.swing.table.DefaultTableModel;

public class CampusEventApp extends JFrame {

    // Service Layer encapsulated cleanly inside the application
    static class RegistrationService {
        private final RegistrationDAO dao;

        RegistrationService(RegistrationDAO dao) {
            this.dao = dao;
        }

        void registerStudent(String studentId, String studentName, String dept, String eventName) throws Exception {
            if (studentId == null || !studentId.trim().matches("[0-9]{6,12}")) {
                throw new IllegalArgumentException("Invalid Student ID: Enter 6 to 12 digits (e.g., 192511064).");
            }
            if (studentName == null || studentName.trim().length() < 2) {
                throw new IllegalArgumentException("Invalid Student Name: Name cannot be empty.");
            }
            if (eventName == null || eventName.trim().isEmpty()) {
                throw new IllegalArgumentException("Please select an event.");
            }
            dao.insertRegistration(studentId.trim(), studentName.trim(), dept, eventName);
        }

        List<RegistrationRecord> getFilteredPage(String eventFilter, int page, int pageSize) throws Exception {
            int offset = page * pageSize;
            return dao.fetchRegistrations(eventFilter, pageSize, offset);
        }

        void exportCurrentList(String filePath, List<RegistrationRecord> records) throws Exception {
            if (records.isEmpty()) {
                throw new IllegalStateException("No registrations available to export.");
            }
            dao.exportToCsv(filePath, records);
        }
    }

    private final RegistrationService service;
    private List<RegistrationRecord> currentDisplayedRows = new ArrayList<>();
    private int currentPage = 0;
    private static final int PAGE_SIZE = 10;

    // Form Components
    private final JTextField studentIdField = new JTextField(12);
    private final JTextField studentNameField = new JTextField(15);
    private final JComboBox<String> deptCombo = new JComboBox<>(new String[] { "CSE", "ECE", "EEE", "IT", "MECH" });
    private final JComboBox<String> eventCombo = new JComboBox<>(
            new String[] { "Hackathon 2026", "Classical Dance", "Battle of Bands", "RoboWars", "Short Film Fest" });
    private final JButton registerButton = new JButton("Register Student");

    // Committee Filter & Table Components
    private final JComboBox<String> filterEventCombo = new JComboBox<>(
            new String[] { "All Events", "Hackathon 2026", "Classical Dance", "Battle of Bands", "RoboWars",
                    "Short Film Fest" });
    private final JButton refreshButton = new JButton("Refresh");
    private final JButton exportButton = new JButton("Export CSV");

    private final DefaultTableModel tableModel = new DefaultTableModel(
            new String[] { "Reg ID", "Student ID", "Student Name", "Department", "Event" }, 0) {
        @Override
        public boolean isCellEditable(int row, int column) {
            return false;
        }
    };
    private final JTable registrationTable = new JTable(tableModel);
    private final JLabel statusLabel = new JLabel("Ready.");

    public CampusEventApp(RegistrationService service) {
        super("SIMATS Cultural Committee - Event Registration Manager");
        this.service = service;

        // 1. JMenuBar (Stage 2 requirement)
        setJMenuBar(buildMenuBar());

        // 2. Left Panel: Student Registration Form
        JPanel formPanel = new JPanel(new GridLayout(5, 2, 8, 10));
        formPanel.setBorder(BorderFactory.createTitledBorder("Student Event Registration"));
        formPanel.add(new JLabel("Student ID:"));
        formPanel.add(studentIdField);
        formPanel.add(new JLabel("Student Name:"));
        formPanel.add(studentNameField);
        formPanel.add(new JLabel("Department:"));
        formPanel.add(deptCombo);
        formPanel.add(new JLabel("Select Event:"));
        formPanel.add(eventCombo);
        formPanel.add(new JLabel(""));
        formPanel.add(registerButton);

        // 3. Center/Right Panel: Committee Table + Filter Bar
        JPanel tableContainer = new JPanel(new BorderLayout(6, 6));
        tableContainer.setBorder(BorderFactory.createTitledBorder("Committee View - Registered Participants"));

        JPanel filterBar = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 4));
        filterBar.add(new JLabel("Filter by Event:"));
        filterBar.add(filterEventCombo);
        filterBar.add(refreshButton);
        filterBar.add(exportButton);

        registrationTable.setRowHeight(24);
        tableContainer.add(filterBar, BorderLayout.NORTH);
        tableContainer.add(new JScrollPane(registrationTable), BorderLayout.CENTER);

        statusLabel.setBorder(BorderFactory.createEmptyBorder(5, 10, 6, 10));
        statusLabel.setForeground(new Color(0, 90, 160));

        add(formPanel, BorderLayout.WEST);
        add(tableContainer, BorderLayout.CENTER);
        add(statusLabel, BorderLayout.SOUTH);

        // Event Listeners
        registerButton.addActionListener(e -> handleRegister());
        filterEventCombo.addActionListener(e -> {
            currentPage = 0;
            loadTableAsync();
        });
        refreshButton.addActionListener(e -> loadTableAsync());
        exportButton.addActionListener(e -> handleExportCsv());

        setSize(860, 440);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        // Load initial table off the Event-Dispatch Thread
        loadTableAsync();
    }

    private JMenuBar buildMenuBar() {
        JMenuBar menuBar = new JMenuBar();

        JMenu fileMenu = new JMenu("File");
        JMenuItem exportItem = new JMenuItem("Export Filtered List (CSV)");
        JMenuItem exitItem = new JMenuItem("Exit");
        exportItem.addActionListener(e -> handleExportCsv());
        exitItem.addActionListener(e -> dispose());
        fileMenu.add(exportItem);
        fileMenu.addSeparator();
        fileMenu.add(exitItem);

        JMenu viewMenu = new JMenu("View");
        JMenuItem reloadItem = new JMenuItem("Reload Registrations");
        reloadItem.addActionListener(e -> loadTableAsync());
        viewMenu.add(reloadItem);

        JMenu helpMenu = new JMenu("Help");
        JMenuItem aboutItem = new JMenuItem("About");
        aboutItem.addActionListener(e -> JOptionPane.showMessageDialog(this,
                "Campus Event Registration System\nLayered Swing + JDBC Architecture (CO4)",
                "About", JOptionPane.INFORMATION_MESSAGE));
        helpMenu.add(aboutItem);

        menuBar.add(fileMenu);
        menuBar.add(viewMenu);
        menuBar.add(helpMenu);
        return menuBar;
    }

    private void handleRegister() {
        final String id = studentIdField.getText();
        final String name = studentNameField.getText();
        final String dept = (String) deptCombo.getSelectedItem();
        final String event = (String) eventCombo.getSelectedItem();

        registerButton.setEnabled(false);
        statusLabel.setForeground(new Color(0, 90, 160));
        statusLabel.setText("Submitting registration...");

        // Keep database work off the Event-Dispatch Thread
        SwingWorker<Void, Void> worker = new SwingWorker<Void, Void>() {
            @Override
            protected Void doInBackground() throws Exception {
                service.registerStudent(id, name, dept, event);
                return null;
            }

            @Override
            protected void done() {
                registerButton.setEnabled(true);
                try {
                    get();
                    studentIdField.setText("");
                    studentNameField.setText("");
                    statusLabel.setForeground(new Color(0, 128, 0));
                    statusLabel.setText("Registered " + id.trim() + " for " + event + ".");
                    JOptionPane.showMessageDialog(CampusEventApp.this,
                            "Registration Confirmed for " + name.trim() + " (" + event + ")!",
                            "Registration Success", JOptionPane.INFORMATION_MESSAGE);
                    loadTableAsync();
                } catch (Exception ex) {
                    Throwable cause = (ex.getCause() != null) ? ex.getCause() : ex;
                    statusLabel.setForeground(Color.RED);
                    statusLabel.setText("Error: " + cause.getMessage());
                    JOptionPane.showMessageDialog(CampusEventApp.this,
                            cause.getMessage(), "Registration Rejected", JOptionPane.ERROR_MESSAGE);
                }
            }
        };
        worker.execute();
    }

    private void loadTableAsync() {
        final String selectedFilter = (String) filterEventCombo.getSelectedItem();
        refreshButton.setEnabled(false);
        statusLabel.setForeground(new Color(0, 90, 160));
        statusLabel.setText("Loading registrations for '" + selectedFilter + "'...");

        SwingWorker<List<RegistrationRecord>, Void> worker = new SwingWorker<List<RegistrationRecord>, Void>() {
            @Override
            protected List<RegistrationRecord> doInBackground() throws Exception {
                return service.getFilteredPage(selectedFilter, currentPage, PAGE_SIZE);
            }

            @Override
            protected void done() {
                refreshButton.setEnabled(true);
                try {
                    currentDisplayedRows = get();
                    tableModel.setRowCount(0);
                    for (RegistrationRecord r : currentDisplayedRows) {
                        tableModel.addRow(
                                new Object[] { r.regId, r.studentId, r.studentName, r.department, r.eventName });
                    }
                    statusLabel.setForeground(new Color(0, 128, 0));
                    statusLabel.setText(
                            "Loaded " + currentDisplayedRows.size() + " registration(s) [" + selectedFilter + "].");
                } catch (Exception ex) {
                    Throwable cause = (ex.getCause() != null) ? ex.getCause() : ex;
                    statusLabel.setForeground(Color.RED);
                    statusLabel.setText("Load failed: " + cause.getMessage());
                }
            }
        };
        worker.execute();
    }

    private void handleExportCsv() {
        try {
            String outputFile = "Task06/registrations_export.csv";
            service.exportCurrentList(outputFile, currentDisplayedRows);
            statusLabel.setForeground(new Color(0, 128, 0));
            statusLabel.setText("Exported " + currentDisplayedRows.size() + " record(s) to " + outputFile);
            JOptionPane.showMessageDialog(this, "Exported successfully to:\n" + outputFile,
                    "CSV Export Complete", JOptionPane.INFORMATION_MESSAGE);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Export Error", JOptionPane.WARNING_MESSAGE);
        }
    }

    public static void main(String[] args) {
        // Set false for standalone execution; set true when MySQL server + Connector/J
        // are active
        RegistrationDAO dao = new RegistrationDAO(false);
        RegistrationService service = new RegistrationService(dao);
        SwingUtilities.invokeLater(() -> new CampusEventApp(service).setVisible(true));
    }
}
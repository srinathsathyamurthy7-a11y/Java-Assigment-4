package Task06;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.SQLIntegrityConstraintViolationException;
import java.util.ArrayList;
import java.util.List;

public class RegistrationDAO {

    private static final String URL = "jdbc:mysql://localhost:3306/campus_events_db?useSSL=false&serverTimezone=UTC";
    private static final String USER = "root";
    private static final String PWD = "password";

    private final boolean useMySQL;
    private final List<RegistrationRecord> inMemoryStore = new ArrayList<>();
    private int nextId = 1;

    public RegistrationDAO(boolean useMySQL) {
        this.useMySQL = useMySQL;
        if (!useMySQL) {
            // Seed initial records matching schema.sql so standalone runs work identically
            inMemoryStore.add(new RegistrationRecord(nextId++, "192511001", "Aarav Krishnan", "CSE", "Hackathon 2026"));
            inMemoryStore.add(new RegistrationRecord(nextId++, "192511002", "Divya Nair", "ECE", "Classical Dance"));
            inMemoryStore.add(new RegistrationRecord(nextId++, "192511003", "Rohan Sharma", "IT", "Battle of Bands"));
        }
    }

    public void insertRegistration(String studentId, String studentName, String department, String eventName)
            throws Exception {
        // Simulate realistic database I/O delay to prove SwingWorker keeps UI
        // responsive
        Thread.sleep(300);

        if (useMySQL) {
            String sql = "INSERT INTO event_registrations (student_id, student_name, department, event_name) VALUES (?, ?, ?, ?)";
            try (Connection conn = DriverManager.getConnection(URL, USER, PWD);
                    PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setString(1, studentId);
                ps.setString(2, studentName);
                ps.setString(3, department);
                ps.setString(4, eventName);
                ps.executeUpdate();
            } catch (SQLIntegrityConstraintViolationException dup) {
                throw new IllegalStateException("Duplicate registration: Student " + studentId
                        + " is already registered for " + eventName + ".");
            } catch (SQLException ex) {
                throw new Exception("Database error: " + ex.getMessage(), ex);
            }
        } else {
            // Enforce UNIQUE(student_id, event_name) constraint in standalone mode
            for (RegistrationRecord r : inMemoryStore) {
                if (r.studentId.equalsIgnoreCase(studentId) && r.eventName.equalsIgnoreCase(eventName)) {
                    throw new IllegalStateException("Duplicate registration: Student " + studentId
                            + " is already registered for " + eventName + ".");
                }
            }
            inMemoryStore.add(new RegistrationRecord(nextId++, studentId, studentName, department, eventName));
        }
    }

    public List<RegistrationRecord> fetchRegistrations(String eventFilter, int limit, int offset) throws Exception {
        Thread.sleep(200);
        List<RegistrationRecord> results = new ArrayList<>();

        if (useMySQL) {
            boolean allEvents = (eventFilter == null || "All Events".equals(eventFilter));
            String sql = allEvents
                    ? "SELECT reg_id, student_id, student_name, department, event_name FROM event_registrations ORDER BY reg_id LIMIT ? OFFSET ?"
                    : "SELECT reg_id, student_id, student_name, department, event_name FROM event_registrations WHERE event_name = ? ORDER BY reg_id LIMIT ? OFFSET ?";
            try (Connection conn = DriverManager.getConnection(URL, USER, PWD);
                    PreparedStatement ps = conn.prepareStatement(sql)) {
                if (allEvents) {
                    ps.setInt(1, limit);
                    ps.setInt(2, offset);
                } else {
                    ps.setString(1, eventFilter);
                    ps.setInt(2, limit);
                    ps.setInt(3, offset);
                }
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        results.add(new RegistrationRecord(
                                rs.getInt("reg_id"),
                                rs.getString("student_id"),
                                rs.getString("student_name"),
                                rs.getString("department"),
                                rs.getString("event_name")));
                    }
                }
            } catch (SQLException ex) {
                throw new Exception("Failed to load registrations: " + ex.getMessage(), ex);
            }
        } else {
            List<RegistrationRecord> filtered = new ArrayList<>();
            for (RegistrationRecord r : inMemoryStore) {
                if (eventFilter == null || "All Events".equals(eventFilter) || r.eventName.equals(eventFilter)) {
                    filtered.add(r);
                }
            }
            int end = Math.min(offset + limit, filtered.size());
            for (int i = offset; i < end; i++) {
                results.add(filtered.get(i));
            }
        }
        return results;
    }

    public void exportToCsv(String filePath, List<RegistrationRecord> rows) throws IOException {
        try (PrintWriter out = new PrintWriter(new FileWriter(filePath))) {
            out.println("RegID,StudentID,StudentName,Department,EventName");
            for (RegistrationRecord r : rows) {
                out.printf("%d,%s,\"%s\",%s,\"%s\"%n",
                        r.regId, r.studentId, r.studentName, r.department, r.eventName);
            }
        }
    }
}
package Task06;

public class RegistrationRecord {
    public final int regId;
    public final String studentId;
    public final String studentName;
    public final String department;
    public final String eventName;

    public RegistrationRecord(int regId, String studentId, String studentName, String department, String eventName) {
        this.regId = regId;
        this.studentId = studentId;
        this.studentName = studentName;
        this.department = department;
        this.eventName = eventName;
    }
}
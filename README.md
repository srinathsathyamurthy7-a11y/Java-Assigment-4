# CSA0904 – Java Assignment 4 (CO4)
## Event-Driven GUI Design (AWT, Swing & GUI-JDBC Integration)

### Student Details
- **Name:** PURUSHOTHAMAN.V
- **Register Number:** 192511064
- **Department:** Computer Science and Engineering

### Development Environment
- **IDE:** Visual Studio Code (VS Code)
- **Programming Language:** Java
- **JDK:** Java SE Development Kit 8 (JDK 1.8)
- **Operating System:** Windows
- **Terminal:** VS Code Integrated Terminal (PowerShell)

### Java Version
```bash
java -version
javac -version
```
**Java Version:** Java SE Development Kit 8 (JDK 1.8)

### Compilation and Execution
All Java programs were compiled and executed from the root repository folder:

```bash
# Task 1 - Student Fee Payment Layered GUI Application
javac Task01/FeeService.java Task01/FeePaymentApp.java
java -cp . Task01.FeePaymentApp

# Task 2 - Hospital Outpatient Appointment Desk
javac Task02/OutpatientAppointmentDesk.java
java -cp . Task02.OutpatientAppointmentDesk

# Task 3 - Campus Library Search Screen (Layout Managers)
javac Task03/LibrarySearchScreen.java
java -cp . Task03.LibrarySearchScreen

# Task 4 - Event-Handling Forensics (DrawPad)
javac Task04/DrawPad.java
java -cp . Task04.DrawPad

# Task 5 - Currency Converter Across Four Event-Handling Styles
javac Task05/Converter.java Task05/ConverterFourStyles.java
java -cp . Task05.Converter
java -cp . Task05.ConverterFourStyles

# Task 6 - Campus Event Registration Desktop Application (Swing + JDBC)
javac Task06/RegistrationRecord.java Task06/RegistrationDAO.java Task06/CampusEventApp.java
java -cp . Task06.CampusEventApp
```

### Assignment Results

| Task | Description | Status | Key Outcome / Finding |
|---|---|---|---|
| Task 1 | Student Fee Payment Window Diagnosis & Layered Redesign | Completed | Separated `FeeService` from Swing UI; moved DB/sleep off EDT via `SwingWorker` |
| Task 2 | Hospital Outpatient Appointment Desk | Completed | Built dynamic department-to-doctor dropdown filtering and exit confirmation guard |
| Task 3 | Campus Library Search Screen (`BorderLayout` + `FlowLayout`) | Completed | Center `JTable` expands smoothly across window sizes while toolbars stay fixed height |
| Task 4 | Event-Handling Forensics (`DrawPad`) | Completed | Fixed `mouseClicked` override and retained dots across minimize/restore via `paint()` |
| Task 5 | Currency Converter (4 Event-Handling Styles) | Completed | Compared Frame-listener, Named Inner, Anonymous Inner, and Lambda styles |
| Task 6 | Campus Event Registration App (Swing + JDBC) | Completed | Layered UI/Service/DAO app with `JMenuBar`, duplicate rejection, filtering, and CSV export |

### Screenshots & SQL Script
- Screenshots demonstrating every tested interaction are stored inside `Task01` through `Task06`.
- MySQL database schema for Task 6 is located at `Task06/schema.sql`.

---
**Submitted by:** PURUSHOTHAMAN.V  
**Register Number:** 192511064

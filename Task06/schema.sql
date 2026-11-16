CREATE DATABASE IF NOT EXISTS campus_events_db;

USE campus_events_db;

CREATE TABLE IF NOT EXISTS event_registrations (
    reg_id INT AUTO_INCREMENT PRIMARY KEY,
    student_id VARCHAR(20) NOT NULL,
    student_name VARCHAR(100) NOT NULL,
    department VARCHAR(50) NOT NULL,
    event_name VARCHAR(80) NOT NULL,
    registered_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uq_student_event UNIQUE (student_id, event_name)
);

-- Seed sample records for verification
INSERT IGNORE INTO event_registrations (student_id, student_name, department, event_name)
VALUES
    (
        '192511001',
        'Aarav Krishnan',
        'CSE',
        'Hackathon 2026'
    ),
    (
        '192511002',
        'Divya Nair',
        'ECE',
        'Classical Dance'
    ),
    (
        '192511003',
        'Rohan Sharma',
        'IT',
        'Battle of Bands'
    );
# Intelligent Complaint Management & Priority Routing System

This is a complete Java Swing frontend version of the console-based ICMPRS project. It keeps the same OOP and Java/JDBC ideas from the PPT, but adds a working graphical interface.

## Features

- Add complaints with validation.
- Automatic priority calculation using urgency + impact scoring.
- Automatic department routing to Finance, IT Support, Logistics, Product Team, or General Support.
- View all complaints sorted by priority.
- Search complaint by ID.
- Update complaint status.
- Delete complaints.
- Dashboard cards for total, high priority, open, and resolved complaints.
- MySQL storage when available, with local file fallback for easy demo.

## Files

- `Main.java` - Swing frontend.
- `ComplaintManager.java` - connects frontend to storage operations.
- `ComplaintRepository.java` - abstraction for data layer.
- `JdbcComplaintRepository.java` - MySQL/JDBC implementation.
- `FileComplaintRepository.java` - fallback storage in `data/complaints.tsv`.
- `Complaint.java`, `BaseComplaint.java`, `User.java`, `Admin.java`, `Customer.java` - OOP model classes.
- `PriorityCalculator.java` - intelligent scoring logic.
- `DepartmentRouter.java` - automatic routing logic.
- `DBConnection.java` - database configuration.

## MySQL Setup

Run this in MySQL if you want database storage:

```sql
CREATE DATABASE IF NOT EXISTS complaintdb;
USE complaintdb;

CREATE TABLE IF NOT EXISTS complaints (
    id INT PRIMARY KEY,
    description VARCHAR(500) NOT NULL,
    priority INT NOT NULL,
    priority_level VARCHAR(20) NOT NULL,
    department VARCHAR(80) NOT NULL,
    status VARCHAR(50) NOT NULL,
    high_priority_flag INT NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);
```

Default DB settings are in `DBConnection.java`:

```java
jdbc:mysql://localhost:3306/complaintdb
root
Tanu1234#
```

You can also override them without editing code:

```powershell
$env:ICMPRS_DB_URL="jdbc:mysql://localhost:3306/complaintdb"
$env:ICMPRS_DB_USER="root"
$env:ICMPRS_DB_PASSWORD="your_password"
```

## Run

Compile:

```powershell
javac *.java
```

Run:

```powershell
java Main
```

If MySQL or the MySQL connector is unavailable, the app still runs using local file storage.
** updated by khushi**
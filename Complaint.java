import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class Complaint extends BaseComplaint {
    private final int priority;
    private final String priorityLevel;
    private final String department;
    private String status;
    private final LocalDateTime createdAt;

    public Complaint(int id, String description) {
        this(id, description, PriorityCalculator.calculate(description),
                DepartmentRouter.detectDepartment(description), "Open", LocalDateTime.now());
    }

    public Complaint(int id, String description, int priority, String department, String status, LocalDateTime createdAt) {
        super(id, description);
        this.priority = Math.max(1, Math.min(10, priority));
        this.priorityLevel = PriorityCalculator.getPriorityLevel(this.priority);
        this.department = department == null || department.isBlank() ? DepartmentRouter.detectDepartment(description) : department;
        this.status = status == null || status.isBlank() ? "Open" : status;
        this.createdAt = createdAt == null ? LocalDateTime.now() : createdAt;
    }

    public int getPriority() {
        return priority;
    }

    public String getPriorityLevel() {
        return priorityLevel;
    }

    public String getDepartment() {
        return department;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        if (status != null && !status.isBlank()) {
            this.status = status;
        }
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public boolean isHighPriority() {
        return priority >= 7;
    }

    public String getFormattedCreatedAt() {
        return createdAt.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));
    }

    @Override
    public String getSummary() {
        return "#" + getId() + " | " + priorityLevel + " | " + department + " | " + status;
    }
}

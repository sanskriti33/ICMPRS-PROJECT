import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class JdbcComplaintRepository implements ComplaintRepository {
    public JdbcComplaintRepository() throws SQLException {
        initializeDatabase();
    }

    @Override
    public void add(Complaint complaint) throws SQLException {
        String sql = """
                INSERT INTO complaints
                (id, description, priority, priority_level, department, status, high_priority_flag, created_at)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?)
                """;

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, complaint.getId());
            statement.setString(2, complaint.getDescription());
            statement.setInt(3, complaint.getPriority());
            statement.setString(4, complaint.getPriorityLevel());
            statement.setString(5, complaint.getDepartment());
            statement.setString(6, complaint.getStatus());
            statement.setInt(7, complaint.isHighPriority() ? 1 : 0);
            statement.setTimestamp(8, Timestamp.valueOf(complaint.getCreatedAt()));
            statement.executeUpdate();
        }
    }

    @Override
    public List<Complaint> findAll() throws SQLException {
        String sql = "SELECT * FROM complaints ORDER BY priority DESC, created_at DESC";
        List<Complaint> complaints = new ArrayList<>();

        try (Connection connection = DBConnection.getConnection();
             Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery(sql)) {
            while (resultSet.next()) {
                complaints.add(mapComplaint(resultSet));
            }
        }
        return complaints;
    }

    @Override
    public Optional<Complaint> findById(int id) throws SQLException {
        String sql = "SELECT * FROM complaints WHERE id = ?";

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, id);
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return Optional.of(mapComplaint(resultSet));
                }
            }
        }
        return Optional.empty();
    }

    @Override
    public boolean updateStatus(int id, String status) throws SQLException {
        String sql = "UPDATE complaints SET status = ? WHERE id = ?";

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, status);
            statement.setInt(2, id);
            return statement.executeUpdate() > 0;
        }
    }

    @Override
    public boolean delete(int id) throws SQLException {
        String sql = "DELETE FROM complaints WHERE id = ?";

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, id);
            return statement.executeUpdate() > 0;
        }
    }

    @Override
    public String getStorageName() {
        return "MySQL database";
    }

    private void initializeDatabase() throws SQLException {
        try (Connection connection = DBConnection.getConnection();
             Statement statement = connection.createStatement()) {
            statement.executeUpdate("""
                    CREATE TABLE IF NOT EXISTS complaints (
                        id INT PRIMARY KEY,
                        description VARCHAR(500) NOT NULL,
                        priority INT NOT NULL,
                        priority_level VARCHAR(20) NOT NULL,
                        department VARCHAR(80) NOT NULL,
                        status VARCHAR(50) NOT NULL,
                        high_priority_flag INT NOT NULL,
                        created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
                    )
                    """);
            addColumnIfMissing(connection, "priority_level", "VARCHAR(20) NOT NULL DEFAULT 'LOW'");
            addColumnIfMissing(connection, "department", "VARCHAR(80) NOT NULL DEFAULT 'General Support'");
        }
    }

    private void addColumnIfMissing(Connection connection, String column, String definition) throws SQLException {
        DatabaseMetaData metaData = connection.getMetaData();
        try (ResultSet resultSet = metaData.getColumns(connection.getCatalog(), null, "complaints", column)) {
            if (!resultSet.next()) {
                try (Statement statement = connection.createStatement()) {
                    statement.executeUpdate("ALTER TABLE complaints ADD COLUMN " + column + " " + definition);
                }
            }
        }
    }

    private Complaint mapComplaint(ResultSet resultSet) throws SQLException {
        Timestamp timestamp = resultSet.getTimestamp("created_at");
        LocalDateTime createdAt = timestamp == null ? LocalDateTime.now() : timestamp.toLocalDateTime();
        String department = readOptionalString(resultSet, "department");
        if (department == null || department.isBlank()) {
            department = DepartmentRouter.detectDepartment(resultSet.getString("description"));
        }

        return new Complaint(
                resultSet.getInt("id"),
                resultSet.getString("description"),
                resultSet.getInt("priority"),
                department,
                resultSet.getString("status"),
                createdAt
        );
    }

    private String readOptionalString(ResultSet resultSet, String column) {
        try {
            return resultSet.getString(column);
        } catch (SQLException ignored) {
            return null;
        }
    }
}

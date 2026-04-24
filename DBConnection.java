import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DBConnection {
    private static final String URL = getSetting("ICMPRS_DB_URL", "jdbc:mysql://localhost:3306/complaintdb");
    private static final String USER = getSetting("ICMPRS_DB_USER", "root");
    private static final String PASSWORD = getSetting("ICMPRS_DB_PASSWORD", "sanskriti03");

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(URL, USER, PASSWORD);
    }

    private static String getSetting(String key, String defaultValue) {
        String value = System.getenv(key);
        if (value == null || value.isBlank()) {
            value = System.getProperty(key);
        }
        return value == null || value.isBlank() ? defaultValue : value;
    }
}

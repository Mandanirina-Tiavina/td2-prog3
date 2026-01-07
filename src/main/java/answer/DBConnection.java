package answer;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DBConnection {
    public static Connection getDBConnection() {
        String url = System.getenv("JDBC_URL");
        String username = System.getenv("USERNAME");
        String password = System.getenv("PASSWORD");

        try {
            return DriverManager.getConnection(url, username, password);
        } catch (SQLException e) {
            e.printStackTrace();
            throw new RuntimeException("Error connecting to database");
        }
    }
}

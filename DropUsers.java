import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;

public class DropUsers {
    public static void main(String[] args) {
        try {
            Connection conn = DriverManager.getConnection("jdbc:postgresql://localhost:5432/aftercare_db", "postgres", "Monishka123#");
            Statement stmt = conn.createStatement();
            stmt.executeUpdate("DROP TABLE IF EXISTS users CASCADE");
            System.out.println("Dropped users table successfully");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

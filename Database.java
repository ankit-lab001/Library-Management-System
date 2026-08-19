import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;

public class Database {

    public static void main(String[] args) {

        try {

            Connection connection =
                    DriverManager.getConnection(
                            "jdbc:sqlite:library.db"
                    );

            Statement statement =
                    connection.createStatement();

            // ================= BOOKS TABLE =================

            String booksTable =
                    "CREATE TABLE IF NOT EXISTS books (" +
                    "id TEXT PRIMARY KEY, " +
                    "name TEXT NOT NULL, " +
                    "author TEXT NOT NULL, " +
                    "category TEXT NOT NULL, " +
                    "quantity INTEGER NOT NULL)";

            statement.executeUpdate(booksTable);

            // ================= STUDENTS TABLE =================

            String studentsTable =
                    "CREATE TABLE IF NOT EXISTS students (" +
                    "id TEXT PRIMARY KEY, " +
                    "name TEXT NOT NULL, " +
                    "phone TEXT NOT NULL)";

            statement.executeUpdate(studentsTable);

            // ================= ISSUES TABLE =================

            String issuesTable =
                    "CREATE TABLE IF NOT EXISTS issues (" +
                    "book_id TEXT NOT NULL, " +
                    "student_id TEXT NOT NULL)";

            statement.executeUpdate(issuesTable);

            System.out.println(
                    "All tables created successfully!"
            );

            connection.close();

        } catch (Exception e) {

            System.out.println(
                    "Database error!"
            );

            e.printStackTrace();
        }
    }
}
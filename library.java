import javax.swing.*;
import java.awt.*;
import java.sql.*;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;

public class library {

    // =========================================================
    // DATABASE CONNECTION
    // =========================================================

    static Connection connect() {

        try {

            return DriverManager.getConnection(
                    "jdbc:sqlite:library.db"
            );

        } catch (SQLException e) {

            JOptionPane.showMessageDialog(
                    null,
                    "Database connection failed!\n" + e.getMessage()
            );

            return null;
        }
    }

    // =========================================================
    // ARRAYS
    // =========================================================

    static ArrayList<String[]> books = new ArrayList<>();
    static ArrayList<String[]> studentList = new ArrayList<>();
    static ArrayList<String[]> issueList = new ArrayList<>();

    // =========================================================
    // DASHBOARD LABELS
    // =========================================================

    static JLabel totalBooks;
    static JLabel totalStudents;
    static JLabel issuedBooks;
    static JLabel availableBooks;

    // =========================================================
    // CREATE TABLES
    // =========================================================

    static void createTables() {

        Connection con = connect();

        if (con == null) {
            return;
        }

        try {

            Statement st = con.createStatement();

            // BOOKS
            st.executeUpdate(
                    "CREATE TABLE IF NOT EXISTS books (" +
                    "id TEXT PRIMARY KEY, " +
                    "name TEXT NOT NULL, " +
                    "author TEXT NOT NULL, " +
                    "category TEXT NOT NULL, " +
                    "quantity INTEGER NOT NULL)"
            );

            // STUDENTS
            st.executeUpdate(
                    "CREATE TABLE IF NOT EXISTS students (" +
                    "id TEXT PRIMARY KEY, " +
                    "name TEXT NOT NULL, " +
                    "phone TEXT NOT NULL)"
            );

            // ISSUES
            st.executeUpdate(
                    "CREATE TABLE IF NOT EXISTS issues (" +
                    "issue_id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    "book_id TEXT, " +
                    "student_id TEXT, " +
                    "issue_date TEXT, " +
                    "due_date TEXT, " +
                    "return_date TEXT, " +
                    "late_days INTEGER DEFAULT 0, " +
                    "fine INTEGER DEFAULT 0, " +
                    "status TEXT DEFAULT 'Issued')"
            );

            // -------------------------------------------------
            // If old issues table exists from previous version,
            // add missing columns.
            // -------------------------------------------------

            addColumnIfMissing(
                    con,
                    "issues",
                    "issue_id",
                    "INTEGER"
            );

            addColumnIfMissing(
                    con,
                    "issues",
                    "issue_date",
                    "TEXT"
            );

            addColumnIfMissing(
                    con,
                    "issues",
                    "due_date",
                    "TEXT"
            );

            addColumnIfMissing(
                    con,
                    "issues",
                    "return_date",
                    "TEXT"
            );

            addColumnIfMissing(
                    con,
                    "issues",
                    "late_days",
                    "INTEGER DEFAULT 0"
            );

            addColumnIfMissing(
                    con,
                    "issues",
                    "fine",
                    "INTEGER DEFAULT 0"
            );

            addColumnIfMissing(
                    con,
                    "issues",
                    "status",
                    "TEXT DEFAULT 'Issued'"
            );

            st.close();
            con.close();

            System.out.println("Database tables ready.");

        } catch (SQLException e) {

            e.printStackTrace();
        }
    }

    // =========================================================
    // ADD COLUMN IF MISSING
    // =========================================================

    static void addColumnIfMissing(
            Connection con,
            String table,
            String column,
            String definition) {

        try {

            DatabaseMetaData meta =
                    con.getMetaData();

            ResultSet rs =
                    meta.getColumns(
                            null,
                            null,
                            table,
                            column
                    );

            if (!rs.next()) {

                Statement st =
                        con.createStatement();

                st.executeUpdate(
                        "ALTER TABLE " + table +
                        " ADD COLUMN " + column +
                        " " + definition
                );

                st.close();
            }

            rs.close();

        } catch (SQLException e) {

            // Ignore if column already exists
        }
    }

    // =========================================================
    // LOAD BOOKS FROM DATABASE
    // =========================================================

    static void loadBooks() {

        books.clear();

        Connection con = connect();

        if (con == null) {
            return;
        }

        try {

            Statement st =
                    con.createStatement();

            ResultSet rs =
                    st.executeQuery(
                            "SELECT * FROM books"
                    );

            while (rs.next()) {

                String[] book = {

                        rs.getString("id"),

                        rs.getString("name"),

                        rs.getString("author"),

                        rs.getString("category"),

                        String.valueOf(
                                rs.getInt("quantity")
                        )
                };

                books.add(book);
            }

            rs.close();
            st.close();
            con.close();

        } catch (SQLException e) {

            e.printStackTrace();
        }
    }

    // =========================================================
    // LOAD STUDENTS
    // =========================================================

    static void loadStudents() {

        studentList.clear();

        Connection con = connect();

        if (con == null) {
            return;
        }

        try {

            Statement st =
                    con.createStatement();

            ResultSet rs =
                    st.executeQuery(
                            "SELECT * FROM students"
                    );

            while (rs.next()) {

                String[] student = {

                        rs.getString("name"),

                        rs.getString("id"),

                        rs.getString("phone")
                };

                studentList.add(student);
            }

            rs.close();
            st.close();
            con.close();

        } catch (SQLException e) {

            e.printStackTrace();
        }
    }

    // =========================================================
    // LOAD ACTIVE ISSUES
    // =========================================================

    static void loadIssues() {

        issueList.clear();

        Connection con = connect();

        if (con == null) {
            return;
        }

        try {

            PreparedStatement ps =
                    con.prepareStatement(
                            "SELECT book_id, student_id " +
                            "FROM issues " +
                            "WHERE status = 'Issued'"
                    );

            ResultSet rs =
                    ps.executeQuery();

            while (rs.next()) {

                String[] issue = {

                        rs.getString("book_id"),

                        rs.getString("student_id")
                };

                issueList.add(issue);
            }

            rs.close();
            ps.close();
            con.close();

        } catch (SQLException e) {

            e.printStackTrace();
        }
    }

    // =========================================================
    // UPDATE DASHBOARD
    // =========================================================

    static void updateDashboard() {

        int totalBookCount = 0;

        for (int i = 0;
             i < books.size();
             i++) {

            totalBookCount +=
                    Integer.parseInt(
                            books.get(i)[4]
                    );
        }

        int totalStudentCount =
                studentList.size();

        int issuedBookCount =
                issueList.size();

        int availableBookCount =
                totalBookCount - issuedBookCount;

        if (availableBookCount < 0) {
            availableBookCount = 0;
        }

        totalBooks.setText(
                "Total Books: " +
                totalBookCount
        );

        totalStudents.setText(
                "Total Students: " +
                totalStudentCount
        );

        issuedBooks.setText(
                "Issued Books: " +
                issuedBookCount
        );

        availableBooks.setText(
                "Available Books: " +
                availableBookCount
        );
    }

    // =========================================================
    // FIND BOOK
    // =========================================================

    static int findBook(String id) {

        for (int i = 0;
             i < books.size();
             i++) {

            if (books.get(i)[0].equals(id)) {
                return i;
            }
        }

        return -1;
    }

    // =========================================================
    // FIND STUDENT
    // =========================================================

    static int findStudent(String id) {

        for (int i = 0;
             i < studentList.size();
             i++) {

            if (studentList.get(i)[1].equals(id)) {
                return i;
            }
        }

        return -1;
    }

    // =========================================================
    // MAIN
    // =========================================================

    public static void main(String[] args) {

        System.out.println(
                "Starting Library Management System..."
        );

        createTables();

        loadBooks();
        loadStudents();
        loadIssues();

        JFrame frame =
                new JFrame(
                        "Library Management System"
                );

        frame.setSize(900, 700);

        frame.setDefaultCloseOperation(
                JFrame.EXIT_ON_CLOSE
        );

        frame.setLayout(null);

        // =====================================================
        // TITLE
        // =====================================================

        JLabel title =
                new JLabel(
                        "LIBRARY MANAGEMENT SYSTEM"
                );

        title.setFont(
                new Font(
                        "Arial",
                        Font.BOLD,
                        24
                )
        );

        title.setBounds(
                270,
                15,
                400,
                35
        );

        frame.add(title);

        // =====================================================
        // DASHBOARD
        // =====================================================

        JLabel dashboardTitle =
                new JLabel("DASHBOARD");

        dashboardTitle.setFont(
                new Font(
                        "Arial",
                        Font.BOLD,
                        16
                )
        );

        dashboardTitle.setBounds(
                50,
                65,
                200,
                30
        );

        frame.add(dashboardTitle);

        totalBooks =
                new JLabel("Total Books: 0");

        totalBooks.setBounds(
                50,
                100,
                180,
                35
        );

        frame.add(totalBooks);

        totalStudents =
                new JLabel("Total Students: 0");

        totalStudents.setBounds(
                250,
                100,
                180,
                35
        );

        frame.add(totalStudents);

        issuedBooks =
                new JLabel("Issued Books: 0");

        issuedBooks.setBounds(
                450,
                100,
                180,
                35
        );

        frame.add(issuedBooks);

        availableBooks =
                new JLabel("Available Books: 0");

        availableBooks.setBounds(
                650,
                100,
                180,
                35
        );

        frame.add(availableBooks);

        // =====================================================
        // BOOK MANAGEMENT
        // =====================================================

        JLabel bookSection =
                new JLabel("BOOK MANAGEMENT");

        bookSection.setFont(
                new Font(
                        "Arial",
                        Font.BOLD,
                        16
                )
        );

        bookSection.setBounds(
                50,
                150,
                250,
                30
        );

        frame.add(bookSection);

        // ADD BOOK

        JButton addBook =
                new JButton("Add Book");

        addBook.setBounds(
                50,
                190,
                180,
                40
        );

        frame.add(addBook);

        addBook.addActionListener(e -> {

            JFrame addFrame =
                    new JFrame("Add Book");

            addFrame.setSize(
                    450,
                    430
            );

            addFrame.setLayout(null);

            JLabel idLabel =
                    new JLabel("Book ID:");

            idLabel.setBounds(
                    50,
                    40,
                    100,
                    30
            );

            addFrame.add(idLabel);

            JTextField idField =
                    new JTextField();

            idField.setBounds(
                    160,
                    40,
                    200,
                    30
            );

            addFrame.add(idField);

            JLabel nameLabel =
                    new JLabel("Book Name:");

            nameLabel.setBounds(
                    50,
                    90,
                    100,
                    30
            );

            addFrame.add(nameLabel);

            JTextField nameField =
                    new JTextField();

            nameField.setBounds(
                    160,
                    90,
                    200,
                    30
            );

            addFrame.add(nameField);

            JLabel authorLabel =
                    new JLabel("Author:");

            authorLabel.setBounds(
                    50,
                    140,
                    100,
                    30
            );

            addFrame.add(authorLabel);

            JTextField authorField =
                    new JTextField();

            authorField.setBounds(
                    160,
                    140,
                    200,
                    30
            );

            addFrame.add(authorField);

            JLabel categoryLabel =
                    new JLabel("Category:");

            categoryLabel.setBounds(
                    50,
                    190,
                    100,
                    30
            );

            addFrame.add(categoryLabel);

            JTextField categoryField =
                    new JTextField();

            categoryField.setBounds(
                    160,
                    190,
                    200,
                    30
            );

            addFrame.add(categoryField);

            JLabel quantityLabel =
                    new JLabel("Quantity:");

            quantityLabel.setBounds(
                    50,
                    240,
                    100,
                    30
            );

            addFrame.add(quantityLabel);

            JTextField quantityField =
                    new JTextField();

            quantityField.setBounds(
                    160,
                    240,
                    200,
                    30
            );

            addFrame.add(quantityField);

            JButton saveButton =
                    new JButton("Save Book");

            saveButton.setBounds(
                    150,
                    300,
                    120,
                    35
            );

            addFrame.add(saveButton);

            saveButton.addActionListener(event -> {

                String id =
                        idField.getText().trim();

                String name =
                        nameField.getText().trim();

                String author =
                        authorField.getText().trim();

                String category =
                        categoryField.getText().trim();

                String quantity =
                        quantityField.getText().trim();

                if (id.isEmpty()
                        || name.isEmpty()
                        || author.isEmpty()
                        || category.isEmpty()
                        || quantity.isEmpty()) {

                    JOptionPane.showMessageDialog(
                            addFrame,
                            "Please fill all fields!"
                    );

                    return;
                }

                if (findBook(id) != -1) {

                    JOptionPane.showMessageDialog(
                            addFrame,
                            "Book ID already exists!"
                    );

                    return;
                }

                int q;

                try {

                    q =
                            Integer.parseInt(
                                    quantity
                            );

                    if (q < 0) {

                        JOptionPane.showMessageDialog(
                                addFrame,
                                "Quantity cannot be negative!"
                        );

                        return;
                    }

                } catch (NumberFormatException ex) {

                    JOptionPane.showMessageDialog(
                            addFrame,
                            "Quantity must be a number!"
                    );

                    return;
                }

                Connection con = connect();

                if (con == null) {
                    return;
                }

                try {

                    PreparedStatement ps =
                            con.prepareStatement(
                                    "INSERT INTO books " +
                                    "(id,name,author,category,quantity) " +
                                    "VALUES (?,?,?,?,?)"
                            );

                    ps.setString(1, id);
                    ps.setString(2, name);
                    ps.setString(3, author);
                    ps.setString(4, category);
                    ps.setInt(5, q);

                    ps.executeUpdate();

                    ps.close();
                    con.close();

                    loadBooks();

                    updateDashboard();

                    JOptionPane.showMessageDialog(
                            addFrame,
                            "Book Added Successfully!"
                    );

                    idField.setText("");
                    nameField.setText("");
                    authorField.setText("");
                    categoryField.setText("");
                    quantityField.setText("");

                } catch (SQLException ex) {

                    JOptionPane.showMessageDialog(
                            addFrame,
                            "Database Error:\n" +
                            ex.getMessage()
                    );
                }
            });

            addFrame.setVisible(true);
        });

        // SEARCH BOOK

        JButton searchBook =
                new JButton("Search Book");

        searchBook.setBounds(
                250,
                190,
                180,
                40
        );

        frame.add(searchBook);

        searchBook.addActionListener(e -> {

            JFrame searchFrame =
                    new JFrame("Search Book");

            searchFrame.setSize(
                    650,
                    400
            );

            searchFrame.setLayout(null);

            JLabel label =
                    new JLabel(
                            "Enter Book ID or Name:"
                    );

            label.setBounds(
                    30,
                    30,
                    180,
                    30
            );

            searchFrame.add(label);

            JTextField field =
                    new JTextField();

            field.setBounds(
                    210,
                    30,
                    220,
                    30
            );

            searchFrame.add(field);

            JButton button =
                    new JButton("Search");

            button.setBounds(
                    450,
                    30,
                    100,
                    30
            );

            searchFrame.add(button);

            JTextArea result =
                    new JTextArea();

            result.setEditable(false);

            JScrollPane scroll =
                    new JScrollPane(result);

            scroll.setBounds(
                    30,
                    80,
                    520,
                    250
            );

            searchFrame.add(scroll);

            button.addActionListener(event -> {

                String search =
                        field.getText()
                                .trim()
                                .toLowerCase();

                result.setText("");

                boolean found = false;

                for (int i = 0;
                     i < books.size();
                     i++) {

                    String id =
                            books.get(i)[0];

                    String name =
                            books.get(i)[1];

                    if (id.equalsIgnoreCase(search)
                            || name.toLowerCase()
                            .contains(search)) {

                        result.append(
                                "Book ID: " +
                                books.get(i)[0] +
                                "\n"
                        );

                        result.append(
                                "Book Name: " +
                                books.get(i)[1] +
                                "\n"
                        );

                        result.append(
                                "Author: " +
                                books.get(i)[2] +
                                "\n"
                        );

                        result.append(
                                "Category: " +
                                books.get(i)[3] +
                                "\n"
                        );

                        result.append(
                                "Available Quantity: " +
                                books.get(i)[4] +
                                "\n"
                        );

                        result.append(
                                "--------------------------\n"
                        );

                        found = true;
                    }
                }

                if (!found) {

                    result.setText(
                            "Book not found!"
                    );
                }
            });

            searchFrame.setVisible(true);
        });

        // VIEW BOOKS

        JButton viewBooks =
                new JButton("View Books");

        viewBooks.setBounds(
                450,
                190,
                180,
                40
        );

        frame.add(viewBooks);

        viewBooks.addActionListener(e -> {

            loadBooks();

            JFrame viewFrame =
                    new JFrame("All Books");

            viewFrame.setSize(
                    800,
                    450
            );

            String[] columns = {

                    "Book ID",
                    "Book Name",
                    "Author",
                    "Category",
                    "Quantity"
            };

            String[][] data =
                    new String[books.size()][5];

            for (int i = 0;
                 i < books.size();
                 i++) {

                for (int j = 0;
                     j < 5;
                     j++) {

                    data[i][j] =
                            books.get(i)[j];
                }
            }

            JTable table =
                    new JTable(
                            data,
                            columns
                    );

            viewFrame.add(
                    new JScrollPane(table)
            );

            viewFrame.setVisible(true);
        });

        // DELETE BOOK

        JButton deleteBook =
                new JButton("Delete Book");

        deleteBook.setBounds(
                650,
                190,
                180,
                40
        );

        frame.add(deleteBook);

        deleteBook.addActionListener(e -> {

            String id =
                    JOptionPane.showInputDialog(
                            frame,
                            "Enter Book ID to delete:"
                    );

            if (id == null || id.trim().isEmpty()) {
                return;
            }

            int index =
                    findBook(id.trim());

            if (index == -1) {

                JOptionPane.showMessageDialog(
                        frame,
                        "Book not found!"
                );

                return;
            }

            Connection con = connect();

            try {

                PreparedStatement check =
                        con.prepareStatement(
                                "SELECT COUNT(*) FROM issues " +
                                "WHERE book_id=? " +
                                "AND status='Issued'"
                        );

                check.setString(
                        1,
                        id.trim()
                );

                ResultSet rs =
                        check.executeQuery();

                int count =
                        rs.getInt(1);

                rs.close();
                check.close();

                if (count > 0) {

                    con.close();

                    JOptionPane.showMessageDialog(
                            frame,
                            "Cannot delete this book!\n" +
                            "It is currently issued."
                    );

                    return;
                }

                PreparedStatement ps =
                        con.prepareStatement(
                                "DELETE FROM books WHERE id=?"
                        );

                ps.setString(
                        1,
                        id.trim()
                );

                ps.executeUpdate();

                ps.close();
                con.close();

                loadBooks();

                updateDashboard();

                JOptionPane.showMessageDialog(
                        frame,
                        "Book deleted successfully!"
                );

            } catch (SQLException ex) {

                JOptionPane.showMessageDialog(
                        frame,
                        "Database Error:\n" +
                        ex.getMessage()
                );
            }
        });

        // UPDATE BOOK

        JButton updateBook =
                new JButton("Update Book");

        updateBook.setBounds(
                650,
                240,
                180,
                40
        );

        frame.add(updateBook);

        updateBook.addActionListener(e -> {

            String id =
                    JOptionPane.showInputDialog(
                            frame,
                            "Enter Book ID:"
                    );

            if (id == null || id.trim().isEmpty()) {
                return;
            }

            int index =
                    findBook(id.trim());

            if (index == -1) {

                JOptionPane.showMessageDialog(
                        frame,
                        "Book not found!"
                );

                return;
            }

            String name =
                    JOptionPane.showInputDialog(
                            frame,
                            "Enter Book Name:",
                            books.get(index)[1]
                    );

            if (name == null) {
                return;
            }

            String author =
                    JOptionPane.showInputDialog(
                            frame,
                            "Enter Author:",
                            books.get(index)[2]
                    );

            if (author == null) {
                return;
            }

            String category =
                    JOptionPane.showInputDialog(
                            frame,
                            "Enter Category:",
                            books.get(index)[3]
                    );

            if (category == null) {
                return;
            }

            String quantity =
                    JOptionPane.showInputDialog(
                            frame,
                            "Enter Quantity:",
                            books.get(index)[4]
                    );

            if (quantity == null) {
                return;
            }

            int q;

            try {

                q =
                        Integer.parseInt(
                                quantity
                        );

                if (q < 0) {

                    JOptionPane.showMessageDialog(
                            frame,
                            "Quantity cannot be negative!"
                    );

                    return;
                }

            } catch (NumberFormatException ex) {

                JOptionPane.showMessageDialog(
                        frame,
                        "Quantity must be a number!"
                );

                return;
            }

            Connection con = connect();

            try {

                PreparedStatement ps =
                        con.prepareStatement(
                                "UPDATE books SET " +
                                "name=?, author=?, " +
                                "category=?, quantity=? " +
                                "WHERE id=?"
                        );

                ps.setString(1, name);
                ps.setString(2, author);
                ps.setString(3, category);
                ps.setInt(4, q);
                ps.setString(5, id.trim());

                ps.executeUpdate();

                ps.close();
                con.close();

                loadBooks();

                updateDashboard();

                JOptionPane.showMessageDialog(
                        frame,
                        "Book updated successfully!"
                );

            } catch (SQLException ex) {

                JOptionPane.showMessageDialog(
                        frame,
                        "Database Error:\n" +
                        ex.getMessage()
                );
            }
        });

        // =====================================================
        // STUDENT MANAGEMENT
        // =====================================================

        JLabel studentSection =
                new JLabel("STUDENT MANAGEMENT");

        studentSection.setFont(
                new Font(
                        "Arial",
                        Font.BOLD,
                        16
                )
        );

        studentSection.setBounds(
                50,
                300,
                250,
                30
        );

        frame.add(studentSection);

        // ADD STUDENT

        JButton students =
                new JButton("Add Student");

        students.setBounds(
                50,
                340,
                180,
                40
        );

        frame.add(students);

        students.addActionListener(e -> {

            JFrame studentFrame =
                    new JFrame(
                            "Add Student"
                    );

            studentFrame.setSize(
                    500,
                    350
            );

            studentFrame.setLayout(null);

            JLabel nameLabel =
                    new JLabel(
                            "Student Name:"
                    );

            nameLabel.setBounds(
                    50,
                    50,
                    120,
                    30
            );

            studentFrame.add(nameLabel);

            JTextField nameField =
                    new JTextField();

            nameField.setBounds(
                    180,
                    50,
                    220,
                    30
            );

            studentFrame.add(nameField);

            JLabel idLabel =
                    new JLabel(
                            "Student ID:"
                    );

            idLabel.setBounds(
                    50,
                    100,
                    120,
                    30
            );

            studentFrame.add(idLabel);

            JTextField idField =
                    new JTextField();

            idField.setBounds(
                    180,
                    100,
                    220,
                    30
            );

            studentFrame.add(idField);

            JLabel phoneLabel =
                    new JLabel(
                            "Phone:"
                    );

            phoneLabel.setBounds(
                    50,
                    150,
                    120,
                    30
            );

            studentFrame.add(phoneLabel);

            JTextField phoneField =
                    new JTextField();

            phoneField.setBounds(
                    180,
                    150,
                    220,
                    30
            );

            studentFrame.add(phoneField);

            JButton save =
                    new JButton(
                            "Save Student"
                    );

            save.setBounds(
                    180,
                    210,
                    140,
                    35
            );

            studentFrame.add(save);

            save.addActionListener(event -> {

                String name =
                        nameField.getText().trim();

                String id =
                        idField.getText().trim();

                String phone =
                        phoneField.getText().trim();

                if (name.isEmpty()
                        || id.isEmpty()
                        || phone.isEmpty()) {

                    JOptionPane.showMessageDialog(
                            studentFrame,
                            "Please fill all fields!"
                    );

                    return;
                }

                if (findStudent(id) != -1) {

                    JOptionPane.showMessageDialog(
                            studentFrame,
                            "Student ID already exists!"
                    );

                    return;
                }

                Connection con = connect();

                try {

                    PreparedStatement ps =
                            con.prepareStatement(
                                    "INSERT INTO students " +
                                    "(id,name,phone) " +
                                    "VALUES (?,?,?)"
                            );

                    ps.setString(1, id);
                    ps.setString(2, name);
                    ps.setString(3, phone);

                    ps.executeUpdate();

                    ps.close();
                    con.close();

                    loadStudents();

                    updateDashboard();

                    JOptionPane.showMessageDialog(
                            studentFrame,
                            "Student added successfully!"
                    );

                    nameField.setText("");
                    idField.setText("");
                    phoneField.setText("");

                } catch (SQLException ex) {

                    JOptionPane.showMessageDialog(
                            studentFrame,
                            "Database Error:\n" +
                            ex.getMessage()
                    );
                }
            });

            studentFrame.setVisible(true);
        });

        // SEARCH STUDENT

        JButton searchStudent =
                new JButton("Search Student");

        searchStudent.setBounds(
                250,
                340,
                180,
                40
        );

        frame.add(searchStudent);

        searchStudent.addActionListener(e -> {

            JFrame searchFrame =
                    new JFrame(
                            "Search Student"
                    );

            searchFrame.setSize(
                    650,
                    400
            );

            searchFrame.setLayout(null);

            JLabel label =
                    new JLabel(
                            "Enter Student ID or Name:"
                    );

            label.setBounds(
                    30,
                    30,
                    180,
                    30
            );

            searchFrame.add(label);

            JTextField field =
                    new JTextField();

            field.setBounds(
                    210,
                    30,
                    220,
                    30
            );

            searchFrame.add(field);

            JButton button =
                    new JButton("Search");

            button.setBounds(
                    450,
                    30,
                    100,
                    30
            );

            searchFrame.add(button);

            JTextArea result =
                    new JTextArea();

            result.setEditable(false);

            JScrollPane scroll =
                    new JScrollPane(result);

            scroll.setBounds(
                    30,
                    80,
                    520,
                    250
            );

            searchFrame.add(scroll);

            button.addActionListener(event -> {

                String search =
                        field.getText()
                                .trim()
                                .toLowerCase();

                result.setText("");

                boolean found = false;

                for (int i = 0;
                     i < studentList.size();
                     i++) {

                    String name =
                            studentList.get(i)[0];

                    String id =
                            studentList.get(i)[1];

                    if (id.equalsIgnoreCase(search)
                            || name.toLowerCase()
                            .contains(search)) {

                        result.append(
                                "Student Name: " +
                                name +
                                "\n"
                        );

                        result.append(
                                "Student ID: " +
                                id +
                                "\n"
                        );

                        result.append(
                                "Phone: " +
                                studentList.get(i)[2] +
                                "\n"
                        );

                        result.append(
                                "--------------------------\n"
                        );

                        found = true;
                    }
                }

                if (!found) {

                    result.setText(
                            "Student not found!"
                    );
                }
            });

            searchFrame.setVisible(true);
        });

        // VIEW STUDENTS

        JButton viewStudents =
                new JButton("View Students");

        viewStudents.setBounds(
                450,
                340,
                180,
                40
        );

        frame.add(viewStudents);

        viewStudents.addActionListener(e -> {

            loadStudents();

            JFrame viewFrame =
                    new JFrame(
                            "All Students"
                    );

            viewFrame.setSize(
                    650,
                    450
            );

            String[] columns = {

                    "Student ID",
                    "Student Name",
                    "Phone"
            };

            String[][] data =
                    new String[
                            studentList.size()
                    ][3];

            for (int i = 0;
                 i < studentList.size();
                 i++) {

                data[i][0] =
                        studentList.get(i)[1];

                data[i][1] =
                        studentList.get(i)[0];

                data[i][2] =
                        studentList.get(i)[2];
            }

            JTable table =
                    new JTable(
                            data,
                            columns
                    );

            viewFrame.add(
                    new JScrollPane(table)
            );

            viewFrame.setVisible(true);
        });

        // DELETE STUDENT

        JButton deleteStudent =
                new JButton("Delete Student");

        deleteStudent.setBounds(
                650,
                340,
                180,
                40
        );

        frame.add(deleteStudent);

        deleteStudent.addActionListener(e -> {

            String id =
                    JOptionPane.showInputDialog(
                            frame,
                            "Enter Student ID:"
                    );

            if (id == null || id.trim().isEmpty()) {
                return;
            }

            int index =
                    findStudent(id.trim());

            if (index == -1) {

                JOptionPane.showMessageDialog(
                        frame,
                        "Student not found!"
                );

                return;
            }

            Connection con = connect();

            try {

                PreparedStatement check =
                        con.prepareStatement(
                                "SELECT COUNT(*) " +
                                "FROM issues " +
                                "WHERE student_id=? " +
                                "AND status='Issued'"
                        );

                check.setString(
                        1,
                        id.trim()
                );

                ResultSet rs =
                        check.executeQuery();

                int count =
                        rs.getInt(1);

                rs.close();
                check.close();

                if (count > 0) {

                    con.close();

                    JOptionPane.showMessageDialog(
                            frame,
                            "Cannot delete student!\n" +
                            "Student has an issued book."
                    );

                    return;
                }

                PreparedStatement ps =
                        con.prepareStatement(
                                "DELETE FROM students " +
                                "WHERE id=?"
                        );

                ps.setString(
                        1,
                        id.trim()
                );

                ps.executeUpdate();

                ps.close();
                con.close();

                loadStudents();

                updateDashboard();

                JOptionPane.showMessageDialog(
                        frame,
                        "Student deleted successfully!"
                );

            } catch (SQLException ex) {

                JOptionPane.showMessageDialog(
                        frame,
                        "Database Error:\n" +
                        ex.getMessage()
                );
            }
        });

        // =====================================================
        // TRANSACTION MANAGEMENT
        // =====================================================

        JLabel transactionSection =
                new JLabel(
                        "TRANSACTION MANAGEMENT"
                );

        transactionSection.setFont(
                new Font(
                        "Arial",
                        Font.BOLD,
                        16
                )
        );

        transactionSection.setBounds(
                50,
                400,
                300,
                30
        );

        frame.add(transactionSection);

        // ISSUE BOOK

        JButton issueBook =
                new JButton("Issue Book");

        issueBook.setBounds(
                50,
                440,
                180,
                40
        );

        frame.add(issueBook);

        issueBook.addActionListener(e -> {

            String bookId =
                    JOptionPane.showInputDialog(
                            frame,
                            "Enter Book ID:"
                    );

            if (bookId == null
                    || bookId.trim().isEmpty()) {

                return;
            }

            bookId =
                    bookId.trim();

            int bookIndex =
                    findBook(bookId);

            if (bookIndex == -1) {

                JOptionPane.showMessageDialog(
                        frame,
                        "Book not found!"
                );

                return;
            }

            int quantity =
                    Integer.parseInt(
                            books.get(bookIndex)[4]
                    );

            if (quantity <= 0) {

                JOptionPane.showMessageDialog(
                        frame,
                        "Book is not available!"
                );

                return;
            }

            String studentId =
                    JOptionPane.showInputDialog(
                            frame,
                            "Enter Student ID:"
                    );

            if (studentId == null
                    || studentId.trim().isEmpty()) {

                return;
            }

            studentId =
                    studentId.trim();

            int studentIndex =
                    findStudent(studentId);

            if (studentIndex == -1) {

                JOptionPane.showMessageDialog(
                        frame,
                        "Student not found!"
                );

                return;
            }

            // CHECK IF SAME STUDENT ALREADY HAS THIS BOOK

            Connection con = connect();

            try {

                PreparedStatement check =
                        con.prepareStatement(
                                "SELECT COUNT(*) " +
                                "FROM issues " +
                                "WHERE book_id=? " +
                                "AND student_id=? " +
                                "AND status='Issued'"
                        );

                check.setString(
                        1,
                        bookId
                );

                check.setString(
                        2,
                        studentId
                );

                ResultSet rs =
                        check.executeQuery();

                int count =
                        rs.getInt(1);

                rs.close();
                check.close();

                if (count > 0) {

                    con.close();

                    JOptionPane.showMessageDialog(
                            frame,
                            "This student already has this book!"
                    );

                    return;
                }

                LocalDate issueDate =
                        LocalDate.now();

                LocalDate dueDate =
                        issueDate.plusDays(14);

                PreparedStatement ps =
                        con.prepareStatement(
                                "INSERT INTO issues " +
                                "(book_id, student_id, " +
                                "issue_date, due_date, " +
                                "status, late_days, fine) " +
                                "VALUES (?,?,?,?,?,?,?)"
                        );

                ps.setString(
                        1,
                        bookId
                );

                ps.setString(
                        2,
                        studentId
                );

                ps.setString(
                        3,
                        issueDate.toString()
                );

                ps.setString(
                        4,
                        dueDate.toString()
                );

                ps.setString(
                        5,
                        "Issued"
                );

                ps.setInt(6, 0);
                ps.setInt(7, 0);

                ps.executeUpdate();

                ps.close();

                // REDUCE BOOK QUANTITY

                PreparedStatement update =
                        con.prepareStatement(
                                "UPDATE books " +
                                "SET quantity = quantity - 1 " +
                                "WHERE id=?"
                        );

                update.setString(
                        1,
                        bookId
                );

                update.executeUpdate();

                update.close();

                con.close();

                loadBooks();
                loadIssues();

                updateDashboard();

                JOptionPane.showMessageDialog(
                        frame,
                        "Book issued successfully!\n\n" +
                        "Issue Date: " +
                        issueDate +
                        "\nDue Date: " +
                        dueDate +
                        "\n\n" +
                        "Late Fee: ₹5 per day"
                );

            } catch (SQLException ex) {

                JOptionPane.showMessageDialog(
                        frame,
                        "Database Error:\n" +
                        ex.getMessage()
                );
            }
        });

        // RETURN BOOK

        JButton returnBook =
                new JButton("Return Book");

        returnBook.setBounds(
                250,
                440,
                180,
                40
        );

        frame.add(returnBook);

        returnBook.addActionListener(e -> {

            String bookId =
                    JOptionPane.showInputDialog(
                            frame,
                            "Enter Book ID:"
                    );

            if (bookId == null
                    || bookId.trim().isEmpty()) {

                return;
            }

            String studentId =
                    JOptionPane.showInputDialog(
                            frame,
                            "Enter Student ID:"
                    );

            if (studentId == null
                    || studentId.trim().isEmpty()) {

                return;
            }

            bookId =
                    bookId.trim();

            studentId =
                    studentId.trim();

            Connection con = connect();

            if (con == null) {
                return;
            }

            try {

                PreparedStatement ps =
                        con.prepareStatement(
                                "SELECT * FROM issues " +
                                "WHERE book_id=? " +
                                "AND student_id=? " +
                                "AND status='Issued'"
                        );

                ps.setString(
                        1,
                        bookId
                );

                ps.setString(
                        2,
                        studentId
                );

                ResultSet rs =
                        ps.executeQuery();

                if (!rs.next()) {

                    rs.close();
                    ps.close();
                    con.close();

                    JOptionPane.showMessageDialog(
                            frame,
                            "Issue record not found!"
                    );

                    return;
                }

                int issueId =
                        rs.getInt(
                                "issue_id"
                        );

                String issueDateString =
                        rs.getString(
                                "issue_date"
                        );

                String dueDateString =
                        rs.getString(
                                "due_date"
                        );

                rs.close();
                ps.close();

                LocalDate dueDate =
                        LocalDate.parse(
                                dueDateString
                        );

                LocalDate returnDate =
                        LocalDate.now();

                long lateDaysLong =
                        ChronoUnit.DAYS.between(
                                dueDate,
                                returnDate
                        );

                int lateDays;

                if (lateDaysLong > 0) {

                    lateDays =
                            (int) lateDaysLong;

                } else {

                    lateDays = 0;
                }

                int fine =
                        lateDays * 5;

                PreparedStatement update =
                        con.prepareStatement(
                                "UPDATE issues SET " +
                                "return_date=?, " +
                                "late_days=?, " +
                                "fine=?, " +
                                "status='Returned' " +
                                "WHERE issue_id=?"
                        );

                update.setString(
                        1,
                        returnDate.toString()
                );

                update.setInt(
                        2,
                        lateDays
                );

                update.setInt(
                        3,
                        fine
                );

                update.setInt(
                        4,
                        issueId
                );

                update.executeUpdate();

                update.close();

                // INCREASE BOOK QUANTITY

                PreparedStatement bookUpdate =
                        con.prepareStatement(
                                "UPDATE books " +
                                "SET quantity = quantity + 1 " +
                                "WHERE id=?"
                        );

                bookUpdate.setString(
                        1,
                        bookId
                );

                bookUpdate.executeUpdate();

                bookUpdate.close();

                con.close();

                loadBooks();
                loadIssues();

                updateDashboard();

                JOptionPane.showMessageDialog(
                        frame,
                        "Book returned successfully!\n\n" +
                        "Issue Date: " +
                        issueDateString +
                        "\nDue Date: " +
                        dueDate +
                        "\nReturn Date: " +
                        returnDate +
                        "\nLate Days: " +
                        lateDays +
                        "\nLate Fee: ₹" +
                        fine
                );

            } catch (Exception ex) {

                JOptionPane.showMessageDialog(
                        frame,
                        "Error:\n" +
                        ex.getMessage()
                );
            }
        });

        // ISSUED BOOKS

        JButton issued =
                new JButton("Issued Books");

        issued.setBounds(
                450,
                440,
                180,
                40
        );

        frame.add(issued);

        issued.addActionListener(e -> {

            JFrame issueFrame =
                    new JFrame(
                            "Currently Issued Books"
                    );

            issueFrame.setSize(
                    850,
                    450
            );

            String[] columns = {

                    "Book ID",
                    "Student ID",
                    "Issue Date",
                    "Due Date",
                    "Status"
            };

            ArrayList<String[]> records =
                    new ArrayList<>();

            Connection con = connect();

            try {

                PreparedStatement ps =
                        con.prepareStatement(
                                "SELECT book_id, student_id, " +
                                "issue_date, due_date, status " +
                                "FROM issues " +
                                "WHERE status='Issued'"
                        );

                ResultSet rs =
                        ps.executeQuery();

                while (rs.next()) {

                    records.add(
                            new String[]{

                                    rs.getString(
                                            "book_id"
                                    ),

                                    rs.getString(
                                            "student_id"
                                    ),

                                    rs.getString(
                                            "issue_date"
                                    ),

                                    rs.getString(
                                            "due_date"
                                    ),

                                    rs.getString(
                                            "status"
                                    )
                            }
                    );
                }

                rs.close();
                ps.close();
                con.close();

            } catch (SQLException ex) {

                JOptionPane.showMessageDialog(
                        issueFrame,
                        ex.getMessage()
                );
            }

            String[][] data =
                    new String[
                            records.size()
                    ][5];

            for (int i = 0;
                 i < records.size();
                 i++) {

                for (int j = 0;
                     j < 5;
                     j++) {

                    data[i][j] =
                            records.get(i)[j];
                }
            }

            JTable table =
                    new JTable(
                            data,
                            columns
                    );

            issueFrame.add(
                    new JScrollPane(table)
            );

            issueFrame.setVisible(true);
        });

        // TRANSACTION HISTORY

        JButton history =
                new JButton(
                        "Transaction History"
                );

        history.setBounds(
                650,
                440,
                180,
                40
        );

        frame.add(history);

        history.addActionListener(e -> {

            JFrame historyFrame =
                    new JFrame(
                            "Transaction History"
                    );

            historyFrame.setSize(
                    1000,
                    500
            );

            String[] columns = {

                    "Issue ID",
                    "Book ID",
                    "Student ID",
                    "Issue Date",
                    "Due Date",
                    "Return Date",
                    "Late Days",
                    "Fine",
                    "Status"
            };

            ArrayList<String[]> records =
                    new ArrayList<>();

            Connection con = connect();

            try {

                Statement st =
                        con.createStatement();

                ResultSet rs =
                        st.executeQuery(
                                "SELECT * FROM issues " +
                                "ORDER BY issue_id DESC"
                        );

                while (rs.next()) {

                    records.add(
                            new String[]{

                                    String.valueOf(
                                            rs.getInt(
                                                    "issue_id"
                                            )
                                    ),

                                    rs.getString(
                                            "book_id"
                                    ),

                                    rs.getString(
                                            "student_id"
                                    ),

                                    rs.getString(
                                            "issue_date"
                                    ),

                                    rs.getString(
                                            "due_date"
                                    ),

                                    rs.getString(
                                            "return_date"
                                    ),

                                    String.valueOf(
                                            rs.getInt(
                                                    "late_days"
                                            )
                                    ),

                                    "₹" +
                                    rs.getInt(
                                            "fine"
                                    ),

                                    rs.getString(
                                            "status"
                                    )
                            }
                    );
                }

                rs.close();
                st.close();
                con.close();

            } catch (SQLException ex) {

                JOptionPane.showMessageDialog(
                        historyFrame,
                        ex.getMessage()
                );
            }

            String[][] data =
                    new String[
                            records.size()
                    ][9];

            for (int i = 0;
                 i < records.size();
                 i++) {

                for (int j = 0;
                     j < 9;
                     j++) {

                    data[i][j] =
                            records.get(i)[j];
                }
            }

            JTable table =
                    new JTable(
                            data,
                            columns
                    );

            historyFrame.add(
                    new JScrollPane(table)
            );

            historyFrame.setVisible(true);
        });

        // =====================================================
        // REFRESH DASHBOARD
        // =====================================================

        JButton refresh =
                new JButton("Refresh Dashboard");

        refresh.setBounds(
                50,
                510,
                180,
                40
        );

        frame.add(refresh);

        refresh.addActionListener(e -> {

            loadBooks();
            loadStudents();
            loadIssues();

            updateDashboard();

            JOptionPane.showMessageDialog(
                    frame,
                    "Dashboard refreshed!"
            );
        });

        // =====================================================
        // EXIT
        // =====================================================

        JButton exit =
                new JButton("Exit");

        exit.setBounds(
                650,
                510,
                180,
                40
        );

        frame.add(exit);

        exit.addActionListener(e -> {

            int choice =
                    JOptionPane.showConfirmDialog(
                            frame,
                            "Are you sure you want to exit?",
                            "Exit",
                            JOptionPane.YES_NO_OPTION
                    );

            if (choice ==
                    JOptionPane.YES_OPTION) {

                System.exit(0);
            }
        });

        // =====================================================
        // INITIAL DASHBOARD
        // =====================================================

        updateDashboard();

        frame.setLocationRelativeTo(null);

        frame.setVisible(true);
    }
}
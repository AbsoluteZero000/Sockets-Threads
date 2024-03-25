package org.bookstore.datastore;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;

public class LibraryManager {

    public static String DB_URL = "jdbc:sqlite:library.db";

    public static void createTables() {
        Connection connection = null;
        Statement statement = null;

        try {
            connection = DriverManager.getConnection(DB_URL);
            statement = connection.createStatement();
            String createUserTable = "CREATE TABLE IF NOT EXISTS user (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    "username TEXT UNIQUE NOT NULL, " +
                    "password TEXT NOT NULL, " +
                    "name TEXT NOT NULL, " +
                    "role TEXT NOT NULL" +
                    ");";
            statement.execute(createUserTable);

            // Create Books table
            String createBooksTable = "CREATE TABLE IF NOT EXISTS books (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    "title TEXT NOT NULL, " +
                    "author TEXT NOT NULL, " +
                    "genre TEXT, " +
                    "owner INTEGER, " +
                    "price REAL, " +
                    "quantity INTEGER DEFAULT 1, " +
                    "FOREIGN KEY(owner) REFERENCES user(id) ON DELETE SET NULL" +
                    ");";
            statement.execute(createBooksTable);

            // Create Requests table
            String createRequestsTable = "CREATE TABLE IF NOT EXISTS requests (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    "book INTEGER NOT NULL, " +
                    "requester INTEGER NOT NULL, " +
                    "status TEXT NOT NULL, " +
                    "FOREIGN KEY(book) REFERENCES books(id) ON DELETE CASCADE, " +
                    "FOREIGN KEY(requester) REFERENCES user(id) ON DELETE CASCADE" +
                    ");";
            statement.execute(createRequestsTable);

            System.out.println("Tables created successfully!");

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            // Close resources
            if (connection != null) {
                try {
                    connection.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            if (statement != null) {
                try {
                    statement.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public String selectBookByTitle(String title) throws SQLException {
        String sql = "SELECT * FROM books WHERE title = ?";


        try (Connection connection = DriverManager.getConnection(DB_URL);
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, title);
            ResultSet resultSet = statement.executeQuery();

            ArrayList<ArrayList<String>> books = new ArrayList<>();
            int j = 0;
            while (resultSet.next()) {
                books.add(new ArrayList<String>());
                for(int i =1 ;i <= 6; i++ ){
                    books.get(j).add(resultSet.getString(i));
                }
                j++;
            }
            resultSet.close();
            ArrayList<String> result = new ArrayList<>();
            for(int i = 0 ;i < books.size(); i++){
                result.add(String.join(":", books.get(i)));
            }

            return String.join("!", result);
        }
    }

    public String selectBookByAuthor(String author) throws SQLException {
        String sql = "SELECT * FROM books WHERE author = ?";


        try (Connection connection = DriverManager.getConnection(DB_URL);
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, author);
            ResultSet resultSet = statement.executeQuery();

            ArrayList<ArrayList<String>> books = new ArrayList<>();
            int j = 0;
            while (resultSet.next()) {
                books.add(new ArrayList<String>());
                for(int i =1 ;i <= 6; i++ ){
                    books.get(j).add(resultSet.getString(i));
                }
                j++;
            }
            resultSet.close();
            ArrayList<String> result = new ArrayList<>();
            for(int i = 0 ;i < books.size(); i++){
                result.add(String.join(":", books.get(i)));
            }

            return String.join("!", result);
        }
    }
    public String signup(String username, String password, String name) throws SQLException{
        String sql = "INSERT INTO user (username, password, name, role) VALUES (?, ?, ?, ?)";


        try (Connection connection =  DriverManager.getConnection("jdbc:sqlite:library.db");
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, username);
            statement.setString(2, password);
            statement.setString(3, name);
            statement.setString(4, "");

            int id = statement.executeUpdate();
            return "200:n" + ";" + id;
        }
        catch (Exception e) {
            return "400:n";
        }
    }
    public String login(String username, String password) throws SQLException {
        String sql = "SELECT id, role, password FROM user where username =?";

        try (Connection connection = DriverManager.getConnection("jdbc:sqlite:library.db");
        PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, username);
            ResultSet resultSet = statement.executeQuery();
            if (resultSet.next()) {
                if (resultSet.getString(3).equals(password)) {
                    return "200:n"+ resultSet.getString(2) + ";" + resultSet.getString(1);
                } else {
                    return "401:n";
                }
            } else {
                return "404:n";
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return "500:";
        }
    }


    public String selectBookByGenre(String genre) throws SQLException {
        String sql = "SELECT * FROM books WHERE genre = ?";


        try (Connection connection = DriverManager.getConnection(DB_URL);
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, genre);
            ResultSet resultSet = statement.executeQuery();

            ArrayList<ArrayList<String>> books = new ArrayList<>();
            int j = 0;
            while (resultSet.next()) {
                books.add(new ArrayList<String>());
                for(int i =1 ;i <= 6; i++ ){
                    books.get(j).add(resultSet.getString(i));
                }
                j++;
            }
            resultSet.close();
            ArrayList<String> result = new ArrayList<>();
            for(int i = 0 ;i < books.size(); i++){
                result.add(String.join(":", books.get(i)));
            }

            return String.join("!", result);

        }
    }

    public String deleteBook(int bookId) throws SQLException {
        String sql = "DELETE FROM books WHERE id = ?";


        try (Connection connection = DriverManager.getConnection(DB_URL);
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, bookId);
            int rowsDeleted = statement.executeUpdate();
            System.out.println(rowsDeleted + " book(s) deleted.");
            return "200";
        }catch(Exception e){
            e.printStackTrace();
            return "400";
        }
    }

    public String insertBook(String title, String author, String genre, double price, int quantity, int ownerId) throws SQLException, ClassNotFoundException {
        String sql = "INSERT INTO books (title, author, genre, price, quantity, owner) VALUES (?, ?, ?, ?, ?, ?)";


        try (Connection connection = DriverManager.getConnection(DB_URL);
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, title);
            statement.setString(2, author);
            statement.setString(3, genre);
            statement.setDouble(4, price);
            statement.setInt(5, quantity);
            statement.setInt(6, ownerId);
            statement.executeUpdate();
            System.out.println("Book inserted successfully!");
            return "200";
        }catch(Exception e){
            e.printStackTrace();
            return "400";
        }
    }

    public String insertRequest(int bookId, int requesterId, String status) throws SQLException {
        String sql = "INSERT INTO requests (book, requester, status) VALUES (?, ?, ?)";


        try (Connection connection =  DriverManager.getConnection("jdbc:sqlite:library.db");
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, bookId);
            statement.setInt(2, requesterId);
            statement.setString(3, status);
            statement.executeUpdate();
            System.out.println("Request inserted successfully!");
            return "200";
        }catch(Exception e){
            return "500";
        }
    }

    public String viewAllBooks() throws SQLException {
        String sql = "SELECT * FROM books";


        try (Connection connection = DriverManager.getConnection(DB_URL);
             PreparedStatement statement = connection.prepareStatement(sql)) {
            ResultSet resultSet = statement.executeQuery();
            ArrayList<ArrayList<String>> books = new ArrayList<>();
            int j = 0;
            while (resultSet.next()) {
                books.add(new ArrayList<String>());
                for(int i =1 ;i <= 6; i++ ){
                    books.get(j).add(resultSet.getString(i));
                }
                j++;
            }
            resultSet.close();
            ArrayList<String> result = new ArrayList<>();
            for(int i = 0 ;i < books.size(); i++){
                result.add(String.join(":", books.get(i)));
            }

            return String.join("!", result);
        }
    }
    public String editBookQuantity(int diff, int bookid) throws SQLException {
        String sql = "UPDATE books SET quantity = ? WHERE id = ?";
        int quantity = getQuantity(bookid);
        if(quantity == -1){
            return "404: Book not found";
        }else if (diff < 0 && quantity < Math.abs(diff)) {
            return "400: Not enough books in stock";
        }

        try (Connection connection = DriverManager.getConnection(DB_URL);
                 PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, diff + quantity);
            statement.setInt(2, bookid);
            int rowsUpdated = statement.executeUpdate();

            if (rowsUpdated == 0) {
                return "404: Book not found";
            } else {
                return "200";
            }
        } catch (Exception e) {
            e.printStackTrace();
            return "500";
        }
    }
    public String selectAllRequestsForOwnedBooks(int ownerId) throws SQLException {
        String sql = "SELECT r.id, r.book, r.requester, r.status, b.title " +
                     "FROM requests r " +
                     "INNER JOIN books b ON r.book = b.id " +
                     "WHERE b.owner = ?";

        try (Connection connection = DriverManager.getConnection(DB_URL);
                 PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, ownerId);
            ResultSet resultSet = statement.executeQuery();

            ArrayList<ArrayList<String>> requests = new ArrayList<>();
            int j = 0;
            while (resultSet.next()) {
                requests.add(new ArrayList<String>());
                for (int i = 1; i <= 5; i++) {
                    requests.get(j).add(resultSet.getString(i));
                }
                j++;
            }
            resultSet.close();
            ArrayList<String> result = new ArrayList<>();
            for (int i = 0; i < requests.size(); i++) {
                result.add(String.join(":", requests.get(i)));
            }

            return String.join("!", result);
        }
    }
    private int getQuantity(int bookid) throws SQLException {
        String sql = "SELECT quantity FROM books WHERE id = ?";

        try (Connection connection = DriverManager.getConnection(DB_URL);
                 PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, bookid);
            ResultSet resultSet = statement.executeQuery();

            if (resultSet.next()) {
                return resultSet.getInt("quantity");
            } else {
                return -1;
            }
        }
    }

    public void viewAllRequests() throws SQLException {
        String sql = "SELECT * FROM requests";


        try (Connection connection = DriverManager.getConnection(DB_URL);
             PreparedStatement statement = connection.prepareStatement(sql)) {
            ResultSet resultSet = statement.executeQuery();

            while (resultSet.next()) {
                System.out.println("Request ID: " + resultSet.getInt("id"));
                System.out.println("Book ID: " + resultSet.getInt("book"));
                System.out.println("Requester ID: " + resultSet.getInt("requester"));
                System.out.println("Status: " + resultSet.getString("status"));
                System.out.println("-------------------------");
            }
            resultSet.close();
        }
    }

    public String selectBookByOwner(int id) throws SQLException {
        String sql = "SELECT * FROM books where owner = ?";


        try (Connection connection = DriverManager.getConnection(DB_URL);
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, id);
            ResultSet resultSet = statement.executeQuery();
            ArrayList<ArrayList<String>> books = new ArrayList<>();
            int j = 0;
            while (resultSet.next()) {
                books.add(new ArrayList<String>());
                for(int i =1 ;i <= 6; i++ ){
                    books.get(j).add(resultSet.getString(i));
                }
                j++;
            }
            resultSet.close();
            ArrayList<String> result = new ArrayList<>();
            for(int i = 0 ;i < books.size(); i++){
                result.add(String.join(":", books.get(i)));
            }

            return String.join("!", result);
        }
    }

    public String acceptRequest(int requestId) throws SQLException {
        // 1. Get request details (book ID and requester ID)
        String sql = "SELECT book, requester FROM requests WHERE id = ?";
        int bookId = 0;
        int requesterId = 0;
        try (Connection connection = DriverManager.getConnection(DB_URL);
                PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, requestId);
            ResultSet resultSet = statement.executeQuery();

            if (!resultSet.next()) {
                throw new SQLException("Request not found"); // Handle non-existent request
            }

            bookId = resultSet.getInt("book");
            requesterId = resultSet.getInt("requester");
            resultSet.close();

            // 2. Check if book is available (quantity > 0)
        }catch(Exception e){
            e.printStackTrace();
            return "500";
        }
        int currentQuantity = getQuantity(bookId);
        if (currentQuantity <= 0) {
            throw new SQLException("Book not available"); // Handle unavailable book
        }

        sql = "UPDATE books SET owner = ?, quantity = quantity - 1 WHERE id = ?";
        try (Connection connection = DriverManager.getConnection(DB_URL);
        PreparedStatement statement = connection.prepareStatement(sql)) {
            // 3. Update book owner and decrement quantity
            statement.setInt(1, requesterId);
            statement.setInt(2, bookId);
            statement.executeUpdate();
        }catch(Exception e){
            e.printStackTrace();
            return "500";
        }
        // 4. Update request status to accepted
        sql = "UPDATE requests SET status = 'accepted' WHERE id = ?";
        try (Connection connection = DriverManager.getConnection(DB_URL);
        PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, requestId);
            statement.executeUpdate();

            System.out.println("Request accepted successfully!");
            return "200";
        }catch(Exception e){
            e.printStackTrace();
            return "500";
        }
    }

    public String rejectRequest(int requestId) throws SQLException {
        String sql = "UPDATE requests SET status = 'rejected' WHERE id = ?";

        try (Connection connection = DriverManager.getConnection(DB_URL);
                PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, requestId);
            statement.executeUpdate();
            System.out.println("Request rejected successfully!");
            return "200";
        }
    }
}

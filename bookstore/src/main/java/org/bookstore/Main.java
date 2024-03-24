package org.bookstore;

import org.bookstore.datastore.LibraryManager;

import java.io.IOException;
import java.net.ServerSocket;
import java.sql.SQLException;

public class Main {
    public static void main(String[] args) throws SQLException, ClassNotFoundException, IOException {
        Server server = new Server(new ServerSocket(8081));
        System.out.println("Server is starting on port 8081");
        server.startServer();
    }
}

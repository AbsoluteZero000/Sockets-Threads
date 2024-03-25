package org.bookstore;
import java.net.Socket;
import java.sql.SQLException;
import java.util.ArrayList;

import org.bookstore.datastore.LibraryManager;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.BufferedWriter;
import java.io.IOException;
public class ClientHandler implements Runnable {
    private Socket socket;
    private BufferedReader reader;
    private BufferedWriter writer;
    private LibraryManager libraryManager;
    private String username;
    private int userid;
    private Boolean isadmin;
    public ClientHandler(Socket socket){
        try{
            this.socket = socket;
            reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            writer = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
            username = "";
            userid = -1;
            libraryManager = new LibraryManager();
        }catch (Exception e){
            closeEverything(socket, reader, writer);
        }
    }
    private void closeEverything(Socket socket2, BufferedReader reader2, BufferedWriter writer2) {

        try {
            if(reader2 != null){
                reader2.close();
            }
            if(writer2 != null){
                writer2.close();
            }
            if(socket2 != null){
                socket2.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    public Boolean auth(String choice, BufferedWriter writer) throws IOException, SQLException{
        Boolean authenticated = false;
        if(choice == null) {
            writer.write("Please Enter you choice again");
            writer.newLine();
            writer.flush();}
        if(username.equals("")){
            if(choice.equals("1")){
                String username = reader.readLine();
                String password = reader.readLine();
                String Status = libraryManager.login(username, password);
                if(Status.split(":")[0].equals("200")){
                    this.username = username;
                    this.isadmin = Status.split(":")[1].split(";")[0].equals("ra");
                    this.userid = Integer.valueOf(Status.split(";")[1]);
                    authenticated = true;
                    System.out.println(username + "logged in correctly");
                }
                writer.write(Status);
                writer.newLine();
                writer.flush();
            }
            else if(choice.equals("2")){
                String username = reader.readLine();
                String password = reader.readLine();
                String name = reader.readLine();
                String Status = libraryManager.signup(username, password, name);
                if(Status.split(":")[0].equals("200")){
                    this.username = username;
                    authenticated = true;
                    System.out.println(username + "signed up correctly");
                }
                writer.write(Status);
                writer.newLine();
                writer.flush();
            }
            else{
                writer.write("400");
                writer.newLine();
                writer.flush();
            }
        }else{
            authenticated = true;
        }
        return authenticated;
    }
    @Override
    public void run() {

        while(socket.isConnected()){
            try{

                String choice = reader.readLine();
                if(!auth(choice, writer)){
                    continue;}
                while(true){
                    choice = reader.readLine();
                    switch (choice) {
                        case "1":
                            String books = libraryManager.viewAllBooks();
                            System.out.println(books);
                            writer.write(books);
                            writer.newLine();
                            writer.flush();
                            break;
                        case "2":
                            String choice2 = reader.readLine();
                            String searchQuery = reader.readLine();
                            String searchResult = "";
                            switch(choice2){
                                case "1":
                                    searchResult = libraryManager.selectBookByTitle(searchQuery);
                                    break;
                                case "2":
                                    searchResult = libraryManager.selectBookByAuthor(searchQuery);
                                    break;
                                case "3":
                                    searchResult = libraryManager.selectBookByGenre(searchQuery);
                                    break;
                            }
                            System.out.println(searchResult);
                            writer.write(searchResult);
                            writer.newLine();
                            writer.flush();
                            break;
                        case "3":
                            addBook();
                            break;
                        case "4":
                            deleteBook();
                            break;
                        case "5":
                            browseRequests();
                            break;
                        case "6":
                            submitRequest();
                            break;
                        case "7":
                            respondToRequest();
                            break;
                        case "8":
                            closeEverything(socket, reader, writer);
                            break;
                    }
                }
            }catch(IOException e){
                e.printStackTrace();
                closeEverything(socket, reader, writer);
                break;
            }catch(SQLException e){
                e.printStackTrace();
                closeEverything(socket, reader, writer);
                break;
            }catch(NullPointerException e){
                System.out.println(username + " has left the server");
            } catch (NumberFormatException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            } catch (ClassNotFoundException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
    }
private void respondToRequest() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'respondToRequest'");
    }
private void browseRequests() throws IOException, SQLException {
    writer.write(libraryManager.selectAllRequestsForOwnedBooks(userid));
    writer.newLine();
    writer.flush();

}
    private void submitRequest() throws IOException, SQLException {
        writer.write(libraryManager.viewAllBooks());
        writer.newLine();
        writer.flush();
        int bookId = Integer.valueOf(reader.readLine());
        String status = libraryManager.insertRequest(bookId, userid, "pending");
        writer.write(status);
        writer.newLine();
        writer.flush();
    }
    private void deleteBook() throws SQLException, IOException {
        writer.write(libraryManager.selectBookByOwner(userid));
        writer.newLine();
        writer.flush();
        int bookId = Integer.valueOf(reader.readLine());
        String status = libraryManager.deleteBook(bookId);
        writer.write(status);
        writer.newLine();
        writer.flush();
    }
    private void addBook() throws IOException, NumberFormatException, ClassNotFoundException, SQLException {
        String[] bookDetails = reader.readLine().split(":");
        String status = libraryManager.insertBook(bookDetails[0], bookDetails[1], bookDetails[2], Double.valueOf(bookDetails[3]), Integer.valueOf(bookDetails[4]), userid);
        writer.write(status);
        writer.newLine();
        writer.flush();
    }

}

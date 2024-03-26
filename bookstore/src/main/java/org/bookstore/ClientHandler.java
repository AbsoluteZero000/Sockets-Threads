package org.bookstore;
import java.net.Socket;
import java.sql.SQLException;
import java.util.ArrayList;
import java.net.SocketException;
import org.bookstore.datastore.LibraryManager;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.BufferedWriter;
import java.io.IOException;
import java.util.Map;
import java.util.HashMap;

public class ClientHandler implements Runnable {
    private static Map<Integer, Socket> availableUsers= new HashMap<>();
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
            availableUsers.remove(userid);
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
                    this.isadmin = Status.split(":")[1].split(";")[0].equals("na");
                    this.userid = Integer.valueOf(Status.split(";")[1]);
                    authenticated = true;
                    System.out.println(Status);
                    System.out.println(username + " logged in correctly");
                    availableUsers.put(userid, socket);
                    if(isadmin)
                        System.out.println("Welcome Admin "+ username);
                }
                writer.write(Status.split(";")[0]);
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
                    System.out.println(username + " signed up correctly");
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
                while(socket.isConnected()){
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
                            chat();
                            break;
                        case "9":
                            closeEverything(socket, reader, writer);
                            break;
                        case "10":
                            if(isadmin)
                                getStatistics();
                            else
                                writer.write("403");
                            break;
                    }
                }
            }catch(IOException e){
                System.out.println(username + " has left the server");
                closeEverything(socket, reader, writer);
                break;
            }catch(SQLException e){
                e.printStackTrace();
                break;
            }catch(NullPointerException e){
                System.out.println(username + " has left the server");
                closeEverything(socket, reader, writer);
            } catch (NumberFormatException e) {
                e.printStackTrace();
                break;
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
                break;
            }
        }
    }

    private void chat() throws SQLException, IOException {
        ArrayList<ArrayList<String>> results = libraryManager.selectAllAcceptedRequestsForOwnedBooks(userid);
        ArrayList<ArrayList<String>> available = new ArrayList<>();
        for(int i =0 ;i <results.size(); i++){
            if(availableUsers.containsKey(Integer.valueOf(results.get(i).get(0))))
                available.add(results.get(i));
        }
        for (Map.Entry<Integer, Socket> entry : availableUsers.entrySet())
            System.out.println(entry.getKey() + ": " + entry.getValue());

        ArrayList<String> result = new ArrayList<>();
        for (int i = 0; i < available.size(); i++) {
            result.add(String.join(":", available.get(i)));
        }
        writer.write(String.join("!", result));
        writer.newLine();
        writer.flush();

        int id = Integer.valueOf(reader.readLine());


        Socket targetSocket = availableUsers.get(id);
        BufferedReader targetreader = new BufferedReader(new InputStreamReader(targetSocket.getInputStream()));
        BufferedWriter targetwriter = new BufferedWriter(new OutputStreamWriter(targetSocket.getOutputStream()));
        Thread listenForMessage = new Thread(new Runnable() {
            @Override
            public void run() {
                String messageFromtarget;
                while(targetSocket.isConnected()){
                    try {
                        messageFromtarget = targetreader.readLine();
                        writer.write(messageFromtarget);
                        writer.newLine();
                        writer.flush();
                        if(messageFromtarget.equals("exit")){
                            writer.write("the other user has left the chat");
                            writer.newLine();
                            writer.flush();
                            closeEverything(targetSocket, targetreader, targetwriter);
                            break;
                        }
                    } catch (IOException e) {
                        closeEverything(targetSocket, targetreader, targetwriter);
                    }
                }
            }
        });
        listenForMessage.start();
        String messageFromServer;
        while(socket.isConnected()){

            messageFromServer = reader.readLine();
            if(messageFromServer.equals("exit")){
                if(targetwriter != null){
                    targetwriter.write("the other user has left the chat");
                    targetwriter.newLine();
                    targetwriter.flush();
                }
                    break;
            }
            if(targetwriter != null){
                targetwriter.write(messageFromServer);
                targetwriter.newLine();
                targetwriter.flush();
            }
            }

    }
    private void getStatistics() throws SQLException, IOException {
        String result = libraryManager.getLibraryStatistics();
        writer.write(result);
        writer.newLine();
        writer.flush();
    }
    private void respondToRequest() throws IOException, SQLException {
        browsePendingRequests();
        String id = reader.readLine();
        String response = reader.readLine();
        String status = "";
        if(response.equals("1")){
            status = libraryManager.acceptRequest(Integer.valueOf(id));
        }
        else if(response.equals("2")){
            status = libraryManager.rejectRequest(Integer.valueOf(id));

        }else{
            status = "400";
        }
        writer.write(status);
        writer.newLine();
        writer.flush();
    }
    private void browseRequests() throws IOException, SQLException {
        writer.write(libraryManager.selectAllRequestsForOwnedBooks(userid));
        writer.newLine();
        writer.flush();

    }
    private void browsePendingRequests() throws IOException, SQLException {
        writer.write(libraryManager.selectAllPendingRequestsForOwnedBooks(userid));
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

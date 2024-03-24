import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.util.Scanner;
import java.lang.Runnable;
import java.util.ArrayList;
public class Client {
    Socket socket;
    BufferedReader reader;
    BufferedWriter writer;
    String username;
    Boolean isAdmin;
    public Client(Socket socket, String username){
        this.socket = socket;
        try {
            reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            writer = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
            this.username = username;
        } catch (IOException e) {
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
    public void sendMessage(){
        try{


            Scanner scanner = new Scanner(System.in);
            while(socket.isConnected()){
                String messageToSend = scanner.nextLine();
                writer.write(messageToSend);
                writer.newLine();
                writer.flush();
            }
        }catch (IOException e){
            closeEverything(socket, reader, writer);
        }
    }
    public String readMessage() throws IOException{
        return reader.readLine();
    }
    public void sendMessage(String message){
        try{
            writer.write(message);
            writer.newLine();
            writer.flush();
        }catch (IOException e){
            closeEverything(socket, reader, writer);
        }
    }
    public void listenForMessage(){
        new Thread(new Runnable() {
            @Override
            public void run() {
                String messageFromServer;
                while(socket.isConnected()){
                    try {
                        messageFromServer = reader.readLine();
                        System.out.println(messageFromServer);
                    } catch (IOException e) {
                        closeEverything(socket, reader, writer);
                    }
                }
            }
        }).start();

    }
    public static void main(String[] args){
        Scanner scanner = new Scanner(System.in);
        System.out.print("Hello,\n1- Login\n2- Register\n3- Exit\n- ");
        String choice = scanner.nextLine();
        String username = "", password = "", name = "";

        try {
            Socket socket = new Socket("localhost", 8081);
            Client client = new Client(socket, username);
            client.sendMessage(choice);
            switch (choice) {
                case "1":
                    System.out.println("Login");
                    System.out.println("Enter username: ");
                    username = scanner.nextLine();
                    System.out.println("Enter password: ");
                    password = scanner.nextLine();
                    client.sendMessage(username);
                    client.sendMessage(password);
                    System.out.println(client.readMessage());
                    break;
                case "2":
                    System.out.println("Sign Up");
                    System.out.println("Enter username: ");
                    username = scanner.nextLine();
                    System.out.println("Enter password: ");
                    password = scanner.nextLine();
                    System.out.println("Enter name: ");
                    name = scanner.nextLine();
                    client.sendMessage(username);
                    client.sendMessage(password);
                    client.sendMessage(name);
                    String message = client.readMessage();
                    if(message.split(":")[0].equals("200")){
                        client.username = username;
                        client.isAdmin = message.split(":")[1].equals("a");
                    }
                    break;
                default:
                    System.exit(0);
                    break;
            }
            System.out.println("Hello, " + username + "!");
            while(true){
                System.out.print("Welcome to the menu:\n1- browse books\n2- search books\n3- add book\n4- delete book\n5- Submit a request\n6- respond to requests\n7- exit\n- ");
                choice = scanner.nextLine();
                client.sendMessage(choice);
                switch (choice) {
                    case "1":
                        client.browseBooks(client);
                        break;
                    case "2":
                        client.searchBooks(client, scanner);
                        break;
                    // case "3":
                    //     client.addBook();
                    //     break;
                    // case "4":
                    //     client.deleteBook();
                    //     break;
                    // case "5":
                    //     client.submitRequest();
                    //     break;
                    // case "6":
                    //     client.respondToRequest();
                    //     break;
                    case "7":
                        System.exit(0);
                }

            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    private void searchBooks(Client client, Scanner scanner) throws IOException {
        System.out.print("Search By:\n1- title\n2- author\n3- genre\n-");
        String choice = scanner.nextLine();
        System.out.println("Enter the search word:");
        String keyWord = scanner.nextLine();
        client.sendMessage(choice);
        client.sendMessage(keyWord);
        String recivedString = client.readMessage();
        if(recivedString.length() == 0){
            System.out.println("No books are available!");
            return;
        }
        String[] book = recivedString.split("!");
        for(String i: book){
            System.out.println();
            String[] bookStrings = i.split(":");
            System.out.print(bookStrings[0] + "- ");
            for(int j = 1;j < bookStrings.length;j++){
                System.out.print(bookStrings[j]+ " ");
            }
        }
        System.out.println();
    }
    private void browseBooks(Client client) throws IOException {
        String recivedString = client.readMessage();

        if(recivedString.length() == 0){
            System.out.println("No books are available!");
            return;
        }
        String[] book = recivedString.split("!");
        for(String i: book){
            System.out.println();
            String[] bookStrings = i.split(":");
            System.out.print(bookStrings[0] + "- ");
            for(int j = 1;j < bookStrings.length;j++){
                System.out.print(bookStrings[j]+ " ");
            }
        }
        System.out.println();
    }
}

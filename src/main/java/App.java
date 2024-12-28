import akka.actor.ActorRef;
import akka.actor.ActorSelection;
import akka.actor.ActorSystem;
import akka.actor.Props;

import java.util.Scanner;


public class App {

    private static Scanner scanner = new Scanner(System.in);
    private static String username;
    private static String input;
    private static ActorRef userActor;
    private static ActorSystem system = ActorSystem.create("ClientSystem");


    public static void main(String[] args) {

        // Login user
        username = promptUsername();
        // create user actor
        userActor = system.actorOf(Props.create(User.class, username), username);


        while (true) {
            menu();
            if (input.equals("exit")) {
                break;
            }
            switch (input) {
                case "1": {
                    startChatting();
                    break;
                }
                case "2": {
                    //profileSettings();
                    break;
                }
                case "3": // Video calling functionality
                    //startVideoCall();
                    break;
                default:
                    System.out.println("Invalid option.");
            }
        }




        //system.terminate();
    }




    private static String promptUsername() {
        System.out.println("Enter your username:");
        return scanner.nextLine();
    }

    private static String promptRecipient() {
        System.out.println("Enter your recipient:");
        return scanner.nextLine();
    }

    private static String promptMessage() {
        System.out.println("Enter your message:");
        return scanner.nextLine();
    }

    private static void menu() {
        System.out.println();
        System.out.println("Menu:");
        System.out.println("1 - Messaging");
        System.out.println("2 - Profile Settings");
        System.out.println("3 - VideoCall");
        System.out.println("exit - Quit WhatsApp");
        input = scanner.nextLine();
    }

    private static void startChatting(){
        String message;
        String recipient;

        // Specify the remote path to the server actor
        ActorSelection serverActor = system.actorSelection("akka://ServerSystem@127.0.0.1:2551/user/serverActor");
        // Send a message to the server actor
        serverActor.tell(new ChatServer.ConnectUser(username, userActor), userActor);

        System.out.println("\nEnter recipient name:");
        recipient = scanner.nextLine();

        System.out.println(Database.getChatHistory(username, recipient));

        System.out.println("Type your message below:");
        while (true) {
            message = scanner.nextLine();

            if (message.equals("back")) {
                serverActor.tell(new ChatServer.DisconnectUser(username, userActor), userActor);
                break;
            }
            serverActor.tell(new ChatServer.SendMessage(username, recipient, message), userActor);
        }
    }
}

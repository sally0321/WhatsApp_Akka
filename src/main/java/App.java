import akka.actor.ActorRef;
import akka.actor.ActorSelection;
import akka.actor.ActorSystem;
import akka.actor.Props;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Map;
import java.util.Scanner;
import java.util.concurrent.TimeUnit;


public class App {

    private static Scanner scanner = new Scanner(System.in);
    private static String username;
    private static String input;
    private static ActorRef userActor;
    private static ActorSystem system = ActorSystem.create("ClientSystem");


    public static void main(String[] args) {
    // test

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
        // system.terminate();
    }

    private static String promptUsername() {
        System.out.println("Enter your username:");
        return scanner.nextLine();
    }

    private static String promptRecipient() {
        ActorSelection serverActor = system.actorSelection("akka://ServerSystem@127.0.0.1:2551/user/serverActor");

        while (true) {
            showContactList();

            System.out.println("1 - Add contact");
            System.out.println("Back - Return to main menu");
            System.out.println("Enter recipient name:");

            String input = scanner.nextLine();

            if (input.equals("1")) {
                promptAddContact();
            } else if (input.equalsIgnoreCase("back")) {
                return input;
            } else if (!Database.getContacts(username).containsKey(input)) {
                System.out.println("Please enter a valid contact.");
            } else {
                Database.updateMessageStatus(username, input, 0);
                return input;
            }
        }
    }

    private static String promptAddContact() {
        ActorSelection serverActor = system.actorSelection("akka://ServerSystem@127.0.0.1:2551/user/serverActor");

        while (true) {
            System.out.println("Enter user to add (Enter back to cancel):");
            String input = scanner.nextLine();

            if (input.equals("back")) {
                if (Database.getContacts(username).isEmpty()) {
                    System.out.println("Contact list empty, please add a new contact.\n");
                }
                else {
                    break;
                }
            } else if (Database.getUsers().contains(input)) {
                if (Database.getContacts(username).containsKey(input)) {
                    System.out.println("Contact already exists.");
                } else {
                    serverActor.tell(new ChatServer.AddContact(username, input), userActor);
                    try {
                        TimeUnit.SECONDS.sleep(1);
                    } catch (InterruptedException e) {
                        System.out.println("Something went wrong while adding a contact.");
                    }
                }
                break;
            } else {
                System.out.println("User does not exist.\n");
            }
        }
        return input;
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

    private static void showContactList() {
        if (Database.getContacts(username).isEmpty()) {
            System.out.println("No contacts found. Please add a contact.");
            promptAddContact();
        } else {
            System.out.println("\n" + username + "'s contact list:");

            for (String contact : Database.getContacts(username).keySet()) {
                System.out.println(contact + " [" + Database.getContacts(username).get(contact) + " new messages]");
            }

            System.out.println();
        }
    }

    private static void startChatting(){
        String message;
        String recipient;

        // Specify the remote path to the server actor
        ActorSelection serverActor = system.actorSelection("akka://ServerSystem@127.0.0.1:2551/user/serverActor");

        // Send a message to the server actor
        serverActor.tell(new ChatServer.ConnectUser(username, userActor), userActor);

        while (true) {
            recipient = promptRecipient();

            if (recipient.equalsIgnoreCase("back")) {
                break;
            }

            serverActor.tell(new ChatServer.SetCommunicationChannel(username, recipient), userActor);

            System.out.println(Database.getChatHistory(username, recipient));
            System.out.println("Enter your message:");

            while (true) {
                message = scanner.nextLine();

                if (message.equals("back")) {
                    serverActor.tell(new ChatServer.CloseCommunicationChannel(username, recipient), userActor);
                    break;
                } else {
                    serverActor.tell(new ChatServer.SendMessage(username, recipient, message), userActor);
                }
            }

        }
        serverActor.tell(new ChatServer.DisconnectUser(username, userActor), userActor);
    }

}

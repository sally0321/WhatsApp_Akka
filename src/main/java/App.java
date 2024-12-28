import akka.actor.ActorRef;
import akka.actor.ActorSelection;
import akka.actor.ActorSystem;
import akka.actor.Props;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Map;
import java.util.Scanner;


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
        //system.terminate();
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
                String contact = promptAddContact();
                serverActor.tell(new ChatServer.AddContact(username, contact), userActor);
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
        while (true) {
            System.out.println("Enter user to add (Enter 0 to cancel):");
            String input = scanner.nextLine();

            if (input.equals("0")) {
                if (Database.getContacts(input).isEmpty()) {
                    System.out.println("Contact list empty, please add a new contact.\n");
                } else {
                    break;
                }
            } else if (Database.getUsers().contains(input)) {
                if (Database.getContacts(username).containsKey(input)) {
                    System.out.println("Contact already exists.\n");
                } else {
                    System.out.println("User added to contacts.\n");
                }
                break;
            } else {
                System.out.println("User does not exist.\n");
            }
        }
        return input;
    }

    private static String promptMessage(String recipient) {
        System.out.println(Database.getChatHistory(username, recipient));
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

    private static void showContactList() {
        Map<String, Integer> contacts = (Database.getContacts(username));

        if (contacts.isEmpty()) {
            System.out.println("No contacts found.");
            promptAddContact();
        } else {
            System.out.println(username + "'s contact list:");

            for (String contact : contacts.keySet()) {
                System.out.println(contact + " [" + contacts.get(contact) + " new messages]");
            }

            System.out.println();
        }
    }

    private static void startChatting(){
        String message;
        String recipient;

        // Specify the remote path to the server actor
        ActorSelection serverActor = system.actorSelection("akka://ServerSystem@127.0.0.1:2551/user/serverActor");

        while (true) {
            recipient = promptRecipient();

            if (recipient.equalsIgnoreCase("back")) {
                break;
            }

            // Send a message to the server actor
            serverActor.tell(new ChatServer.ConnectUser(username, userActor), userActor);

            while (true) {
                message = promptMessage(recipient);

                if (message.equals("back")) {
                    serverActor.tell(new ChatServer.DisconnectUser(username, userActor), userActor);
                    break;
                } else {
                    serverActor.tell(new ChatServer.SendMessage(username, recipient, message), userActor);
                }

            }
        }
    }

}

import akka.actor.ActorRef;
import akka.actor.ActorSelection;
import akka.actor.ActorSystem;
import akka.actor.Props;

import java.util.ArrayList;
import java.util.Map;
import java.util.Scanner;
import java.util.concurrent.TimeUnit;


public class App {

    private static Scanner scanner = new Scanner(System.in);
    private static String username;
    private static String phoneNum;
    private static String input;
    private static ActorRef userActor;
    private static ActorSystem system = ActorSystem.create("ClientSystem");


    public static void main(String[] args) {
        initialMenu();
        userActor = system.actorOf(Props.create(User.class, username), username);
        //Database.saveUser(username, phoneNum);


        while (true) {
            menu();
            if (input.equals("exit")) {
                system.terminate();
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
                    startCall();
                    break;
                default:
                    System.out.println("Invalid option.");
            }
        }

    }
    // Prompt user to enter their username
    private static String promptUsername() {
        System.out.println("Enter your username:");
        String username = scanner.nextLine();
        return username;
    }

    private static void initialMenu() {
        while (true) {
            System.out.println("\nWelcome to WhatsApp!");
            System.out.println("1 - Register");
            System.out.println("2 - Login");
            System.out.println("exit - Quit the app");
            input = scanner.nextLine();

            if (input.equalsIgnoreCase("exit")) {
                system.terminate();
                System.exit(0);
            }

            switch (input) {
                case "1": {
                    register();
                    return; // Exit the initial menu after registration
                }
                case "2": {
                    login();
                    return; // Exit the initial menu after successful login
                     // Retry on failed login
                }
                default:
                    System.out.println("Invalid option. Please try again.");
            }
        }
    }

    // Prompt user to enter their phone number
    private static String promptPhoneNum() {
        System.out.println("Enter your phone number:");
        String phoneNum = scanner.nextLine();
        return phoneNum;
    }

    // Register a new user
    private static void register() {

        System.out.println("Register a new account.");
        String newUsername = promptUsername();
        String newPhoneNum = promptPhoneNum();


        if (Database.getPhoneNumberIfExists(newPhoneNum) != null) {
            System.out.println("Phone number already registered. Please try logging in.");
            return;
        }

        Database.saveUser(newUsername, newPhoneNum);
        System.out.println("Registration successful! Welcome, " + newUsername + "!");

        // Set global username and phoneNum
        username = newUsername;
        phoneNum = newPhoneNum;
    }

    // Log in an existing user
    private static void login() {
        String newPhoneNum = promptPhoneNum();

        // Find the username associated with the phone number
        ArrayList<String> usernames = Database.getUsernameList();
        boolean found = false;

        for (String user : usernames) {
            if (Database.getPhoneNumber(user).equals(newPhoneNum)) {
                System.out.println("Welcome back, " + user + "!");
                found = true;
                username = user;
                phoneNum = newPhoneNum;
                break;
            }
        }

        if (!found) {
            System.out.println("Phone number not found. Please register first.");
        }
    }

    private static String promptRecipient() {
        while (true) {
            if (Database.getContacts(username).isEmpty()) {
                System.out.println("You have no contacts. Please add a contact.");
                promptAddContact();
            }
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
                    break;
                } else {
                    Database.saveContact(username, input);
                    System.out.println("User added to contacts.");
                    break;
                }
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
        System.out.println("3 - Call");
        System.out.println("exit - Quit WhatsApp");
        input = scanner.nextLine();
    }

    private static void showContactList() {
        Map<String, Integer> contacts = Database.getContacts(username);

        System.out.println("\n" + username + "'s contact list:");

        for (String contact : contacts.keySet()) {
            System.out.println(contact + " [" + contacts.get(contact) + " new messages]");
        }

        System.out.println();

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

    private static void startCall() {
        // Create the actor selection inside the method
        ActorSelection callServerActor = system.actorSelection("akka://CallServerSystem@127.0.0.1:2552/user/callServer");
        System.out.println("Enter the recipient to call:");
        String targetUsername = scanner.nextLine();

        // Initiate call
        callServerActor.tell(new CallServer.InitiateCall(username, targetUsername), userActor);
        System.out.println("Call initiated. Waiting for " + targetUsername + " to accept or reject the call.");

        while (true) {
            System.out.println("Press `0` to end the call. ");
            String input = scanner.nextLine();

            if ("0".equals(input)) {
                callServerActor.tell(new CallServer.EndCall(username), userActor);
                System.out.println("You ended the call.");
                break;
            } else {
                System.out.println("Invalid input. Press `0` to end the call. ");
            }
        }
    }
}

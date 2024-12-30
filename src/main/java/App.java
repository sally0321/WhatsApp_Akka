import akka.actor.ActorRef;
import akka.actor.ActorSelection;
import akka.actor.ActorSystem;
import akka.actor.Props;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.concurrent.TimeUnit;


public class App {

    private static final Scanner scanner = new Scanner(System.in);

    private static String username;
    private static String phoneNum;
    private static String input;
    private static ActorRef userActor;

    
    private static final ActorSystem system = ActorSystem.create("ClientSystem");


    private static final ActorSelection authServer = system.actorSelection(
            "akka://AuthenticationServerSystem@127.0.0.1:2553/user/authenticationServer"
    );

    public static void main(String[] args) {
        
        initialMenu();

        
        userActor = system.actorOf(Props.create(User.class, username), username);

        
        ActorSelection callServerActor = system.actorSelection(
                "akka://CallServerSystem@127.0.0.1:2552/user/callServer"
        );
        // e.g., connect the user to the call server
        callServerActor.tell(new CallServer.ConnectUser(username, userActor), userActor);

        
        while (true) {
            menu(); // display the main menu
            if (input.equalsIgnoreCase("exit")) {
                system.terminate();
                break;
            }
            switch (input) {
                case "1": {
                    startChatting();
                    break;
                }
                case "2": {
                    profileSettings();
                    break;
                }
                case "3": {
                    // Video calling functionality (placeholder)
                    startCall();
                    break;
                }
                case "4": {
                    accountSettings();
                    break;
                }
                default:
                    System.out.println("Invalid option.");
            }
        }
    }

    /**
     * Displays the initial registration/login menu in a loop until
     * the user successfully registers or logs in (so that username != null).
     */
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
                    boolean registered = register();
                    if (registered) {
                        // If registration succeeded, we have a valid username.
                        return; // exit initialMenu and proceed to main
                    }
                    // If registration failed (e.g. phone number exists),
                    // we do not return, so we remain in this while-loop,
                    // re-displaying the initial menu.
                    break;
                }
                case "2": {
                    boolean loggedIn = login();
                    if (loggedIn) {
                        // If login succeeded, we have a valid username.
                        return; // exit initialMenu and proceed to main
                    }
                    // If login failed, we remain in the loop
                    break;
                }
                default:
                    System.out.println("Invalid option. Please try again.");
            }
        }
    }

    /**
     * Attempts to register a new account. Returns true if successful, false otherwise.
     */
    private static boolean register() {
        System.out.println("Register a new account.");
        String newUsername = promptUsername();
        String newPhoneNum = promptPhoneNum();

        // Check if phone number is already in DB
        if (Database.getPhoneNumberIfExists(newPhoneNum) != null) {
            System.out.println("Phone number already registered. Please try logging in.");
            return false; // Registration fails; stay in initialMenu
        }

        // Otherwise, proceed with registration
        Database.saveUser(newUsername, newPhoneNum);
        System.out.println("Registration successful! Welcome, " + newUsername + "!");

        // Set global username and phoneNum
        username = newUsername;
        phoneNum = newPhoneNum;

        return true; // Registration succeeded; exit initialMenu
    }

    /**
     * Attempts to log in an existing user. Returns true if successful, false otherwise.
     */
    private static boolean login() {
        String inputPhoneNum = promptPhoneNum();

        // Find the username(s) associated with the phone number
        ArrayList<String> usernames = Database.getUsernameList();
        for (String user : usernames) {
            List<String> phoneNumbers = Database.getPhoneNumbers(user);
            if (phoneNumbers.contains(inputPhoneNum)) {
                // Found a matching user
                System.out.println("Welcome back, " + user + "!");
                username = user;
                phoneNum = inputPhoneNum;
                return true; // Login succeeded
            }
        }
        System.out.println("No user found with that phone number. Please try again.");
        return false; // Login failed; stay in initialMenu
    }

    private static void menu() {
        System.out.println("\nMain Menu:");
        System.out.println("1 - Start Chatting");
        System.out.println("2 - Profile Settings");
        System.out.println("3 - Video Calling");
        System.out.println("4 - Account Settings");
        System.out.println("exit - Quit the app");
        input = scanner.nextLine();
    }

    private static String accountSettings() {
        System.out.println("1 - Logout");
        System.out.println("2 - Delete account");
        System.out.println("Back - Return to main menu");

        String input = scanner.nextLine();

        if (input.equals("1")) {
            logout();
        } else if (input.equalsIgnoreCase("back")) {
            return input;
        } else if (input.equals("2")) {
            deleteAccount();
        } else {
            System.out.println("Invalid option. Please try again.");
        }
        return input;
    }

    private static void logout() {
        username = null;
        phoneNum = null;
        System.out.println("You have been logged out.");
        // Return user to initial menu
        initialMenu();
    }

    private static void deleteAccount() {
        // Confirm deletion
        System.out.println("Are you sure you want to delete your account? This action cannot be undone. (yes/no)");
        String confirmation = scanner.nextLine();

        if (!confirmation.equalsIgnoreCase("yes")) {
            System.out.println("Account deletion canceled.");
            return;
        }

        // Call the Database method to handle the actual deletion
        boolean success = Database.deleteUserAccount(username, phoneNum);

        if (success) {
            System.out.println("Account successfully deleted.");
            logout(); // Log out the user after account deletion
        } else {
            System.out.println("There was an error deleting your account.");
        }
    }

    // ======================================
    // Helper methods for user interactions
    // ======================================

    // Prompt user to enter their username
    private static String promptUsername() {
        System.out.println("Enter your username:");
        return scanner.nextLine();
    }

    // Prompt user to enter their phone number
    private static String promptPhoneNum() {
        System.out.println("Enter your phone number:");
        return scanner.nextLine();
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
/*
    private static void menu() {
        System.out.println();
        System.out.println("Menu:");
        System.out.println("1 - Messaging");
        System.out.println("2 - Profile Settings");
        System.out.println("3 - Call");
        System.out.println("4 - Account Settings");
        System.out.println("exit - Quit WhatsApp");
        input = scanner.nextLine();
    }

 */

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

        System.out.println("\nStart a call by entering recipient name. \nBack - return to main menu");


        while (true) {
            String input = scanner.nextLine();

            // Send all inputs to CallServer as a normal string
            callServerActor.tell(input, userActor);

            if (input.equals("back")) {
                callServerActor.tell(new CallServer.DisconnectUser(username, userActor), userActor);
                break; // Exit the loop when the user ends the call
            }
        }
    }

    private static void profileSettings() {
        ActorSelection serverActor = system.actorSelection("akka://ServerSystem@127.0.0.1:2553/user/serverActor");
        System.out.println("1 - View Profile");
        System.out.println("2 - Update Username");
        System.out.println("3 - Update Bio");
        System.out.println("back - Return to main menu");

        String option = scanner.nextLine();

        switch (option) {
            case "1": // View Profile
                serverActor.tell(new ProfileServer.ViewProfile(username, bio), userActor);
                break;

            case "2": // Update Username
                System.out.println("Enter your new username:");
                String oldUsername = username;
                username = scanner.nextLine(); // Update the username locally
                serverActor.tell(new ProfileServer.UpdateUsername(oldUsername, username, bio), userActor);
                break;

            case "3": // Update Bio
                System.out.println("Enter your new bio:");
                bio = scanner.nextLine(); // Update the bio locally
                serverActor.tell(new ProfileServer.UpdateBio(username, bio), userActor);
                break;

            case "back":
                menu();
                break;

            default:
                System.out.println("Invalid option.");
        }

        try {
            Thread.sleep(500); // Simulate waiting for server feedback
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}



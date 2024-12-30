import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Database {

    // Constants for directory paths
    private static final String CHAT_DIRECTORY = "src/main/resources/chat_history/";
    private static final String CONTACT_DIRECTORY = "src/main/resources/contact_list/";
    private static final String USER_FILE = "src/main/resources/user_list.txt";
    private static final String TEST_FILE = "src/main/resources/testUser_list.txt";
    private static final String MVN_DIRECTORY = "src/main/resources/chat_history/";


    // Save the message in a text file named after the users
    public static void saveMessage(String sender, String recipient, String message) {
        String fileName = getSortedFileName(sender, recipient);

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(CHAT_DIRECTORY + fileName, true))) {
            writer.write(message);
            writer.newLine();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void saveContact(String user, String contact) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(CONTACT_DIRECTORY + user + ".txt", true))) {
            writer.write(contact + ",0");
            writer.newLine();
        } catch (IOException e) {
            e.printStackTrace();
        }

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(CONTACT_DIRECTORY + contact + ".txt", true))) {
            writer.write(user + ",0");
            writer.newLine();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void updateMessageStatus(String recipient, String sender, int messageCount) {
        Map<String, Integer> contacts = getContacts(recipient);
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(CONTACT_DIRECTORY + recipient + ".txt"))) {
            contacts.put(sender, messageCount);
            for (String contact : contacts.keySet()) {
                writer.write(contact + "," + contacts.get(contact));
                writer.newLine();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Function to print the content of the chat history file
    public static String getChatHistory(String userA, String userB) {

        String fileName = getSortedFileName(userA, userB);
        String chat = "";

        // Create the full file path
        File file = new File(CHAT_DIRECTORY + fileName);

        // Check if the file exists
        if (!file.exists()) {
            try {
                new FileWriter(CHAT_DIRECTORY + fileName, true);
                return ("\nNew chat created for " + userA + " and " + userB + "!\n");
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line = reader.readLine();
            if (line == null) {
                // If the first line is null, the chat is new
                return "\nNew chat created for " + userA + " and " + userB + "!\n";
            } else {
                chat += ("\nChat history between " + userA + " and " + userB + ":\n");
                chat += line + '\n';
                while ((line = reader.readLine()) != null) {
                    chat = chat + line + '\n';
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return chat;
    }

    public static Map<String, Integer> getContacts(String user) {
        File file = new File(CONTACT_DIRECTORY + user + ".txt");
        Map<String, Integer> contacts = new HashMap<String, Integer>();

        // Check if the file exists
        if (!file.exists()) {
            try {
                new FileWriter(CONTACT_DIRECTORY + user + ".txt", true);
                return contacts;
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;

            while ((line = reader.readLine()) != null) {
                String[] contactPair = line.split(",");
                contacts.put(contactPair[0], Integer.parseInt(contactPair[1]));
            }

        } catch (IOException e) {
            e.printStackTrace();
        } catch (ArrayIndexOutOfBoundsException e) {
            return contacts;
        }
        return contacts;
    }

    public static ArrayList<String> getUsers() {
        File file = new File(USER_FILE);
        ArrayList<String> users = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;

            while ((line = reader.readLine()) != null) {
                users.add(line);
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
        return users;
    }

    // Ensure the file name is in sorted order (userA_userB.txt)
    private static String getSortedFileName(String userA, String userB) {
        return userA.compareTo(userB) < 0 ? userA + "_" + userB + ".txt" : userB + "_" + userA + ".txt";
    }

    public static void saveUser(String username, String phoneNumber) {
        List<String> phoneNumbers = getPhoneNumbers(username);
        if (phoneNumbers.contains(phoneNumber)) {
            System.out.println("This username and phone number combination already exists.");
            return;
        }

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(TEST_FILE, true))) {
            writer.write(username + "," + phoneNumber);  // Save username and phone number
            writer.newLine();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Function to get the list of all usernames
    public static ArrayList<String> getUsernameList() {
        File file = new File(TEST_FILE);
        ArrayList<String> usernames = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;

            while ((line = reader.readLine()) != null) {
                usernames.add(line.split(",")[0]);  // Extract only the username (before the comma)
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
        return usernames;
    }

    public static List<String> getPhoneNumbers(String username) {
        File file = new File(TEST_FILE);
        List<String> phoneNumbers = new ArrayList<>();

        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(",");
                if (parts[0].equals(username)) {
                    phoneNumbers.add(parts[1]); // Add all phone numbers for the username
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return phoneNumbers; // Return all matching phone numbers
    }

    public static String getPhoneNumberIfExists(String phoneNum) {
        File file = new File(TEST_FILE);
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] userData = line.split(","); // Assuming each line is "username,phoneNumber"
                if (userData.length > 1 && userData[1].equals(phoneNum)) {
                    return userData[1]; // Return the phone number if it exists
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null; // Return null if the phone number is not found
    }

    public static boolean deleteUserAccount(String username, String phoneNum) {
        File userFile = new File(TEST_FILE);
        List<String> updatedUsers = new ArrayList<>(); // List to hold the remaining users

        try (BufferedReader reader = new BufferedReader(new FileReader(userFile))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(",");
                if (!(parts[0].equals(username) && parts[1].equals(phoneNum))) {
                    updatedUsers.add(line); // Keep lines that don't match both username and phone number
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
            return false; // Return false if an error occurs during file reading
        }

        // Write the updated users back to the file
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(userFile, false))) {
            for (String user : updatedUsers) {
                writer.write(user);
                writer.newLine();
            }
        } catch (IOException e) {
            e.printStackTrace();
            return false; // Return false if an error occurs during file writing
        }

        return true; // Return true if deletion was successful
    }


}

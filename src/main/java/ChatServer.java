import akka.actor.AbstractActor;
import akka.actor.ActorSystem;
import akka.actor.Props;
import akka.actor.ActorRef;

import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.Map;
import java.io.Serializable;


public class ChatServer extends AbstractActor {
    private static final SimpleDateFormat timeFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
    private static Timestamp timestamp;
    private Map<String, ActorRef> userActors = new HashMap<>();
    private Map<String, String> chats = new HashMap<>();

    static public Props props() {
        return Props.create(ChatServer.class, () -> new ChatServer());
    }

    @Override
    public Receive createReceive() {
        return receiveBuilder()
                .match(String.class, message -> {
                    System.out.println("Server received: " + message);
                    //getSender().tell("Acknowledgment: Received your message", getSelf());
                })
                .match(DisconnectUser.class, message -> {
                    System.out.println("Server disconnect " + message.username);
                    System.out.println(userActors);
                    userActors.remove(message.username);
                    //getSender().tell(message.username + " log in successfully!", getSelf());
                })
                .match(ConnectUser.class, message -> {
                    System.out.println("Server connect " + message.username);
                    System.out.println(userActors);
                    userActors.put(message.username, message.userActor);
                    //getSender().tell(message.username + " log in successfully!", getSelf());
                })
                .match(SetCommunicationChannel.class, message -> {
                    System.out.println("Communication channel set between " + message.sender + " and " + message.recipient);
                    chats.put(message.sender, message.recipient);
                })
                .match(CloseCommunicationChannel.class, message -> {
                    System.out.println("Communication channel closed between " + message.sender + " and " + message.recipient);
                    chats.remove(message.sender);
                })
                .match(SendMessage.class, message -> {
                    timestamp = new Timestamp(System.currentTimeMillis());
                    String time = timeFormat.format(timestamp);
                    String lastUser = getLastUser(message.sender, message.recipient);

                    if (lastUser.equals(message.sender)) {
                        message.message = "[" + time + "]\n[" + message.sender + "]:" + message.message;
                    } else{
                        message.message = "\n[" + time + "]\n[" + message.sender + "]:" + message.message;
                    }

                    Database.saveMessage(message.sender, message.recipient, message.message);

                    Integer newMessagesCount = Database.getContacts(message.recipient).get(message.sender);
                    newMessagesCount ++;
                    Database.updateMessageStatus(message.recipient, message.sender, newMessagesCount);

                    System.out.println("Message sent from " + message.sender + " to " + message.recipient);

                    if (userActors.containsKey(message.sender)) {
                        ActorRef senderActor = userActors.get(message.sender);

                        senderActor.tell(Database.getChatHistory(message.sender, message.recipient), getSelf());
                        senderActor.tell("Enter your message:", getSelf());

                        if (userActors.containsKey(message.recipient) && message.sender.equals(chats.get(message.recipient)) && !message.sender.equals(message.recipient)) {
                            newMessagesCount = 0;

                            ActorRef recipientActor = userActors.get(message.recipient);
                            recipientActor.tell(Database.getChatHistory(message.sender, message.recipient), getSelf());
                            recipientActor.tell("Enter your message:", getSelf());

                            Database.updateMessageStatus(message.recipient, message.sender, newMessagesCount);
                        }
                    }
                })
                .match(AddContact.class, message -> {
                    Database.saveContact(message.username, message.contact);
                    getSender().tell("User added to contacts.", getSelf());
                })
                .build();
    }

    public static void main(String[] args) {
        ActorSystem system = ActorSystem.create("ServerSystem");
        ActorRef serverActor = system.actorOf(ChatServer.props(), "serverActor");
    }

    // Get the user who sent last message
    public static String getLastUser(String sender, String recipient){
        String chat = Database.getChatHistory(sender, recipient);

        try {
            String[] lines = chat.split("\n");
            String[] lastMessage = lines[lines.length - 1].split("[\\[\\]]");
            return lastMessage[1];

        } catch (ArrayIndexOutOfBoundsException e) {
            return sender;
        }
    }

    public static class ConnectUser implements Serializable {
        public final String username;
        public final ActorRef userActor;

        public ConnectUser(String username, ActorRef userActor) {
            this.username = username;
            this.userActor = userActor;
        }
    }

    public static class DisconnectUser extends ConnectUser {
        public DisconnectUser(String username, ActorRef userActor) {
            super(username, userActor);
        }
    }

    public static class SendMessage implements Serializable {
        public final String sender;
        public final String recipient;
        public String message;

        public SendMessage(String sender, String recipient, String message) {
            this.sender = sender;
            this.recipient = recipient;
            this.message = message;
        }
    }

    public static class AddContact implements Serializable {
        public final String username;
        public final String contact;

        public AddContact(String contact, String username) {
            this.contact = contact;
            this.username = username;
        }
    }

    public static class SetCommunicationChannel implements Serializable {
        public final String sender;
        public final String recipient;

        public SetCommunicationChannel(String sender, String recipient) {
            this.sender = sender;
            this.recipient = recipient;
        }
    }

    public static class CloseCommunicationChannel implements Serializable {
        public final String sender;
        public final String recipient;

        public CloseCommunicationChannel(String sender, String recipient) {
            this.sender = sender;
            this.recipient = recipient;
        }
    }


}






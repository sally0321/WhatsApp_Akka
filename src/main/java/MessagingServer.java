import akka.actor.AbstractActor;
import akka.actor.ActorSystem;
import akka.actor.Props;
import akka.actor.ActorRef;
import akka.io.Udp;

import java.util.HashMap;
import java.util.Map;
import java.io.Serializable;


public class MessagingServer extends AbstractActor {

    private Map<String, ActorRef> userActors = new HashMap<>();

    static public Props props() {
        return Props.create(MessagingServer.class, () -> new MessagingServer());
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
                .match(SendMessage.class, message -> {
                    if (userActors.containsKey(message.recipient)){
                        ActorRef recipientActor = userActors.get(message.recipient);
                        recipientActor.tell(new SendMessage(message.sender, message.recipient, message.message), getSelf());
                    }
                    else {
                        System.out.println("User not online");
                    }
                })
                .build();
    }

    public static void main(String[] args) {
        ActorSystem system = ActorSystem.create("ServerSystem");
        ActorRef serverActor = system.actorOf(MessagingServer.props(), "serverActor");
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
        public final String message;

        public SendMessage(String sender, String recipient, String message) {
            this.sender = sender;
            this.recipient = recipient;
            this.message = message;
        }
    }
}






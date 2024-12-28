import akka.actor.AbstractActor;
import akka.actor.Props;

public class User extends AbstractActor {
    private final String username;

    public User(String username) {
        this.username = username;
    }

    public static Props props(String username) {
        return Props.create(User.class, () -> new User(username));
    }

    @Override
    public Receive createReceive() {
        return receiveBuilder()
                .match(String.class, message -> {
                    System.out.println("User received: " + message);
                })
                .match(MessagingServer.SendMessage.class, message -> {
                    System.out.println("[" + message.sender + "]: " + message.message);
                })
                .build();
    }
}
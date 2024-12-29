import akka.actor.AbstractActor;
import akka.actor.Props;
import java.util.Scanner;

public class User extends AbstractActor {

    private final String username;
    private static final Scanner scanner = new Scanner(System.in);

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
                System.out.println(message);
            })
            .match(ChatServer.SendMessage.class, message -> {
                System.out.println("[" + message.sender + "]: " + message.message);
            })
            // Handle incoming call
            .match(CallServer.IncomingCall.class, msg -> {
                System.out.println(msg.callerUsername + " is calling you.");
                System.out.println("Y = Accept");
                System.out.println("N = Reject");

                String response = scanner.nextLine();
                if ("Y".equalsIgnoreCase(response)) {
                    getSender().tell(new CallServer.RespondCall(username, true), getSelf());
                    System.out.println("You accepted the call from " + msg.callerUsername + ".");
                } else {
                    getSender().tell(new CallServer.RespondCall(username, false), getSelf());
                    System.out.println("You rejected the call from " + msg.callerUsername + ".");
                }
            })
            // Notify user about call state changes
            .match(CallServer.CallAccepted.class, msg -> {
                System.out.println("Call accepted by " + msg.username + ". Press `0` to end the call.");
                while (true) {
                    String input = scanner.nextLine();
                    if ("0".equals(input)) {
                        getSender().tell(new CallServer.EndCall(username), getSelf());
                        System.out.println("You ended the call.");
                        break;
                    } else {
                        System.out.println("Invalid input. Press `0` to end the call.");
                    }
                }
            })

            .match(CallServer.CallRejected.class, msg -> {
                System.out.println("Call rejected by " + msg.username + ".");
            })
            .match(CallServer.CallEnded.class, msg -> {
                System.out.println("Call ended by " + msg.username + ".");
            })

            .build();
    }
}

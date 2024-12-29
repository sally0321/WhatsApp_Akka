import akka.actor.AbstractActor;
import akka.actor.Props;
import java.util.Scanner;

public class User extends AbstractActor {

    private String username;
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
                        // Simulate ongoing call
                        handleOngoingCall();
                    } else if ("N".equalsIgnoreCase(response)) {
                        getSender().tell(new CallServer.RespondCall(username, false), getSelf());
                        System.out.println("You rejected the call from " + msg.callerUsername + ".");
                    } else {
                        System.out.println("Invalid option. Returning to the menu...");
                    }
                })
                // Notify user about call acceptance
                .match(CallServer.CallAccepted.class, msg -> {
                    System.out.println("Call accepted by " + msg.username + ". Press `0` to end the call.");
                    handleOngoingCall();
                })
                // Notify user about call rejection
                .match(CallServer.CallRejected.class, msg -> {
                    System.out.println("Call rejected by " + msg.username + ".");
                    System.out.println("Returning to the main menu...");
                })
                // Notify user about call ending
                .match(CallServer.CallEnded.class, msg -> {
                    System.out.println("Call ended by " + msg.username + ".");
                    System.out.println("Returning to the main menu...");
                })
                // Handle call failure
                .match(CallServer.CallFailed.class, msg -> {
                    System.out.println(msg.message);
                    System.out.println("Returning to the main menu...");
                })
                .build();
    }

    private void handleOngoingCall() {
        while (true) {
            System.out.println("Press `0` to end the call.");
            String input = scanner.nextLine();
            if ("0".equals(input)) {
                getSender().tell(new CallServer.EndCall(username), getSelf());
                System.out.println("You ended the call.");
                break;
            } else {
                System.out.println("Invalid input. Press `0` to end the call.");
            }
        }
    }
}
                .match(ProfileServer.ViewProfile.class, msg -> {
                    System.out.println("Profile for " + msg.username + ":");
                })
                .match(ProfileServer.UpdateUsername.class, msg -> {
                    System.out.println("Username updated from " + username + " to " + msg.newUsername);
                    username = msg.newUsername; // Update local username
                })
                .match(ProfileServer.UpdateBio.class, msg -> {
                    System.out.println("Bio updated for " + msg.username);
                })
                .match(ProfileServer.ViewProfileResponse.class, response -> {
                    System.out.println("Username: " + response.username + ", Bio: " + response.bio);
                })
                .match(ProfileServer.ErrorResponse.class, error -> {
                    System.out.println("Error: " + error.message);
                })
                .build();
    }
}

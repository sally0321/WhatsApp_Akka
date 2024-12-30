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
                    System.out.println("\n" + msg.callerUsername + " is calling you.");
                    System.out.println("Y = Accept");
                    System.out.println("N = Reject");
                    System.out.println("0 = End Call");

                })
                // Notify user about call acceptance
                .match(CallServer.CallAccepted.class, msg -> {
                    System.out.println("Call accepted by " + msg.username + ". Press `0` to end the call.");

                })
                // Notify user about call rejection
                .match(CallServer.CallRejected.class, msg -> {
                    System.out.println("Call rejected by " + msg.username + ".");
                    System.out.println("\nStart a call by entering recipient name. \nBack - return to main menu");
                })
                // Notify user about call ending
                .match(CallServer.CallEnded.class, msg -> {
                    System.out.println("Call ended by " + msg.username + ".");
                    System.out.println("\nStart a call by entering recipient name. \nBack - return to main menu");
                })
                // Handle call failure
                .match(CallServer.CallFailed.class, msg -> {
                    System.out.println(msg.message);
                    System.out.println("\nStart a call by entering recipient name. \nBack - return to main menu");
                })
                .build();
    }

}

/*
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
*/
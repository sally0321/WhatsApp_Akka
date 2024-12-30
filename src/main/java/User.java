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

            .match(ProfileServer.ViewProfile.class, message -> {
                    // View the profile with the current username and bio
                    String bio = message.bio == null ? "No bio available." : message.bio;
                    System.out.println("Display profile for " + message.username);
                    getSender().tell("Username: " + message.username + "\nBio: " + bio, getSelf());
                })
                .match(ProfileServer.UpdateUsername.class, message -> {
                    // Handle username update and preserve bio
                    System.out.println("Username updated from " + message.oldUsername + " to " + message.username);
                    getSender().tell(new ProfileServer.ViewProfile(message.username, message.bio), getSelf());
                })
                .match(ProfileServer.UpdateBio.class, message -> {
                    // Handle bio update
                    System.out.println("Bio updated for " + message.username + " to " + message.bio);
                    getSender().tell(new ProfileServer.ViewProfile(message.username, message.bio), getSelf());
                })
                .build();
    }

}

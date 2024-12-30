import akka.actor.AbstractActor;
import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.Props;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

public class ProfileServer extends AbstractActor {
    private static Scanner scanner = new Scanner(System.in);
    private final Map<String, String> profiles = new HashMap<>();

    public static Props props() {
        return Props.create(ProfileServer.class, ProfileServer::new);
    }

    @Override
    public Receive createReceive() {
        return receiveBuilder()
                .match(ViewProfile.class, message -> {
                    String bio = profiles.getOrDefault(message.username, "No bio available.");
                    System.out.println("Display profile for " + message.username);
                    getSender().tell("Username: " + message.username + "\nBio: " + bio, getSelf());
                })
                .match(UpdateUsername.class, message -> {
                    profiles.put(message.oldUsername, message.username);
                    System.out.println("Username updated for " + message.username);
                    getSender().tell("Username updated for " + message.username + ".", getSelf());
                })
                .match(UpdateBio.class, message -> {
                    profiles.put(message.username, message.bio);
                    System.out.println("Bio updated for " + message.username + " to " + message.bio);
                    getSender().tell("Bio updated for " + message.username + ".", getSelf());
                })
                .build();
    }

    public static class ViewProfile implements Serializable {
        public final String username;
        public final String bio;

        public ViewProfile(String username, String bio) {
            this.username = username;
            this.bio = bio;
        }
    }

    public static class UpdateUsername implements Serializable {
        public final String oldUsername;
        public final String username;
        public final String bio;

        public UpdateUsername(String oldUsername, String username, String bio) {
            this.oldUsername = oldUsername;
            this.username = username;
            this.bio = bio; // Preserve the bio during username updates
        }
    }

    public static class UpdateBio implements Serializable {
        public final String username;
        public final String bio;

        public UpdateBio(String username, String bio) {
            this.username = username;
            this.bio = bio;
        }
    }


    public static void main(String[] args) {
        ActorSystem system = ActorSystem.create("ServerSystem");
        ActorRef serverActor = system.actorOf(ProfileServer.props(), "serverActor");
        while (true){
            if (scanner.nextLine().equalsIgnoreCase("exit")){
                system.terminate();
                break;
            }
        }
    }
}

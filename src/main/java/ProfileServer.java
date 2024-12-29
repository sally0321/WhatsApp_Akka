import akka.actor.AbstractActor;
import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.Props;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

import akka.actor.AbstractActor;
import akka.actor.Props;
import akka.actor.Status;

import java.util.HashMap;
import java.util.Map;

public class ProfileServer extends AbstractActor {
    private final Map<String, String> profiles = new HashMap<>();

    public static Props props() {
        return Props.create(ProfileServer.class);
    }

    @Override
    public Receive createReceive() {
        return receiveBuilder()
                .match(ViewProfile.class, this::handleViewProfile)
                .match(UpdateUsername.class, this::handleUpdateUsername)
                .match(UpdateBio.class, this::handleUpdateBio)
                .build();
    }

    private void handleViewProfile(ViewProfile msg) {
        if (profiles.containsKey(msg.username)) {
            getSender().tell("Username: " + msg.username + ", Bio: " + profiles.get(msg.username), getSelf());
        } else {
            getSender().tell(new Status.Failure(new Exception("Profile not found")), getSelf());
        }
    }

    private void handleUpdateUsername(UpdateUsername msg) {
        if (profiles.containsKey(msg.oldUsername)) {
            String bio = profiles.remove(msg.oldUsername);
            profiles.put(msg.newUsername, bio);
            getSender().tell("Username updated successfully to: " + msg.newUsername, getSelf());
        } else {
            getSender().tell(new Status.Failure(new Exception("Profile not found")), getSelf());
        }
    }

    private void handleUpdateBio(UpdateBio msg) {
        if (profiles.containsKey(msg.username)) {
            profiles.put(msg.username, msg.newBio);
            getSender().tell("Bio updated successfully to: " + msg.newBio, getSelf());
        } else {
            getSender().tell(new Status.Failure(new Exception("Profile not found")), getSelf());
        }
    }

    public static class ViewProfile {
        public final String username;

        public ViewProfile(String username) {
            this.username = username;
        }
    }

    public static class UpdateUsername {
        public final String oldUsername;
        public final String newUsername;

        public UpdateUsername(String oldUsername, String newUsername) {
            this.oldUsername = oldUsername;
            this.newUsername = newUsername;
        }
    }

    public static class UpdateBio {
        public final String username;
        public final String newBio;

        public UpdateBio(String username, String newBio) {
            this.username = username;
            this.newBio = newBio;
        }
    }

    public class ViewProfileResponse {
        public String username;
        public String bio;
    }

    public class ErrorResponse {
        public String message;
    }
}


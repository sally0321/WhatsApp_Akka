import akka.actor.AbstractActor;
import akka.actor.ActorRef;
import akka.actor.Props;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

public class AuthenticationServer extends AbstractActor {

    // Instead of username → password, we now store phoneNumber → username
    private final Map<String, String> phoneToUserMap = new HashMap<>();
    private final Map<String, ActorRef> loggedInUsers = new HashMap<>(); // Track logged-in users by phone number

    public static Props props() {
        return Props.create(AuthenticationServer.class, AuthenticationServer::new);
    }

    @Override
    public Receive createReceive() {
        return receiveBuilder()
                .match(RegisterUser.class, message -> {
                    // Check if phone number already exists
                    if (phoneToUserMap.containsKey(message.phoneNumber)) {
                        message.userActor.tell("Phone number already exists. Please try a different one.", getSelf());
                    } else {
                        // Store phoneNumber → username
                        phoneToUserMap.put(message.phoneNumber, message.username);
                        //Database.saveUser(message.username, message.phoneNumber);
                        message.userActor.tell("Registration successful! Welcome " + message.username, getSelf());
                    }
                })
                .match(LoginUser.class, message -> {
                    // Check if phone number is known
                    if (phoneToUserMap.containsKey(message.phoneNumber)) {
                        String foundUsername = phoneToUserMap.get(message.phoneNumber);
                        loggedInUsers.put(message.phoneNumber, message.userActor);
                        message.userActor.tell("Login successful! Welcome back " + foundUsername, getSelf());
                    } else {
                        message.userActor.tell("Invalid phone number. Please try again.", getSelf());
                    }
                })
                .match(LogoutUser.class, message -> {
                    loggedInUsers.remove(message.phoneNumber);
                    message.userActor.tell("You have been logged out.", getSelf());
                })
                .match(DeleteUserAccount.class, message -> {
                    if (phoneToUserMap.containsKey(message.phoneNumber)) {
                        // Remove from both phoneToUserMap and loggedInUsers
                        phoneToUserMap.remove(message.phoneNumber);
                        //String username = phoneToUserMap.get(message.phoneNumber);
                        //Database.deleteUserAccount(username, message.phoneNumber);
                        loggedInUsers.remove(message.phoneNumber);
                        message.userActor.tell("Your account has been deleted.", getSelf());
                    } else {
                        message.userActor.tell("Account not found.", getSelf());
                    }
                })
                .build();
    }

    // Message classes for authentication operations
    public static class RegisterUser implements Serializable {
        public final String username;
        public final String phoneNumber;
        public final ActorRef userActor;

        public RegisterUser(String username, String phoneNumber, ActorRef userActor) {
            this.username = username;
            this.phoneNumber = phoneNumber;
            this.userActor = userActor;
        }
    }

    public static class LoginUser implements Serializable {
        public final String phoneNumber;
        public final ActorRef userActor;

        public LoginUser(String phoneNumber, ActorRef userActor) {
            this.phoneNumber = phoneNumber;
            this.userActor = userActor;
        }
    }

    public static class LogoutUser implements Serializable {
        public final String phoneNumber;
        public final ActorRef userActor;

        public LogoutUser(String phoneNumber, ActorRef userActor) {
            this.phoneNumber = phoneNumber;
            this.userActor = userActor;
        }
    }

    public static class DeleteUserAccount implements Serializable {
        public final String phoneNumber;
        public final ActorRef userActor;

        public DeleteUserAccount(String phoneNumber, ActorRef userActor) {
            this.phoneNumber = phoneNumber;
            this.userActor = userActor;
        }
    }

    public static void main(String[] args) {
        akka.actor.ActorSystem system = akka.actor.ActorSystem.create("AuthenticationServerSystem");
        system.actorOf(AuthenticationServer.props(), "authenticationServer");
    }
}
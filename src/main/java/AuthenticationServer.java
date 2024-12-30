import akka.actor.AbstractActor;
import akka.actor.ActorRef;
import akka.actor.Props;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

public class AuthenticationServer extends AbstractActor {

    private final Map<String, String> users = new HashMap<>(); // Map to store username and password
    private final Map<String, ActorRef> loggedInUsers = new HashMap<>(); // Track logged in users

    public static Props props() {
        return Props.create(AuthenticationServer.class, AuthenticationServer::new);
    }

    @Override
    public Receive createReceive() {
        return receiveBuilder()
                .match(RegisterUser.class, message -> {
                    if (users.containsKey(message.username)) {
                        message.userActor.tell("Username already exists. Please try logging in.", getSelf());
                    } else {
                        users.put(message.username, message.password);
                        message.userActor.tell("Registration successful! Welcome " + message.username, getSelf());
                    }
                })
                .match(LoginUser.class, message -> {
                    if (users.containsKey(message.username) && users.get(message.username).equals(message.password)) {
                        loggedInUsers.put(message.username, message.userActor);
                        message.userActor.tell("Login successful! Welcome back " + message.username, getSelf());
                    } else {
                        message.userActor.tell("Invalid credentials. Please try again.", getSelf());
                    }
                })
                .match(LogoutUser.class, message -> {
                    loggedInUsers.remove(message.username);
                    message.userActor.tell("You have been logged out.", getSelf());
                })
                .match(DeleteUserAccount.class, message -> {
                    if (users.containsKey(message.username)) {
                        users.remove(message.username);
                        loggedInUsers.remove(message.username);
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
        public final String password;
        public final ActorRef userActor;

        public RegisterUser(String username, String password, ActorRef userActor) {
            this.username = username;
            this.password = password;
            this.userActor = userActor;
        }
    }

    public static class LoginUser implements Serializable {
        public final String username;
        public final String password;
        public final ActorRef userActor;

        public LoginUser(String username, String password, ActorRef userActor) {
            this.username = username;
            this.password = password;
            this.userActor = userActor;
        }
    }

    public static class LogoutUser implements Serializable {
        public final String username;
        public final ActorRef userActor;

        public LogoutUser(String username, ActorRef userActor) {
            this.username = username;
            this.userActor = userActor;
        }
    }

    public static class DeleteUserAccount implements Serializable {
        public final String username;
        public final ActorRef userActor;

        public DeleteUserAccount(String username, ActorRef userActor) {
            this.username = username;
            this.userActor = userActor;
        }
    }

    public static void main(String[] args) {
        akka.actor.ActorSystem system = akka.actor.ActorSystem.create("AuthenticationServerSystem");
        system.actorOf(AuthenticationServer.props(), "authenticationServer");
    }
}
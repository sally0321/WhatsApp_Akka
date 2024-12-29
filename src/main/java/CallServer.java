import akka.actor.AbstractActor;
import akka.actor.ActorRef;
import akka.actor.Props;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

public class CallServer extends AbstractActor {

    private final Map<String, ActorRef> userActors = new HashMap<>();
    private final Map<ActorRef, ActorRef> activeCalls = new HashMap<>();

    public static Props props() {
        return Props.create(CallServer.class, CallServer::new);
    }

    @Override
    public Receive createReceive() {
        return receiveBuilder()
                // Handle user registration
                .match(ConnectUser.class, message -> {
                    System.out.println("Server connected: " + message.username);
                    userActors.put(message.username, message.userActor);
                })
                // Handle user disconnection
                .match(DisconnectUser.class, message -> {
                    System.out.println("Server disconnected: " + message.username);
                    userActors.remove(message.username);
                })
                // Handle initiating a call
                .match(InitiateCall.class, msg -> {
                    ActorRef target = userActors.get(msg.targetUsername);
                    if (target != null) {
                        target.tell(new IncomingCall(msg.callerUsername), getSelf());
                        activeCalls.put(getSender(), target);
                        System.out.println(msg.callerUsername + " initiated a call to " + msg.targetUsername);
                    } else {
                        getSender().tell(new CallFailed(msg.targetUsername + " is not online."), getSelf());
                    }
                })
                // Handle string-based user inputs
                .match(String.class, input -> {
                    ActorRef caller = activeCalls.get(getSender());
                    if ("Y".equalsIgnoreCase(input)) {
                        // Accept the call
                        if (caller != null) {
                            caller.tell(new CallAccepted(getSender().path().name()), getSelf());
                            System.out.println("Call accepted by " + getSender().path().name());
                        }
                    } else if ("N".equalsIgnoreCase(input)) {
                        // Reject the call
                        if (caller != null) {
                            caller.tell(new CallRejected(getSender().path().name()), getSelf());
                            activeCalls.remove(getSender());
                            System.out.println("Call rejected by " + getSender().path().name());
                        }
                    } else if ("0".equals(input)) {
                        // End the call
                        if (caller != null) {
                            caller.tell(new CallEnded(getSender().path().name()), getSelf());
                            activeCalls.remove(getSender());
                            System.out.println("Call ended by " + getSender().path().name());
                        } else {
                            System.out.println("No active call found for " + getSender().path().name());
                        }
                    } else {
                        System.out.println("Invalid input: " + input);
                    }
                })
                // Handle call response (accept/reject)
                .match(RespondCall.class, msg -> {
                    ActorRef caller = activeCalls.get(getSender());
                    if (caller != null) {
                        if (msg.accepted) {
                            caller.tell(new CallAccepted(msg.targetUsername), getSelf());
                        } else {
                            caller.tell(new CallRejected(msg.targetUsername), getSelf());
                            activeCalls.remove(getSender());
                        }
                    }
                })
                // Handle ending a call
                .match(EndCall.class, msg -> {
                    ActorRef other = activeCalls.remove(getSender());
                    if (other != null) {
                        other.tell(new CallEnded(msg.username), getSelf());
                        activeCalls.remove(other);
                        System.out.println("Call ended by " + msg.username);
                    } else {
                        System.out.println("No active call to end for " + msg.username);
                    }
                })

                .build();
    }

    // Message classes
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

    public static class InitiateCall implements Serializable {
        public final String callerUsername;
        public final String targetUsername;

        public InitiateCall(String callerUsername, String targetUsername) {
            this.callerUsername = callerUsername;
            this.targetUsername = targetUsername;
        }
    }

    public static class RespondCall implements Serializable {
        public final String targetUsername;
        public final boolean accepted;

        public RespondCall(String responderUsername, boolean accepted) {
            this.targetUsername = responderUsername;
            this.accepted = accepted;
        }
    }

    public static class EndCall implements Serializable {
        public final String username;

        public EndCall(String username) {
            this.username = username;
        }
    }

    public static class IncomingCall implements Serializable {
        public final String callerUsername;

        public IncomingCall(String callerUsername) {
            this.callerUsername = callerUsername;
        }
    }

    public static class CallAccepted implements Serializable {
        public final String username;

        public CallAccepted(String username) {
            this.username = username;
        }
    }

    public static class CallRejected implements Serializable {
        public final String username;

        public CallRejected(String username) {
            this.username = username;
        }
    }

    public static class CallEnded implements Serializable {
        public final String username;

        public CallEnded(String username) {
            this.username = username;
        }
    }

    public static class CallFailed implements Serializable {
        public final String message;

        public CallFailed(String message) {
            this.message = message;
        }
    }

    public static void main(String[] args) {
        akka.actor.ActorSystem system = akka.actor.ActorSystem.create("CallServerSystem");
        system.actorOf(CallServer.props(), "callServer");
    }
}

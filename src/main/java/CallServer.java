import akka.actor.AbstractActor;
import akka.actor.ActorRef;
import akka.actor.Props;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

public class CallServer extends AbstractActor {

    private final Map<String, ActorRef> clients = new HashMap<>();
    private final Map<ActorRef, ActorRef> activeCalls = new HashMap<>();

    public static Props props() {
        return Props.create(CallServer.class, CallServer::new);
    }

    @Override
    public Receive createReceive() {
        return receiveBuilder()
                .match(RegisterClient.class, msg -> {
                    clients.put(msg.username, getSender());
                    System.out.println("User registered: " + msg.username);
                })
                .match(InitiateCall.class, msg -> {
                    ActorRef target = clients.get(msg.targetUsername);
                    if (target != null) {
                        target.tell(new IncomingCall(msg.callerUsername), getSelf());
                        activeCalls.put(getSender(), target);
                    } else {
                        getSender().tell(new CallFailed(msg.targetUsername + " is not online."), getSelf());
                    }
                })
                .match(RespondCall.class, msg -> {
                    ActorRef caller = activeCalls.get(getSender());
                    if (caller != null) {
                        if (msg.accepted) {
                            caller.tell(new CallAccepted(msg.responderUsername), getSelf());
                            getSender().tell(new CallAccepted(msg.responderUsername), getSelf());
                        } else {
                            caller.tell(new CallRejected(msg.responderUsername), getSelf());
                            activeCalls.remove(getSender());
                        }
                    }
                })
                .match(EndCall.class, msg -> {
                    ActorRef other = activeCalls.remove(getSender());
                    if (other != null) {
                        other.tell(new CallServer.CallEnded(msg.username), getSelf());
                        activeCalls.remove(other);
                    } else {
                        System.out.println("The call has ended.");
                    }
                })
                .build();
    }

    // Message classes
    public static class RegisterClient implements Serializable {
        public final String username;

        public RegisterClient(String username) {
            this.username = username;
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
        public final String responderUsername;
        public final boolean accepted;

        public RespondCall(String responderUsername, boolean accepted) {
            this.responderUsername = responderUsername;
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

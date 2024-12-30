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
                /*
                .match(InitiateCall.class, msg -> {
                    ActorRef target = userActors.get(msg.targetUsername);
                    if (target != null) {
                        target.tell(new IncomingCall(msg.callerUsername), getSelf());
                        activeCalls.put(getSender(), target);
                        System.out.println(msg.callerUsername + " initiated a call to " + msg.targetUsername);
                    } else {
                        getSender().tell(new CallFailed(msg.targetUsername + " is not online."), getSelf());
                    }
                })*/
                // Handle string-based user inputs
                .match(String.class, input -> {
                    ActorRef caller1 = getSender();
                    ActorRef callee1 = activeCalls.get(getSender());

                    ActorRef callee2 = getSender();
                    ActorRef caller2 = null;

                    for (Map.Entry<ActorRef, ActorRef> entry : activeCalls.entrySet()) {
                        if (entry.getValue().equals(callee2)) { // Reverse lookup
                            caller2 = entry.getKey();
                            break; // Stop searching once the match is found
                        }
                    }


                    //ActorRef callee = activeCalls.get(getSender());
                    if (input.equalsIgnoreCase("Y")) {
                        // Accept the call
                        if (caller2 != null) {
                            caller2.tell(new CallAccepted(callee2.path().name()), getSelf());
                            callee2.tell("You accepted the call from " + caller2.path().name() + ". Press '0' to end the call.", getSelf());
                            System.out.println("Call accepted by " + callee2.path().name());
                        }
                    } else if (input.equalsIgnoreCase("N")) {
                        // Reject the call
                        if (caller2 != null) {
                            caller2.tell(new CallRejected(callee2.path().name()), getSelf());
                            callee2.tell("You rejected the call from " + caller2.path().name() + ".\n\nStart a call by entering recipient name. \nBack - return to main menu", getSelf());
                            activeCalls.remove(caller2);
                            System.out.println("Call rejected by " + callee2.path().name());
                        }
                    } else if ("0".equals(input)) {
                        // End the call
                        if (caller2 != null) {
                            caller2.tell(new CallEnded(callee2.path().name()), getSelf());
                            activeCalls.remove(caller2);
                            System.out.println("Call ended by " + callee2.path().name());
                            callee2.tell("You ended the call.\n\nStart a call by entering recipient name. \nBack - return to main menu", getSelf());
                        } else if (callee1 != null) {
                            callee1.tell(new CallEnded(caller1.path().name()), getSelf());
                            activeCalls.remove(caller1);
                            System.out.println("Call ended by " + caller1.path().name());
                            caller1.tell("You ended the call.\n\nStart a call by entering recipient name. \nBack - return to main menu", getSelf());
                        } else {
                            System.out.println("No active call found for " + getSender().path().name());
                            getSender().tell("You ended the call.\n\nStart a call by entering recipient name. \nBack - return to main menu", getSelf());
                        }
                    }
                    else if (input.equalsIgnoreCase("back")){
                        // do nothing
                    }
                    else {
                        ActorRef target = userActors.get(input);
                        if (target != null) {
                            target.tell(new IncomingCall(getSender().path().name()), getSelf());
                            activeCalls.put(getSender(), target);
                            System.out.println( getSender().path().name() + " initiated a call to " + input);
                            getSender().tell("Call initiated. Waiting for " + input + " to accept or reject the call. Press '0' to end the call.", getSelf());
                        } else {
                            getSender().tell("Call initiated. Waiting for " + input + " to accept or reject the call. Press '0' to end the call.", getSelf());
                            // no need this one cz should be able to call anyone although not online
                            //getSender().tell(new CallFailed(input + " is not online."), getSelf());
                        }

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


/*
import akka.actor.AbstractActor;
import akka.actor.Props;
import akka.actor.ActorRef;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
public class AuthenticationServer extends AbstractActor {
    private Map<String, UserProfile> userProfiles = new HashMap<>();
    private Map<String, String> activeVerificationCodes = new HashMap<>(); // phone -> code
    private Map<String, Boolean> loggedInUsers = new HashMap<>(); // username -> loginStatus

    static public Props props() {
        return Props.create(AuthenticationServer.class, () -> new AuthenticationServer());
    }

    public static class UserProfile implements Serializable {
        public final String username;
        public final String phoneNumber;

        public UserProfile(String username, String phoneNumber) {
            this.username = username;
            this.phoneNumber = phoneNumber;
        }
    }

    public static class RegisterRequest implements Serializable {
        public final String username;
        public final String phoneNumber;

        public RegisterRequest(String username, String phoneNumber) {
            this.username = username;
            this.phoneNumber = phoneNumber;
        }
    }

    public static class LoginRequest implements Serializable {
        public final String phoneNumber;

        public LoginRequest(String phoneNumber) {
            this.phoneNumber = phoneNumber;
        }
    }

    public static class VerifyCodeRequest implements Serializable {
        public final String phoneNumber;
        public final String code;

        public VerifyCodeRequest(String phoneNumber, String code) {
            this.phoneNumber = phoneNumber;
            this.code = code;
        }
    }

    public static class DeleteAccountRequest implements Serializable {
        public final String username;
        public final String code;

        public DeleteAccountRequest(String username, String code) {
            this.username = username;
            this.code = code;
        }
    }

    public static class LogoutRequest implements Serializable {
        public final String username;

        public LogoutRequest(String username) {
            this.username = username;
        }
    }

    @Override
    public Receive createReceive() {
        return receiveBuilder()
                .match(RegisterRequest.class, this::handleRegistration)
                .match(LoginRequest.class, this::handleLogin)
                .match(VerifyCodeRequest.class, this::handleVerification)
                .match(LogoutRequest.class, this::handleLogout)
                .match(DeleteAccountRequest.class, this::handleDeleteAccount)
                .build();
    }

    private void handleRegistration(RegisterRequest request) {
        if (userProfiles.values().stream()
                .anyMatch(profile -> profile.username.equals(request.username))) {
            getSender().tell("Username already exists", getSelf());
            return;
        }

        if (userProfiles.values().stream()
                .anyMatch(profile -> profile.phoneNumber.equals(request.phoneNumber))) {
            getSender().tell("Phone number already registered", getSelf());
            return;
        }

        UserProfile newProfile = new UserProfile(request.username, request.phoneNumber);
        userProfiles.put(request.phoneNumber, newProfile);
        Database.saveUser(request.username); // Add user to existing Database class

        getSender().tell("Registration successful", getSelf());
    }

    private void handleLogin(LoginRequest request) {
        if (!userProfiles.containsKey(request.phoneNumber)) {
            getSender().tell("Phone number not registered", getSelf());
            return;
        }

        String verificationCode = generateVerificationCode();
        activeVerificationCodes.put(request.phoneNumber, verificationCode);
        sendVerificationCode(request.phoneNumber, verificationCode);

        getSender().tell("Verification code sent", getSelf());
    }

    private void handleVerification(VerifyCodeRequest request) {
        String storedCode = activeVerificationCodes.get(request.phoneNumber);
        if (storedCode != null && storedCode.equals(request.code)) {
            UserProfile profile = userProfiles.get(request.phoneNumber);
            loggedInUsers.put(profile.username, true);
            activeVerificationCodes.remove(request.phoneNumber);
            getSender().tell("Login successful:" + profile.username, getSelf());
        } else {
            getSender().tell("Invalid verification code", getSelf());
        }
    }

    private void handleLogout(LogoutRequest request) {
        if (loggedInUsers.containsKey(request.username)) {
            loggedInUsers.remove(request.username);
            getSender().tell("Logout successful", getSelf());
        } else {
            getSender().tell("User not logged in", getSelf());
        }
    }

    private void handleDeleteAccount(DeleteAccountRequest request) {
        String phoneNumber = null;
        for (Map.Entry<String, UserProfile> entry : userProfiles.entrySet()) {
            if (entry.getValue().username.equals(request.username)) {
                phoneNumber = entry.getKey();
                break;
            }
        }

        if (phoneNumber == null) {
            getSender().tell("User not found", getSelf());
            return;
        }

        String storedCode = activeVerificationCodes.get(phoneNumber);
        if (storedCode != null && storedCode.equals(request.code)) {
            userProfiles.remove(phoneNumber);
            loggedInUsers.remove(request.username);
            activeVerificationCodes.remove(phoneNumber);
            // Remove from Database
            Database.removeUser(request.username);
            getSender().tell("Account deleted successfully", getSelf());
        } else {
            getSender().tell("Invalid verification code", getSelf());
        }
    }

    private String generateVerificationCode() {
        Random random = new Random();
        return String.format("%06d", random.nextInt(1000000));
    }

    private void sendVerificationCode(String phoneNumber, String code) {
        // In a real implementation, this would integrate with an SMS service
        System.out.println("Sending verification code: " + code + " to " + phoneNumber);
    }

    public boolean isUserLoggedIn(String username) {
        return loggedInUsers.getOrDefault(username, false);
    }
}

*/

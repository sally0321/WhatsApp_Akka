import akka.actor.ActorRef;
import akka.actor.ActorSelection;
import akka.actor.ActorSystem;
import akka.actor.Props;

import java.util.Scanner;


public class App {

    private static Scanner scanner = new Scanner(System.in);
    private static String username;


    public static void main(String[] args) {
        ActorSystem system = ActorSystem.create("ClientSystem");

        // Specify the remote path to the server actor
        ActorSelection serverActor = system.actorSelection("akka://ServerSystem@127.0.0.1:2551/user/serverActor");

        // Login user
        username = promptUsername();
        // create user actor
        ActorRef userActor = system.actorOf(Props.create(User.class, username), username);


        // Send a message to the server actor
        serverActor.tell(new ChatServer.LoginUser(username, userActor), userActor);
        // Log message to confirm client sent the message
        System.out.println("Message sent to server.");

        serverActor.tell(new ChatServer.SendMessage(username, promptRecipient(), promptMessage()), userActor);
        System.out.println("Message sent to server.");


        //system.terminate();
    }




    private static String promptUsername() {
        System.out.println("Enter your username:");
        return scanner.nextLine();
    }

    private static String promptRecipient() {
        System.out.println("Enter your recipient:");
        return scanner.nextLine();
    }

    private static String promptMessage() {
        System.out.println("Enter your message:");
        return scanner.nextLine();
    }
}

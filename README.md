## Whatsapp Application: A Prototype

### 1.0 Introduction 
This project is a prototype of __Whatsapp__ _- a leading instant messaging platform_. 
Focusing on demonstrating its operation as a __distributed system__, the project adapts a __microservices client-server architecture__, developed with:<br/>
`AKKA Actor Model` using `Java Programming Language`.

[AKKA Actor Model](https://doc.akka.io/libraries/akka-core/current/typed/actors.html) is a __concurrency framework__ based on the __Actors__' concurrency model. 
Actors are independent entities that encapsulate state and behavior, and they communicate with each other through asynchronous message passing, 
avoiding the need for shared memory and complex synchronization mechanisms. The actor-based architecture allows the application to remain scalable and responsive.<br/>

> [!NOTE]
> This project was developed as part of a university assignment. 
<br/>

### 2.0 Features  
#### 2.1 Send and Receive Message (Chat Server) 
The __[Chat Server](https://github.com/sally0321/WhatsApp_Akka/blob/master/src/main/java/ChatServer.java)__ 
facilitates real-time messaging and user management in a chat application. Key functionalities include:
- `User Management`: Tracks connected users and their actors for communication.
- `Messaging`: Manages sending and receiving messages between users, formats messages with timestamps, and updates message status in a database.
- `Chat History`: Retrieves and displays chat history for ongoing conversations.
- `Communication Channels`: Establishes and closes private communication channels between users.
- `Contact Management`: Allows users to add contacts to their lists.
<br/>

#### 2.2 Make a Call (Call Server)
The __[Call Server](https://github.com/sally0321/WhatsApp_Akka/blob/master/src/main/java/ChallServer.java)__ 
manages real-time call functionality between users in the application, using the Akka Actor Model to handle 
concurrent user interactions and maintain active call states. Key functionalities are: 
- `User Management`: Tracks connected users and their actors for communication. Manages user connections and disconnections.
- `Call Initiation`: Handles starting calls between users. Notifies recipients of incoming calls and updates active call states.
- `Call Response`: Manages user responses to calls whether to accepts (Y) or rejects (N) calls. 
- `Call Termination`: Allows users to end active calls. Ensures proper cleanup of active call states to maintain system integrity.
- `Error Handling`: Informs users of issues, such as when a recipient is offline or unavailable for a call.
<br/>

#### 2.3 Profile Settings (Profile Server) 
The __[Profile Server](https://github.com/sally0321/WhatsApp_Akka/blob/master/src/main/java/ProfileServer.java)__ 
handles user profiles by enabling users to view and update their bio while maintaining data consistency. Functionalities are as such:
- `User Management`: Tracks and manages user profiles, including usernames and associated bios.
- `View Profile`: Allows users to view their profile information, including the username and bio.
- `Update Bio`: Allows users to update the bio associated with their username.
- `Data Consistency`: Ensures that profile information is consistently stored and accessible.
<br/>
  
#### 2.4 Authentication (Authentication Server) 
The __[Authentication Server](https://github.com/sally0321/WhatsApp_Akka/blob/master/src/main/java/AuthenticationServer.java)__ handles user registration, 
login, logout, and account deletion while ensuring phone numbers are unique and properly mapped to usernames. The functionalities are as listed: 
- `User Registration`: Registers a new user by storing their phone number and username, ensuring no duplicates in the phone number field.
- `User Login`: Validates user login by checking if the phone number exists and retrieves the associated username for successful login.
- `User Logout`: Manages user logout by removing the phone number from the list of logged-in users.
- `Account Deletion`: Handles account deletion by removing the user's phone number and username from the system.
<br/>

#### 2.5 Database 
The __[Database](https://github.com/sally0321/WhatsApp_Akka/blob/master/src/main/java/Database.java)__ class is a utility class that handles file operations for storing and retrieving various types of data related to users, contacts, and chat history.<br/> 
<br/>

#### 2.6 User Interaction 
The __[User](https://github.com/sally0321/WhatsApp_Akka/blob/master/src/main/java/User.java)__ actor, on the other hand, is responsible for the user's interactions with the system, specifically handling incoming messages, notifications, and actions related to chat, calls, and profile updates. It is more focused on receiving and responding to messages rather than managing the overall application flow.<br/>
<br/>

#### 2.7 Controller Class (App)
The __[App](https://github.com/sally0321/WhatsApp_Akka/blob/master/src/main/java/App.java)__ class is the central controller that manages the user experience and interacts with various backend server actors. It controls the flow of the application, including handling menus, user input, and calling different features.<br/>
<br/>


<br/>

### 3.0 Use Case Diagram  
![image](https://github.com/user-attachments/assets/0640973d-8587-4cb5-9828-0eb1598cef24)

<br/>

### 4.0 Running the Project 
Before running the project, do a maven compilation: <br/> 
> `mvn clean compile`

Next, activate the servers with the following commands: <br/>
> __Authentication Server__<br/>
`mvn exec:java -D"exec.mainClass"="AuthenticationServer" -D"config.file"="src/main/resources/AuthenticationServer.conf"`<br/> <br/>
__Chat Server__<br/>
`mvn exec:java -D"exec.mainClass"="ChatServer" -D"config.file"="src/main/resources/ChatServer.conf"`<br/><br/>
__Call Server__<br/>
`mvn exec:java -D"exec.mainClass"="CallServer" -D"config.file"="src/main/resources/CallServer.conf"`<br/><br/>
__Profile Server__<br/>
`mvn exec:java -D"exec.mainClass"="ProfileServer" -D"config.file"="src/main/resources/ProfileServer.conf"` <br/><br/> 

Once all servers are running, you may execute the application with the command:
> `mvn exec:java -D"exec.mainClass"="App" -D"config.file"="src/main/resources/User.conf"`
<br/>

<br/>

### 5.0 Output 
#### 5.1 Authentication 
Sign-in Options are as shown:<br/>
> ![Picture11](https://github.com/user-attachments/assets/8d557fcf-7523-4f16-ae83-5c4ac507e0db)
<br/>

Registering an Account (phone number MUST be unique): <br/>
> ![Picture10](https://github.com/user-attachments/assets/b6ab9ff7-e23d-451f-a199-b48fd8eded56)
<br/>

Logging into an Account (phone number should be registered already): <br/>
> <img width="500" alt="image" src="https://github.com/user-attachments/assets/a03c6446-c482-4235-9193-9913c3443469" />
<br/>

Logging Out of Account through Account Settings: <br/>
> <img width="500" alt="image" src="https://github.com/user-attachments/assets/fb5ade3b-d6a0-4af9-bde7-1385408fc1af" />
<br/>

Deleting an Account through Account Settings: <br/> 
> <img width="500" alt="image" src="https://github.com/user-attachments/assets/9f9b51e5-1d39-420d-ade6-72b26447a20a" />
<br/>

User List in Database (all changes will be reflected): <br/>
> <img width="500" alt="image" src="https://github.com/user-attachments/assets/0d6b02a2-be02-4653-8d0c-0db4c529a6bf" />
<br/>
<br/>

#### 5.2 Chat Services 
User Contact List and Menu: <br/> 
> <img width="600" alt="image" src="https://github.com/user-attachments/assets/fba05b37-0c9e-4fda-a155-c244fd8e1c0f" />
<br/> 

Adding User Contact: <br/> 
> <img width="600" alt="image" src="https://github.com/user-attachments/assets/bd12c83c-e334-459e-b5c5-5518e5e66a15" />
<br/> 

Starting a Chat: <br/> 
> ![picture12](https://github.com/user-attachments/assets/d812dc66-3abb-4652-ad1b-974a75794e7f)
<br/>

Chat History between users: <br/> 
> <img width="600" alt="image" src="https://github.com/user-attachments/assets/0302145d-bdd6-4146-b07f-b597fa51d214" />
<br/>

Sending Message: <br/> 
> <img width="600" alt="image" src="https://github.com/user-attachments/assets/6b5d02be-b477-4acb-a407-65772ecc2b65" />
<br/>

Message Status: <br/> 
> <img width="600" alt="image" src="https://github.com/user-attachments/assets/2364cb0c-2d12-46ac-b8c1-c0ab52998968" />
<br/>
<br/> 

#### 5.3 Make a Call 
Call Initiation: <br/> 
> <img width="500" alt="picture13" src="https://github.com/user-attachments/assets/545781e6-3fc8-4fdf-89c5-bb3c108fa7fe" />
<br/>

Call Acceptance Options (Receiver): <br/> 
> <img width="500" alt="picture13" src="https://github.com/user-attachments/assets/6e7cbc58-fa00-4a40-9a6c-e4500e7fbb8e" />
<br/>

Call Accepted (Caller): <br/> 
> <img width="550" alt="picture13" src="https://github.com/user-attachments/assets/a34e08cc-26a3-471f-99a4-84c9040eddde" />
<br/>

Call Rejected (Caller): <br/>
> ![image](https://github.com/user-attachments/assets/980ac9ad-bee4-4908-8339-67f49768afd1)
<br/>

Updates on Call Server: <br/>
> <img width="500" alt="image" src="https://github.com/user-attachments/assets/5e7994ca-4161-4922-92bf-8097dc685afe" />

<br/>
<br/>

#### 5.4 Profile Settings
Profile Settings Menu: <br/>
> <img width="500" alt="image" src="https://github.com/user-attachments/assets/a3bfc7e1-e40a-44d5-a960-6a38fd185479" />
<br/>

View Profile: <br/>
> <img width="500" alt="image" src="https://github.com/user-attachments/assets/890bce1c-9e9b-4802-a34e-362635c39fb8" />
<br/>

Update Bio: <br/>
> <img width="800" alt="image" src="https://github.com/user-attachments/assets/636ed76b-5855-43f2-aeed-4fa28907d228" />
<br/>

Updated Profile: <br/>
> <img width="500" alt="image" src="https://github.com/user-attachments/assets/84d7b22d-2d93-47de-abc1-187b43f3c6ba" />
<br/>
<br/>

### 6.0 Remarks 
The project does not include encryption for the user interactions. 

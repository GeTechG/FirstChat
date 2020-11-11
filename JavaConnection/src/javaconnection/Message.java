package javaconnection;

public class Message {

    int id;
    String username;
    String message;

    Message(int id, String message) {
        this.id = id;
        this.username = Handle.users.get(id).name;
        this.message = message;
    }

}

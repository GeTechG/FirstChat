package javaconnection;

import java.util.ArrayList;

public class StorageData {

    ArrayList<User> users;
    ArrayList<Message> messages;

    StorageData(ArrayList<User> users, ArrayList<Message> messages) {
        this.users = users;
        this.messages = messages;
    }


}

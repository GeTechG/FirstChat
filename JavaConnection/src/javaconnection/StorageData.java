package javaconnection;

import java.util.ArrayList;

public class StorageData {

    ArrayList<User> users;
    ArrayList<Message> messages;
    ArrayList<ArrayList<Message>> privateChats;

    StorageData(ArrayList<User> users, ArrayList<Message> messages, ArrayList<ArrayList<Message>> privateChats) {
        this.users = users;
        this.messages = messages;
        this.privateChats = privateChats;
    }


}

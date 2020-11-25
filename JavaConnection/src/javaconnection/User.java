package javaconnection;

import javafx.util.Pair;

import java.util.HashMap;

public class User {

    public User(String name, String login, String pass) {
        this.name = name;
        this.login = login;
        this.pass = pass;
        this.private_chats_id = new HashMap<>();
    }

    String name;
    String login;
    String pass;
    HashMap<Integer, Pair<Integer,String>> private_chats_id;


}

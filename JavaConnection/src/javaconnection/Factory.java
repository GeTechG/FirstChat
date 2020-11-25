package javaconnection;

import com.eclipsesource.json.Json;
import com.eclipsesource.json.JsonObject;
import com.google.gson.Gson;
import javafx.util.Pair;

import java.util.ArrayList;

public class Factory {

    static User createNewUser(String name, String username, String pass) {

        User user = null;

        if (name != null) {
            user = new User(name, username, pass);
        }

        return user;
    }

    static Message createNewMessage(String json,int chatID) {
        Message message = null;

        try {
            JsonObject jsonValue = Json.parse(json).asObject();

            int id = jsonValue.get("id").asInt();
            String message_str = jsonValue.get("message").asString();

            if (chatID == -1) {
                message = new Message(id, message_str);
            } else {

            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return message;

    }

    static boolean AddMassageToBase(Message message) {

        int id = message.id;
        if (id >= 0 && id < Handle.users.size()) {
            Handle.messages.add(message);
            saveBase();
            return true;
        }
        return false;
    }

    static Integer AddPrivateChat(int first_user_id, int two_user_id) {
        int chat_id = Handle.privateChats.size();
        if (!Handle.users.get(first_user_id).private_chats_id.containsKey(two_user_id)) {
            Handle.users.get(first_user_id).private_chats_id.put(two_user_id, new Pair<Integer,String>(chat_id,Handle.users.get(two_user_id).name));
            Handle.users.get(two_user_id).private_chats_id.put(first_user_id, new Pair<Integer,String>(chat_id,Handle.users.get(first_user_id).name));
            Handle.privateChats.add(new ArrayList<>());
            return chat_id;
        }
        return null;
    }

    static void saveBase() {
        try {
            StorageData storageData = new StorageData(Handle.users, Handle.messages, Handle.privateChats);
            Gson gson = new Gson();

            String json = gson.toJson(storageData);
            Io.saveStringToFile("Base.json", json);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    static void loadBase() {
        Gson gson = new Gson();
        String json = Io.loadStringFromFile("Base.json");
        if (json != null) {
            StorageData storageData = gson.fromJson(json, StorageData.class);

            Handle.users = storageData.users;
            Handle.messages = storageData.messages;

            for (int i = 0; i < Handle.users.size(); i++) {
                User user = Handle.users.get(i);
                Pair<String,String> acc = new Pair<>(user.login,user.pass);
                Handle.users_search.put(acc,i);
            }

        }
    }

}

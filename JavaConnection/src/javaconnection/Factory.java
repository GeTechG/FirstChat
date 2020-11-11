package javaconnection;

import com.eclipsesource.json.Json;
import com.eclipsesource.json.JsonObject;
import com.eclipsesource.json.JsonValue;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import javafx.util.Pair;

import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;

public class Factory {

    static User createNewUser(String name, String username, String pass) {

        User user = null;

        if (name != null) {
            user = new User(name, username, pass);
            saveBase();
        }

        return user;
    }

    static Message createNewMessage(String json) {
        Message message = null;

        try {
            JsonObject jsonValue = Json.parse(json).asObject();

            int id = jsonValue.get("id").asInt();
            String message_str = jsonValue.get("message").asString();

            message = new Message(id, message_str);
            saveBase();

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

    static void saveBase() {
        StorageData storageData = new StorageData(Handle.users, Handle.messages);
        Gson gson = new Gson();

        Io.saveStringToFile("Base.json",gson.toJson(storageData));
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
                Pair<String,String> acc = new Pair<>(user.username,user.pass);
                Handle.users_search.put(acc,i);
            }

        }
    }

}

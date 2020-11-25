package javaconnection;

import com.eclipsesource.json.Json;
import com.eclipsesource.json.JsonArray;
import com.eclipsesource.json.JsonObject;
import com.google.gson.Gson;
import com.sun.net.httpserver.*;
import javafx.util.Pair;

import java.io.*;
import java.net.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.stream.Collectors;

import static javaconnection.Factory.saveBase;

public class Handle {

    static HashMap<Pair<String,String>,Integer> users_search = new HashMap<Pair<String,String>,Integer>();
    static ArrayList<User> users = new ArrayList<>();
    static ArrayList<Message> messages = new ArrayList<>();
    static ArrayList<ArrayList<Message>> privateChats = new ArrayList<>();

    public static void main(String[] args) throws Exception {
        Factory.loadBase();
        ThreadPoolExecutor threadPoolExecutor = (ThreadPoolExecutor) Executors.newFixedThreadPool(100);
        HttpServer server = HttpServer.create(new InetSocketAddress(10000), 0);
        server.createContext("/", new MyHandler());
        server.createContext("/login", new MyHandler());
        server.createContext("/chat", new MyHandler());
        server.setExecutor(threadPoolExecutor); // creates a default executor
        server.start();
    }

    static long getNewUID() {
        URLConnection connection = null;
        try {
            connection = new URL("http://showcase.api.linx.twenty57.net/UnixTime/tounix?date=now").openConnection();
            InputStream is = connection.getInputStream();
            InputStreamReader reader = new InputStreamReader(is);
            char[] buffer = new char[256];
            int rc;

            StringBuilder sb = new StringBuilder();

            while ((rc = reader.read(buffer)) != -1)
                sb.append(buffer, 0, rc);

            reader.close();

            return Long.parseLong(sb.toString());
        } catch (Exception e) {
            e.printStackTrace();
        }

        return 0;
    }

    static class MyHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange t) throws IOException {

            String url = t.getHttpContext().getPath();
            t.getResponseBody();


            switch (url) {
                case "/":
                    registerUser(t);
                    break;
                case "/login":
                    loginUser(t);
                    break;
                case "/chat":
                    chat(t);
                    break;
            }

//            HashMap<String,Object> query = null;
//            if ("GET".equals(t.getRequestMethod())) {
//                query = getQuery(t);
//            }
//            if ("POST".equals(t.getRequestMethod())) {
//                post(t);
//            }
//
//            System.out.println(query.toString());
//
//            String response = "Привет, ";
//
//            response += query.get("name");
//            response += "!";
//            t.getResponseHeaders().add("Access-Control-Allow-Origin","*");
//            t.getResponseHeaders().add("Content-Type", "text/html; charset=UTF-8");
//            t.sendResponseHeaders(200, 0);
//            OutputStream os = t.getResponseBody();
//            os.write(response.getBytes());
//            os.flush();
//            os.close();
        }

        void sendBadResponse(HttpExchange t) {
            try {
                t.getResponseHeaders().add("Access-Control-Allow-Origin","*");
                t.getResponseHeaders().add("Content-Type", "text/html; charset=UTF-8");
                t.sendResponseHeaders(400, 0);
                OutputStream os = t.getResponseBody();
                os.flush();
                os.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        void sendBadResponse(HttpExchange t, int code) {
            try {
                t.getResponseHeaders().add("Access-Control-Allow-Origin","*");
                t.getResponseHeaders().add("Content-Type", "text/html; charset=UTF-8");
                t.sendResponseHeaders(code, 0);
                OutputStream os = t.getResponseBody();
                os.flush();
                os.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        void sendGoodResponse(HttpExchange t, String body) {
            try {
                t.getResponseHeaders().add("Access-Control-Allow-Origin","*");
                t.getResponseHeaders().add("Content-Type", "text/html; charset=UTF-8");
                t.sendResponseHeaders(200, 0);
                OutputStream os = t.getResponseBody();
                os.write(body.getBytes());
                os.flush();
                os.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        HashMap<String,Object> getQuery(HttpExchange t) {
            HashMap<String,Object> map = new HashMap<>();
            try {
                String queryStr = t.getRequestURI().getQuery();
                if (queryStr != null) {
                    String[] aQueries = queryStr.split("&");
                    for (String query : aQueries) {
                        String[] aQuery = query.split("=");
                        map.put(aQuery[0], aQuery[1]);
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            return map;
        }

        HashMap<String,Object> post(HttpExchange t) {

            return null;
        }

        void registerUser(HttpExchange t) {
            HashMap<String,Object> query = null;
            query = getQuery(t);
            if ("GET".equals(t.getRequestMethod())) {

                User user = Factory.createNewUser(query.get("name").toString(), query.get("login").toString(),query.get("pass").toString());
                if (user == null) {
                    sendBadResponse(t);
                    return;
                }

                Pair<String,String> acc_user = new Pair<String,String>(query.get("login").toString(),query.get("pass").toString());
                int id = users.size();
                users_search.put(acc_user,id);
                users.add(user);
                saveBase();


                sendGoodResponse(t, String.valueOf(id));
            }
        }

        void loginUser(HttpExchange t) {
            HashMap<String,Object> query = null;
            query = getQuery(t);
            if ("GET".equals(t.getRequestMethod())) {

                Pair<String,String> acc = new Pair<>(query.get("login").toString(),query.get("pass").toString());
                Integer id = users_search.get(acc);
                if (id == null) {
                    sendBadResponse(t,422);
                    return;
                }

                sendGoodResponse(t, String.valueOf(id));
            }
        }

        void chat(HttpExchange t) {
            HashMap<String,Object> query = null;
            query = getQuery(t);
            switch (t.getRequestMethod()) {
                case "GET":
                    Integer userID = -1;
                    try {
                        userID = Integer.valueOf((String) query.get("id"));
                    } catch (Exception e) {
                        e.printStackTrace();
                        sendBadResponse(t, 403);
                        return;
                    }
                    if (users.get(userID) != null) {
                        Gson gson = new Gson();

                        String json = gson.toJson(users.get(userID).private_chats_id);

                        sendGoodResponse(t, json);
                    } else {
                        sendBadResponse(t, 403);
                    }

                    break;
                case "POST":

                    String json_message = null;

                    try {
                        InputStream inputStream = t.getRequestBody();
                        json_message = new BufferedReader(new InputStreamReader(inputStream))
                                .lines().collect(Collectors.joining("\n"));

                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                    try {
                        switch (Integer.parseInt((String) query.get("mode"))) {
                            case 0:

                                HashMap<String, Object> finalQuery = query;
                                String finalJson_message = json_message;
                                Runnable task = () -> {
                                    int last_time_general = -1;
                                    JsonObject lastTimePrivate = null;
                                    int userId = -1;
                                    try {
                                        JsonObject jsonValue = Json.parse(finalJson_message).asObject();

                                        last_time_general = jsonValue.getInt("last_time_general", -1);
                                        if (jsonValue.get("last_time_privates") != null)
                                            lastTimePrivate = jsonValue.get("last_time_privates").asObject();
                                        userId = Integer.parseInt((String) finalQuery.get("id"));
                                    } catch (Exception e) {
                                        e.printStackTrace();
                                    }


                                    Object[] message_arr = new Object[2];

                                    HashMap<Integer, MessageDataSend> privateMessages = new HashMap<>();

                                    int timeout = 60_000;
                                    int time = 0;
                                    boolean findNewMessages = false;
                                    while (!findNewMessages && time < timeout) {
                                        try {
                                            if (last_time_general < messages.size() - 1) {
                                                findNewMessages = true;
                                                message_arr[0] = new MessageDataSend(messages.size() - 1, messages.subList(last_time_general + 1, messages.size()));
                                            }
                                            if (lastTimePrivate != null) {
                                                List<String> names = lastTimePrivate.names();
                                                for (int i = 0; i < names.size(); i++) {
                                                    if (lastTimePrivate.get(names.get(i)).asInt() < privateChats.get(Integer.parseInt(names.get(i))).size() - 1) {
                                                        findNewMessages = true;
                                                        privateMessages.put(Integer.valueOf(names.get(i)), new MessageDataSend(privateChats.get(Integer.parseInt(names.get(i))).size() - 1, privateChats.get(Integer.parseInt(names.get(i))).subList(lastTimePrivate.get(names.get(i)).asInt() + 1, privateChats.get(Integer.parseInt(names.get(i))).size())));
                                                    }
                                                }
                                            }

                                            Thread.sleep(1);
                                            time++;
                                        } catch (InterruptedException e) {
                                            e.printStackTrace();
                                        }
                                    }
                                    ;
                                    message_arr[1] = privateMessages;


                                    Gson gson = new Gson();

                                    String jsonStr = gson.toJson(message_arr);

                                    sendGoodResponse(t, jsonStr);
                                };

                                Thread thread_message = new Thread(task);
                                thread_message.start();

                                break;
                            case 1:

                                int chatId = -1;
                                try {
                                    chatId = Integer.parseInt((String) query.get("chatID"));
                                } catch (Exception e) {
                                    e.printStackTrace();
                                    sendBadResponse(t, 406);
                                    return;
                                }
                                Message message = Factory.createNewMessage(json_message, chatId);
                                if (message != null) {
                                    if (Factory.AddMassageToBase(message)) {
                                        sendGoodResponse(t, "");
                                        return;
                                    }
                                }
                                sendBadResponse(t, 403);

                                break;
                            case 2:

                                JsonObject jsonObject = Json.parse(json_message).asObject();

                                int self_id = jsonObject.getInt("self_id", -1);
                                int user_id = jsonObject.getInt("user_id", -1);

                                Integer chat_id = Factory.AddPrivateChat(self_id, user_id);

                                if (chat_id != null) {
                                    sendGoodResponse(t, String.valueOf(chat_id));
                                    Factory.saveBase();
                                } else {
                                    sendBadResponse(t, 406);
                                    return;
                                }

                                break;
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                        sendBadResponse(t,403);
                        return;
                    }
                    break;
            }
        }
    }
}
package javaconnection;

import com.google.gson.Gson;
import com.sun.net.httpserver.*;
import javafx.util.Pair;

import java.io.*;
import java.net.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.stream.Collectors;

public class Handle {

    static HashMap<Pair<String,String>,Integer> users_search = new HashMap<Pair<String,String>,Integer>();
    static ArrayList<User> users = new ArrayList<>();
    static ArrayList<Message> messages = new ArrayList<>();

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

                    HashMap<String, Object> finalQuery = query;
                    Runnable task = () -> {
                        int lastMessage = 0;

                        try {
                            lastMessage = Integer.parseInt(String.valueOf(finalQuery.get("last_time")));
                        } catch (Exception e) {
                            e.printStackTrace();
                        }


                        Object[] message_arr = new Object[2];

                        int timeout = 60_000;
                        int time = 0;
                        while (lastMessage >= messages.size() - 1 && time < timeout) {
                            try {
                                Thread.sleep(1);
                                time++;
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                        };
                        message_arr[0] = messages.size() - 1;
                        if (lastMessage < messages.size() - 1) {
                            message_arr[1] = messages.subList(lastMessage + 1, messages.size());
                        }


                        Gson gson = new Gson();

                        String jsonStr = gson.toJson(message_arr);

                        sendGoodResponse(t, jsonStr);
                    };

                    Thread thread_message = new Thread(task);
                    thread_message.start();

                    break;

                case "POST":

                    int user_id = -1;

                    String json_message = null;

                    try {
                        InputStream inputStream = t.getRequestBody();
                        json_message = new BufferedReader(new InputStreamReader(inputStream))
                                .lines().collect(Collectors.joining("\n"));

                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    Message message = Factory.createNewMessage(json_message);
                    if (message != null) {
                        if (Factory.AddMassageToBase(message)) {
                            sendGoodResponse(t, "");
                            return;
                        }
                    }
                    sendBadResponse(t, 403);

                    break;
            }
        }
    }
}
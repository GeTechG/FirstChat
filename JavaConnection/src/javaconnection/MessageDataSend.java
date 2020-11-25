package javaconnection;

import java.util.ArrayList;
import java.util.List;

public class MessageDataSend {

    int last_time;
    List<Message> messages;

    MessageDataSend(int last_time, List<Message> messages) {
        this.last_time = last_time;
        this.messages = messages;
    }

}

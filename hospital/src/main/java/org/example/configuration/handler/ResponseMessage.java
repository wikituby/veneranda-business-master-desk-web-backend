package org.example.configuration.handler;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class ResponseMessage {


    public String timestamp;

    public String message;

    public Object data;

    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public ResponseMessage() {

        super();
    }

    public ResponseMessage(String message) {

        super();
        this.message = message;
        this.timestamp = LocalDateTime.now().format(formatter);
    }

    public ResponseMessage(String message, Object data) {

        super();
        this.message = message;
        this.data = data;
        this.timestamp = LocalDateTime.now().format(formatter);
    }


}







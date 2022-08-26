package com.example.messagingrabbitmq;

public class Data {

    Data(String msg) {
        message = msg;
    }

    String message;

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

}

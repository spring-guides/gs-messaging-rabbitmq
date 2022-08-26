package com.example.messagingrabbitmq;

import java.io.Serializable;

public class Data implements Serializable {


    String message;

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

}

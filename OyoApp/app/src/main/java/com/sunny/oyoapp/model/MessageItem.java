package com.sunny.oyoapp.model;

public class MessageItem {
    String name ="";
    String message;
    String pofileUrl;

    public MessageItem() {
    }

    public MessageItem(String name, String message, String pofileUrl) {
        this.name = name;
        this.message = message;
        this.pofileUrl = pofileUrl;
    }

    // getter & setter

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getPofileUrl() {
        return pofileUrl;
    }

    public void setPofileUrl(String pofileUrl) {
        this.pofileUrl = pofileUrl;
    }
}

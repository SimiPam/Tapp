package com.example.pam.tapp;

public class Messages {
    private String from, message, type, emotion;

    public Messages() {
    }

    public Messages(String from, String message, String type, String emotion) {
        this.from = from;
        this.message = message;
        this.type = type;
        this.emotion = emotion;
    }

    public String getFrom() {
        return from;
    }

    public void setFrom(String from) {
        this.from = from;
    }

    public String getMessage() {
        return message;
    }

    public String getEmotion() {
        return emotion;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public void setEmotion(String emotion) {
        this.emotion = emotion;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }
}

package com.example.pam.tapp;

public class ApiResponse {

    private String text;

    public ApiResponse() {
    }

    public ApiResponse(String text) {
        this.text = text;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }
}

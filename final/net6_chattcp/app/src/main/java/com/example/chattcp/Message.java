package com.example.chattcp;


public class Message {
    private String content;
    private boolean isSentByMe;

    public Message(String content, boolean isSentByMe) {
        this.content = content;
        this.isSentByMe = isSentByMe;
    }

    public String getContent() {
        return content;
    }

    public boolean isSentByMe() {
        return isSentByMe;
    }
}

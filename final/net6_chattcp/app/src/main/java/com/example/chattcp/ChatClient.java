package com.example.chattcp;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class ChatClient {
    private static final int PORT = 12345;
    private String serverIp;
    private Socket socket;
    private BufferedReader inputStream;
    private PrintWriter outputStream;
    private boolean isConnected = false;
    private ChatServer.MessageCallback messageCallback;

    public ChatClient(String serverIp, ChatServer.MessageCallback callback) {
        this.serverIp = serverIp;
        this.messageCallback = callback;
    }

    public void connect() {
        new Thread(() -> {
            try {
                socket = new Socket(serverIp, PORT);
                isConnected = true;

                // Set up streams
                inputStream = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                outputStream = new PrintWriter(socket.getOutputStream(), true);

                // Start listening for messages
                String message;
                while (isConnected && (message = inputStream.readLine()) != null) {
                    final String receivedMessage = message;
                    messageCallback.onMessageReceived(receivedMessage);
                }

            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                disconnect();
            }
        }).start();
    }

    public void sendMessage(String message) {
        if (outputStream != null) {
            new Thread(() -> outputStream.println(message)).start();
        }
    }

    public void disconnect() {
        isConnected = false;
        try {
            if (inputStream != null) inputStream.close();
            if (outputStream != null) outputStream.close();
            if (socket != null) socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
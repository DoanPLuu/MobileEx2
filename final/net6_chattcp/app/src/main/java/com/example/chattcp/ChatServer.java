package com.example.chattcp;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;

public class ChatServer {
    private static final int PORT = 12345;
    private ServerSocket serverSocket;
    private Socket clientSocket;
    private BufferedReader inputStream;
    private PrintWriter outputStream;
    private boolean isRunning = false;
    private MessageCallback messageCallback;

    public interface MessageCallback {
        void onMessageReceived(String message);
    }

    public ChatServer(MessageCallback callback) {
        this.messageCallback = callback;
    }



    public void start() {
        new Thread(() -> {
            try {
                serverSocket = new ServerSocket(PORT);
                isRunning = true;

                // Wait for a client to connect
                clientSocket = serverSocket.accept();

                // Set up streams
                inputStream = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
                outputStream = new PrintWriter(clientSocket.getOutputStream(), true);

                // Start listening for messages
                String message;
                while (isRunning && (message = inputStream.readLine()) != null) {
                    final String receivedMessage = message;
                    messageCallback.onMessageReceived(receivedMessage);
                }

            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                stop();
            }
        }).start();
    }

    public void sendMessage(String message) {
        if (outputStream != null) {
            new Thread(() -> outputStream.println(message)).start();
        }
    }

    public void stop() {
        isRunning = false;
        try {
            if (inputStream != null) inputStream.close();
            if (outputStream != null) outputStream.close();
            if (clientSocket != null) clientSocket.close();
            if (serverSocket != null) serverSocket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
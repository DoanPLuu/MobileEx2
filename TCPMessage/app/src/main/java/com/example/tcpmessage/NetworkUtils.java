package com.example.tcpmessage;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;

public class NetworkUtils {
    public interface OnMessageReceived {
        void onMessageReceived(String message);
    }

    public static void startServer(int port, OnMessageReceived listener) {
        new Thread(() -> {
            try (ServerSocket serverSocket = new ServerSocket(port)) {
                while (true) {
                    Socket client = serverSocket.accept();
                    BufferedReader in = new BufferedReader(new InputStreamReader(client.getInputStream()));
                    String receivedMessage = in.readLine();
                    if (receivedMessage != null) {
                        listener.onMessageReceived(receivedMessage);
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }

    public static void sendMessage(String ip, int port, String message) {
        new Thread(() -> {
            try (Socket socket = new Socket(ip, port);
                 PrintWriter out = new PrintWriter(new OutputStreamWriter(socket.getOutputStream()), true)) {
                out.println(message);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }
}

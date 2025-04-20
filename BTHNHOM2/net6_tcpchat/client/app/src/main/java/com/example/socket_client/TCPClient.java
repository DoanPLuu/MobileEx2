package com.example.socket_client;

import android.os.AsyncTask;
import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class TCPClient extends Thread {
    final String serverIp;
    final int serverPort;
    Socket socket;
    PrintWriter out;
    final OnMessageListener listener;
    boolean running = true;

    public interface OnMessageListener {
        void onMessage(String msg);
    }

    public TCPClient(String ip, int port, OnMessageListener listener) {
        this.serverIp = ip;
        this.serverPort = port;
        this.listener = listener;
    }

    @Override
    public void run() {
        try {
            socket = new Socket(serverIp, serverPort);
            Log.d("TAG", "run: ");
            out = new PrintWriter(socket.getOutputStream(), true);
            BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

            listener.onMessage("[Client] Đã kết nối đến server: " + serverIp);

            String line;
            while (running && (line = in.readLine()) != null) {
                listener.onMessage("Server: " + line);
            }

        } catch (IOException e) {
            listener.onMessage("Lỗi client: " + e.getMessage());
        }
    }

    public void sendMessageToServer(String message) {
        if (out != null) {
            out.println(message);
        }
    }

    public void stopClient() {
        running = false;
        try {
            if (socket != null) socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}


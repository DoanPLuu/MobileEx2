package com.example.socket_server;

import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;

public class TCPServer extends Thread {
    private ServerSocket serverSocket;
    private boolean running = true;
    private final int port = 12345;
    private final OnMessageReceived listener;
    private PrintWriter out;

    public interface OnMessageReceived {
        void onMessage(String message);
    }

    public TCPServer(OnMessageReceived listener) {
        this.listener = listener;
    }

    @Override
    public void run() {
        try {
            serverSocket = new ServerSocket(port);
            while (running) {
                Log.d("TAG", "run: ");
                Socket clientSocket = serverSocket.accept();
                if (clientSocket!=null) {
                    Log.d("TAG", clientSocket.getInetAddress()+ "");
                }
                else {
                    Log.d("TAG", "null ");
                }
//                gui du lieu den client
                out = new PrintWriter(clientSocket.getOutputStream(), true);

//                doc du lieu
                BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));


                String line;
                while (running && (line = in.readLine()) != null) {
                    listener.onMessage("Client: " + line);
                }


            }
        } catch (IOException e) {
            if (listener != null) {
                listener.onMessage("Lỗi: " + e.getMessage());
            }
        }
    }

    public void stopServer() {
        running = false;
        try {
            if (serverSocket != null) serverSocket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void sendMessageToClient(String message) {
        if (out != null) {
            out.println(message);
        }
    }
}

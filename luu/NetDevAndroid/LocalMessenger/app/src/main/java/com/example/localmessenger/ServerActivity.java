package com.example.localmessenger;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;

public class ServerActivity extends AppCompatActivity {
    private TextView textViewMessages;
    private ServerSocket serverSocket;
    private final int PORT = 5000;  // Cổng lắng nghe
    private Handler uiHandler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_server);

        textViewMessages = findViewById(R.id.textViewMessages);
        uiHandler = new Handler(Looper.getMainLooper());

        new Thread(new ServerThread()).start();  // Chạy Server ở luồng riêng
    }

    class ServerThread implements Runnable {
        @Override
        public void run() {
            try {
                serverSocket = new ServerSocket(PORT);
                appendMessage("Server đang chạy, chờ kết nối...");

                while (!Thread.currentThread().isInterrupted()) {
                    Socket clientSocket = serverSocket.accept();
                    appendMessage("Client đã kết nối: " + clientSocket.getInetAddress());

                    new Thread(new ClientHandler(clientSocket)).start();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    class ClientHandler implements Runnable {
        private Socket clientSocket;

        public ClientHandler(Socket socket) {
            this.clientSocket = socket;
        }

        @Override
        public void run() {
            try {
                BufferedReader input = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
                PrintWriter output = new PrintWriter(clientSocket.getOutputStream(), true);

                String message;
                while ((message = input.readLine()) != null) {
                    appendMessage("Client: " + message);
                }

                clientSocket.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void appendMessage(String message) {
        uiHandler.post(() -> textViewMessages.append(message + "\n"));
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        try {
            if (serverSocket != null) {
                serverSocket.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}


package com.example.chatrealtime;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ScrollView;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity"; // Thêm TAG để log
    private TextView tvMessages;
    private EditText etMessage, etServerIp, etPort;
    private Button btnStartServer, btnConnectClient, btnSend;
    private ScrollView scrollView;

    private ServerSocket serverSocket;
    private Socket clientSocket;
    private PrintWriter out;
    private BufferedReader in;
    private Handler handler = new Handler(Looper.getMainLooper());

    private boolean isServer = false;
    private boolean isClientConnected = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        try {
            // Ánh xạ các thành phần giao diện
            tvMessages = findViewById(R.id.tvMessages);
            etMessage = findViewById(R.id.etMessage);
            etServerIp = findViewById(R.id.etServerIp);
            etPort = findViewById(R.id.etPort);
            btnStartServer = findViewById(R.id.btnStartServer);
            btnConnectClient = findViewById(R.id.btnConnectClient);
            btnSend = findViewById(R.id.btnSend);
            scrollView = findViewById(R.id.scrollView);

            // Kiểm tra xem ánh xạ có thành công không
            if (scrollView == null) {
                Log.e(TAG, "ScrollView is null!");
            }

            // Khi nhấn Start Server
            btnStartServer.setOnClickListener(v -> startServer());

            // Khi nhấn Connect as Client
            btnConnectClient.setOnClickListener(v -> {
                etServerIp.setVisibility(View.VISIBLE);
                etPort.setVisibility(View.VISIBLE);
                btnConnectClient.setText("Connect");
                btnConnectClient.setOnClickListener(v1 -> connectClient());
            });

            // Khi nhấn Send
            btnSend.setOnClickListener(v -> sendMessage());
        } catch (Exception e) {
            Log.e(TAG, "Error in onCreate: " + e.getMessage(), e);
        }
    }

    private void startServer() {
        isServer = true;
        btnStartServer.setEnabled(false);
        btnConnectClient.setEnabled(false);
        new Thread(() -> {
            try {
                serverSocket = new ServerSocket(8080);
                appendMessage("Server started. Waiting for client...");
                clientSocket = serverSocket.accept();
                appendMessage("Client connected!");
                out = new PrintWriter(clientSocket.getOutputStream(), true);
                in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
                receiveMessages();
            } catch (IOException e) {
                appendMessage("Server error: " + e.getMessage());
                Log.e(TAG, "Error in startServer: " + e.getMessage(), e);
            }
        }).start();
    }

    private void connectClient() {
        String serverIp = etServerIp.getText().toString();
        String portStr = etPort.getText().toString();
        if (serverIp.isEmpty() || portStr.isEmpty()) {
            appendMessage("Please enter Server IP and Port");
            return;
        }
        try {
            int port = Integer.parseInt(portStr);
            new Thread(() -> {
                try {
                    clientSocket = new Socket(serverIp, port);
                    appendMessage("Connected to server!");
                    out = new PrintWriter(clientSocket.getOutputStream(), true);
                    in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
                    isClientConnected = true;
                    receiveMessages();
                } catch (IOException e) {
                    appendMessage("Client error: " + e.getMessage());
                    Log.e(TAG, "Error in connectClient: " + e.getMessage(), e);
                }
            }).start();
        } catch (NumberFormatException e) {
            appendMessage("Invalid port number!");
            Log.e(TAG, "Error parsing port: " + e.getMessage(), e);
        }
    }

    private void receiveMessages() {
        new Thread(() -> {
            try {
                String message;
                while ((message = in.readLine()) != null) {
                    String finalMessage = message;
                    handler.post(() -> appendMessage("Received: " + finalMessage));
                }
            } catch (IOException e) {
                handler.post(() -> appendMessage("Error receiving message: " + e.getMessage()));
                Log.e(TAG, "Error in receiveMessages: " + e.getMessage(), e);
            }
        }).start();
    }

    private void sendMessage() {
        String message = etMessage.getText().toString();
        if (message.isEmpty()) return;
        if (out != null) {
            new Thread(() -> {
                out.println(message);
                handler.post(() -> appendMessage("Sent: " + message));
            }).start();
        } else {
            appendMessage("Not connected to any device!");
        }
        etMessage.setText("");
    }

    private void appendMessage(String message) {
        runOnUiThread(() -> {
            tvMessages.append(message + "\n");
            scrollView.post(() -> scrollView.fullScroll(View.FOCUS_DOWN));
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        try {
            if (clientSocket != null) clientSocket.close();
            if (serverSocket != null) serverSocket.close();
        } catch (IOException e) {
            Log.e(TAG, "Error in onDestroy: " + e.getMessage(), e);
        }
    }
}
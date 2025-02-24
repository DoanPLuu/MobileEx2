package com.example.localmessenger;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import java.io.PrintWriter;
import java.net.Socket;

public class ClientActivity extends AppCompatActivity {
    private TextView textViewMessages;
    private EditText editTextMessage, editTextServerIP;
    private Button buttonSend;
    private Socket clientSocket;
    private PrintWriter output;
    private final int PORT = 5000;
    private Handler uiHandler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_client);

        textViewMessages = findViewById(R.id.textViewMessages);
        editTextMessage = findViewById(R.id.editTextMessage);
        editTextServerIP = findViewById(R.id.editTextServerIP);
        buttonSend = findViewById(R.id.buttonSend);
        uiHandler = new Handler(Looper.getMainLooper());

        buttonSend.setOnClickListener(view -> {
            String serverIP = editTextServerIP.getText().toString();
            String message = editTextMessage.getText().toString();
            if (!serverIP.isEmpty() && !message.isEmpty()) {
                new Thread(() -> sendMessage(serverIP, message)).start();
            }
        });
    }

    private void sendMessage(String serverIP, String message) {
        try {
            if (clientSocket == null || clientSocket.isClosed()) {
                clientSocket = new Socket(serverIP, PORT);
                output = new PrintWriter(clientSocket.getOutputStream(), true);
            }
            output.println(message);
            appendMessage("Bạn: " + message);
        } catch (Exception e) {
            e.printStackTrace();
            appendMessage("❌ Lỗi gửi tin: " + e.getMessage());
        }
    }

    private void appendMessage(String message) {
        uiHandler.post(() -> textViewMessages.append(message + "\n"));
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        try {
            if (clientSocket != null && !clientSocket.isClosed()) {
                clientSocket.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

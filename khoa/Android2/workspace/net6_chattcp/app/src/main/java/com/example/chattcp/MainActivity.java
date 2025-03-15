package com.example.chattcp;

import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;

public class MainActivity extends AppCompatActivity {
    private RecyclerView recyclerView;
    private MessageAdapter messageAdapter;
    private EditText messageEditText;
    private Button sendButton, startServerButton;
    private ServerSocket serverSocket;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        recyclerView = findViewById(R.id.recyclerView);
        messageEditText = findViewById(R.id.messageEditText);
        sendButton = findViewById(R.id.sendButton);
        startServerButton = findViewById(R.id.startServerButton);
        messageAdapter = new MessageAdapter();
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(messageAdapter);

        // Khởi động server
        startServerButton.setOnClickListener(v -> new Thread(() -> startServer()).start());

        // Gửi tin nhắn
        sendButton.setOnClickListener(v -> {
            String message = messageEditText.getText().toString();
            if (!message.isEmpty()) {
                new SendMessageTask().execute(message);
                messageAdapter.addMessage("Sent: " + message);
                messageEditText.setText("");
            }
        });
    }

    private void startServer() {
        try {
            serverSocket = new ServerSocket(12345);
            runOnUiThread(() -> messageAdapter.addMessage("Server started..."));
            while (true) {
                Socket client = serverSocket.accept();
                BufferedReader in = new BufferedReader(new InputStreamReader(client.getInputStream()));
                String receivedMsg = in.readLine();
                runOnUiThread(() -> messageAdapter.addMessage("Received: " + receivedMsg));
                client.close();
            }
        } catch (Exception e) {
            runOnUiThread(() -> messageAdapter.addMessage("Server error: " + e.getMessage()));
        }
    }

    private class SendMessageTask extends AsyncTask<String, Void, Void> {
        @Override
        protected Void doInBackground(String... params) {
            try {
                Socket socket = new Socket("192.168.1.3", 12345); // Thay IP của server
                PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
                out.println(params[0]);
                socket.close();
            } catch (Exception e) {
                runOnUiThread(() -> messageAdapter.addMessage("Client error: " + e.getMessage()));
            }
            return null;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        try {
            if (serverSocket != null) serverSocket.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
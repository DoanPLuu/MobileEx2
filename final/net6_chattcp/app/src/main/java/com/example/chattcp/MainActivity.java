package com.example.chattcp;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private RecyclerView messageRecyclerView;
    private MessageAdapter messageAdapter;
    private List<Message> messageList;
    private EditText messageEditText;
    private Button sendButton;
    private Button startServerButton;
    private Button connectButton;
    private EditText ipAddressEditText;
    private Handler mainHandler;
    private ChatServer chatServer;
    private ChatClient chatClient;
    private boolean isServer = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mainHandler = new Handler(Looper.getMainLooper());

        // Initialize UI components
        messageRecyclerView = findViewById(R.id.messageRecyclerView);
        messageEditText = findViewById(R.id.messageEditText);
        sendButton = findViewById(R.id.sendButton);
        startServerButton = findViewById(R.id.startServerButton);
        connectButton = findViewById(R.id.connectButton);
        ipAddressEditText = findViewById(R.id.ipAddressEditText);

        // Setup RecyclerView
        messageList = new ArrayList<>();
        messageAdapter = new MessageAdapter(messageList);
        messageRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        messageRecyclerView.setAdapter(messageAdapter);

        // Set up button listeners
        startServerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startServer();
            }
        });

        connectButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                connectToServer();
            }
        });

        sendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendMessage();
            }
        });
    }

    private void startServer() {
        isServer = true;
        chatServer = new ChatServer(message -> {
            mainHandler.post(() -> {
                messageList.add(new Message(message, false));
                messageAdapter.notifyItemInserted(messageList.size() - 1);
                messageRecyclerView.smoothScrollToPosition(messageList.size() - 1);
            });
        });

        chatServer.start();
        Toast.makeText(this, "Server started on port 12345", Toast.LENGTH_SHORT).show();
        startServerButton.setEnabled(false);
        connectButton.setEnabled(false);
    }

    private void connectToServer() {
        String ipAddress = ipAddressEditText.getText().toString().trim();
        if (ipAddress.isEmpty()) {
            Toast.makeText(this, "Please enter server IP address", Toast.LENGTH_SHORT).show();
            return;
        }

        chatClient = new ChatClient(ipAddress, message -> {
            mainHandler.post(() -> {
                messageList.add(new Message(message, false));
                messageAdapter.notifyItemInserted(messageList.size() - 1);
                messageRecyclerView.smoothScrollToPosition(messageList.size() - 1);
            });
        });

        chatClient.connect();
        Toast.makeText(this, "Connecting to " + ipAddress, Toast.LENGTH_SHORT).show();
        startServerButton.setEnabled(false);
        connectButton.setEnabled(false);
    }

    private void sendMessage() {
        String messageText = messageEditText.getText().toString().trim();
        if (messageText.isEmpty()) {
            return;
        }

        // Add message to our list
        messageList.add(new Message(messageText, true));
        messageAdapter.notifyItemInserted(messageList.size() - 1);
        messageRecyclerView.smoothScrollToPosition(messageList.size() - 1);

        // Send the message
        if (isServer && chatServer != null) {
            chatServer.sendMessage(messageText);
        } else if (chatClient != null) {
            chatClient.sendMessage(messageText);
        }

        // Clear the input field
        messageEditText.setText("");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (chatServer != null) {
            chatServer.stop();
        }
        if (chatClient != null) {
            chatClient.disconnect();
        }
    }
}
package com.example.tcpmessage;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    private RecyclerView recyclerView;
    private MessageAdapter messageAdapter;
    private List<Message> messageList;
    private EditText editTextMessage;
    private Button btnSend;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        recyclerView = findViewById(R.id.recyclerView);
        editTextMessage = findViewById(R.id.editTextMessage);
        btnSend = findViewById(R.id.btnSend);

        messageList = new ArrayList<>();
        messageAdapter = new MessageAdapter(messageList);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(messageAdapter);

        // Khởi động server
        NetworkUtils.startServer(12345, message -> runOnUiThread(() -> {
            messageList.add(new Message(message, false));
            messageAdapter.notifyItemInserted(messageList.size() - 1);
        }));

        // Gửi tin nhắn
        btnSend.setOnClickListener(v -> {
            String msg = editTextMessage.getText().toString().trim();
            if (!msg.isEmpty()) {
                messageList.add(new Message(msg, true));
                messageAdapter.notifyItemInserted(messageList.size() - 1);
                NetworkUtils.sendMessage("192.168.1.101", 12345, msg);
                editTextMessage.setText("");
            }
        });
    }
}

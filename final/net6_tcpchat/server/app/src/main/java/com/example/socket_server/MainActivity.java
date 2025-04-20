package com.example.socket_server;

import android.os.Bundle;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class MainActivity extends AppCompatActivity {

    LinearLayout view;
    Button sendMess;
    TextView textInput;
    private TCPServer serverThread;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        serverThread = new TCPServer(message -> runOnUiThread(() -> {
            if (message != null && view != null) {

                TextView tv = new TextView(view.getContext());
                tv.setText(message);
                view.addView(tv); // Hiển thị log
            }

        }));


        view = findViewById(R.id.content);
        sendMess = findViewById(R.id.sendMess);
        textInput = findViewById(R.id.textInput);
        sendMess.setOnClickListener(v -> {
            handleSendMess();
        });

        serverThread.start();

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        serverThread.stopServer();
    }

    // send mess event
    public void handleSendMess() {
        String mess = textInput.getText().toString();
        new Thread(() -> {
            serverThread.sendMessageToClient(mess);
        }).start(); // Gửi trong thread phụ


        TextView tv = new TextView(view.getContext());
        tv.setText("server:"+ mess);
        view.addView(tv);
    }
}
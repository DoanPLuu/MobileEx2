package com.example.socket_client;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.textfield.TextInputEditText;

public class MainActivity extends AppCompatActivity {
    LinearLayout view;
    Button sendMess, connectBtn;
    TextInputEditText textInput, ipInput;
    TCPClient clientThread;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        view = findViewById(R.id.content);
        sendMess = findViewById(R.id.sendMess);
        connectBtn = findViewById(R.id.connectBtn);
        textInput = findViewById(R.id.textInput);
        ipInput = findViewById(R.id.ipInput);

        sendMess.setEnabled(false); // Chưa kết nối thì không gửi được

        connectBtn.setOnClickListener(v -> {
            String ip = ipInput.getText().toString().trim();
            if (!ip.isEmpty()) {
                connectToServer(ip);
            } else {
                appendLog("Vui lòng nhập IP hợp lệ.");
            }
        });

        sendMess.setOnClickListener(v -> handleSendMess());
    }

    private void connectToServer(String ip) {
        clientThread = new TCPClient(ip, 12345, this::appendLog);
        clientThread.start();
        sendMess.setEnabled(true);
        appendLog("Đang kết nối đến " + ip + "...");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (clientThread != null) {
            clientThread.stopClient();
        }
    }

    private void appendLog(String message) {
        TextView tv = new TextView(this);
        tv.setText(message);
        runOnUiThread(() -> view.addView(tv));
    }

    public void handleSendMess() {
        String mess = textInput.getText().toString();
        new Thread(() -> {
            if (clientThread != null) {
                clientThread.sendMessageToServer(mess);
            }
        }).start();

        appendLog("Client: " + mess);
        textInput.setText("");
    }
}

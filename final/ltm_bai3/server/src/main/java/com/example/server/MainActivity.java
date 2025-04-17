package com.example.server;

import android.os.Bundle;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;

public class MainActivity extends AppCompatActivity {
    LinearLayout content;

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

        content = findViewById(R.id.showMess);

        new Thread(() -> connServer()).start();
    }

    public void connServer() {
        int port = 12345;
        try (ServerSocket serverSocket = new ServerSocket(port)) {
            runOnUiThread(() -> addMessageToUI("Đang lắng nghe trên cổng " + port));
            while (true) {
                Socket socket = serverSocket.accept();
                String clientAddress = socket.getInetAddress().toString();
                runOnUiThread(() -> addMessageToUI("Kết nối từ: " + clientAddress));

                BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                String message = in.readLine();

                if (message != null) {
                    runOnUiThread(() -> addMessageToUI("Tin nhắn nhận được: " + message));

                    PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
                    out.println("Server nhận được: " + message);
                }

                socket.close();
            }
        } catch (IOException e) {
            runOnUiThread(() -> addMessageToUI("Lỗi server: " + e.getMessage()));
        }
    }

    private void addMessageToUI(String message) {
        TextView tv = new TextView(this);
        tv.setText(message);
        content.addView(tv);
    }
}

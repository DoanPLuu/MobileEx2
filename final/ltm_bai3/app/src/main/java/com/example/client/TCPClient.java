package com.example.client;

import android.os.AsyncTask;
import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class TCPClient extends AsyncTask<String, Void, String> {
    private static final String serverId = "192.168.1.203";
    private static final int server_port = 12345;

    @Override
    protected String doInBackground(String... strings) {
        String mess = strings[0];
        try (Socket socket = new Socket(serverId, server_port)) {
            PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
            BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            out.println(mess);
            Log.d("TAG", "con caac");
            return in.readLine();
        } catch (IOException e) {
            Log.e("TCPClient", "Loi ket noi "+ e);
//            throw new RuntimeException(e);
            return null;
        }


    }

    @Override
    protected void onCancelled() {
        super.onCancelled();
    }
}

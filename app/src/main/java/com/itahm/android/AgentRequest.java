package com.itahm.android;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Locale;

/**
 * Created by ITAhM on 2016-04-23.
 */
abstract public class AgentRequest extends Handler {

    public final String host;
    public final int port;

    public AgentRequest(String host, int port) {
        super();
        Log.d(MainActivity.TAG, String.format(Locale.US, "%s:%d", host, port));

        this.host = host;
        this.port = port;
    }

    public void register(String id, String token) {
        try {
            sendRequest(new JSONObject()
            .put("command", "register")
            .put("id", id)
            .put("token", token));
        } catch (JSONException jsone) {
            jsone.printStackTrace();

            onRequestComplete(0);
        }
    }

    public void unregister(String token) {
        try {
            sendRequest(new JSONObject()
                .put("command", "unregister")
                .put("token", token));
        } catch (JSONException jsone) {
            jsone.printStackTrace();

            onRequestComplete(0);
        }
    }

    private void sendRequest(JSONObject jsono) {
        final byte [] data = jsono.toString().getBytes();

        new Thread(new Runnable() {
            @Override
            public void run() {
                int status = 0;

                try {
                    status = sendRequest(data);
                } catch (IOException e) {
                    e.printStackTrace();
                }

                Message message = obtainMessage();

                message.what = status;

                sendMessage(message);
            }
        }).start();
    }

    private int sendRequest(byte [] data) throws IOException {
        URL url = new URL("http", host, port, "");
        HttpURLConnection hurlc = null;
        OutputStream os = null;

        try {
            hurlc = (HttpURLConnection) url.openConnection();

            hurlc.setDoOutput(true);
            hurlc.setConnectTimeout(3000);

            os = hurlc.getOutputStream();
            os.write(data);
            os.flush();
            os.close();

            return hurlc.getResponseCode();
        }
        finally {
            if (os != null) {
                os.close();
            }

            if (hurlc != null) {
                hurlc.disconnect();
            }
        }
    }

    public void handleMessage(Message msg) {
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        onRequestComplete(msg.what);
    }

    abstract void onRequestComplete(int status);
}

package com.itahm.android;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.media.RingtoneManager;
import android.os.Bundle;

import android.support.v4.app.NotificationCompat;
import android.support.v4.content.LocalBroadcastManager;

import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * Created by ITAhM on 2016-04-15.
 */
public class GcmListenerService extends com.google.android.gms.gcm.GcmListenerService{

    @Override
    public void onMessageReceived(String from, Bundle data) {
        Intent intent = new Intent(this, MainActivity.class);
        String host = data.getString("host");
        String log = data.getString("body");
        long date = System.currentTimeMillis();

        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this)
            .setSmallIcon(R.drawable.ic_w_256)
            .setContentTitle(data.getString("title"))
            .setContentText(log)
            .setAutoCancel(true)
            .setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION))
            .setVibrate(new long[] {0, 1000})
            .setContentIntent(PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_ONE_SHOT));

        ((NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE))
            .notify(0 /* ID of notification */, notificationBuilder.build());

        try {
            JSONFile file = new JSONFile(getApplicationContext(), MainActivity.FILE_LOG);
            JSONObject json = new JSONObject();

            file.getJSONObject().put(String.valueOf(date), json);

            json.put("date", date);
            json.put("log", log);
            json.put("host", host);

            file.save();

            intent = new Intent(MainActivity.ACTION_LOG);
            intent.putExtra("date", date);
            intent.putExtra("log", log);
            intent.putExtra("host", host);
        } catch (IOException | JSONException e) {
            intent = new Intent(MainActivity.ACTION_EXCEPTION);
        }

        Log.d("ITAhM", intent.toString());

        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
    }
}

package com.itahm.android;

import android.app.IntentService;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.google.android.gms.gcm.GoogleCloudMessaging;
import com.google.android.gms.iid.InstanceID;

import java.io.IOException;

/**
 * Created by ITAhM on 2016-04-15.
 */
public class RegistrationIntentService extends IntentService {

    public RegistrationIntentService () {
        super("RegistrationIntentService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        Log.d(MainActivity.TAG, "RegistrationIntentService start");
        InstanceID instanceID = InstanceID.getInstance(this);

        intent = new Intent(MainActivity.ACTION_REGISTRATION);

        try {
            synchronized (this) {
                String scope = GoogleCloudMessaging.INSTANCE_ID_SCOPE;
                String id = instanceID.getId();
                String token = instanceID.getToken(getString(R.string.gcm_defaultSenderId), scope);

                SharedPreferences.Editor editor = getSharedPreferences(MainActivity.TAG, MODE_PRIVATE).edit();

                editor.putString("id", id);
                editor.putString("token", token);
                editor.putLong("date", System.currentTimeMillis());

                editor.apply();
            }
        } catch (IOException ioe) {
            intent.putExtra("exception", ioe.toString());

            ioe.printStackTrace();
        }

        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
    }
}

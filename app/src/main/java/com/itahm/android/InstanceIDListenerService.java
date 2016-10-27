package com.itahm.android;

import android.content.Intent;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.Iterator;

/**
 * Created by ITAhM on 2016-04-15.
 *
 */
public class InstanceIDListenerService extends com.google.android.gms.iid.InstanceIDListenerService {

    @Override
    public void onTokenRefresh() {
        try {
            JSONFile file = new JSONFile(getApplicationContext(), MainActivity.FILE_AGENT);
            JSONObject json = file.getJSONObject();
            Iterator<String> it = json.keys();
            JSONObject agent;

            while(it.hasNext()) {
                agent = json.getJSONObject(it.next());

                agent.put("updated", false);
            }

            file.save();
        } catch (IOException | JSONException e) {
            e.printStackTrace();

            throw new RuntimeException(e);
        }

        startService(new Intent(this, RegistrationIntentService.class));
    }

}

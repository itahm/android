package com.itahm.android;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AlertDialog;

/**
 * Created by ITAhM on 2016-05-01.
 */
public class RegistrationBroadcastReceiver extends BroadcastReceiver {

    private final ProgressDialog progressDialog;

    public RegistrationBroadcastReceiver(ProgressDialog progressDialog) {
        this.progressDialog = progressDialog;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        switch(intent.getAction()) {
            case MainActivity.ACTION_REGISTRATION:
                progressDialog.dismiss();

                if (intent.getStringExtra("exception") != null) {
                    final Activity activity = (MainActivity)context;
                    new AlertDialog.Builder (context)
                            .setPositiveButton("close", new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int whichButton){
                                    activity.finish();
                                }
                            })
                            .setMessage("server is not responding")
                            .show();
                }

                LocalBroadcastManager.getInstance(context).unregisterReceiver(this);

                break;
        }
    }
}

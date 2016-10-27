package com.itahm.android;

import android.app.*;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;

import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.*;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.text.Collator;
import java.text.SimpleDateFormat;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

public class MainActivity extends AppCompatActivity implements AdapterView.OnItemLongClickListener, MessageListener{

    public final static String TAG = "ITAhM";

    public final static SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    public final static String ACTION_REGISTRATION = "actionRegistration";
    public final static String ACTION_LOG = "actionLog";
    public final static String ACTION_EXCEPTION = "actionException";
    public final static String FILE_LOG = "file_log";
    public final static String FILE_AGENT = "file_agent";

    private ListView logList;
    private ArrayAdapter<JSONObject> listAdaptor;
    MessageBroadcastReceiver messageReceiver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        getSupportActionBar().setTitle("ITAhM");

        logList = (ListView)findViewById(R.id.log_list);

        logList.setOnItemLongClickListener(this);

        GoogleApiAvailability gaa = GoogleApiAvailability.getInstance();
        int resultCode = gaa.isGooglePlayServicesAvailable(this);

        if(resultCode != ConnectionResult.SUCCESS) {
            exit("Google play service is not supported.");
        }

        Log.d(TAG, "google play support.");

        String token = getSharedPreferences(TAG, MODE_PRIVATE).getString("token", null);
        if (token == null) {
            Log.d(TAG, "empty token.");

            LocalBroadcastManager.getInstance(this)
                .registerReceiver(new RegistrationBroadcastReceiver(ProgressDialog.show(this, "", "Token request is in progress..."))
                    , new IntentFilter(ACTION_REGISTRATION));

            startService(new Intent(getApplicationContext(), RegistrationIntentService.class));
        }
        else {
            Log.d(TAG, token);
        }

        try {
            JSONObject json = new JSONFile(getApplicationContext(), FILE_AGENT).getJSONObject();
            Iterator<String> it = json.keys();
            JSONObject agent;

            while (it.hasNext()) {
                agent = json.getJSONObject(it.next());

                if (!agent.getBoolean("updated")) {
                    Log.d(TAG, "update required.");

                    startActivity(new Intent(getApplicationContext(), ListActivity.class));

                    break;
                }
            }
        } catch (IOException | JSONException e) {
            e.printStackTrace();

            new RuntimeException(e);
        }

        listAdaptor = new JSONArrayAdaptor(this, android.R.layout.simple_selectable_list_item);

        logList.setAdapter(listAdaptor);

        try {
            JSONObject json = new JSONFile(getApplicationContext(), FILE_LOG).getJSONObject();
            Iterator<String> it = json.keys();

            while (it.hasNext()) {
                listAdaptor.add(json.getJSONObject(it.next()));
            }
        } catch (IOException | JSONException e) {
            e.printStackTrace();

            throw new RuntimeException(e);
        }

        listAdaptor.sort(JSONArrayAdaptor.sortFunc);
        listAdaptor.notifyDataSetChanged();

        messageReceiver = new MessageBroadcastReceiver(this);

        LocalBroadcastManager.getInstance(this).registerReceiver(messageReceiver, new IntentFilter(ACTION_LOG));
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            Intent intent = new Intent(this, ListActivity.class);

            startActivity(intent);

            return true;
        }
        else if (id == R.id.action_info) {
            Intent intent = new Intent(this, InfoActivity.class);

            startActivity(intent);

            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onResume() {

        super.onResume();
    }

    @Override
    protected void onPause() {

        super.onPause();
    }

    @Override
    protected void onDestroy() {
        LocalBroadcastManager.getInstance(this).unregisterReceiver(this.messageReceiver);
        super.onDestroy();
    }

    private void exit(String message) {
        Toast.makeText(getApplicationContext(), message, Toast.LENGTH_LONG).show();

        finish();
    }

    @Override
    public boolean onItemLongClick(AdapterView<?> parent, View view, final int position, long id) {
        final JSONObject log = this.listAdaptor.getItem(position);

        AlertDialog.Builder alertDlg = new AlertDialog.Builder(view.getContext());

        alertDlg.setPositiveButton( R.string.remove, new DialogInterface.OnClickListener()
        {
            @Override
            public void onClick(DialogInterface dialog, int which )
            {
                try {
                    JSONFile file = new JSONFile(getApplicationContext(), FILE_LOG);

                    file.getJSONObject().remove(String.valueOf(log.getLong("date")));

                    file.save();
                } catch (IOException | JSONException e) {
                    e.printStackTrace();

                    throw new RuntimeException(e);
                }

                listAdaptor.remove(log);
                listAdaptor.sort(JSONArrayAdaptor.sortFunc);
                listAdaptor.notifyDataSetChanged();

                dialog.dismiss();
            }
        });

        alertDlg.setNegativeButton( R.string.cancel, new DialogInterface.OnClickListener()
        {
            @Override
            public void onClick( DialogInterface dialog, int which ) {
                dialog.dismiss();
            }
        });

        alertDlg.setMessage("remove this log?").show();

        return true;
    }

    @Override
    public void onMessage(JSONObject message) {
        this.listAdaptor.add(message);
        this.listAdaptor.sort(JSONArrayAdaptor.sortFunc);
        this.listAdaptor.notifyDataSetChanged();
    }
}

class JSONArrayAdaptor extends ArrayAdapter<JSONObject> {

    public final static Comparator sortFunc= new Comparator<JSONObject>() {

        @Override
        public int compare(JSONObject object1,JSONObject object2) {
            try {
                long date1 = object1.getLong("date");
                long date2 = object2.getLong("date");

                return date1 < date2? -1: date1 == date2? 0: 1;
            } catch (JSONException e) {
                e.printStackTrace();

                throw new RuntimeException(e);
            }
        }
    };

    public JSONArrayAdaptor(Context context, int resource) {
        super(context, resource);
    }

    @Override
    public View getView(int position, View view, ViewGroup viewGroup) {
        view = ((LayoutInflater)getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE)).inflate(R.layout.list_log, null);

        JSONObject json = getItem(position);

        try {
            ((TextView)view.findViewById(R.id.log_host)).setText(json.getString("host"));
            ((TextView)view.findViewById(R.id.log_date)).setText(MainActivity.dateFormat.format(new Date(json.getLong("date"))));
            ((TextView)view.findViewById(R.id.log_msg)).setText(json.getString("log"));
        } catch (JSONException e) {
            e.printStackTrace();

            throw new RuntimeException(e);
        }

        return view;
    }

}

class MessageBroadcastReceiver extends BroadcastReceiver {

    private final MessageListener listener;

    public MessageBroadcastReceiver(MessageListener listener) {
        this.listener = listener;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d("ITAhM", "br received");

        switch(intent.getAction()) {
            case MainActivity.ACTION_LOG:
                try {
                    this.listener.onMessage(new JSONObject()
                            .put("date", intent.getLongExtra("date", System.currentTimeMillis()))
                            .put("host", intent.getStringExtra("host"))
                            .put("log", intent.getStringExtra("log")));
                } catch (JSONException e) {
                    e.printStackTrace();

                    throw new RuntimeException(e);
                }

                break;
        }
    }

}

interface MessageListener {
    public void onMessage(JSONObject message);
}
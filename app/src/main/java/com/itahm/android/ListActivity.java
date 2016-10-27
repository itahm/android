package com.itahm.android;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Comparator;
import java.util.Date;
import java.util.Iterator;

public class ListActivity extends AppCompatActivity implements AdapterView.OnItemClickListener, View.OnClickListener {

    private ArrayAdapter<JSONObject> adaptor;
    private View customView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list);

        ActionBar ab = getSupportActionBar();

        if (ab == null) {
            throw new RuntimeException();
        }

        ab.setDisplayHomeAsUpEnabled(true);
        ab.setTitle("AGENT LIST");

        final ListView list = (ListView)findViewById(R.id.list);

        if (list == null) {
            throw new RuntimeException();
        }

        list.setOnItemClickListener(this);

        adaptor = new ArrayAdapter<JSONObject>(this, android.R.layout.simple_selectable_list_item) {
            @Override
            public View getView(int position, View view, ViewGroup viewGroup) {
                customView = ((LayoutInflater)getSystemService(LAYOUT_INFLATER_SERVICE)).inflate(R.layout.list_agent, list, false);

                TextView agentHost = (TextView)customView.findViewById(R.id.agent_host);
                TextView agentUpdate = (TextView)customView.findViewById(R.id.agent_update);
                JSONObject agent = getItem(position);

                try {
                    agentHost.setText(agent.getString("host"));
                    agentUpdate.setText(agent.getBoolean("updated")? "": "not registered");
                } catch (JSONException jsone) {
                    throw new RuntimeException(jsone);
                }

                return customView;
            }
        };

        list.setAdapter(adaptor);

        ((FloatingActionButton) findViewById(R.id.fab)).setOnClickListener(this);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId()) {
            case android.R.id.home:
                finish();

                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onResume() {
        Log.d(MainActivity.TAG, "ListActivity resume");

        try {
            JSONObject json = new JSONFile(getApplicationContext(), MainActivity.FILE_AGENT).getJSONObject();
            Iterator<String> it = json.keys();

            while (it.hasNext()) {
                this.adaptor.add(json.getJSONObject(it.next()));
            }
        } catch (IOException | JSONException e) {
            e.printStackTrace();

            new RuntimeException(e);
        }

        this.adaptor.notifyDataSetChanged();

        super.onResume();
    }

    @Override
    protected void onPause() {
        Log.d(MainActivity.TAG, "ListActivity pause");

        super.onPause();

        this.adaptor.clear();

        this.adaptor.notifyDataSetChanged();
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        Intent intent = new Intent(ListActivity.this, EditActivity.class);

        JSONObject agent = adaptor.getItem(position);

        try {
            intent.putExtra("host", agent.getString("host"));
            intent.putExtra("port", agent.getInt("port"));
        } catch (JSONException jsone) {
            throw new RuntimeException(jsone);
        }

        startActivity(intent);
    }

    @Override
    public void onClick(View v) {
        Intent intent = new Intent(getApplicationContext(), EditActivity.class);
        startActivity(intent);
    }
}

package com.itahm.android;

import android.content.SharedPreferences;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.TextView;

import java.util.Date;

public class InfoActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_info);

        ActionBar ab = getSupportActionBar();

        if (ab == null) {
            throw new RuntimeException();
        }

        ab.setDisplayHomeAsUpEnabled(true);

        SharedPreferences sp = getSharedPreferences(MainActivity.TAG, MODE_PRIVATE);

        String id = sp.getString("id", null);

        if (id == null) {
            throw new RuntimeException();
        }

        ((TextView)findViewById(R.id.info_id)).setText(id);

        String token = sp.getString("token", null);

        if (token == null) {
            throw new RuntimeException();
        }

        ((TextView)findViewById(R.id.info_token)).setText(token);

        long date = sp.getLong("date", System.currentTimeMillis());

        ((TextView)findViewById(R.id.info_date)).setText(MainActivity.dateFormat.format(new Date(date)));
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
}

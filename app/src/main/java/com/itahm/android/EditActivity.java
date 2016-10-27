package com.itahm.android;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.Locale;
import java.util.StringTokenizer;

public class EditActivity extends AppCompatActivity implements View.OnFocusChangeListener, View.OnClickListener{

    private ProgressDialog progressDialog;
    //private InputMethodManager softKey;
    private  AlertDialog.Builder alertDialog;
    private  ActionBar actionBar;

    private String token;
    private String id;
    private EditText editHost;
    private EditText editPort;
    private Button btnRemove;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit);

        this.actionBar = getSupportActionBar();
        if (this.actionBar == null) {
            throw new RuntimeException("null actionbar");
        }

        this.actionBar.setDisplayHomeAsUpEnabled(true);

        SharedPreferences sp = getSharedPreferences(MainActivity.TAG, MODE_PRIVATE);

        //this.softKey = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);

        this.alertDialog = new AlertDialog.Builder (EditActivity.this)
                .setPositiveButton("close", null)
                .setMessage("agent is not responding.");

        token = sp.getString("token", null);
        id = sp.getString("id", null);

        if (token == null || id == null) {
            throw new RuntimeException("token not found");
        }

        editHost = (EditText)findViewById(R.id.host);
        editPort = (EditText)findViewById(R.id.port);
        btnRemove = (Button)findViewById(R.id.remove);

        setEventListener();

        initActivity();
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
    public void onResume() {
        super.onResume();

        //initActivity();
    }

    @Override
    public void onPause() {
        Log.d(MainActivity.TAG, "EditActivity pause");

        this.editHost.setOnFocusChangeListener(null);
        this.editPort.setOnFocusChangeListener(null);

        View focused = getCurrentFocus();

        if(focused != null) {
            focused.clearFocus();
        }

        super.onPause();
    }

    private void setEventListener() {
        editHost.setOnFocusChangeListener(this);
        editPort.setOnFocusChangeListener(this);

        findViewById(R.id.apply).setOnClickListener(this);
        findViewById(R.id.cancel).setOnClickListener(this);

        btnRemove.setOnClickListener(this);
    }

    private void initActivity() {
        Intent intent = getIntent();

        String host = intent.getStringExtra("host");

        // 수정
        if (host != null) {
            int port = intent.getIntExtra("port", 2014);

            this.actionBar.setTitle(host);

            this.editHost.setText(host);
            this.editHost.setEnabled(false);

            this.editPort.setText(String.format(Locale.US, "%d", port));
            this.editPort.setSelectAllOnFocus(true);
            this.editPort.requestFocus();
            this.editPort.setSelectAllOnFocus(false);
        }
        // 추가
        else {
            this.btnRemove.setEnabled(false);

            this.editHost.requestFocus();
        }
    }

    private void onRegister() {
        Log.d(MainActivity.TAG, "onRegister");

        progressDialog = ProgressDialog.show(this, "", "request in progress...");

        new AgentRequest(this.editHost.getText().toString(), Integer.parseInt(this.editPort.getText().toString(), 10)) {
            @Override
            public void onRequestComplete(int status) {
                progressDialog.dismiss();

                if (status == 200) {
                    onRegister(this.host, this.port);

                    finish();
                }
                else {
                    Log.d(MainActivity.TAG, String.format(Locale.US, "%d", status));

                    alertDialog.show();
                }
            }
        }.register(id, token);

    }

    private void onRemove() {
        Log.d(MainActivity.TAG, "remove agent");

        progressDialog = ProgressDialog.show(this, "", "request in progress...");

        new AgentRequest(this.editHost.getText().toString(), Integer.parseInt(this.editPort.getText().toString(), 10)) {
            @Override
            public void onRequestComplete(int status) {
                progressDialog.dismiss();

                if (status == 200) {
                    onRemove(this.host);

                    finish();
                }
                else {
                    Log.d(MainActivity.TAG, String.format(Locale.US, "%d", status));

                    alertDialog.show();
                }
            }
        }.unregister(token);
    }

    @Override
    public void onFocusChange(View v, boolean hasFocus) {/*
        if (hasFocus) {
            Log.d(MainActivity.TAG, String.format(Locale.US, "%d focused", v.getId()));
            this.softKey.showSoftInput(v, InputMethodManager.SHOW_FORCED);
        }
        else {
            Log.d(MainActivity.TAG, String.format(Locale.US, "%d unfocused", v.getId()));
            this.softKey.hideSoftInputFromWindow(v.getWindowToken(), 0);
        }*/
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();

        if (id == R.id.apply) {
            Editable text = editPort.getText();

            if (text.length() == 0) {
                editPort.requestFocus();

                return;
            }

            try {
                Integer.parseInt(text.toString());
            } catch (NumberFormatException nfe) {
                editPort.setSelectAllOnFocus(true);

                editPort.requestFocus();

                editPort.setSelectAllOnFocus(false);

                return;
            }

            text = editHost.getText();

            if (text.length() == 0) {
                editHost.requestFocus();

                return;
            }

            onRegister();
        } else if (id == R.id.cancel) {
            finish();
        } else if (id == R.id.remove) {
            onRemove();
        }
    }

    private void onRegister(String host, int port) {
        try {
            JSONFile file = new JSONFile(getApplicationContext(), MainActivity.FILE_AGENT);

            file.getJSONObject().put(host, new JSONObject()
                    .put("host", host)
                    .put("port", port)
                    .put("updated", true));

            file.save();
        } catch (IOException | JSONException e) {
            e.printStackTrace();

            throw new RuntimeException(e);
        }
    }

    private void onRemove(String host) {
        try {
            JSONFile file = new JSONFile(getApplicationContext(), MainActivity.FILE_AGENT);

            file.getJSONObject().remove(host);

            file.save();
        } catch (IOException e) {
            e.printStackTrace();

            throw new RuntimeException(e);
        }
    }

}

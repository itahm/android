package com.itahm.android;

import android.content.Context;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * Created by ITAhM on 2016-05-02.
 */
public class JSONFile {

    private JSONObject json;
    private final Context context;
    private final String fileName;

    public JSONFile(Context context, String fileName) throws IOException {
        this.context = context;
        this.fileName = fileName;

        try {
            FileInputStream fis = context.openFileInput(fileName);
            byte [] buffer = new byte [fis.available()];

            fis.read(buffer);
            fis.close();

            json = new JSONObject(new String(buffer));
        } catch (FileNotFoundException fnfe) {
            json = new JSONObject();
        } catch (JSONException | IOException e) {
            throw new IOException(e);
        }
    }
    public JSONObject getJSONObject() {
        return this.json;
    }

    public synchronized void save() throws IOException {
        FileOutputStream fos = this.context.openFileOutput(this.fileName, Context.MODE_PRIVATE);

        fos.write(this.json.toString().getBytes());

        fos.close();
    }
}

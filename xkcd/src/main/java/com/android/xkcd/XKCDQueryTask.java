package com.android.xkcd;

import android.os.AsyncTask;
import android.text.TextUtils;

import com.google.gson.Gson;

import org.json.JSONException;

import java.io.IOException;
import java.net.URL;

/**
 * Created by max on 13/07/17.
 */

public class XKCDQueryTask extends AsyncTask<URL, Object, String> {

    private IAsyncTaskListener listener;

    public XKCDQueryTask(IAsyncTaskListener listener){
        this.listener = listener;
    }

    @Override
    protected String doInBackground(URL... params) {
        String result = null;

        try {
            URL url = params[0];
            result = NetworkUtils.getResponseFromHttpUrl(url);
        } catch (IOException e) {
            e.printStackTrace();
        }

        return result;
    }

    @Override
    protected void onPreExecute() {
        listener.onPreExecute();
    }

    @Override
    protected void onPostExecute(String s) {
        XKCDPic xPic = new Gson().fromJson(s, XKCDPic.class);
        listener.onPostExecute(xPic);
    }
}

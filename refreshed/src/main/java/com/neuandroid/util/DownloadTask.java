package com.neuandroid.util;

import android.os.AsyncTask;

import com.google.gson.Gson;

import java.io.IOException;
import java.io.Serializable;
import java.net.URL;

/**
 * Created by max on 25/07/17.
 */

public class DownloadTask extends AsyncTask<URL, Object, String> {

    public interface IAsyncTaskListener {
        void onPreExecute();
        void onPostExecute(Serializable result);
    }

    private IAsyncTaskListener listener;
    private Serializable bean;

    public DownloadTask(IAsyncTaskListener listener, Serializable bean) {
        this.listener = listener;
        this.bean = bean;
    }

    @Override
    protected void onPreExecute() {
        listener.onPreExecute();
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
    protected void onPostExecute(String result) {
        Serializable resultBean = new Gson().fromJson(result, bean.getClass());
        listener.onPostExecute(resultBean);
    }
}

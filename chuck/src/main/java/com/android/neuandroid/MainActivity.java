package com.android.neuandroid;

import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.URL;

public class MainActivity extends AppCompatActivity {

    private static final String API_QUERY = "http://api.icndb.com/jokes/random?limitTo=[nerdy]&escape=javascript";

    private TextView chuckText;
    private ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        chuckText = (TextView) findViewById(R.id.tv_chuck);
        chuckText.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view){
                loadChuckJoke();
            }
        });

        progressBar = (ProgressBar) findViewById(R.id.loadingIndicator);
    }

    private void loadChuckJoke(){
        new ChuckJokeTask().execute(API_QUERY);
    }

    private class ChuckJokeTask extends AsyncTask<String, Object, String>{

        @Override
        protected String doInBackground(String... params) {
            String result = null;
            try{
                URL url = new URL(params[0]);
                result = NetworkUtils.getResponseFromHttpUrl(url);
            } catch (IOException e){
                e.printStackTrace();
            }
            return result;
        }

        @Override
        protected void onPreExecute() {
            chuckText.setText("Loading...");
            progressBar.setVisibility(View.VISIBLE);
        }

        @Override
        protected void onPostExecute(String s) {

            progressBar.setVisibility(View.GONE);
            String joke = "";
            try{
                joke = extractJokeFromJson(s);
            } catch (JSONException e){
                e.printStackTrace();
            }

            chuckText.setText(joke);
        }
    }

    private String extractJokeFromJson(String json) throws JSONException{
        JSONObject jsonObject = new JSONObject(json);
        JSONObject value = jsonObject.optJSONObject("value");
        return value.optString("joke");
    }
}

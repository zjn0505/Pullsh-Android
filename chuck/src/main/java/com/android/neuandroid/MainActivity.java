package com.android.neuandroid;

import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity {

    private TextView chuckText;
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
    }

    private void loadChuckJoke(){
        new ChuckJokeTask().execute();
    }

    private class ChuckJokeTask extends AsyncTask<Void, Object, String>{

        @Override
        protected String doInBackground(Void... params) {
            try {
                Thread.sleep(3000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            String result = "Joke.";
            return result;
        }

        @Override
        protected void onPreExecute() {
            chuckText.setText("Loading...");
        }

        @Override
        protected void onPostExecute(String s) {
            chuckText.setText(s);
        }
    }
}

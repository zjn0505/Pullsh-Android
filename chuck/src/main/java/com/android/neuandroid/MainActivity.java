package com.android.neuandroid;

import android.net.Uri;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

public class MainActivity extends AppCompatActivity {

    private static final String API_QUERY = "http://api.icndb.com/jokes/random?escape=javascript";

    private TextView chuckText;
    private ProgressBar progressBar;
    private EditText editFirst;
    private EditText editLast;
    private CheckBox[] checkboxes;

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

        progressBar = (ProgressBar) findViewById(R.id.loading_indicator);
        editFirst = (EditText) findViewById(R.id.edit_first);
        editLast = (EditText) findViewById(R.id.edit_last);

        checkboxes = new CheckBox[]{
                (CheckBox) findViewById(R.id.explicit_checkbox),
                (CheckBox) findViewById(R.id.nerdy_checkbox)
        };
    }

    private void loadChuckJoke(){

        String firstName = editFirst.getText().toString();
        firstName = TextUtils.isEmpty(firstName)?"Chuck":firstName;
        String lastName = editFirst.getText().toString();
        lastName = TextUtils.isEmpty(lastName)?"Norris":lastName;
        new ChuckJokeTask().execute(API_QUERY, firstName, lastName);
    }

    private class ChuckJokeTask extends AsyncTask<String, Object, String>{

        @Override
        protected String doInBackground(String... params) {
            String result = null;
            try{
                //URL url = new URL(params[0]);
                URL url = null;
                Uri.Builder uribuilder = Uri.parse(params[0]).buildUpon()
                        .appendQueryParameter("firstName", params[1])
                        .appendQueryParameter("lastName", params[2]);
                String filter = getFilters();
                if(!TextUtils.isEmpty(filter)){
                    uribuilder.appendQueryParameter("exclude", filter);
                }

                Uri uri = uribuilder.build();
                System.out.println(uri.toString());
                try{
                    String myStr = uri.toString();
                    url = new URL(myStr);
                } catch (MalformedURLException e){
                    e.printStackTrace();
                }
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

    private String getFilters(){
        StringBuilder str = new StringBuilder();

        str.append("[");
        for(int i = 0; i < checkboxes.length; i++){
            if(!checkboxes[i].isChecked()){
                if(!(str.charAt(str.length() - 1) == '[')){
                    str.append(", ");
                }
                str.append(checkboxes[i].getTag());
            }
        }
        str.append("]");
        return str.toString();
    }
}

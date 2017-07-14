package com.android.chuck;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
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
    private TextView loadingText;
    private ProgressBar progressBar;
    private EditText editFirst;
    private EditText editLast;
    private CheckBox[] checkboxes;
    private int mShortAnimationDuration;


    /**
     * Initialize all of the activity.
     * @param savedInstanceState
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        chuckText = (TextView) findViewById(R.id.tv_chuck);
        chuckText.setOnClickListener(new View.OnClickListener() {

            /**
             * Load chuck joke when view is clicked.
             * @param view
             */
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
        loadingText = (TextView) findViewById(R.id.loading_text);
        mShortAnimationDuration = getResources().getInteger(
                android.R.integer.config_shortAnimTime);
    }

    /**
     * Determines whether the name textfields are empty or not and starts a background task to
     * download a joke with either the entered names or the default.
     */
    private void loadChuckJoke(){

        String firstName = editFirst.getText().toString();
        firstName = TextUtils.isEmpty(firstName)?"Chuck":firstName;
        String lastName = editLast.getText().toString();
        lastName = TextUtils.isEmpty(lastName)?"Norris":lastName;
        new ChuckJokeTask().execute(API_QUERY, firstName, lastName);
    }

    private class ChuckJokeTask extends AsyncTask<String, Object, String>{

        /**
         * Makes a http request to download joke.
         * @param params string array:
         *               first parameter: api url
         *               second parameter: first name
         *               third parameter: last name
         * @return result string from http request, returns null if there is an exception
         */
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

        /**
         * Fades from chuckText to loadingText and displays loading indicator.
         */
        @Override
        protected void onPreExecute() {
            crossfade(chuckText, loadingText);
            progressBar.setVisibility(View.VISIBLE);
        }

        /**
         * Fades from loadingText to chuckText and hides loading indicator.
         * @param s result string received from background task
         */
        @Override
        protected void onPostExecute(String s) {

            progressBar.setVisibility(View.GONE);
            String joke = "";
            if(!TextUtils.isEmpty(s)){
                try{
                    joke = extractJokeFromJson(s);
                } catch (JSONException e){
                    e.printStackTrace();
                }

                chuckText.setText(joke);
            } else {
                chuckText.setText(R.string.query_error);
            }

            crossfade(loadingText, chuckText);
            String t = chuckText.getText().toString();
        }
    }

    /**
     * Extracts joke from JSON response according to api: {@link http://www.icndb.com/api/}.
     * @param json string in json format that corresponds to api
     * @return a string containing the extracted joke
     * @throws JSONException
     */
    private String extractJokeFromJson(String json) throws JSONException{
        JSONObject jsonObject = new JSONObject(json);
        JSONObject value = jsonObject.optJSONObject("value");
        return value.optString("joke");
    }

    /**
     * Returns a string of category filter options for use in the api query.
     * The format of the returned string is: [category1, category2]
     * Which categories are available is determined by the classes checkboxes.
     * @return a string of categories in JavaScript array format
     */
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

    /**
     * Takes two View objects as parameters where the first object will be faded out
     * of view and the scecond will fade in.
     * @param outfadeView the view that should fade out and disappear
     * @param infadeView the view that should appear
     * @see View
     */
    private void crossfade(final View outfadeView, final View infadeView){


        outfadeView.animate()
                .alpha(0f)
                .setDuration(mShortAnimationDuration)
                .setListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        outfadeView.setVisibility(View.GONE);
                    }
                });

        infadeView.animate()
                .alpha(1f)
                .setDuration(mShortAnimationDuration)
                .setListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationStart(Animator animation) {
                        infadeView.setVisibility(View.VISIBLE);
                    }

                    // this is in case the fade in animation starts before
                    // a previous faded out animation ends. This will guarantee
                    // that after finishing it will definitely be visible
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        infadeView.setVisibility(View.VISIBLE);
                    }
                });


    }

}

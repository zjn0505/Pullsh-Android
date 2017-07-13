package com.android.xkcd;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.bumptech.glide.Glide;

import java.io.Serializable;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Random;

public class MainActivity extends AppCompatActivity {

    public static final String XKCD_QUERY_BASE_URL = "https://xkcd.com/info.0.json";
    public static final String XKCD_QUERY_BY_ID_URL = "https://xkcd.com/%s/info.0.json";

    private XKCDPic currentPic;

    private TextView titleText;
    private TextView creationDateText;
    private ImageView imageView;
    private ProgressBar progressBar;

    private IAsyncTaskListener iAsyncTaskListener = new IAsyncTaskListener() {
        @Override
        public void onPreExecute() {
            progressBar.setVisibility(View.VISIBLE);
        }

        @Override
        public void onPostExecute(Serializable result) {
            if(result instanceof XKCDPic){
                MainActivity.this.currentPic = (XKCDPic) result;
                renderXKCDPic((XKCDPic) result);
            }

            progressBar.setVisibility(View.GONE);
        }
    };

    private IAsyncTaskListener silentListener = new IAsyncTaskListener() {
        @Override
        public void onPreExecute() {
            progressBar.setVisibility(View.VISIBLE);
        }

        @Override
        public void onPostExecute(Serializable result) {
            if(result instanceof XKCDPic){
                Random r = new Random();
                int newNumber = r.nextInt(((XKCDPic) result).num - 1) + 1;
                loadXKCDpicById(newNumber);
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        titleText = (TextView) findViewById(R.id.image_title_text);
        creationDateText = (TextView) findViewById(R.id.creation_date_text);
        imageView = (ImageView) findViewById(R.id.image_view);
        progressBar = (ProgressBar) findViewById(R.id.progress_bar);

        imageView.setOnClickListener(new View.OnClickListener(){

            @Override
            public void onClick(View v) {
                launchDetailActivity();
                //loadRandomXKCDPic();
            }
        });

        loadXKCDpic();
    }

    private void loadXKCDpic(){
        try{
            URL url = new URL(XKCD_QUERY_BASE_URL);
            new XKCDQueryTask(iAsyncTaskListener).execute(url);
        } catch (MalformedURLException e){
            e.printStackTrace();
        }

    }

    private void loadXKCDpicById(int id){
        try{
            String formatted = String.format(XKCD_QUERY_BY_ID_URL, id);
            URL url = new URL(formatted);
            new XKCDQueryTask(iAsyncTaskListener).execute(url);
        } catch (MalformedURLException e){
            e.printStackTrace();
        }
    }

    private void loadRandomXKCDPic(){
        try{
            URL url = new URL(XKCD_QUERY_BASE_URL);
            new XKCDQueryTask(silentListener).execute(url);
        } catch (MalformedURLException e){
            e.printStackTrace();
        }
    }

    private void renderXKCDPic(XKCDPic pic){
        titleText.setText(pic.num + ": " + pic.title);
        /* override is necessary to ensure picture fills up screen
         * otherwise the images would get smaller and smaller because
         * Glide uses the current size of the imageview as default bounds
         * for the new image
         */
        Glide.with(this)
                .load(pic.img)
                .override(2000, 2000)
                .into(imageView);
        creationDateText.setText(pic.day + "/" + pic.month + "/" + pic.year);

    }

    private void launchDetailActivity(){
        Intent intent = new Intent(MainActivity.this, ImageDetailActivity.class);
        intent.putExtra("URL", currentPic.img);
        startActivity(intent);
    }

}

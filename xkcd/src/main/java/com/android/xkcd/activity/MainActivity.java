package com.android.xkcd.activity;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Environment;
import android.os.PersistableBundle;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.android.xkcd.IAsyncTaskListener;
import com.android.xkcd.OnSwipeTouchListener;
import com.android.xkcd.R;
import com.android.xkcd.model.XKCDPic;
import com.android.xkcd.XKCDQueryTask;
import com.android.xkcd.fragment.AltTextDialog;
import com.android.xkcd.fragment.NumberPickerDialog;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.drawable.GlideDrawable;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.Serializable;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Random;

public class MainActivity extends AppCompatActivity {

    public static final String XKCD_QUERY_BASE_URL = "https://xkcd.com/info.0.json";
    public static final String XKCD_QUERY_BY_ID_URL = "https://xkcd.com/%s/info.0.json";
    public static final String EXPLAIN_XKCD_BASEURL = "http://www.explainxkcd.com/wiki/index.php/";

    private XKCDPic currentPic;
    private int mostRecent = 0;

    private TextView titleText;
    private TextView creationDateText;
    private ImageView imageView;
    private ProgressBar progressBar;
    private TextView altText;

    private FrameLayout mainLayout;

    private IAsyncTaskListener iAsyncTaskListener = new IAsyncTaskListener() {
        @Override
        public void onPreExecute() {
            progressBar.setVisibility(View.VISIBLE);
        }

        @Override
        public void onPostExecute(Serializable result) {
            if(result instanceof XKCDPic){
                MainActivity.this.currentPic = (XKCDPic) result;
                if(mostRecent < currentPic.num){
                    mostRecent = currentPic.num;
                }
                renderXKCDPic((XKCDPic) result);
            }

            //progressBar.setVisibility(View.GONE);
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
        altText = (TextView) findViewById(R.id.alt_text);
        mainLayout = (FrameLayout) findViewById(R.id.main_layout);
        /*
        imageView.setOnClickListener(new View.OnClickListener(){

            @Override
            public void onClick(View v) {
                launchDetailActivity();
                //loadRandomXKCDPic();
            }
        });
        */
        imageView.setOnLongClickListener(new View.OnLongClickListener(){
            @Override
            public boolean onLongClick(View v) {
                //launchAltDialog();
                launchDetailActivity();
                return false;
            }
        });

        mainLayout.setOnTouchListener(new OnSwipeTouchListener(MainActivity.this){
            @Override
            public void onSwipeLeft() {
                loadNext();
            }

            @Override
            public void onSwipeRight() {
                loadPrev();
            }
        });

        if(savedInstanceState==null){
            loadXKCDpic();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case R.id.load_rand_action:
                loadRandomXKCDPic();
                break;
            case R.id.goto_prev:
                if(currentPic != null){
                    loadPrev();
                    break;
                } else {
                    launchAltDialog();
                    break;
                }

            case R.id.goto_next:
                if(currentPic != null){
                    loadNext();
                    break;
                } else {
                    launchAltDialog();
                    break;
                }
            case R.id.goto_explain:
                gotoExplainXKCD();
                break;
            case R.id.view_alt:
                launchAltDialog();
                break;
            case R.id.goto_num_action:
                if(currentPic != null){
                    NumberPickerDialog dialog = new NumberPickerDialog();
                    dialog.setTitle(getResources().getString(R.string.choose_specific_title));
                    dialog.setRange(1, mostRecent);
                    dialog.setListener(new NumberPickerDialog.INumberPickerDialogListener() {
                        @Override
                        public void onPositiveClick(int number) {
                            loadXKCDpicById(number);
                        }

                        @Override
                        public void onNegativeClick() {

                        }
                    });
                    dialog.show(getSupportFragmentManager(), "DialogFragment");
                    break;
                } else {
                    launchAltDialog();
                }

            case R.id.share_action:
                Intent shareIntent = new Intent(Intent.ACTION_SEND);
                shareIntent.setType("image/png");
                shareIntent.putExtra(Intent.EXTRA_STREAM, getLocalBitmapUri(imageView));
                startActivity(Intent.createChooser(shareIntent, getResources()
                        .getString(R.string.share_menu_entry)));
                break;

        }
        return true;
    }

    private void loadNext() {
        int newId = currentPic.num + 1;
        if (newId > mostRecent){
            return;
        }
        loadXKCDpicById(newId);
    }

    private void loadPrev() {
        int newId = currentPic.num - 1;
        if (newId < 1){
            return;
        }
        loadXKCDpicById(newId);
    }

    private void gotoExplainXKCD(){
        if(currentPic != null){
            Intent browserIntent =
                    new Intent(Intent.ACTION_VIEW, Uri.parse(EXPLAIN_XKCD_BASEURL + currentPic.num));
            startActivity(browserIntent);
        } else {
            launchAltDialog();
        }
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
        titleText.setText(pic.num + ": " + pic.safe_title);
        altText.setText(pic.alt);
        /* override is necessary to ensure picture fills up screen
         * otherwise the images would get smaller and smaller because
         * Glide uses the current size of the imageview as default bounds
         * for the new image
         */

        Glide.with(this)
                .load(pic.img)
                .asBitmap()
                .listener(new RequestListener<String, Bitmap>() {
                    @Override
                    public boolean onException(Exception e, String model, Target<Bitmap> target, boolean isFirstResource) {
                        return false;
                    }

                    @Override
                    public boolean onResourceReady(Bitmap resource, String model, Target<Bitmap> target, boolean isFromMemoryCache, boolean isFirstResource) {
                        MainActivity.this.progressBar.setVisibility(View.GONE);
                        return false;
                    }
                })
                .override(2000, 2000)
                .into(imageView);
                /*
                .listener(new RequestListener<String, GlideDrawable>() {

                    @Override
                    public boolean onException(Exception e, String model, Target<GlideDrawable> target, boolean isFirstResource) {
                        return false;
                    }

                    @Override
                    public boolean onResourceReady(GlideDrawable resource, String model, Target<GlideDrawable> target, boolean isFromMemoryCache, boolean isFirstResource) {
                        MainActivity.this.progressBar.setVisibility(View.GONE);
                        return false;
                    }
                })
                .override(2000, 2000)
                .into(imageView);
                */
        creationDateText.setText(pic.day + "/" + pic.month + "/" + pic.year);

    }

    private void launchDetailActivity(){
        Intent intent = new Intent(MainActivity.this, ImageDetailActivity.class);
        intent.putExtra("URL", currentPic.img);
        startActivity(intent);
    }

    private void launchAltDialog(){
        AltTextDialog altFragment = new AltTextDialog();
        if(currentPic != null){
            altFragment.setAltText(currentPic.alt);
            altFragment.setTitle(currentPic.safe_title);
        } else {
            altFragment.setTitle(getResources().getString(R.string.waiting_for_download_message));
        }

        altFragment.setListener(new AltTextDialog.IAltTextInterfaceListener() {
            @Override
            public void onPositiveClick() {

            }

            @Override
            public void onNegativeClick() {
                gotoExplainXKCD();
            }
        });
        altFragment.show(getSupportFragmentManager(), "DialogFragment");
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putInt("pic_number", currentPic.num);
        outState.putInt("most_recent", mostRecent);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);

        this.mostRecent = savedInstanceState.getInt("most_recent");
        int lastImg = savedInstanceState.getInt("pic_number");
        if(lastImg != 0){
            loadXKCDpicById(lastImg);
        }
    }

    public Uri getLocalBitmapUri(ImageView imageView) {
        // Extract Bitmap from ImageView drawable
        Drawable drawable = imageView.getDrawable();
        Bitmap bmp = null;
        if (drawable instanceof BitmapDrawable){
            bmp = ((BitmapDrawable) imageView.getDrawable()).getBitmap();
        } else {
            return null;
        }
        // Store image to default external storage directory
        Uri bmpUri = null;
        try {
            File file = new File(getApplicationContext()
                    .getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS),
                    "share_image_" + System.currentTimeMillis() + "png");
            //File file =  new File(Environment.getExternalStoragePublicDirectory(
            //        Environment.DIRECTORY_DOWNLOADS), "share_image_" + System.currentTimeMillis() + ".png");
            file.getParentFile().mkdirs();
            FileOutputStream out = new FileOutputStream(file);
            bmp.compress(Bitmap.CompressFormat.PNG, 90, out);
            out.close();
            bmpUri = Uri.fromFile(file);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return bmpUri;
    }
}

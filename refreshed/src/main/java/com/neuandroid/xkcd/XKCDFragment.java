package com.neuandroid.xkcd;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.content.FileProvider;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.neuandroid.refreshed.R;
import com.neuandroid.xkcd.model.XKCDPic;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.Serializable;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Random;

/**
 * Created by max on 20/07/17.
 */

public class XKCDFragment extends Fragment {


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
                XKCDFragment.this.currentPic = (XKCDPic) result;
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
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_xkcd, container, false);
        
        titleText = (TextView) view.findViewById(R.id.image_title_text);
        creationDateText = (TextView) view.findViewById(R.id.creation_date_text);
        imageView = (ImageView) view.findViewById(R.id.image_view);
        progressBar = (ProgressBar) view.findViewById(R.id.progress_bar);
        altText = (TextView) view.findViewById(R.id.alt_text);
        mainLayout = (FrameLayout) view.findViewById(R.id.main_layout);

        imageView.setOnLongClickListener(new View.OnLongClickListener(){
            @Override
            public boolean onLongClick(View v) {
                //launchAltDialog();
                launchDetailActivity();
                return false;
            }
        });

        mainLayout.setOnTouchListener(new OnSwipeTouchListener(XKCDFragment.this.getActivity()){
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
        
        return view;
    }


    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_xkcd, menu);
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
                    dialog.show(getFragmentManager(), "DialogFragment");
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
                        XKCDFragment.this.progressBar.setVisibility(View.GONE);
                        return false;
                    }
                })
                .override(2000, 2000)
                .into(imageView);
        creationDateText.setText(pic.day + "/" + pic.month + "/" + pic.year);

    }

    private void launchDetailActivity(){
        Intent intent = new Intent(XKCDFragment.this.getActivity(), ImageDetailActivity.class);
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
        altFragment.show(getFragmentManager(), "DialogFragment");
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt("pic_number", currentPic.num);
        outState.putInt("most_recent", mostRecent);
    }

    @Override
    public void onViewStateRestored(@Nullable Bundle savedInstanceState) {
        super.onViewStateRestored(savedInstanceState);

        if(savedInstanceState != null){
            this.mostRecent = savedInstanceState.getInt("most_recent");
            int lastImg = savedInstanceState.getInt("pic_number");
            if(lastImg != 0){
                loadXKCDpicById(lastImg);
            }
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
            File file = new File(getContext().getApplicationContext()
                    .getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS),
                    "share_image_" + System.currentTimeMillis() + ".png");
            //File file =  new File(Environment.getExternalStoragePublicDirectory(
            //        Environment.DIRECTORY_DOWNLOADS), "share_image_" + System.currentTimeMillis() + ".png");
            file.getParentFile().mkdirs();
            FileOutputStream out = new FileOutputStream(file);
            bmp.compress(Bitmap.CompressFormat.PNG, 90, out);
            out.close();
            //bmpUri = Uri.fromFile(file);
            bmpUri = FileProvider.getUriForFile(getContext(),
                    getContext().getApplicationContext().getPackageName() +
                            ".com.neuandroid.util.fileprovider",
                    file);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return bmpUri;
    }
}

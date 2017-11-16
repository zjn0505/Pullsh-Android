package com.neuandroid.departify;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.graphics.Palette;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.AWSCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.mobileconnectors.apigateway.ApiClientException;
import com.amazonaws.mobileconnectors.apigateway.ApiClientFactory;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.drawable.GlideDrawable;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.deeparteffects.sdk.android.DeepArtEffectsClient;
import com.deeparteffects.sdk.android.model.Result;
import com.deeparteffects.sdk.android.model.Styles;
import com.deeparteffects.sdk.android.model.UploadRequest;
import com.deeparteffects.sdk.android.model.UploadResponse;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import butterknife.BindView;
import butterknife.BindViews;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.OnLongClick;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = MainActivity.class.getSimpleName();
    private static final int REQUEST_CAMERA = 100;
    private static final int REQUEST_GALLERY = 101;
    private static final int CHECK_RESULT_INTERVAL_IN_MS = 2500;
    private static final int IMAGE_MAX_SIDE_LENGTH = 768;
    private static final int MSG_REQUEST_TIMEOUT = 100;
    private static final int MSG_REQUEST_SUCCEED = 101;

    @BindView(R.id.rv_art_styles)
    RecyclerView rvArtStyles;
    @BindView(R.id.iv_departified_pic)
    ImageView ivDepartifiedPic;
    @BindView(R.id.pb_loading)
    ProgressBar pbLoading;
    @BindView(R.id.control_panel)
    LinearLayout controlPanel;
    @BindView(R.id.btn_take_photo)
    Button btnCamera;
    @BindView(R.id.btn_open_album)
    Button btnAlbum;
    @BindView(R.id.btn_load_style)
    Button btnLoadStyle;

    private String apiKey = "", accessKey = "", secretKey = "";
    private DeepArtEffectsClient deepArtEffectsClient;
    private Bitmap mImageBitmap;
    private Context mContext;
    private ArtStyleAdapter styleAdapter;
    private GridLayoutManager mLayoutManager;

    private boolean isProcessing = false;
    private String currentUrl = null;
    private Palette palette;
    private boolean shouldShowTitle;
    private SharedPreferences sharedPreferences;

    ButterKnife.Action<SquareView> setColor = new ButterKnife.Action<SquareView>() {

        @Override
        public void apply(@NonNull SquareView view, int index) {
            if (palette != null) {
                Palette.Swatch swatch = null;
                switch (index) {
                    case 0:
                        swatch = palette.getLightMutedSwatch();
                        break;
                    case 1:
                        swatch = palette.getVibrantSwatch();
                        break;
                    case 2:
                        swatch = palette.getDarkVibrantSwatch();
                        break;
                    case 3:
                        swatch = palette.getLightMutedSwatch();
                        break;
                    case 4:
                        swatch = palette.getMutedSwatch();
                        break;
                    case 5:
                        swatch = palette.getDarkMutedSwatch();
                        break;

                }
                if (swatch != null) {
                    int i = swatch.getRgb();
                    String strColor = String.format("#%06X", 0xFFFFFF & i);
                    view.setText(strColor);
                    view.setTextColor(swatch.getTitleTextColor());
                    view.setBackgroundColor(swatch.getRgb());
                }
            }

        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
        mContext = this;
        rvArtStyles.setHasFixedSize(true);
        mLayoutManager = new GridLayoutManager(this, 2, GridLayoutManager.HORIZONTAL, false);
        mLayoutManager.setSpanSizeLookup(new GridLayoutManager.SpanSizeLookup() {
            @Override
            public int getSpanSize(int position) {
                return styleAdapter.getItemViewType(position);
            }
        });
        rvArtStyles.setLayoutManager(mLayoutManager);
        rvArtStyles.setItemAnimator(new DefaultItemAnimator());


        String packageName = mContext.getApplicationContext().getPackageName();
        try {
            ApplicationInfo appInfo = mContext.getPackageManager().getApplicationInfo(packageName, PackageManager.GET_META_DATA);
            if (appInfo.metaData != null) {
                apiKey = appInfo.metaData.getString("DEEP_ART_EFFECT_API_KEY");
                accessKey = appInfo.metaData.getString("DEEP_ART_EFFECT_ACCESS_KEY");
                secretKey = appInfo.metaData.getString("DEEP_ART_EFFECT_SECRET_KEY");
            }
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }

        ApiClientFactory factory = new ApiClientFactory()
                .apiKey(apiKey)
                .region("eu-west-1")
                .credentialsProvider(new AWSCredentialsProvider() {
                    @Override
                    public AWSCredentials getCredentials() {
                        return new BasicAWSCredentials(accessKey, secretKey); // TODO
                    }

                    @Override
                    public void refresh() {
                    }
                });
        deepArtEffectsClient = factory.build(DeepArtEffectsClient.class);
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(mContext);
        shouldShowTitle = sharedPreferences.getBoolean("pref_title", false);
        loadingStyles();
    }

    @Override
    protected void onResume() {
        super.onResume();
        boolean newShouldShowTitle = sharedPreferences.getBoolean("pref_title", false);
        if (shouldShowTitle != newShouldShowTitle) {
            shouldShowTitle = newShouldShowTitle;
            if (styleAdapter != null) {
                styleAdapter.notifyDataSetChanged();
            }
        }

    }

    @OnClick(R.id.btn_load_style)
    void loadingStyles() {
        new Thread(new Runnable() {
            @Override
            public void run() {

                try {
                    final Styles styles = deepArtEffectsClient.stylesGet();

                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            styleAdapter = new ArtStyleAdapter(
                                    getApplicationContext(),
                                    styles,
                                    new ArtStyleAdapter.IClickListener() {
                                        @Override
                                        public void onClick(String styleId) {
                                            if (!isProcessing) {
                                                if (mImageBitmap != null) {
                                                    Log.d(TAG, String.format("Style with ID %s clicked.", styleId));
                                                    isProcessing = true;
                                                    pbLoading.setVisibility(View.VISIBLE);
                                                    uploadImage(styleId);
                                                } else {
                                                    Toast.makeText(mContext, "Please choose a picture first",
                                                            Toast.LENGTH_SHORT).show();
                                                }
                                            }
                                        }
                                    }
                            );
                            btnLoadStyle.setVisibility(View.GONE);
                            rvArtStyles.setAdapter(styleAdapter);
                            pbLoading.setVisibility(View.GONE);
                            controlPanel.setVisibility(View.VISIBLE);
                        }
                    });
                } catch (ApiClientException e) {
                    e.printStackTrace();
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(MainActivity.this, "Failed to connect to Deep Art Effects service", Toast.LENGTH_SHORT).show();
                            pbLoading.setVisibility(View.GONE);
                        }
                    });
                }

            }
        }).start();
    }

    private void uploadImage(final String styleId) {
//        mStatusText.setText("Uploading picture...");
        Log.d(TAG, String.format("Upload image with style id %s", styleId));
        final MyHandler myHandler = new MyHandler();
        new Thread(new Runnable() {
            @Override
            public void run() {

                UploadRequest uploadRequest = new UploadRequest();
                uploadRequest.setStyleId(styleId);
                uploadRequest.setImageBase64Encoded(convertBitmapToBase64(mImageBitmap));
                UploadResponse response = null;
                try {
                    response = deepArtEffectsClient.uploadPost(uploadRequest);
                } catch (RuntimeException ex) {
                    ex.printStackTrace();
                    myHandler.sendEmptyMessage(MSG_REQUEST_TIMEOUT);
                }
                if (response == null) {
                    return;
                }
                String submissionId = response.getSubmissionId();
                Log.d(TAG, String.format("Upload complete. Got submissionId %s", response.getSubmissionId()));
                Timer timer = new Timer();
                timer.scheduleAtFixedRate(new ImageReadyCheckTimer(submissionId),
                        CHECK_RESULT_INTERVAL_IN_MS, CHECK_RESULT_INTERVAL_IN_MS);
                myHandler.sendEmptyMessage(MSG_REQUEST_SUCCEED);
            }
        }).start();
    }

    private String convertBitmapToBase64(Bitmap bitmap) {
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream);
        byte[] byteArray = stream.toByteArray();
        return Base64.encodeToString(byteArray, 0);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_share:
                Intent shareIntent = new Intent();
                shareIntent.setAction(Intent.ACTION_SEND);
//                shareIntent.putExtra(Intent.EXTRA_STREAM, ivDepartifiedPic.);
                shareIntent.setType("image/jpeg");
                startActivity(Intent.createChooser(shareIntent, getString(R.string.share_to)));

                break;
            case R.id.action_camera:
                openCamera();
                break;
            case R.id.action_gallery:
                openAlbum();
                break;
            case R.id.action_about:
                startActivity(new Intent(this, AboutActivity.class));
                break;
            case R.id.action_settings:
                startActivity(new Intent(this, SettingsActivity.class));
            default:
                break;
        }
        return true;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case REQUEST_CAMERA:
                if (resultCode == RESULT_OK) {
                    setPic();
                }
                break;
            case REQUEST_GALLERY:
                if (resultCode == RESULT_OK) {
                    mImageBitmap = ImageHelper.loadSizeLimitedBitmapFromUri(data.getData(),
                            this.getContentResolver(), IMAGE_MAX_SIDE_LENGTH);
                    ivDepartifiedPic.setImageBitmap(mImageBitmap);
                    currentUrl = null;
                }
                break;
            default:
                break;
        }

        super.onActivityResult(requestCode, resultCode, data);
    }

    @OnClick(R.id.btn_take_photo)
    void openCamera() {
        Intent takePhotoIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePhotoIntent.resolveActivity(getPackageManager()) != null) {
            File photoFile = null;
            try {
                photoFile = createImageFile();
            } catch (IOException e) {
                e.printStackTrace();
                Toast.makeText(mContext, "Failed to take a photo", Toast.LENGTH_SHORT).show();
                return;
            }

            if (photoFile != null) {
                Uri photoURI = FileProvider.getUriForFile(this, BuildConfig.APPLICATION_ID+".fileprovider", photoFile);
                takePhotoIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                startActivityForResult(takePhotoIntent, REQUEST_CAMERA);
            }

        }
    }


    @OnClick(R.id.btn_open_album)
    void openAlbum() {
        Intent openAlbumIntent = new Intent(Intent.ACTION_GET_CONTENT);
        openAlbumIntent.setType("image/*");
        if (openAlbumIntent.resolveActivity(getPackageManager()) != null) {
            startActivityForResult(openAlbumIntent, REQUEST_GALLERY);
        }
    }

    @OnLongClick(R.id.iv_departified_pic)
    boolean onImgLongClick(ImageView imgView) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                createPaletteAsync(mImageBitmap);
            }
        }).start();
        return true;
    }

    private boolean doubleBackToExit = false;

    @Override
    public void onBackPressed() {

        if (doubleBackToExit) {
            finish();
        }
        doubleBackToExit = true;
        Toast.makeText(mContext, getString(R.string.click_back_again), Toast.LENGTH_SHORT).show();
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                doubleBackToExit = false;
            }
        }, 2000);


    }

    private String mCurrentPhotoPath;

    private File createImageFile() throws IOException {
        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(
                imageFileName,  /* prefix */
                ".jpg",         /* suffix */
                storageDir      /* directory */
        );

        // Save a file: path for use with ACTION_VIEW intents
        mCurrentPhotoPath = image.getAbsolutePath();
        return image;
    }

    private void setPic() {
        // Get the dimensions of the View
        int targetW = ivDepartifiedPic.getWidth();
        int targetH = ivDepartifiedPic.getHeight();

        // Get the dimensions of the bitmap
        BitmapFactory.Options bmOptions = new BitmapFactory.Options();
        bmOptions.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(mCurrentPhotoPath, bmOptions);
        int photoW = bmOptions.outWidth;

        // Determine how much to scale down the image
        int scaleFactor = photoW/targetW;

        // Decode the image file into a Bitmap sized to fill the View
        bmOptions.inJustDecodeBounds = false;
        bmOptions.inSampleSize = scaleFactor;
        bmOptions.inPurgeable = true;

        mImageBitmap = BitmapFactory.decodeFile(mCurrentPhotoPath, bmOptions);
        ivDepartifiedPic.setImageBitmap(mImageBitmap);
    }

    private void createPaletteAsync(Bitmap bitmap) {
        Palette.from(bitmap).generate(new Palette.PaletteAsyncListener() {
            public void onGenerated(Palette p) {
                // Use generated instance
                View view = LayoutInflater.from(mContext).inflate(R.layout.dialog_palette, null);
                palette = p;
                AlertDialog.Builder builder = new AlertDialog.Builder(mContext).setView(view);
                DialogViews dialogViews = new DialogViews();
                ButterKnife.bind(dialogViews, view);
                ButterKnife.apply(dialogViews.paletteImages, setColor);
                if (TextUtils.isEmpty(currentUrl)) {
                    dialogViews.ivDialogPic.setImageBitmap(mImageBitmap);
                } else {
                    Glide.with(mContext).load(currentUrl).centerCrop().into(dialogViews.ivDialogPic);
                }
                Palette.Swatch dominantSwatch = palette.getDominantSwatch();
                if (dominantSwatch != null) {
                    int color = dominantSwatch.getRgb();
                    dialogViews.tvDominant.setText(String.format("#%06X", 0xFFFFFF & color));
                    dialogViews.tvDominant.setTextColor(color);
                }

                builder.show();
            }
        });
    }

    private class ImageReadyCheckTimer extends TimerTask {
        private String mSubmissionId;

        public ImageReadyCheckTimer(String submissionId) {
            mSubmissionId = String.valueOf(submissionId);
        }

        @Override
        public void run() {
            try {
                final Result result = deepArtEffectsClient.resultGet(mSubmissionId);
                currentUrl = result.getUrl();
                String submissionStatus = result.getStatus();
                Log.d(TAG, String.format("Submission status is %s", submissionStatus));
                if (submissionStatus.equals(SubmissionStatus.FINISHED)) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Glide.with(mContext).load(result.getUrl()).listener(new RequestListener<String, GlideDrawable>() {
                                @Override
                                public boolean onException(Exception e, String model, Target<GlideDrawable> target, boolean isFirstResource) {
                                    pbLoading.setVisibility(View.GONE);
                                    return false;
                                }

                                @Override
                                public boolean onResourceReady(GlideDrawable resource, String model, Target<GlideDrawable> target, boolean isFromMemoryCache, boolean isFirstResource) {
                                    pbLoading.setVisibility(View.GONE);
                                    return false;
                                }
                            }).centerCrop().crossFade().into(ivDepartifiedPic);
                            ivDepartifiedPic.setVisibility(View.VISIBLE);
//                            mStatusText.setText("");
                        }
                    });
                    isProcessing = false;
                    cancel();
                }
            } catch (Exception e) {
                cancel();
            }
        }
    }

    private class MyHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MSG_REQUEST_SUCCEED:
                    break;
                case MSG_REQUEST_TIMEOUT:
                    pbLoading.setVisibility(View.GONE);
                    isProcessing = false;
                    Toast.makeText(mContext, "Request failed, time out", Toast.LENGTH_SHORT).show();
                    break;
            }
        }
    }

    class DialogViews {
        @BindViews({R.id.iv_palette_1, R.id.iv_palette_2, R.id.iv_palette_3, R.id.iv_palette_4, R.id.iv_palette_5, R.id.iv_palette_6})
        List<SquareView> paletteImages;
        @BindView(R.id.iv_dialog_pic)
        ImageView ivDialogPic;
        @BindView(R.id.tv_dominant_color)
        TextView tvDominant;
    }
}

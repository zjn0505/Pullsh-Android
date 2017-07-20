package com.neuandroid.xkcd;

import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.View;

import com.bumptech.glide.Glide;
import com.github.chrisbanes.photoview.PhotoView;
import com.neuandroid.refreshed.R;

/**
 * Created by max on 13/07/17.
 */

public class ImageDetailActivity extends Activity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.image_detail);

        String url = getIntent().getStringExtra("URL");

        PhotoView photoView = (PhotoView) findViewById(R.id.photo_view);
        Glide.with(this).load(url).into(photoView);

        photoView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

}

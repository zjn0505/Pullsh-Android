package xyz.jienan.pushpull.ui;

import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.ShareCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.app.AppCompatDelegate;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;

import xyz.jienan.pushpull.R;

public class MainActivity extends AppCompatActivity {

    private final static String TAG = MainActivity.class.getSimpleName();
    private OnBackPressedListener mListener;
    private Fragment fragment;


    static {
        AppCompatDelegate.setCompatVectorFromResourcesEnabled(true);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FragmentManager fragmentManager = getSupportFragmentManager();
        fragment = fragmentManager.findFragmentById(R.id.fragment_pushpull);

    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
        String action = intent.getAction();
        String type = intent.getType();
        if (Intent.ACTION_SEND.equals(action) && type != null) {
            if (type.startsWith("text/")) {
                handleSendText();
            } else if (type.startsWith("image/")) {
                // not implemented yet
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        return true;
    }

    @Override
    public void onBackPressed() {
        if (mListener != null && mListener.onBackPressed()) {

        } else{
            super.onBackPressed();
        }

    }

    public void setBackPressListener(OnBackPressedListener listener) {
        mListener = listener;
    }

    public interface OnBackPressedListener {
        boolean onBackPressed();
    }

    private void handleSendText() {
        final StringBuilder txt = new StringBuilder();
        final ShareCompat.IntentReader intentReader = ShareCompat.IntentReader.from(this);
        if (intentReader.getText() != null)
            txt.append(intentReader.getText()+"\n");
        final int N = intentReader.getStreamCount();
        if (N > 0) {
            AlertDialog dialog = new AlertDialog.Builder(this).setTitle("File received").setMessage("Do you want to proceed?")
                    .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            for (int i = 0; i < N; i++) {
                                Uri uri = intentReader.getStream(i);
                                txt.append("Share included stream " + i + ": " + uri + "\n");
                                try {
                                    BufferedReader reader = new BufferedReader(new InputStreamReader(
                                            getContentResolver().openInputStream(uri)));
                                    try {
                                        String str = null;
                                        while ((str = reader.readLine()) != null) {
                                            txt.append(str + "\n");
                                        }
                                    } catch (IOException e) {
                                        Log.e(TAG, "Reading stream threw exception", e);
                                    } finally {
                                        reader.close();
                                    }
                                } catch (FileNotFoundException e) {
                                    Log.e(TAG, "File not found from share.", e);
                                } catch (IOException e) {
                                    Log.d(TAG, "I/O Error", e);
                                }
                                ((FragmentPushPull)fragment).handleSendText(txt.toString());
                            }
                        }
                    })
                    .setNegativeButton("No", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            String str = txt.toString();
                            if (!TextUtils.isEmpty(str))
                                ((FragmentPushPull)fragment).handleSendText(txt.toString());
                        }
                    }).create();
            dialog.show();
        } else {
            String str = txt.toString();
            if (!TextUtils.isEmpty(str))
                ((FragmentPushPull)fragment).handleSendText(txt.toString());
        }
    }
}
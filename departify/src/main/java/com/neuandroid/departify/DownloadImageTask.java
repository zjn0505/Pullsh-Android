package com.neuandroid.departify;

import android.os.AsyncTask;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLConnection;

/**
 * Created by Jienan on 2017/11/20.
 */

public class DownloadImageTask extends AsyncTask<String, String, String> {

    private File f;
    private DownloadImageTaskListener mListener;

    public interface DownloadImageTaskListener {
        void progressUpdate(int progress);
        void downloadResult(String path);
    }

    public DownloadImageTask(File file, DownloadImageTaskListener listener) {
        f = file;
        mListener = listener;
    }

    @Override
    protected String doInBackground(String... urls) {
        int count;
        try {
            URL url = new URL(urls[0]);
            URLConnection connection = url.openConnection();
            connection.connect();
            int lenghtOfFile = connection.getContentLength();
            InputStream input = new BufferedInputStream(url.openStream(), lenghtOfFile);
            OutputStream output = new FileOutputStream(f);
            byte data[] = new byte[1024];
            long total = 0;
            while ((count = input.read(data)) != -1) {
                total += count;
                // publishing the progress....
                // After this onProgressUpdate will be called
                publishProgress(""+(int)((total*100)/lenghtOfFile));

                // writing data to file
                output.write(data, 0, count);
            }

            output.flush();
            output.close();
            input.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    protected void onProgressUpdate(String... progress) {
        mListener.progressUpdate(Integer.valueOf(progress[0]));
    }

    /**
     * After completing background task
     * Dismiss the progress dialog
     * **/
    @Override
    protected void onPostExecute(String file_url) {
        String imagePath = f.getPath();
        mListener.downloadResult(imagePath);
    }
}

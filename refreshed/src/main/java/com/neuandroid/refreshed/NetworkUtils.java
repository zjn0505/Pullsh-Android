package com.neuandroid.refreshed;

import android.net.Uri;
import android.text.TextUtils;
import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Scanner;

/**
 * Created by jienanzhang on 08/07/2017.
 */

public class NetworkUtils {

    private static final String NEWS_API_BASE_URL = "https://newsapi.org/v1/articles?source=%1$s&sortBy=%2$s&apiKey=%3$s";
    public static final String NEWS_API_SOURCES_URL = "https://newsapi.org/v1/sources?language=en";


    private final static String SAMPLE_TOKEN = "2a2736d38cc8461daf7af2a80f2e0ffe";
    private final static String PARAMS_TOP = "top";
    private final static String PARAMS_LATEST = "latest";


    private final static String TAG = "NetworkUtils";

    /**
     * Builds the URL used to query news api.
     *
     * @param source the source of news
     * @param sortBy top or latest
     * @return The URL to use to query the server.
     */
    public static URL buildUrlForSource(String source, String sortBy) {
        String newsUrl = String.format(NEWS_API_BASE_URL, source, sortBy, SAMPLE_TOKEN);
        URL url = buildUrl(newsUrl);
        return url;
    }


    public static URL buildUrl(String urlString) {
        URL url = null;
        try {
            Log.d(TAG, urlString); // Add a log so we can check what URL will be used in debug.
            url = new URL(urlString);
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
        return url;
    }




    public static String getResponseFromHttpUrl(URL url) throws IOException {
        HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
        try {
            InputStream in = urlConnection.getInputStream();

            Scanner scanner = new Scanner(in);
            scanner.useDelimiter("\\A");

            boolean hasInput = scanner.hasNext();
            if (hasInput) {
                return scanner.next();
            } else {
                return null;
            }
        } finally {
            urlConnection.disconnect();
        }
    }
}

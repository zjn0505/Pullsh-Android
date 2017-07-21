package com.neuandroid.news.util;

import android.util.Log;

import com.neuandroid.util.NetworkUtils;

import java.net.MalformedURLException;
import java.net.URL;

/**
 * Created by max on 19/07/17.
 */

public class NewsNetworkUtils extends NetworkUtils {

    private static final String NEWS_API_BASE_URL = "https://newsapi.org/v1/articles?source=%1$s&sortBy=%2$s&apiKey=%3$s";


    private final static String SAMPLE_TOKEN = "2a2736d38cc8461daf7af2a80f2e0ffe";
    private final static String PARAMS_TOP = "top";
    private final static String PARAMS_LATEST = "latest";

    private final static String TAG = "NewsNetworkUtils";

    /**
     * Builds the URL used to query news api.
     *
     * @param source the source of news
     * @param sortBy top or latest
     * @return The URL to use to query the server.
     */
    public static URL buildUrl(String source, String sortBy) {

        String newsUrl = String.format(NEWS_API_BASE_URL, source, sortBy, SAMPLE_TOKEN);

        URL url = null;
        try {
            Log.d(TAG, newsUrl); // Add a log so we can check what URL will be used in debug.
            url = new URL(newsUrl);
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
        return url;
    }
}

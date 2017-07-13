package com.android.xkcd;

import java.io.Serializable;

/**
 * Created by max on 13/07/17.
 */

public interface IAsyncTaskListener {
    void onPreExecute();
    void onPostExecute(Serializable result);
}

package com.neuandroid.xkcd.util;

import java.io.Serializable;

/**
 * Created by max on 13/07/17.
 */

/**
 * Task listener to implement when something needs to be done before and after a task completes.
 */
public interface IAsyncTaskListener {
    void onPreExecute();
    void onPostExecute(Serializable result);
}

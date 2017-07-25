package com.neuandroid.xkcd.model;

import java.io.Serializable;

/**
 * Created by max on 13/07/17.
 */

/**
 * A serializable object to parse the xkcd json data
 */
public class XKCDPic implements Serializable {

    public String year;
    public String month;
    public String day;
    public int num;
    public String title;
    public String img;
    public String alt;
    public String safe_title;

}

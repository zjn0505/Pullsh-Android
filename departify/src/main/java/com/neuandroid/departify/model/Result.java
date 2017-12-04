package com.neuandroid.departify.model;

/**
 * Created by Jienan on 2017/12/4.
 */

import com.google.gson.annotations.SerializedName;

public class Result {
    @SerializedName("status")
    private String status = null;
    @SerializedName("url")
    private String url = null;

    public Result() {
    }

    public String getStatus() {
        return this.status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getUrl() {
        return this.url;
    }

    public void setUrl(String url) {
        this.url = url;
    }
}


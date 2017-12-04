package com.neuandroid.departify.model;

/**
 * Created by Jienan on 2017/12/4.
 */

import com.google.gson.annotations.SerializedName;

public class Style {
    @SerializedName("id")
    private String id = null;
    @SerializedName("title")
    private String title = null;
    @SerializedName("url")
    private String url = null;
    @SerializedName("description")
    private String description = null;

    public Style() {
    }

    public String getId() {
        return this.id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTitle() {
        return this.title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getUrl() {
        return this.url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getDescription() {
        return this.description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}

package com.neuandroid.departify.model;

/**
 * Created by Jienan on 2017/12/4.
 */

import com.google.gson.annotations.SerializedName;

public class UploadRequest {
    @SerializedName("styleId")
    private String styleId = null;
    @SerializedName("imageBase64Encoded")
    private String imageBase64Encoded = null;
    @SerializedName("imageSize")
    private Integer imageSize = null;
    @SerializedName("partnerId")
    private String partnerId = null;

    public UploadRequest() {
    }

    public String getStyleId() {
        return this.styleId;
    }

    public void setStyleId(String styleId) {
        this.styleId = styleId;
    }

    public String getImageBase64Encoded() {
        return this.imageBase64Encoded;
    }

    public void setImageBase64Encoded(String imageBase64Encoded) {
        this.imageBase64Encoded = imageBase64Encoded;
    }

    public Integer getImageSize() {
        return this.imageSize;
    }

    public void setImageSize(Integer imageSize) {
        this.imageSize = imageSize;
    }

    public String getPartnerId() {
        return this.partnerId;
    }

    public void setPartnerId(String partnerId) {
        this.partnerId = partnerId;
    }
}

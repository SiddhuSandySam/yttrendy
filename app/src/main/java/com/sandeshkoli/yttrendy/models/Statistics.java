package com.sandeshkoli.yttrendy.models;

import com.google.gson.annotations.SerializedName;

public class Statistics {
    @SerializedName("viewCount")
    private String viewCount;

    public String getViewCount() {
        return viewCount;
    }
}
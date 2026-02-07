package com.sandeshkoli.yttrendy.models;

import com.google.gson.annotations.SerializedName;

public class VideoItem {

    // Ab hum ise seedha String rakh sakte hain kyunki backend normalize kar raha hai
    @SerializedName("id")
    private String id;

    @SerializedName("snippet")
    private Snippet snippet;

    @SerializedName("statistics")
    private Statistics statistics;

    public String getId() {
        return id != null ? id : "";
    }

    public Snippet getSnippet() {
        return snippet;
    }

    public Statistics getStatistics() {
        return statistics;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setSnippet(Snippet snippet) {
        this.snippet = snippet;
    }
}
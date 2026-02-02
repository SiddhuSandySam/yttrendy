package com.sandeshkoli.yttrendy.models;

import com.google.gson.annotations.SerializedName;

public class Snippet {
    @SerializedName("title")
    private String title;

    @SerializedName("channelTitle")
    private String channelTitle;

    @SerializedName("thumbnails")
    private Thumbnails thumbnails;

    public String getTitle() {
        return title;
    }

    public String getChannelTitle() {
        return channelTitle;
    }

    public Thumbnails getThumbnails() {
        return thumbnails;
    }

    public Snippet(String title, String channelTitle, Thumbnails thumbnails) {
        this.title = title;
        this.channelTitle = channelTitle;
        this.thumbnails = thumbnails;
    }
}
package com.sandeshkoli.yttrendy.models;

import com.google.gson.annotations.SerializedName;

public class Thumbnails {
    @SerializedName("high")
    private Thumbnail high;

    public Thumbnail getHigh() {
        return high;
    }

    // Thumbnails.java me
    public Thumbnails(Thumbnail high) { this.high = high; }

    // Thumbnail.java me

}
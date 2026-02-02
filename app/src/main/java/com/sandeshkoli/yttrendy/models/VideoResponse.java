package com.sandeshkoli.yttrendy.models;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class VideoResponse {
    @SerializedName("items")
    private List<VideoItem> items;

    public List<VideoItem> getItems() {
        return items;
    }
}
package com.sandeshkoli.yttrendy.models;

import com.google.gson.annotations.SerializedName;
import com.google.gson.internal.LinkedTreeMap; // Import zaroori hai

public class VideoItem {

    // Trick: Hum ID ko generic Object lenge
    @SerializedName("id")
    private Object id;

    @SerializedName("snippet")
    private Snippet snippet;

    @SerializedName("statistics")
    private Statistics statistics;

    public String getId() {
        if (id == null) return "";

        // Case 1: Trending API (ID ek seedha String hota hai)
        if (id instanceof String) {
            return (String) id;
        }

        // Case 2: Search API (ID ek Object/Map hota hai)
        // Gson ise LinkedTreeMap bana deta hai
        else if (id instanceof LinkedTreeMap) {
            LinkedTreeMap map = (LinkedTreeMap) id;
            if (map.containsKey("videoId")) {
                return (String) map.get("videoId");
            }
        }

        return ""; // Fallback
    }

    public Snippet getSnippet() {
        return snippet;
    }

    public Statistics getStatistics() {
        return statistics;
    }

    public void setId(Object id) { this.id = id; }
    public void setSnippet(Snippet snippet) { this.snippet = snippet; }
}
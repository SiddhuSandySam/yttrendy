package com.sandeshkoli.yttrendy.models;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "saved_videos")
public class VideoEntity {
    @PrimaryKey
    @NonNull
    public String videoId;
    public String title;
    public String channelName;
    public String thumbnailUrl;

    public VideoEntity(@NonNull String videoId, String title, String channelName, String thumbnailUrl) {
        this.videoId = videoId;
        this.title = title;
        this.channelName = channelName;
        this.thumbnailUrl = thumbnailUrl;
    }
}
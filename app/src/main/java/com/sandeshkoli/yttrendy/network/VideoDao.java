package com.sandeshkoli.yttrendy.network;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import java.util.List;
import com.sandeshkoli.yttrendy.models.VideoEntity;

@Dao
public interface VideoDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void saveVideo(VideoEntity video);

    @Query("SELECT * FROM saved_videos")
    List<VideoEntity> getAllSavedVideos();

    @Query("SELECT EXISTS(SELECT * FROM saved_videos WHERE videoId = :id)")
    boolean isSaved(String id);

    @Delete
    void deleteVideo(VideoEntity video);
}
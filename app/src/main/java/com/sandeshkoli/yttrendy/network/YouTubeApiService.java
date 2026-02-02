package com.sandeshkoli.yttrendy.network;

import com.sandeshkoli.yttrendy.models.VideoResponse;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface YouTubeApiService {
    // network/YouTubeApiService.java

    @GET("youtube/v3/videos")
    Call<VideoResponse> getVideos(
            @Query("part") String part,          // snippet,statistics
            @Query("chart") String chart,        // mostPopular
            @Query("regionCode") String regionCode, // IN
            @Query("videoCategoryId") String videoCategoryId, // Category ID
            @Query("maxResults") int maxResults,
            @Query("key") String apiKey
    );
    @GET("youtube/v3/search")
    Call<VideoResponse> searchVideos(
            @Query("part") String part,
            @Query("q") String query,
            @Query("order") String order,
            @Query("publishedAfter") String publishedAfter,
            @Query("videoDuration") String videoDuration,
            @Query("eventType") String eventType, // <--- YE PARAMETER ZAROORI HAI
            @Query("type") String type,
            @Query("maxResults") int maxResults,
            @Query("key") String apiKey
    );
}
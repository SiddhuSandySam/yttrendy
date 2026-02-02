package com.sandeshkoli.yttrendy;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.sandeshkoli.yttrendy.adapter.VideoAdapter;
import com.sandeshkoli.yttrendy.models.VideoItem;
import com.sandeshkoli.yttrendy.models.VideoResponse;
import com.sandeshkoli.yttrendy.network.RetrofitClient;
import com.sandeshkoli.yttrendy.network.YouTubeApiService;
import com.sandeshkoli.yttrendy.utils.JsonCacheManager;
import com.sandeshkoli.yttrendy.utils.KeyManager;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class CategoryListActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private VideoAdapter adapter;
    private List<VideoItem> videoList = new ArrayList<>();
    private ProgressBar progressBar;

    private String categoryId, categoryName;
    private TextView btnFilter, titleView;
    private ImageView btnBack;

    private String API_KEY;
    // Filters State
    private String currentOrder = "relevance";
    private String publishedAfter = null;
    private String videoDuration = "any";
    private String eventType = "completed"; // <--- YE LINE MISSING THI

    private boolean isFilterApplied = false; // Flag to check if we need Search API

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_category_list);

        categoryId = getIntent().getStringExtra("CAT_ID");
        categoryName = getIntent().getStringExtra("CAT_NAME");

        API_KEY = KeyManager.getApiKey(this);

        // Init Views (Make sure IDs match your XML)
        titleView = findViewById(R.id.toolbar_title);
        btnBack = findViewById(R.id.btn_menu);
        btnFilter = findViewById(R.id.btn_filter);
        progressBar = findViewById(R.id.search_loading); // XML me ProgressBar add karlena agar nahi hai
        recyclerView = findViewById(R.id.vertical_recycler_view);

        if(titleView != null) titleView.setText(categoryName);
        if(btnBack != null) btnBack.setOnClickListener(v -> finish());
        if(btnFilter != null) btnFilter.setOnClickListener(v -> showFilterMenu());

        setupRecyclerView();

        // Pehli baar load karo
        decideAndFetchData(0);
    }

    private void setupRecyclerView() {
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        // GALTI YAHAN THI: Tum purana constructor use kar rahe the
        // adapter = new VideoAdapter(this, videoList, listener);  <-- YE GALAT HAI

        // SAHI CODE (Pass 'true' for Full Width):
        adapter = new VideoAdapter(this, videoList, true, item -> {
            Intent intent = new Intent(this, VideoPlayerActivity.class);
            intent.putExtra("VIDEO_ID", item.getId());
            intent.putExtra("VIDEO_TITLE", item.getSnippet().getTitle());
            intent.putExtra("VIDEO_DESC", item.getSnippet().getChannelTitle());
            startActivity(intent);
        });

        recyclerView.setAdapter(adapter);
    }

    private void showFilterMenu() {
        PopupMenu popup = new PopupMenu(this, btnFilter);
        popup.getMenuInflater().inflate(R.menu.menu_filter, popup.getMenu());

        popup.setOnMenuItemClickListener(item -> {
            item.setChecked(true);
            int id = item.getItemId();

            // Default: Hum maan ke chalte hain filter lag gaya hai
            isFilterApplied = true;

            // 1. TIME FILTERS
            if (id == R.id.time_hour) setTimeFilter(1);
            else if (id == R.id.time_today) setTimeFilter(24);
            else if (id == R.id.time_week) setTimeFilter(24 * 7);
            else if (id == R.id.time_any) {
                publishedAfter = null;
                // Agar koi aur filter nahi hai, toh Trending API pe wapas ja sakte hain
            }

            // 2. SORT FILTERS
            else if (id == R.id.sort_views) currentOrder = "viewCount";
            else if (id == R.id.sort_date) currentOrder = "date";
            else if (id == R.id.sort_rating) currentOrder = "rating";
            else if (id == R.id.sort_relevance) currentOrder = "relevance";

                // 3. DURATION FILTERS
            else if (id == R.id.dur_short) videoDuration = "short";
            else if (id == R.id.dur_long) videoDuration = "long";
            else if (id == R.id.dur_any) videoDuration = "any";

                // 4. CONTENT TYPE FILTERS (Live vs All)
            else if (id == R.id.filter_live) eventType = "live";
            else if (id == R.id.filter_all) {
                eventType = "completed";
                // Agar user ne sab default kar diya, toh flag reset kardo
                if (publishedAfter == null && currentOrder.equals("relevance") && videoDuration.equals("any")) {
                    isFilterApplied = false;
                }
            }

            // Refresh Data with New Filters
            decideAndFetchData(0);
            return true;
        });
        popup.show();
    }

    private void setTimeFilter(int hoursBack) {
        android.text.format.Time t = new android.text.format.Time();
        t.setToNow();
        t.toMillis(true);
        long now = t.toMillis(true);
        long past = now - (hoursBack * 3600 * 1000L);
        java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
        sdf.setTimeZone(java.util.TimeZone.getTimeZone("UTC"));
        publishedAfter = sdf.format(new java.util.Date(past));
    }

    private void decideAndFetchData(int retryCount) {
        String currentKey = KeyManager.getApiKey(this);
        YouTubeApiService apiService = RetrofitClient.getRetrofitInstance(this).create(YouTubeApiService.class);
        String cacheKey = "view_more_" + (categoryId != null ? categoryId : categoryName) + "_" + currentOrder;
        JsonCacheManager cm = new JsonCacheManager(this);
        com.google.gson.Gson gson = new com.google.gson.Gson();

        Callback<VideoResponse> callback = new Callback<VideoResponse>() {
            @Override
            public void onResponse(Call<VideoResponse> call, Response<VideoResponse> response) {
                if (response.code() == 403 && retryCount < KeyManager.getKeyCount()) {
                    KeyManager.rotateKey(CategoryListActivity.this);
                    decideAndFetchData(retryCount + 1); // RETRY
                    return;
                }
                handleApiResponse(response, cacheKey, cm, gson);
            }
            @Override
            public void onFailure(Call<VideoResponse> call, Throwable t) {
                loadLocalCache(cacheKey, videoList, adapter, gson, cm);
            }
        };

        if (!isFilterApplied && categoryId != null) {
            apiService.getVideos("snippet,statistics", "mostPopular", "IN", categoryId, 50, currentKey).enqueue(callback);
        } else {
            String query = categoryName.equals("🔥 Trending Now") ? "Trending India" : categoryName;
            apiService.searchVideos("snippet", query, currentOrder, publishedAfter, videoDuration, eventType, "video", 50, currentKey).enqueue(callback);
        }
    }

    private void handleApiResponse(Response<VideoResponse> response, String key, JsonCacheManager cm, com.google.gson.Gson gson) {
        if(progressBar != null) progressBar.setVisibility(View.GONE);
        if (response.isSuccessful() && response.body() != null) {
            cm.saveCache(key, gson.toJson(response.body()));
            videoList.addAll(response.body().getItems());
            adapter.notifyDataSetChanged();
        } else {
            loadLocalCache(key, videoList, adapter, gson, cm);
        }
    }
    // Isse Activity class ke andar sabse niche paste kar do
    private void loadLocalCache(String key, List<VideoItem> list, RecyclerView.Adapter adapter, com.google.gson.Gson gson, JsonCacheManager cm) {
        String cachedJson = cm.getCache(key);
        if (cachedJson != null) {
            try {
                VideoResponse cachedData = gson.fromJson(cachedJson, VideoResponse.class);
                if (cachedData != null && cachedData.getItems() != null) {
                    list.clear(); // Purani list saaf karo
                    list.addAll(cachedData.getItems()); // Cache wala data bharo
                    adapter.notifyDataSetChanged(); // UI update karo
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
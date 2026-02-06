package com.sandeshkoli.yttrendy;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.ProgressBar;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.gson.Gson;
import com.sandeshkoli.yttrendy.adapter.VideoAdapter;
import com.sandeshkoli.yttrendy.models.VideoItem;
import com.sandeshkoli.yttrendy.utils.FirebaseDataManager;
import com.sandeshkoli.yttrendy.utils.JsonCacheManager;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class CategoryListActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private VideoAdapter adapter;
    private List<VideoItem> videoList = new ArrayList<>();
    private ProgressBar progressBar;

    private String categoryId, categoryName;
    private TextView btnFilter, titleView;
    private String currentRegion = "IN"; // Default

    private JsonCacheManager cacheManager;
    private Gson gson = new Gson();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_category_list);

        cacheManager = new JsonCacheManager(this);
        currentRegion = getSharedPreferences("PREFS", MODE_PRIVATE).getString("region", "IN");

        categoryId = getIntent().getStringExtra("CAT_ID");
        categoryName = getIntent().getStringExtra("CAT_NAME");

        titleView = findViewById(R.id.toolbar_title);
        progressBar = findViewById(R.id.category_list_progress);
        recyclerView = findViewById(R.id.vertical_recycler_view);
        btnFilter = findViewById(R.id.btn_filter);

        if(titleView != null) titleView.setText(categoryName);
        findViewById(R.id.btn_menu).setOnClickListener(v -> finish()); // Back button

        if(btnFilter != null) btnFilter.setOnClickListener(v -> {
            // Future logic for filters if needed
        });

        setupRecyclerView();
        decideAndFetchData();
    }

    private void setupRecyclerView() {
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new VideoAdapter(this, videoList, true, item -> {
            Intent intent = new Intent(this, VideoPlayerActivity.class);
            intent.putExtra("VIDEO_ID", item.getId());
            intent.putExtra("VIDEO_TITLE", item.getSnippet().getTitle());
            intent.putExtra("VIDEO_DESC", item.getSnippet().getChannelTitle());
            intent.putExtra("VIDEO_THUMB", item.getSnippet().getThumbnails().getHigh().getUrl());
            startActivity(intent);
        });
        recyclerView.setAdapter(adapter);
    }

    private void decideAndFetchData() {
        String query = categoryName;
        String categoryIdParam = (categoryId != null && categoryId.matches("\\d+")) ? categoryId : null;

        // 🔥 Region-specific cache key
        String cacheKey = "view_more_" + (categoryIdParam != null ? categoryIdParam : categoryName.replaceAll("\\s+", "_").toLowerCase()) + "_" + currentRegion;

        // 1. Check Local Cache First
        String localJson = cacheManager.getCache(cacheKey);
        if (localJson != null) {
            android.util.Log.d("DATA_SOURCE_TRACKER", "📱 SOURCE: [Local Mobile Cache] View More: " + categoryName);
            handleFirebaseResponse(gson.fromJson(localJson, Map.class));
            return;
        }

        if (progressBar != null) progressBar.setVisibility(View.VISIBLE);

        // 2. Fetch from Firebase with Region
        FirebaseDataManager.getVideosFromFirebase(query, categoryIdParam, currentRegion)
                .addOnCompleteListener(task -> {
                    if (progressBar != null) progressBar.setVisibility(View.GONE);
                    if (task.isSuccessful()) {
                        Map<String, Object> result = (Map<String, Object>) task.getResult().getData();
                        String source = (String) result.get("source");
                        android.util.Log.d("DATA_SOURCE_TRACKER", "📊 VIEW_MORE SOURCE: [" + source.toUpperCase() + "] Region: " + currentRegion);

                        cacheManager.saveCache(cacheKey, gson.toJson(result));
                        handleFirebaseResponse(result);
                    }
                });
    }

    private void handleFirebaseResponse(Map<String, Object> result) {
        List<Map<String, Object>> items = (List<Map<String, Object>>) result.get("items");
        if (items != null) {
            videoList.clear();
            for (Map<String, Object> itemMap : items) {
                VideoItem item = gson.fromJson(gson.toJson(itemMap), VideoItem.class);
                videoList.add(item);
            }
            adapter.notifyDataSetChanged();
        }
    }
}
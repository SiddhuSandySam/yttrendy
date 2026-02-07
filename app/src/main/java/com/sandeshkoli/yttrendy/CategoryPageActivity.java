package com.sandeshkoli.yttrendy;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
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
import com.sandeshkoli.yttrendy.utils.SubCategoryHelper;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class CategoryPageActivity extends AppCompatActivity {

    private LinearLayout container;
    private String categoryName;
    private String currentRegion = "IN";
    private Gson gson = new Gson();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_category_page);

        currentRegion = getSharedPreferences("PREFS", MODE_PRIVATE).getString("region", "IN");
        categoryName = getIntent().getStringExtra("CAT_NAME");

        TextView title = findViewById(R.id.toolbar_title);
        title.setText(categoryName);
        findViewById(R.id.btn_menu).setOnClickListener(v -> finish()); // Back button

        container = findViewById(R.id.main_content_container);

        // Load Sub-Categories for the selected Chip
        Map<String, String> subCats = SubCategoryHelper.getSubCategories(categoryName);
        for (Map.Entry<String, String> entry : subCats.entrySet()) {
            addSection(entry.getKey(), entry.getValue());
        }
    }

    private void addSection(String title, String searchQuery) {
        View sectionView = LayoutInflater.from(this).inflate(R.layout.item_home_section, container, false);

        TextView titleTv = sectionView.findViewById(R.id.section_title);
        TextView viewMoreTv = sectionView.findViewById(R.id.section_view_more);
        RecyclerView rv = sectionView.findViewById(R.id.section_recycler_view);
        ProgressBar progressBar = sectionView.findViewById(R.id.section_loading);

        titleTv.setText(title);
        rv.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));

        List<VideoItem> sectionList = new ArrayList<>();
        VideoAdapter adapter = new VideoAdapter(this, sectionList, false, item -> {
            Intent intent = new Intent(this, VideoPlayerActivity.class);
            intent.putExtra("VIDEO_ID", item.getId());
            intent.putExtra("VIDEO_TITLE", item.getSnippet().getTitle());
            intent.putExtra("VIDEO_DESC", item.getSnippet().getChannelTitle());
            intent.putExtra("VIDEO_THUMB", item.getSnippet().getThumbnails().getHigh().getUrl());
            startActivity(intent);
        });
        rv.setAdapter(adapter);

        viewMoreTv.setOnClickListener(v -> {
            Intent intent = new Intent(this, CategoryListActivity.class);
            // CAT_ID mein keyword (searchQuery) bhejna zaroori hai
            intent.putExtra("CAT_ID", searchQuery);
            intent.putExtra("CAT_NAME", title);
            startActivity(intent);
        });

        container.addView(sectionView);
        loadSubCategoryData(searchQuery, sectionList, adapter, progressBar);
    }

    private void loadSubCategoryData(String query, List<VideoItem> list, VideoAdapter adapter, ProgressBar loading) {
        // 🔥 Region-specific cache key for Sub-sections
        String cacheKey = "sub_" + query.replaceAll("\\s+", "_").toLowerCase() + "_" + currentRegion;
        JsonCacheManager cm = new JsonCacheManager(this);

        // 1. Check Local Cache
        String localJson = cm.getCache(cacheKey);
        if (localJson != null) {
            android.util.Log.d("DATA_SOURCE_TRACKER", "📱 SOURCE: [Local Cache] Sub-Section: " + query);
            updateChipUI(gson.fromJson(localJson, Map.class), list, adapter, loading);
            return;
        }

        loading.setVisibility(View.VISIBLE);

        // 2. Fetch from Firebase with Region
        FirebaseDataManager.getVideosFromFirebase(query, null, currentRegion)
                .addOnCompleteListener(task -> {
                    loading.setVisibility(View.GONE);
                    if (task.isSuccessful()) {
                        Map<String, Object> result = (Map<String, Object>) task.getResult().getData();
                        String source = (String) result.get("source");
                        android.util.Log.d("DATA_SOURCE_TRACKER", "🧩 SUB_SECTION SOURCE: [" + source.toUpperCase() + "] Query: " + query);

                        cm.saveCache(cacheKey, gson.toJson(result));
                        updateChipUI(result, list, adapter, loading);
                    }
                });
    }

    private void updateChipUI(Map<String, Object> result, List<VideoItem> list, VideoAdapter adapter, ProgressBar loading) {
        List<Map<String, Object>> items = (List<Map<String, Object>>) result.get("items");
        if (items != null) {
            list.clear();
            for (Map<String, Object> itemMap : items) {
                try {
                    // --- SAFE ID EXTRACTION ---
                    Object idObj = itemMap.get("id");
                    String finalId = "";

                    if (idObj instanceof String) {
                        finalId = (String) idObj;
                    } else if (idObj instanceof Map) {
                        Map<String, Object> idMap = (Map<String, Object>) idObj;
                        if (idMap.containsKey("videoId")) {
                            finalId = (String) idMap.get("videoId");
                        }
                    }

                    // ID ko string format mein fix karo
                    itemMap.put("id", finalId);

                    VideoItem item = gson.fromJson(gson.toJson(itemMap), VideoItem.class);
                    list.add(item);
                } catch (Exception e) {
                    android.util.Log.e("JSON_ERROR", "CategoryPage parse error: " + e.getMessage());
                }
            }
            adapter.notifyDataSetChanged();
        }
        if (loading != null) loading.setVisibility(View.GONE);
    }
}
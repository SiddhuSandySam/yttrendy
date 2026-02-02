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

import com.sandeshkoli.yttrendy.adapter.VideoAdapter;
import com.sandeshkoli.yttrendy.models.VideoItem;
import com.sandeshkoli.yttrendy.models.VideoResponse;
import com.sandeshkoli.yttrendy.network.RetrofitClient;
import com.sandeshkoli.yttrendy.network.YouTubeApiService;
import com.sandeshkoli.yttrendy.utils.JsonCacheManager;
import com.sandeshkoli.yttrendy.utils.KeyManager;
import com.sandeshkoli.yttrendy.utils.SubCategoryHelper;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class CategoryPageActivity extends AppCompatActivity {

    private LinearLayout container;
    private String API_KEY;
    private String categoryName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_category_page);

        categoryName = getIntent().getStringExtra("CAT_NAME");

        // Toolbar setup
        TextView title = findViewById(R.id.toolbar_title);
        title.setText(categoryName);
        findViewById(R.id.btn_menu).setOnClickListener(v -> finish()); // Back button

        container = findViewById(R.id.main_content_container);
        API_KEY = KeyManager.getApiKey(this);

        // --- SUB-CATEGORY LOGIC ---
        // Helper se list mango (e.g., Gaming -> Minecraft, GTA...)
        Map<String, String> subCats = SubCategoryHelper.getSubCategories(categoryName);

        for (Map.Entry<String, String> entry : subCats.entrySet()) {
            // Key = Section Title (Minecraft)
            // Value = Search Query (Minecraft gameplay)
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
        // Yahan 'false' bheja kyuki horizontal list me chhota card chahiye
        VideoAdapter adapter = new VideoAdapter(this, sectionList, false, item -> {
            Intent intent = new Intent(this, VideoPlayerActivity.class);
            intent.putExtra("VIDEO_ID", item.getId());
            intent.putExtra("VIDEO_TITLE", item.getSnippet().getTitle());
            intent.putExtra("VIDEO_DESC", item.getSnippet().getChannelTitle());
            startActivity(intent);
        });
        rv.setAdapter(adapter);

        // View More -> Open Vertical List with Filter
        viewMoreTv.setOnClickListener(v -> {
            Intent intent = new Intent(this, CategoryListActivity.class);
            intent.putExtra("CAT_NAME", title); // e.g. Minecraft
            // ID nahi bhej rahe, sirf naam bhej rahe hain search ke liye
            startActivity(intent);
        });

        container.addView(sectionView);

        // Fetch Data using SEARCH API (Sub-categories needs search)
        loadSubCategoryData(searchQuery, sectionList, adapter, progressBar,0);
    }

// CategoryPageActivity.java mein loadSubCategoryData method update karo:

    private void loadSubCategoryData(String query, List<VideoItem> list, VideoAdapter adapter, ProgressBar loading, int retryCount) {
        String currentKey = KeyManager.getApiKey(this);
        String cacheKey = "sub_page_" + query.toLowerCase().replace(" ", "_");
        JsonCacheManager cm = new JsonCacheManager(this);
        com.google.gson.Gson gson = new com.google.gson.Gson();

        YouTubeApiService api = RetrofitClient.getRetrofitInstance(this).create(YouTubeApiService.class);
        api.searchVideos("snippet", query, "relevance", null, "any", "completed", "video", 10, currentKey)
                .enqueue(new Callback<VideoResponse>() {
                    @Override
                    public void onResponse(Call<VideoResponse> call, Response<VideoResponse> response) {
                        if (response.code() == 403 && retryCount < KeyManager.getKeyCount()) {
                            KeyManager.rotateKey(CategoryPageActivity.this);
                            loadSubCategoryData(query, list, adapter, loading, retryCount + 1); // RETRY
                            return;
                        }
                        loading.setVisibility(View.GONE);
                        if (response.isSuccessful() && response.body() != null) {
                            cm.saveCache(cacheKey, gson.toJson(response.body()));
                            list.clear();
                            list.addAll(response.body().getItems());
                            adapter.notifyDataSetChanged();
                        } else {
                            loadLocalCache(cacheKey, list, adapter, gson, cm);
                        }
                    }
                    @Override
                    public void onFailure(Call<VideoResponse> call, Throwable t) {
                        loading.setVisibility(View.GONE);
                        loadLocalCache(cacheKey, list, adapter, gson, cm);
                    }
                });
    }    // Isse Activity class ke andar sabse niche paste kar do
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
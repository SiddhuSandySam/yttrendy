package com.sandeshkoli.yttrendy;

import android.content.Intent;
import android.content.res.ColorStateList; // Use this for setting color to Button Tint
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.facebook.shimmer.ShimmerFrameLayout;
import com.google.android.material.navigation.NavigationView;
import com.google.gson.Gson; // Gson ka import
import com.sandeshkoli.yttrendy.adapter.ShortsAdapter;
import com.sandeshkoli.yttrendy.adapter.VideoAdapter;
import com.sandeshkoli.yttrendy.models.VideoItem;
import com.sandeshkoli.yttrendy.models.VideoResponse;
import com.sandeshkoli.yttrendy.network.RetrofitClient;
import com.sandeshkoli.yttrendy.network.YouTubeApiService;
import com.sandeshkoli.yttrendy.utils.AdManager;
import com.sandeshkoli.yttrendy.utils.CategoryHelper;
import com.sandeshkoli.yttrendy.utils.JsonCacheManager;
import com.sandeshkoli.yttrendy.utils.KeyManager;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainActivity extends AppCompatActivity {

    private DrawerLayout drawerLayout;
    private LinearLayout contentContainer, chipsContainer;
    private ShimmerFrameLayout shimmerContainer;

    private int sectionsLoaded = 0;
    private int totalSections = 0;

    private RecyclerView rvLive;
    private VideoAdapter liveAdapter;
    private List<VideoItem> liveList = new ArrayList<>();
    private LinearLayout liveLayout;

    private RecyclerView rvShorts;
    private ShortsAdapter shortsAdapter;
    private List<VideoItem> shortsList = new ArrayList<>();
    private LinearLayout shortsLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // 1. SABSE PEHLE KEY INITIALIZE KARO
        KeyManager.getApiKey(this); // KeyManager ko context de diya
        AdManager.init(this);

        // 2. PHIR VIEWS INIT KARO
        drawerLayout = findViewById(R.id.drawer_layout);
        contentContainer = findViewById(R.id.main_content_container);
        chipsContainer = findViewById(R.id.chips_container);
        shimmerContainer = findViewById(R.id.shimmer_view_container);
        rvShorts = findViewById(R.id.rv_shorts);
        shortsLayout = findViewById(R.id.shorts_section_layout);
        rvLive = findViewById(R.id.rv_live);
        liveLayout = findViewById(R.id.live_section_layout);

        // 3. AB SHORTS & LIVE SETUP KARO
        setupLiveRecyclerView();
        fetchLiveVideos(0);
        setupShortsRecyclerView();
        fetchShorts(0);

        setupToolbar();
        setupNavigationDrawer();

        // 4. LAST ME CONTENT LOAD KARO
        loadAppContent();
    }

    // ====================== UI SETUP ======================

    private void setupLiveRecyclerView() {
        rvLive.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        liveAdapter = new VideoAdapter(this, liveList, item -> {
            Intent intent = new Intent(this, VideoPlayerActivity.class);
            intent.putExtra("VIDEO_ID", item.getId());
            intent.putExtra("VIDEO_TITLE", item.getSnippet().getTitle());
            intent.putExtra("VIDEO_DESC", item.getSnippet().getChannelTitle());
            intent.putExtra("VIDEO_THUMB", item.getSnippet().getThumbnails().getHigh().getUrl());
            startActivity(intent);
        });
        rvLive.setAdapter(liveAdapter);
    }

    private void setupShortsRecyclerView() {
        rvShorts.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));

        shortsAdapter = new ShortsAdapter(this, shortsList, item -> {
            Intent intent = new Intent(this, VideoPlayerActivity.class);
            intent.putExtra("VIDEO_ID", item.getId());
            intent.putExtra("VIDEO_TITLE", item.getSnippet().getTitle());
            intent.putExtra("VIDEO_DESC", item.getSnippet().getChannelTitle());
            intent.putExtra("VIDEO_THUMB", item.getSnippet().getThumbnails().getHigh().getUrl());
            startActivity(intent);
        });
        rvShorts.setAdapter(shortsAdapter);
    }

    private void setupToolbar() {
        ImageView btnMenu = findViewById(R.id.btn_menu);
        ImageView btnSearch = findViewById(R.id.btn_search);
        TextView btnFilter = findViewById(R.id.btn_filter);

        btnMenu.setOnClickListener(v -> drawerLayout.openDrawer(GravityCompat.START));
        btnSearch.setOnClickListener(v -> startActivity(new Intent(this, SearchActivity.class)));

        // Filter button hidden hai UI me, phir bhi logic dal diya
        if(btnFilter.getVisibility() == View.VISIBLE) {
            btnFilter.setOnClickListener(v -> {
                PopupMenu popup = new PopupMenu(this, btnFilter);
                popup.getMenuInflater().inflate(R.menu.menu_filter, popup.getMenu());
                popup.show();
            });
        }
    }

    private void setupNavigationDrawer() {
        NavigationView navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.nav_saved) {
                startActivity(new Intent(MainActivity.this, SavedVideosActivity.class));
            } else if (id == R.id.nav_share) {
                Intent shareIntent = new Intent(Intent.ACTION_SEND);
                shareIntent.setType("text/plain");
                shareIntent.putExtra(Intent.EXTRA_TEXT, "Download ViralVideo for curated trending content! \n\n https://play.google.com/store/apps/details?id=" + getPackageName());
                startActivity(Intent.createChooser(shareIntent, "Share via"));
            } else if (id == R.id.nav_rate) {
                try {
                    startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + getPackageName())));
                } catch (Exception e) {
                    startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=" + getPackageName())));
                }
            }
            drawerLayout.closeDrawer(GravityCompat.START);
            return true;
        });
    }

    private void addChip(String categoryName) {
        Button btn = new Button(this);
        btn.setText(categoryName);
        btn.setTextColor(Color.WHITE);
        btn.setBackgroundResource(R.drawable.bg_filter_button);

        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT, 100);
        params.setMargins(0, 0, 20, 0);
        btn.setLayoutParams(params);
        btn.setPadding(40, 0, 40, 0);

        btn.setOnClickListener(v -> {
            AdManager.showInterstitial(this, () -> {
                Intent intent = new Intent(this, CategoryPageActivity.class);
                intent.putExtra("CAT_NAME", categoryName);
                startActivity(intent);
            });
        });
        chipsContainer.addView(btn);
    }

    private void addSection(String title, String catIdOrKeyword) {
        View sectionView = LayoutInflater.from(this).inflate(R.layout.item_home_section, contentContainer, false);
        TextView titleTv = sectionView.findViewById(R.id.section_title);
        TextView viewMoreTv = sectionView.findViewById(R.id.section_view_more);
        RecyclerView rv = sectionView.findViewById(R.id.section_recycler_view);
        ProgressBar progressBar = sectionView.findViewById(R.id.section_loading);

        titleTv.setText(title);
        rv.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));

        List<VideoItem> list = new ArrayList<>();

        VideoAdapter adapter = new VideoAdapter(this, list, item -> {
            Intent i = new Intent(this, VideoPlayerActivity.class);
            i.putExtra("VIDEO_ID", item.getId());
            i.putExtra("VIDEO_TITLE", item.getSnippet().getTitle());
            i.putExtra("VIDEO_DESC", item.getSnippet().getChannelTitle());
            i.putExtra("VIDEO_THUMB", item.getSnippet().getThumbnails().getHigh().getUrl());
            startActivity(i);
        });

        rv.setAdapter(adapter);

        viewMoreTv.setOnClickListener(v -> {
            AdManager.showInterstitial(this, () -> {
                Intent i = new Intent(MainActivity.this, CategoryListActivity.class);
                i.putExtra("CAT_ID", catIdOrKeyword); // Pass ID or Keyword
                i.putExtra("CAT_NAME", title);
                startActivity(i);
            });
        });

        contentContainer.addView(sectionView);
        fetchData(catIdOrKeyword, list, adapter, progressBar, 0);
    }


    // ====================== DATA/NETWORK/CACHE LOGIC ======================

    private void loadAppContent() {
        shimmerContainer.startShimmer();
        Map<String, String> categories = CategoryHelper.getCategories();
        totalSections = categories.size();
        sectionsLoaded = 0;

        for (Map.Entry<String, String> entry : categories.entrySet()) {
            addChip(entry.getKey());
            addSection(entry.getKey(), entry.getValue());
        }
    }

    // 🔥 MAIN CORE METHOD (QUOTA SAVER) 🔥
// MainActivity.java ke andar fetchData method ko replace karein:

    private void fetchData(String catIdOrKeyword, List<VideoItem> list, VideoAdapter adapter, ProgressBar bar, int retryCount) {
        String currentKey = KeyManager.getApiKey(this);

        // --- CRASH FIX: Safe Key Generation (for null/keywords) ---
        // Hum sirf null check karenge, replaceAll ko avoid karenge agar null hai
        String safeKeyPart;
        if (catIdOrKeyword == null || catIdOrKeyword.isEmpty() || !catIdOrKeyword.matches("\\d+")) {
            safeKeyPart = "trending_overall";
        } else {
            safeKeyPart = catIdOrKeyword;
        }
        String cacheKey = "main_unique_" + safeKeyPart.replaceAll("\\s+", "_").toLowerCase();
        // --- CRASH FIX END ---

        JsonCacheManager cm = new JsonCacheManager(this);
        Gson gson = new Gson();
        YouTubeApiService api = RetrofitClient.getRetrofitInstance(this).create(YouTubeApiService.class);

        Call<VideoResponse> call;

        // 🔥 MAIN LOGIC: 'Constant.equals(variable)' pattern for safety
        if ("MOST_VIEWED".equals(catIdOrKeyword)) { // Now safe from NullPointerException
            call = api.searchVideos("snippet", "most viewed videos", "viewCount", null, "any", "completed", "video", 10, currentKey);
        }
        else if ("MOST_LIKED".equals(catIdOrKeyword)) { // Now safe from NullPointerException
            call = api.searchVideos("snippet", "top rated videos", "rating", null, "any", "completed", "video", 10, currentKey);
        }
        else if ("SHORTS_TREND".equals(catIdOrKeyword)) { // Now safe from NullPointerException
            call = api.searchVideos("snippet", "trending shorts india", "relevance", null, "short", "completed", "video", 10, currentKey);
        }
        else if (catIdOrKeyword == null || "🔥 Trending Now".equals(catIdOrKeyword)) { // NULL is handled first
            call = api.getVideos("snippet,statistics", "mostPopular", "IN", null, 10, currentKey);
        }
        else if (catIdOrKeyword.matches("\\d+")) { // CHEAP CALL: Standard Category ID (25, 20, etc.)
            call = api.getVideos("snippet,statistics", "mostPopular", "IN", catIdOrKeyword, 10, currentKey);
        }
        else {
            // EXPENSIVE CALL: Travel/Education Keywords (Since it's not null and not a digit ID)
            call = api.searchVideos("snippet", catIdOrKeyword, "relevance", null, "any", "completed", "video", 10, currentKey);
        }

        call.enqueue(new Callback<VideoResponse>() {
            @Override
            public void onResponse(Call<VideoResponse> call, Response<VideoResponse> response) {
                if (response.code() == 403 && retryCount < KeyManager.getKeyCount()) {
                    KeyManager.rotateKey(MainActivity.this);
                    fetchData(catIdOrKeyword, list, adapter, bar, retryCount + 1); // RETRY
                    return;
                }
                bar.setVisibility(View.GONE);
                if (response.isSuccessful() && response.body() != null) {
                    cm.saveCache(cacheKey, gson.toJson(response.body()));
                    list.clear();
                    list.addAll(response.body().getItems());
                    adapter.notifyDataSetChanged();
                } else {
                    loadLocalCache(cacheKey, list, adapter, gson, cm);
                }
                checkAllLoaded();
            }

            @Override
            public void onFailure(Call<VideoResponse> call, Throwable t) {
                bar.setVisibility(View.GONE);
                loadLocalCache(cacheKey, list, adapter, gson, cm);
                checkAllLoaded();
            }
        });
    }
    // --- UTILITY METHODS (Live/Shorts are also here) ---

    // fetchLiveVideos method - Key Rotation added
    private void fetchLiveVideos(int retryCount) {
        String currentKey = KeyManager.getApiKey(this);
        String cacheKey = "main_live_list";
        JsonCacheManager cm = new JsonCacheManager(this);
        Gson gson = new Gson();

        YouTubeApiService api = RetrofitClient.getRetrofitInstance(this).create(YouTubeApiService.class);
        api.searchVideos("snippet", "live news gaming india", "relevance", null, "any", "live", "video", 10, currentKey)
                .enqueue(new Callback<VideoResponse>() {
                    @Override
                    public void onResponse(Call<VideoResponse> call, Response<VideoResponse> response) {
                        if (response.code() == 403 && retryCount < KeyManager.getKeyCount()) {
                            KeyManager.rotateKey(MainActivity.this);
                            fetchLiveVideos(retryCount + 1); // RETRY
                            return;
                        }
                        if (response.isSuccessful() && response.body() != null) {
                            cm.saveCache(cacheKey, gson.toJson(response.body()));
                            liveList.clear();
                            liveList.addAll(response.body().getItems());
                            liveAdapter.notifyDataSetChanged();
                            liveLayout.setVisibility(View.VISIBLE);
                        } else {
                            loadLocalCache(cacheKey, liveList, liveAdapter, gson, cm);
                            if(liveList.size() > 0) liveLayout.setVisibility(View.VISIBLE);
                        }
                    }
                    @Override
                    public void onFailure(Call<VideoResponse> call, Throwable t) {
                        loadLocalCache(cacheKey, liveList, liveAdapter, gson, cm);
                        if(liveList.size() > 0) liveLayout.setVisibility(View.VISIBLE);
                    }
                });
    }

    // fetchShorts method - Key Rotation added
    private void fetchShorts(int retryCount) {
        String currentKey = KeyManager.getApiKey(this);
        String cacheKey = "main_shorts_list";
        JsonCacheManager cm = new JsonCacheManager(this);
        Gson gson = new Gson();

        YouTubeApiService api = RetrofitClient.getRetrofitInstance(this).create(YouTubeApiService.class);
        api.searchVideos("snippet", "trending shorts india", "relevance", null, "short", "completed", "video", 15, currentKey)
                .enqueue(new Callback<VideoResponse>() {
                    @Override
                    public void onResponse(Call<VideoResponse> call, Response<VideoResponse> response) {
                        if (response.code() == 403 && retryCount < KeyManager.getKeyCount()) {
                            KeyManager.rotateKey(MainActivity.this);
                            fetchShorts(retryCount + 1); // RETRY
                            return;
                        }
                        if (response.isSuccessful() && response.body() != null) {
                            cm.saveCache(cacheKey, gson.toJson(response.body()));
                            shortsList.clear();
                            shortsList.addAll(response.body().getItems());
                            shortsAdapter.notifyDataSetChanged();
                            shortsLayout.setVisibility(View.VISIBLE);
                        } else {
                            loadLocalCache(cacheKey, shortsList, shortsAdapter, gson, cm);
                            if(shortsList.size() > 0) shortsLayout.setVisibility(View.VISIBLE);
                        }
                    }
                    @Override
                    public void onFailure(Call<VideoResponse> call, Throwable t) {
                        loadLocalCache(cacheKey, shortsList, shortsAdapter, gson, cm);
                        if(shortsList.size() > 0) shortsLayout.setVisibility(View.VISIBLE);
                    }
                });
    }

    // Helper Method to read cache (This was the missing piece)
    private void loadLocalCache(String key, List<VideoItem> list, RecyclerView.Adapter adapter, Gson gson, JsonCacheManager cm) {
        String cachedJson = cm.getCache(key);
        if (cachedJson != null) {
            try {
                VideoResponse cachedData = gson.fromJson(cachedJson, VideoResponse.class);
                if (cachedData != null && cachedData.getItems() != null) {
                    list.clear();
                    list.addAll(cachedData.getItems());
                    adapter.notifyDataSetChanged();
                }
            } catch (Exception e) {
                android.util.Log.e("CacheError", "Could not load cache for key: " + key + e.getMessage());
            }
        }
    }

    private void checkAllLoaded() {
        sectionsLoaded++;
        if (sectionsLoaded >= totalSections) {
            shimmerContainer.stopShimmer();
            shimmerContainer.setVisibility(View.GONE);
            contentContainer.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onBackPressed() {
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }
}
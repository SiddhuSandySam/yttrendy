package com.sandeshkoli.yttrendy;

import android.content.Intent;
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

import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkManager;

import com.facebook.shimmer.ShimmerFrameLayout;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.gson.Gson;
import com.sandeshkoli.yttrendy.adapter.ShortsAdapter;
import com.sandeshkoli.yttrendy.adapter.VideoAdapter;
import com.sandeshkoli.yttrendy.models.VideoItem;
import com.sandeshkoli.yttrendy.utils.AdManager;
import com.sandeshkoli.yttrendy.utils.CategoryHelper;
import com.sandeshkoli.yttrendy.utils.FirebaseDataManager;
import com.sandeshkoli.yttrendy.utils.JsonCacheManager;
import com.sandeshkoli.yttrendy.utils.NotificationWorker;
import com.sandeshkoli.yttrendy.utils.RegionHelper;
import com.sandeshkoli.yttrendy.utils.SearchHistoryHelper;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

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

    // Region & Cache Variables
    private String currentRegion = "IN";
    private TextView btnRegion;
    private JsonCacheManager cacheManager;
    private Gson gson = new Gson();

    private androidx.swiperefreshlayout.widget.SwipeRefreshLayout swipeRefreshLayout;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        FirebaseApp.initializeApp(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        cacheManager = new JsonCacheManager(this);

        // 1. REGION SETUP (Saved preference uthao)
        currentRegion = getSharedPreferences("PREFS", MODE_PRIVATE).getString("region", "IN");

        // 2. VIEWS INIT
        drawerLayout = findViewById(R.id.drawer_layout);
        contentContainer = findViewById(R.id.main_content_container);
        chipsContainer = findViewById(R.id.chips_container);
        shimmerContainer = findViewById(R.id.shimmer_view_container);
        rvShorts = findViewById(R.id.rv_shorts);
        shortsLayout = findViewById(R.id.shorts_section_layout);
        rvLive = findViewById(R.id.rv_live);
        liveLayout = findViewById(R.id.live_section_layout);
        btnRegion = findViewById(R.id.btn_region_select);
        swipeRefreshLayout = findViewById(R.id.swipe_refresh);


        AdManager.init(this);
        updateRegionUI(); // Toolbar par sahi flag dikhao

        // 3. ANONYMOUS LOGIN (Backend Security bypass ke liye)
        FirebaseAuth.getInstance().signInAnonymously()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        android.util.Log.d("AUTH", "Firebase Auth Success");
                        loadAppFlow();
                    } else {
                        android.util.Log.e("AUTH", "Auth Failed: " + task.getException().getMessage());
                        loadAppFlow(); // Fail hone par bhi try karte hain
                    }
                });

        // 4. BACK PRESS HANDLER
        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
                    drawerLayout.closeDrawer(GravityCompat.START);
                } else {
                    setEnabled(false);
                    getOnBackPressedDispatcher().onBackPressed();
                }
            }
        });

        // Region Button Click
        if (btnRegion != null) {
            btnRegion.setOnClickListener(v -> showRegionSelectionMenu());
        }

        // 5. NOTIFICATION WORKER
        PeriodicWorkRequest request = new PeriodicWorkRequest.Builder(NotificationWorker.class, 24, TimeUnit.HOURS).build();
        WorkManager.getInstance(this).enqueue(request);

        swipeRefreshLayout.setOnRefreshListener(() -> {
            // 1. Mobile Cache Clear
            cacheManager.clearAll();

            // 2. Sections count reset
            sectionsLoaded = 0;

            // 3. UI reset (Shimmer dikhao, content chupao)
            contentContainer.setVisibility(View.GONE);
            shimmerContainer.setVisibility(View.VISIBLE);
            shimmerContainer.startShimmer();

            // 4. Dobara loading shuru
            loadAppFlow();
        });
    }

    private void loadAppFlow() {
        setupLiveRecyclerView();
        fetchLiveVideos();
        // setupRecommendations(); <--- IS LINE KO DELETE KARO (Double loading rokne ke liye)

        setupShortsRecyclerView();
        fetchShorts();

        setupToolbar();
        setupNavigationDrawer();
        loadAppContent(); // Recommendations yahan se load hongi
    }
    private void setupRecommendations() {
        List<String> history = com.sandeshkoli.yttrendy.utils.SearchHistoryHelper.getHistory(this);

        if (history == null || history.isEmpty()) return;

        // Sirf top 2 results uthao (Quota balance karne ke liye)
        int limit = Math.min(history.size(), 2);

        for (int i = 0; i < limit; i++) {
            String keyword = history.get(i);
            String sectionTitle = "✨ More like '" + keyword + "'";

            // Is keyword ke liye section add karo
            addSection(sectionTitle, keyword);
        }
    }

    // ====================== REGION SELECTION LOGIC ======================

    private void showRegionSelectionMenu() {
        PopupMenu popup = new PopupMenu(this, btnRegion);
        Map<String, String> regions = RegionHelper.getAvailableRegions();
        for (String name : regions.keySet()) {
            popup.getMenu().add(name);
        }

        popup.setOnMenuItemClickListener(item -> {
            String selectedName = item.getTitle().toString();
            String selectedCode = regions.get(selectedName);

            // Choice save karo
            getSharedPreferences("PREFS", MODE_PRIVATE).edit().putString("region", selectedCode).apply();
            currentRegion = selectedCode;

            // Activity Refresh taaki naye region ka data load ho
            recreate();
            return true;
        });
        popup.show();
    }

    private void updateRegionUI() {
        if (btnRegion == null) return;
        Map<String, String> regions = RegionHelper.getAvailableRegions();
        for (Map.Entry<String, String> entry : regions.entrySet()) {
            if (entry.getValue().equals(currentRegion)) {
                // Example: "🇮🇳 IN"
                btnRegion.setText(entry.getKey().split(" ")[0] + " " + currentRegion);
                break;
            }
        }
    }

    // ====================== DATA FETCH LOGIC (FIREBASE + REGION) ======================

    private void fetchData(String catIdOrKeyword, List<VideoItem> list, VideoAdapter adapter, ProgressBar bar, int retryCount) {
        String query = (catIdOrKeyword == null) ? "trending now" : catIdOrKeyword;
        String categoryId = (catIdOrKeyword != null && catIdOrKeyword.matches("\\d+")) ? catIdOrKeyword : null;

        // 🔥 SMART QUOTA SAVER LOGIC
        String optimizedKey = com.sandeshkoli.yttrendy.utils.QueryOptimizer.getCleanedKey(query);
        String cacheKey = (categoryId != null) ? "cat_" + categoryId + "_" + currentRegion :
                "search_" + optimizedKey + "_" + currentRegion;

        // Log check karne ke liye:
        android.util.Log.d("QUOTA_SAVER", "Original Query: [" + query + "] -> Optimized Key: [" + cacheKey + "]");

        // 1. Check Local Cache
        String localJson = cacheManager.getCache(cacheKey);
        if (localJson != null) {
            android.util.Log.d("DATA_SOURCE_TRACKER", "📱 SOURCE: [Local Cache] | Key: " + cacheKey);
            updateUIList(gson.fromJson(localJson, Map.class), list, adapter, bar, null);
            checkAllLoaded();
            return;
        }

        // 2. Fetch from Firebase
        FirebaseDataManager.getVideosFromFirebase(query, categoryId, currentRegion)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Map<String, Object> result = (Map<String, Object>) task.getResult().getData();
                        String source = (String) result.get("source");
                        android.util.Log.d("DATA_SOURCE_TRACKER", "🌐 SOURCE: [" + source.toUpperCase() + "] | Key: " + cacheKey);

                        cacheManager.saveCache(cacheKey, gson.toJson(result));
                        updateUIList(result, list, adapter, bar, null);
                        checkAllLoaded();
                    } else {
                        if (retryCount < 3) {
                            int delay = (retryCount + 1) * 1000;
                            new android.os.Handler().postDelayed(() ->
                                    fetchData(catIdOrKeyword, list, adapter, bar, retryCount + 1), delay);
                        } else {
                            if (bar != null) bar.setVisibility(View.GONE);
                            checkAllLoaded();
                        }
                    }
                });
    }

        private void fetchLiveVideos() {
        String cacheKey = "main_live_list_" + currentRegion;
        String query = "live news gaming";

        String localJson = cacheManager.getCache(cacheKey);
        if (localJson != null) {
            android.util.Log.d("DATA_SOURCE_TRACKER", "📱 SOURCE: [Local Cache] Live List");
            updateUIList(gson.fromJson(localJson, Map.class), liveList, liveAdapter, null, liveLayout);
            return;
        }

        FirebaseDataManager.getVideosFromFirebase(query, null, currentRegion)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Map<String, Object> result = (Map<String, Object>) task.getResult().getData();
                        cacheManager.saveCache(cacheKey, gson.toJson(result));
                        updateUIList(result, liveList, liveAdapter, null, liveLayout);
                    }
                });
    }

    private void fetchShorts() {
        String cacheKey = "main_shorts_list_" + currentRegion;
        String query = "trending shorts";

        String localJson = cacheManager.getCache(cacheKey);
        if (localJson != null) {
            android.util.Log.d("DATA_SOURCE_TRACKER", "📱 SOURCE: [Local Cache] Shorts List");
            updateShortsUI(gson.fromJson(localJson, Map.class));
            return;
        }

        FirebaseDataManager.getVideosFromFirebase(query, null, currentRegion)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Map<String, Object> result = (Map<String, Object>) task.getResult().getData();
                        cacheManager.saveCache(cacheKey, gson.toJson(result));
                        updateShortsUI(result);
                    }
                });
    }

    private void updateUIList(Map<String, Object> result, List<VideoItem> list, RecyclerView.Adapter adapter, ProgressBar bar, View layoutToShow) {
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

                    // Map mein wapas string ID daal do taaki VideoItem crash na kare
                    itemMap.put("id", finalId);

                    VideoItem item = gson.fromJson(gson.toJson(itemMap), VideoItem.class);
                    list.add(item);
                } catch (Exception e) {
                    android.util.Log.e("JSON_ERROR", "Error parsing item: " + e.getMessage());
                }
            }
            adapter.notifyDataSetChanged();
            if (layoutToShow != null) layoutToShow.setVisibility(View.VISIBLE);
        }
        if (bar != null) bar.setVisibility(View.GONE);
    }

    private void updateShortsUI(Map<String, Object> result) {
        List<Map<String, Object>> items = (List<Map<String, Object>>) result.get("items");
        if (items != null) {
            shortsList.clear();
            for (Map<String, Object> itemMap : items) {
                VideoItem item = gson.fromJson(gson.toJson(itemMap), VideoItem.class);
                shortsList.add(item);
            }
            shortsAdapter.notifyDataSetChanged();
            shortsLayout.setVisibility(View.VISIBLE);
        }
    }

    // ====================== UI SETUP METHODS ======================

    private void loadAppContent() {
        contentContainer.removeAllViews();
        chipsContainer.removeAllViews();

        setupRecommendations();

        Map<String, String> categories = com.sandeshkoli.yttrendy.utils.CategoryHelper.getCategories();
        List<String> history = com.sandeshkoli.yttrendy.utils.SearchHistoryHelper.getHistory(this);

        // Total = Categories + Recommended (max 2)
        int recCount = Math.min(history.size(), 2);
        totalSections = categories.size() + recCount;

        sectionsLoaded = 0;

        for (Map.Entry<String, String> entry : categories.entrySet()) {
            addChip(entry.getKey());
            addSection(entry.getKey(), entry.getValue());
        }
    }


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
        findViewById(R.id.btn_menu).setOnClickListener(v -> drawerLayout.openDrawer(GravityCompat.START));
        findViewById(R.id.btn_search).setOnClickListener(v -> startActivity(new Intent(this, SearchActivity.class)));
    }

    private void setupNavigationDrawer() {
        NavigationView navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.nav_saved) {
                startActivity(new Intent(this, SavedVideosActivity.class));
            } else if (id == R.id.nav_share) {
                Intent shareIntent = new Intent(Intent.ACTION_SEND);
                shareIntent.setType("text/plain");
                shareIntent.putExtra(Intent.EXTRA_TEXT, "Check out ViralStream for latest trends! https://play.google.com/store/apps/details?id=" + getPackageName());
                startActivity(Intent.createChooser(shareIntent, "Share via"));
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
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, 100);
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
                Intent i = new Intent(this, CategoryListActivity.class);
                i.putExtra("CAT_ID", catIdOrKeyword);
                i.putExtra("CAT_NAME", title);
                startActivity(i);
            });
        });

        contentContainer.addView(sectionView);
        fetchData(catIdOrKeyword, list, adapter, progressBar, 0);
    }

    private void checkAllLoaded() {
        sectionsLoaded++;
        // Jab total sections + Live + Shorts sab load ho jayein (isliye +2)
        if (sectionsLoaded >= (totalSections)) {
            if (shimmerContainer != null) {
                shimmerContainer.stopShimmer();
                shimmerContainer.setVisibility(View.GONE);
            }
            contentContainer.setVisibility(View.VISIBLE);

            // Spinner ko band karo
            if (swipeRefreshLayout != null && swipeRefreshLayout.isRefreshing()) {
                swipeRefreshLayout.setRefreshing(false);
            }
        }
    }
}
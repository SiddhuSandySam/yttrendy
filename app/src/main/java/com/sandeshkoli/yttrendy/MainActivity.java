package com.sandeshkoli.yttrendy;

import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
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

    // Region & Cache Variables
    private String currentRegion = "IN";
    private TextView btnRegion;
    private JsonCacheManager cacheManager;
    private Gson gson = new Gson();

    private androidx.swiperefreshlayout.widget.SwipeRefreshLayout swipeRefreshLayout;

    private final ActivityResultLauncher<String> requestPermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
                if (!isGranted) {
                    Toast.makeText(this, "Notifications are disabled.", Toast.LENGTH_SHORT).show();
                }
            });


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        FirebaseApp.initializeApp(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        cacheManager = new JsonCacheManager(this);
        currentRegion = getSharedPreferences("PREFS", MODE_PRIVATE).getString("region", "IN");

        // VIEWS INIT
        drawerLayout = findViewById(R.id.drawer_layout);
        contentContainer = findViewById(R.id.main_content_container);
        chipsContainer = findViewById(R.id.chips_container);
        shimmerContainer = findViewById(R.id.shimmer_view_container);
        btnRegion = findViewById(R.id.btn_region_select);
        swipeRefreshLayout = findViewById(R.id.swipe_refresh);

        // --- PURANE rvShorts aur rvLive wale findViewById yahan se hata diye gaye hain ---

        AdManager.init(this);
        updateRegionUI();

        FirebaseAuth.getInstance().signInAnonymously()
                .addOnCompleteListener(task -> loadAppFlow());

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

        if (btnRegion != null) {
            btnRegion.setOnClickListener(v -> showRegionSelectionMenu());
        }

        PeriodicWorkRequest request = new PeriodicWorkRequest.Builder(NotificationWorker.class, 24, TimeUnit.HOURS).build();
        WorkManager.getInstance(this).enqueue(request);

        swipeRefreshLayout.setOnRefreshListener(() -> {
            cacheManager.clearAll();
            sectionsLoaded = 0;
            contentContainer.setVisibility(View.GONE);
            shimmerContainer.setVisibility(View.VISIBLE);
            shimmerContainer.startShimmer();
            loadAppFlow();
        });

        checkNotificationPermission();
    }

    private void checkNotificationPermission() {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
            if (androidx.core.content.ContextCompat.checkSelfPermission(this, android.Manifest.permission.POST_NOTIFICATIONS) !=
                    android.content.pm.PackageManager.PERMISSION_GRANTED) {
                requestPermissionLauncher.launch(android.Manifest.permission.POST_NOTIFICATIONS);
            }
        }
    }

    private void loadAppFlow() {
        setupToolbar();
        setupNavigationDrawer();
        loadAppContent();
    }

    private void loadAppContent() {
        if (shimmerContainer != null) shimmerContainer.startShimmer();

        contentContainer.removeAllViews();
        chipsContainer.removeAllViews();

        // 1. SABSE UPAR: Recommended Sections
        setupRecommendations();

        // 2. USKE NICHE: Shorts Section
        addShortsSectionView();

        // 3. USKE NICHE: Live Section
        addLiveSectionView();

        // 4. USKE NICHE: Categories
        Map<String, String> categories = CategoryHelper.getCategories();
        List<String> history = SearchHistoryHelper.getHistory(this);
        int recCount = Math.min(history.size(), 2);

        // Total count for shimmer logic
        totalSections = categories.size() + recCount + 2;
        sectionsLoaded = 0;

        for (Map.Entry<String, String> entry : categories.entrySet()) {
            addChip(entry.getKey());
            addSection(entry.getKey(), entry.getValue());
        }
    }

    private void setupRecommendations() {
        List<String> history = SearchHistoryHelper.getHistory(this);
        if (history == null || history.isEmpty()) return;

        int limit = Math.min(history.size(), 2);
        for (int i = 0; i < limit; i++) {
            String keyword = history.get(i);
            addSection("✨ Recommended: " + keyword, keyword);
        }
    }

    private void addShortsSectionView() {
        View sectionView = LayoutInflater.from(this).inflate(R.layout.item_home_section, contentContainer, false);
        TextView titleTv = sectionView.findViewById(R.id.section_title);
        sectionView.findViewById(R.id.section_view_more).setVisibility(View.GONE);
        ProgressBar bar = sectionView.findViewById(R.id.section_loading);

        titleTv.setText("⚡ Shorts");
        RecyclerView rv = sectionView.findViewById(R.id.section_recycler_view);
        rv.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));

        List<VideoItem> sList = new ArrayList<>();
        ShortsAdapter sAdapter = new ShortsAdapter(this, sList, item -> {
            Intent i = new Intent(this, VideoPlayerActivity.class);
            i.putExtra("VIDEO_ID", item.getId());
            i.putExtra("VIDEO_TITLE", item.getSnippet().getTitle());
            startActivity(i);
        });
        rv.setAdapter(sAdapter);
        contentContainer.addView(sectionView);

        fetchShortsData(sList, sAdapter, bar);
    }

    private void addLiveSectionView() {
        View sectionView = LayoutInflater.from(this).inflate(R.layout.item_home_section, contentContainer, false);
        TextView titleTv = sectionView.findViewById(R.id.section_title);
        sectionView.findViewById(R.id.section_view_more).setVisibility(View.GONE);
        ProgressBar bar = sectionView.findViewById(R.id.section_loading);

        titleTv.setText("🔴 Live Now");
        titleTv.setTextColor(Color.RED);
        RecyclerView rv = sectionView.findViewById(R.id.section_recycler_view);
        rv.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));

        List<VideoItem> lList = new ArrayList<>();
        VideoAdapter lAdapter = new VideoAdapter(this, lList, item -> {
            Intent i = new Intent(this, VideoPlayerActivity.class);
            i.putExtra("VIDEO_ID", item.getId());
            startActivity(i);
        });
        rv.setAdapter(lAdapter);
        contentContainer.addView(sectionView);

        fetchLiveData(lList, lAdapter, bar);
    }

    private void fetchShortsData(List<VideoItem> list, RecyclerView.Adapter adapter, ProgressBar bar) {
        FirebaseDataManager.getVideosFromFirebase("trending shorts", null, currentRegion)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Map<String, Object> result = (Map<String, Object>) task.getResult().getData();
                        List<Map<String, Object>> items = (List<Map<String, Object>>) result.get("items");
                        if (items != null) {
                            list.clear();
                            for (Map<String, Object> m : items) {
                                list.add(gson.fromJson(gson.toJson(m), VideoItem.class));
                            }
                            java.util.Collections.shuffle(list);
                            adapter.notifyDataSetChanged();
                        }
                    }
                    if (bar != null) bar.setVisibility(View.GONE);
                    checkAllLoaded();
                });
    }

    private void fetchLiveData(List<VideoItem> list, RecyclerView.Adapter adapter, ProgressBar bar) {
        FirebaseDataManager.getVideosFromFirebase("live news gaming", null, currentRegion)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Map<String, Object> result = (Map<String, Object>) task.getResult().getData();
                        List<Map<String, Object>> items = (List<Map<String, Object>>) result.get("items");
                        if (items != null) {
                            list.clear();
                            for (Map<String, Object> m : items) {
                                list.add(gson.fromJson(gson.toJson(m), VideoItem.class));
                            }
                            java.util.Collections.shuffle(list);
                            adapter.notifyDataSetChanged();
                        }
                    }
                    if (bar != null) bar.setVisibility(View.GONE);
                    checkAllLoaded();
                });
    }

    private void fetchData(String catIdOrKeyword, List<VideoItem> list, VideoAdapter adapter, ProgressBar bar, int retryCount) {
        String query = (catIdOrKeyword == null) ? "trending now" : catIdOrKeyword;
        String categoryId = (catIdOrKeyword != null && catIdOrKeyword.matches("\\d+")) ? catIdOrKeyword : null;

        String optimizedKey = com.sandeshkoli.yttrendy.utils.QueryOptimizer.getCleanedKey(query);
        String cacheKey = (categoryId != null) ? "cat_" + categoryId + "_" + currentRegion :
                "search_" + optimizedKey + "_" + currentRegion;

        String localJson = cacheManager.getCache(cacheKey);
        if (localJson != null) {
            updateUIListFromMap(gson.fromJson(localJson, Map.class), list, adapter, bar);
            checkAllLoaded();
            return;
        }

        FirebaseDataManager.getVideosFromFirebase(query, categoryId, currentRegion)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Map<String, Object> result = (Map<String, Object>) task.getResult().getData();
                        cacheManager.saveCache(cacheKey, gson.toJson(result));
                        updateUIListFromMap(result, list, adapter, bar);
                        checkAllLoaded();
                    } else {
                        if (retryCount < 3) {
                            new android.os.Handler().postDelayed(() -> fetchData(catIdOrKeyword, list, adapter, bar, retryCount + 1), 1000);
                        } else {
                            if (bar != null) bar.setVisibility(View.GONE);
                            checkAllLoaded();
                        }
                    }
                });
    }

    private void updateUIListFromMap(Map<String, Object> result, List<VideoItem> list, RecyclerView.Adapter adapter, ProgressBar bar) {
        List<Map<String, Object>> items = (List<Map<String, Object>>) result.get("items");
        if (items != null) {
            list.clear();
            for (Map<String, Object> itemMap : items) {
                try {
                    Object idObj = itemMap.get("id");
                    String finalId = (idObj instanceof String) ? (String) idObj : "";
                    itemMap.put("id", finalId);
                    list.add(gson.fromJson(gson.toJson(itemMap), VideoItem.class));
                } catch (Exception e) {}
            }
            java.util.Collections.shuffle(list);
            adapter.notifyDataSetChanged();
        }
        if (bar != null) bar.setVisibility(View.GONE);
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
        if (sectionsLoaded >= totalSections) {
            if (shimmerContainer != null) {
                shimmerContainer.stopShimmer();
                shimmerContainer.setVisibility(View.GONE);
            }
            contentContainer.setVisibility(View.VISIBLE);
            if (swipeRefreshLayout != null) swipeRefreshLayout.setRefreshing(false);
        }
    }

    private void setupToolbar() {
        findViewById(R.id.btn_menu).setOnClickListener(v -> drawerLayout.openDrawer(GravityCompat.START));
        findViewById(R.id.btn_search).setOnClickListener(v -> startActivity(new Intent(this, SearchActivity.class)));
    }

    private void setupNavigationDrawer() {
        NavigationView navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.nav_saved) startActivity(new Intent(this, SavedVideosActivity.class));
            else if (id == R.id.nav_history) startActivity(new Intent(this, SearchActivity.class));
            else if (id == R.id.nav_rate) rateApp();
            else if (id == R.id.nav_share) {
                Intent s = new Intent(Intent.ACTION_SEND);
                s.setType("text/plain");
                s.putExtra(Intent.EXTRA_TEXT, "ViralStream: https://play.google.com/store/apps/details?id=" + getPackageName());
                startActivity(Intent.createChooser(s, "Share"));
            }
            drawerLayout.closeDrawer(GravityCompat.START);
            return true;
        });
    }

    private void rateApp() {
        try {
            startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + getPackageName())));
        } catch (Exception e) {
            startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=" + getPackageName())));
        }
    }

    private void showRegionSelectionMenu() {
        PopupMenu popup = new PopupMenu(this, btnRegion);
        Map<String, String> regions = RegionHelper.getAvailableRegions();
        for (String name : regions.keySet()) popup.getMenu().add(name);
        popup.setOnMenuItemClickListener(item -> {
            String code = regions.get(item.getTitle().toString());
            getSharedPreferences("PREFS", MODE_PRIVATE).edit().putString("region", code).apply();
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
                btnRegion.setText(entry.getKey().split(" ")[0] + " " + currentRegion);
                break;
            }
        }
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
        btn.setOnClickListener(v -> AdManager.showInterstitial(this, () -> {
            Intent i = new Intent(this, CategoryPageActivity.class);
            i.putExtra("CAT_NAME", categoryName);
            startActivity(i);
        }));
        chipsContainer.addView(btn);
    }
}
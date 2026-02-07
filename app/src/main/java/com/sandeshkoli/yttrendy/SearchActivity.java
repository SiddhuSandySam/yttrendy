package com.sandeshkoli.yttrendy;

import android.content.Intent;
import android.os.Bundle;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.gson.Gson;
import com.sandeshkoli.yttrendy.adapter.HistoryAdapter;
import com.sandeshkoli.yttrendy.adapter.VideoAdapter;
import com.sandeshkoli.yttrendy.models.VideoItem;
import com.sandeshkoli.yttrendy.utils.FirebaseDataManager;
import com.sandeshkoli.yttrendy.utils.JsonCacheManager;
import com.sandeshkoli.yttrendy.utils.SearchHistoryHelper;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class SearchActivity extends AppCompatActivity {

    private EditText etSearch;
    private RecyclerView recyclerView;
    private RecyclerView historyRv;
    private VideoAdapter adapter;
    private HistoryAdapter historyAdapter;

    private List<VideoItem> videoList = new ArrayList<>();
    private ProgressBar progressBar;
    private TextView btnFilter;

    private String currentQuery = "";
    private String currentRegion = "IN";
    private JsonCacheManager cacheManager;
    private Gson gson = new Gson();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);

        cacheManager = new JsonCacheManager(this);
        currentRegion = getSharedPreferences("PREFS", MODE_PRIVATE).getString("region", "IN");

        etSearch = findViewById(R.id.et_search);
        ImageView btnBack = findViewById(R.id.btn_back);
        ImageView btnPerformSearch = findViewById(R.id.btn_perform_search);
        btnFilter = findViewById(R.id.btn_search_filter);
        progressBar = findViewById(R.id.search_loading);

        recyclerView = findViewById(R.id.search_recycler_view);
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

        historyRv = findViewById(R.id.history_recycler_view);
        historyRv.setLayoutManager(new LinearLayoutManager(this));

        btnBack.setOnClickListener(v -> finish());
        btnPerformSearch.setOnClickListener(v -> performSearch());

        etSearch.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                performSearch();
                return true;
            }
            return false;
        });

        etSearch.setOnClickListener(v -> showHistory());
        etSearch.setOnFocusChangeListener((v, hasFocus) -> { if (hasFocus) showHistory(); });
        etSearch.requestFocus();

        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                if (historyRv.getVisibility() == View.VISIBLE) historyRv.setVisibility(View.GONE);
                else finish();
            }
        });
    }

    private void performSearch() {
        String query = etSearch.getText().toString().trim();

        if (query.isEmpty()) {
            etSearch.setError("Please enter something");
            return;
        }

        // 🔥 ZAROORI FIX: Search keyword ko history mein save karo
        com.sandeshkoli.yttrendy.utils.SearchHistoryHelper.saveSearch(this, query);

        // Keyboard close logic...
        View view = this.getCurrentFocus();
        if (view != null) {
            InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }

        etSearch.clearFocus();
        historyRv.setVisibility(View.GONE);
        currentQuery = query;

        fetchSearchResults();
    }
    private void fetchSearchResults() {
        historyRv.setVisibility(View.GONE);

        // 🔥 SMART QUOTA SAVER LOGIC
        String optimizedKey = com.sandeshkoli.yttrendy.utils.QueryOptimizer.getCleanedKey(currentQuery);
        String cacheKey = "search_" + optimizedKey + "_" + currentRegion;

        android.util.Log.d("QUOTA_SAVER", "User Searched: [" + currentQuery + "] -> Cache Key: [" + cacheKey + "]");

        String localJson = cacheManager.getCache(cacheKey);
        if (localJson != null) {
            android.util.Log.d("DATA_SOURCE_TRACKER", "📱 SOURCE: [Local Cache] Search: " + optimizedKey);
            updateSearchUI(gson.fromJson(localJson, Map.class));
            return;
        }

        progressBar.setVisibility(View.VISIBLE);
        FirebaseDataManager.getVideosFromFirebase(currentQuery, null, currentRegion)
                .addOnCompleteListener(task -> {
                    progressBar.setVisibility(View.GONE);
                    if (task.isSuccessful()) {
                        Map<String, Object> result = (Map<String, Object>) task.getResult().getData();
                        String source = (String) result.get("source");
                        android.util.Log.d("DATA_SOURCE_TRACKER", "🌐 SOURCE: [" + source.toUpperCase() + "] Search Key: " + cacheKey);

                        cacheManager.saveCache(cacheKey, gson.toJson(result));
                        updateSearchUI(result);
                    }
                });
    }
    private void updateSearchUI(Map<String, Object> result) {
        List<Map<String, Object>> items = (List<Map<String, Object>>) result.get("items");
        if (items != null) {
            videoList.clear();
            for (Map<String, Object> itemMap : items) {
                try {
                    Object idObj = itemMap.get("id");
                    if (idObj instanceof Map) {
                        Map<String, Object> idMap = (Map<String, Object>) idObj;
                        itemMap.put("id", idMap.get("videoId"));
                    }
                    videoList.add(gson.fromJson(gson.toJson(itemMap), VideoItem.class));
                } catch (Exception e) { }
            }
            adapter.notifyDataSetChanged();
        }
    }

    private void showHistory() {
        List<String> list = SearchHistoryHelper.getHistory(this);
        if (!list.isEmpty()) {
            historyAdapter = new HistoryAdapter(list, new HistoryAdapter.OnHistoryClickListener() {
                @Override
                public void onHistoryClick(String query) {
                    etSearch.setText(query);
                    etSearch.setSelection(query.length());
                    performSearch();
                }

                @Override
                public void onDeleteClick(String query) {
                    // Delete single item
                    SearchHistoryHelper.removeSearch(SearchActivity.this, query);
                    showHistory(); // Refresh list
                }
            });
            historyRv.setAdapter(historyAdapter);
            historyRv.setVisibility(View.VISIBLE);
        } else {
            historyRv.setVisibility(View.GONE);
        }
    }

    private void showFilterMenu() { /* Existing Filter Logic */ }
}
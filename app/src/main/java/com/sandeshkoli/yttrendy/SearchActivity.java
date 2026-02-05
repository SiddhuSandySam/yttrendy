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

import androidx.activity.OnBackPressedCallback; // <--- IMPORT ZAROORI HAI
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.sandeshkoli.yttrendy.adapter.HistoryAdapter;
import com.sandeshkoli.yttrendy.adapter.VideoAdapter;
import com.sandeshkoli.yttrendy.models.VideoItem;
import com.sandeshkoli.yttrendy.models.VideoResponse;
import com.sandeshkoli.yttrendy.network.RetrofitClient;
import com.sandeshkoli.yttrendy.network.YouTubeApiService;
import com.sandeshkoli.yttrendy.utils.KeyManager;
import com.sandeshkoli.yttrendy.utils.SearchHistoryHelper;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class SearchActivity extends AppCompatActivity {

    private EditText etSearch;
    private RecyclerView recyclerView; // Results
    private RecyclerView historyRv;    // History
    private VideoAdapter adapter;
    private HistoryAdapter historyAdapter;

    private List<VideoItem> videoList = new ArrayList<>();
    private ProgressBar progressBar;
    private TextView btnFilter;

    // Filter Variables
    private String currentQuery = "";
    private String currentOrder = "relevance";
    private String publishedAfter = null;
    private String videoDuration = "any";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);

        // Init Views
        etSearch = findViewById(R.id.et_search);
        ImageView btnBack = findViewById(R.id.btn_back);
        ImageView btnPerformSearch = findViewById(R.id.btn_perform_search);
        btnFilter = findViewById(R.id.btn_search_filter);
        progressBar = findViewById(R.id.search_loading);

        // 1. Result Recycler View Setup
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

        // 2. History Recycler View Setup
        historyRv = findViewById(R.id.history_recycler_view);
        historyRv.setLayoutManager(new LinearLayoutManager(this));

        // Logic
        btnBack.setOnClickListener(v -> finish());
        btnPerformSearch.setOnClickListener(v -> performSearch());
        btnFilter.setOnClickListener(v -> showFilterMenu());

        // Keyboard "Enter" Logic
        etSearch.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                performSearch();
                return true;
            }
            return false;
        });

        // Show History on Click/Focus
        etSearch.setOnClickListener(v -> showHistory());
        etSearch.setOnFocusChangeListener((v, hasFocus) -> {
            if (hasFocus) showHistory();
        });

        // Initial Focus for keyboard
        etSearch.requestFocus();

        // --- NEW BACK PRESSED LOGIC (Fixed) ---
        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                // Logic: Agar history khuli hai, to pehle usse band karo
                if (historyRv.getVisibility() == View.VISIBLE) {
                    historyRv.setVisibility(View.GONE);
                } else {
                    // Agar history band hai, to Activity finish karo (Normal Back)
                    finish();
                }
            }
        });
    }

    private void performSearch() {
        String query = etSearch.getText().toString().trim();
        if (query.isEmpty()) return;

        // Hide Keyboard
        InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
        if (imm != null) imm.hideSoftInputFromWindow(etSearch.getWindowToken(), 0);

        // 1. Save Query to History
        SearchHistoryHelper.saveSearch(this, query);

        // 2. UI Update
        historyRv.setVisibility(View.GONE); // Hide History
        currentQuery = query;
        fetchSearchResults(0);
    }

    private void showHistory() {
        List<String> list = SearchHistoryHelper.getHistory(this);

        if (!list.isEmpty()) {
            historyAdapter = new HistoryAdapter(list, query -> {
                etSearch.setText(query);
                etSearch.setSelection(query.length());
                performSearch();
            });
            historyRv.setAdapter(historyAdapter);
            historyRv.setVisibility(View.VISIBLE);
        } else {
            historyRv.setVisibility(View.GONE);
        }
    }

    private void fetchSearchResults(int retryCount) {
        String currentKey = KeyManager.getApiKey(this);
        progressBar.setVisibility(View.VISIBLE);
        recyclerView.setVisibility(View.GONE);

        YouTubeApiService apiService = RetrofitClient.getRetrofitInstance(this).create(YouTubeApiService.class);
        apiService.searchVideos("snippet", currentQuery, currentOrder, publishedAfter, videoDuration, "completed", "video", 25, currentKey)
                .enqueue(new Callback<VideoResponse>() {
                    @Override
                    public void onResponse(Call<VideoResponse> call, Response<VideoResponse> response) {
                        if (isFinishing()) return;

                        if (response.code() == 403 && retryCount < KeyManager.getKeyCount()) {
                            KeyManager.rotateKey(SearchActivity.this);
                            fetchSearchResults(retryCount + 1); // RETRY
                            return;
                        }

                        progressBar.setVisibility(View.GONE);
                        recyclerView.setVisibility(View.VISIBLE);

                        if (response.isSuccessful() && response.body() != null) {
                            videoList.clear();
                            videoList.addAll(response.body().getItems());
                            adapter.notifyDataSetChanged();
                        }
                    }

                    @Override
                    public void onFailure(Call<VideoResponse> call, Throwable t) {
                        if (isFinishing()) return;
                        progressBar.setVisibility(View.GONE);
                    }
                });
    }

    private void showFilterMenu() {
        PopupMenu popup = new PopupMenu(this, btnFilter);
        popup.getMenuInflater().inflate(R.menu.menu_filter, popup.getMenu());

        popup.setOnMenuItemClickListener(item -> {
            item.setChecked(true);
            int id = item.getItemId();

            if (id == R.id.time_hour) setTimeFilter(1);
            else if (id == R.id.time_today) setTimeFilter(24);
            else if (id == R.id.time_week) setTimeFilter(24 * 7);
            else if (id == R.id.time_any) publishedAfter = null;

            else if (id == R.id.sort_views) currentOrder = "viewCount";
            else if (id == R.id.sort_date) currentOrder = "date";
            else if (id == R.id.sort_rating) currentOrder = "rating";

            else if (id == R.id.dur_short) videoDuration = "short";
            else if (id == R.id.dur_long) videoDuration = "long";
            else if (id == R.id.dur_any) videoDuration = "any";

            if (!currentQuery.isEmpty()) {
                fetchSearchResults(0);
            }
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
}
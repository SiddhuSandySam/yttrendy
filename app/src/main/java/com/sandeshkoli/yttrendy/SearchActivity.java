package com.sandeshkoli.yttrendy;

import android.content.Intent;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
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
import com.sandeshkoli.yttrendy.utils.KeyManager;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class SearchActivity extends AppCompatActivity {

    private EditText etSearch;
    private RecyclerView recyclerView;
    private VideoAdapter adapter;
    private List<VideoItem> videoList = new ArrayList<>();
    private ProgressBar progressBar;
    private TextView btnFilter;

    private String API_KEY;

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
        recyclerView = findViewById(R.id.search_recycler_view);
        API_KEY = KeyManager.getApiKey(this);

        // Setup RecyclerView
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        // onCreate ke andar jahan adapter bana rahe ho:

        adapter = new VideoAdapter(this, videoList, true, item -> { // <--- Yahan 'true' lagaya
            Intent intent = new Intent(this, VideoPlayerActivity.class);
            intent.putExtra("VIDEO_ID", item.getId());
            intent.putExtra("VIDEO_TITLE", item.getSnippet().getTitle());
            intent.putExtra("VIDEO_DESC", item.getSnippet().getChannelTitle());
            startActivity(intent);
        });
        recyclerView.setAdapter(adapter);

        // Logic
        btnBack.setOnClickListener(v -> finish());

        // Search Button Click
        btnPerformSearch.setOnClickListener(v -> performSearch());

        // Keyboard "Enter" Press
        etSearch.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                performSearch();
                return true;
            }
            return false;
        });

        // Filter Menu (Same logic as CategoryListActivity)
        btnFilter.setOnClickListener(v -> showFilterMenu());
    }

    private void performSearch() {
        String query = etSearch.getText().toString().trim();
        if (query.isEmpty()) return;

        currentQuery = query;
        fetchSearchResults(0);
    }

    private void fetchSearchResults(int retryCount) {
        String currentKey = KeyManager.getApiKey(this);
        progressBar.setVisibility(View.VISIBLE);

        YouTubeApiService apiService = RetrofitClient.getRetrofitInstance(this).create(YouTubeApiService.class);
        apiService.searchVideos("snippet", currentQuery, currentOrder, publishedAfter, videoDuration, "completed", "video", 25, currentKey)
                .enqueue(new Callback<VideoResponse>() {
                    @Override
                    public void onResponse(Call<VideoResponse> call, Response<VideoResponse> response) {
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
                        progressBar.setVisibility(View.GONE);
                    }
                });
    }
    // Reuse the exact same filter logic from CategoryListActivity
    private void showFilterMenu() {
        PopupMenu popup = new PopupMenu(this, btnFilter);
        popup.getMenuInflater().inflate(R.menu.menu_filter, popup.getMenu());

        popup.setOnMenuItemClickListener(item -> {
            item.setChecked(true);
            int id = item.getItemId();

            // Time Logic
            if (id == R.id.time_hour) setTimeFilter(1);
            else if (id == R.id.time_today) setTimeFilter(24);
            else if (id == R.id.time_week) setTimeFilter(24 * 7);
            else if (id == R.id.time_any) publishedAfter = null;

                // Sort Logic
            else if (id == R.id.sort_views) currentOrder = "viewCount";
            else if (id == R.id.sort_date) currentOrder = "date";
            else if (id == R.id.sort_rating) currentOrder = "rating";

                // Duration Logic
            else if (id == R.id.dur_short) videoDuration = "short";
            else if (id == R.id.dur_long) videoDuration = "long";
            else if (id == R.id.dur_any) videoDuration = "any";

            // Refresh Search
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
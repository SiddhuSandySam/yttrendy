package com.sandeshkoli.yttrendy;

import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.PlayerConstants;
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.YouTubePlayer;
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.listeners.AbstractYouTubePlayerListener;
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.views.YouTubePlayerView;
import com.sandeshkoli.yttrendy.adapter.CustomPlayerUiController;
import com.sandeshkoli.yttrendy.adapter.VideoAdapter;
import com.sandeshkoli.yttrendy.models.VideoEntity;
import com.sandeshkoli.yttrendy.models.VideoItem;
import com.sandeshkoli.yttrendy.models.VideoResponse;
import com.sandeshkoli.yttrendy.network.AppDatabase;
import com.sandeshkoli.yttrendy.network.RetrofitClient;
import com.sandeshkoli.yttrendy.network.YouTubeApiService;
import com.sandeshkoli.yttrendy.utils.KeyManager;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class VideoPlayerActivity extends AppCompatActivity {

    private YouTubePlayerView youTubePlayerView;
    private RecyclerView rvRelated;
    private ProgressBar relatedLoading;
    private VideoAdapter relatedAdapter;
    private List<VideoItem> relatedList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video_player);

        String videoId = getIntent().getStringExtra("VIDEO_ID");
        String title = getIntent().getStringExtra("VIDEO_TITLE");
        String desc = getIntent().getStringExtra("VIDEO_DESC");
        String thumbUrl = getIntent().getStringExtra("VIDEO_THUMB");

        youTubePlayerView = findViewById(R.id.youtube_player_view1);
        TextView titleView = findViewById(R.id.player_title_view);
        TextView descView = findViewById(R.id.player_desc_view);
        rvRelated = findViewById(R.id.rv_related);
        relatedLoading = findViewById(R.id.related_loading);

        titleView.setText(title);
        descView.setText(desc);

        getLifecycle().addObserver(youTubePlayerView);

        // --- PLAYER SETUP ---
        youTubePlayerView.addYouTubePlayerListener(new AbstractYouTubePlayerListener() {
            @Override
            public void onReady(@NonNull YouTubePlayer youTubePlayer) {
                View customPlayerUi = youTubePlayerView.inflateCustomPlayerUi(R.layout.custom_ui);
                CustomPlayerUiController controller = new CustomPlayerUiController(VideoPlayerActivity.this, customPlayerUi, youTubePlayer, youTubePlayerView);
                youTubePlayer.addListener(controller);

                if (videoId != null) {
                    youTubePlayer.loadVideo(videoId, 0);
                    // Shorts Sound Fix (Aggressive Unmute)
                    youTubePlayer.unMute();
                    youTubePlayer.setVolume(100);
                }
            }

            @Override
            public void onStateChange(@NonNull YouTubePlayer youTubePlayer, @NonNull PlayerConstants.PlayerState state) {
                if (state == PlayerConstants.PlayerState.PLAYING) {
                    // Thoda ruk kar fir se unmute karo (Shorts ke liye zaroori)
                    new android.os.Handler(android.os.Looper.getMainLooper()).postDelayed(() -> {
                        youTubePlayer.unMute();
                        youTubePlayer.setVolume(100);
                    }, 500);
                }
            }
        });

        setupButtons(videoId, title, desc, thumbUrl);
        setupRelatedRecyclerView();

        // --- FETCH RELATED VIDEOS ---
        if (title != null) {
            // Search API ka use karke related videos layenge
            // Title ke first 4 words use karenge relevance ke liye
            String query = getRelevanceQuery(title);
            fetchRelatedVideos(query, 0);
        }
    }

    private String getRelevanceQuery(String fullTitle) {
        String[] words = fullTitle.split(" ");
        if (words.length > 4) {
            return words[0] + " " + words[1] + " " + words[2] + " " + words[3];
        }
        return fullTitle;
    }

    private void setupRelatedRecyclerView() {
        rvRelated.setLayoutManager(new LinearLayoutManager(this));
        // Use 'true' for full-width cards
        relatedAdapter = new VideoAdapter(this, relatedList, true, item -> {
            // Naya video play karne ke liye Activity reload karenge
            Intent intent = new Intent(this, VideoPlayerActivity.class);
            intent.putExtra("VIDEO_ID", item.getId());
            intent.putExtra("VIDEO_TITLE", item.getSnippet().getTitle());
            intent.putExtra("VIDEO_DESC", item.getSnippet().getChannelTitle());
            intent.putExtra("VIDEO_THUMB", item.getSnippet().getThumbnails().getHigh().getUrl());
            startActivity(intent);
            finish(); // Purani activity band kardo taaki back press sahi kaam kare
        });
        rvRelated.setAdapter(relatedAdapter);
    }

    private void fetchRelatedVideos(String query, int retryCount) {
        String apiKey = KeyManager.getApiKey(this);
        YouTubeApiService api = RetrofitClient.getRetrofitInstance(this).create(YouTubeApiService.class);

        api.searchVideos("snippet", query, "relevance", null, "any", "completed", "video", 15, apiKey)
                .enqueue(new Callback<VideoResponse>() {
                    @Override
                    public void onResponse(Call<VideoResponse> call, Response<VideoResponse> response) {
                        // Safe API Call: Agar activity band ho gayi hai to crash mat karo
                        if (isFinishing() || isDestroyed()) return;

                        if (response.code() == 403 && retryCount < KeyManager.getKeyCount()) {
                            KeyManager.rotateKey(VideoPlayerActivity.this);
                            fetchRelatedVideos(query, retryCount + 1);
                            return;
                        }

                        relatedLoading.setVisibility(View.GONE);
                        if (response.isSuccessful() && response.body() != null) {
                            relatedList.clear();
                            relatedList.addAll(response.body().getItems());
                            relatedAdapter.notifyDataSetChanged();
                        }
                    }

                    @Override
                    public void onFailure(Call<VideoResponse> call, Throwable t) {
                        if (!isFinishing()) relatedLoading.setVisibility(View.GONE);
                    }
                });
    }

    private void setupButtons(String videoId, String title, String desc, String thumbUrl) {
        Button btnSave = findViewById(R.id.btn_save_video);
        AppDatabase db = AppDatabase.getInstance(this);

        if (db.videoDao().isSaved(videoId)) {
            btnSave.setText("❤ Saved");
            btnSave.setBackgroundTintList(android.content.res.ColorStateList.valueOf(Color.RED));
        }

        btnSave.setOnClickListener(v -> {
            VideoEntity entity = new VideoEntity(videoId, title, desc, thumbUrl);
            if (db.videoDao().isSaved(videoId)) {
                db.videoDao().deleteVideo(entity);
                btnSave.setText("❤ Save");
                btnSave.setBackgroundTintList(android.content.res.ColorStateList.valueOf(Color.parseColor("#333333")));
                Toast.makeText(this, "Removed", Toast.LENGTH_SHORT).show();
            } else {
                db.videoDao().saveVideo(entity);
                btnSave.setText("❤ Saved");
                btnSave.setBackgroundTintList(android.content.res.ColorStateList.valueOf(Color.RED));
                Toast.makeText(this, "Saved!", Toast.LENGTH_SHORT).show();
            }
        });

        Button btnShare = findViewById(R.id.btn_share_video);
        btnShare.setOnClickListener(v -> {
            Intent shareIntent = new Intent(Intent.ACTION_SEND);
            shareIntent.setType("text/plain");
            shareIntent.putExtra(Intent.EXTRA_TEXT, "Watch this amazing video: https://youtu.be/" + videoId);
            startActivity(Intent.createChooser(shareIntent, "Share"));
        });
    }

    @Override
    public void onConfigurationChanged(@NonNull Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
            if (getSupportActionBar() != null) getSupportActionBar().hide();
            youTubePlayerView.matchParent();
        } else {
            getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
            if (getSupportActionBar() != null) getSupportActionBar().show();
            youTubePlayerView.wrapContent();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        youTubePlayerView.release();
    }
}
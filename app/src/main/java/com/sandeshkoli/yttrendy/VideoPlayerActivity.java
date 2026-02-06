package com.sandeshkoli.yttrendy;

import android.app.PictureInPictureParams;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Rational;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.gson.Gson;
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.PlayerConstants;
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.YouTubePlayer;
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.listeners.AbstractYouTubePlayerListener;
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.views.YouTubePlayerView;
import com.sandeshkoli.yttrendy.adapter.CustomPlayerUiController;
import com.sandeshkoli.yttrendy.adapter.VideoAdapter;
import com.sandeshkoli.yttrendy.models.VideoEntity;
import com.sandeshkoli.yttrendy.models.VideoItem;
import com.sandeshkoli.yttrendy.network.AppDatabase;
import com.sandeshkoli.yttrendy.utils.FirebaseDataManager;
import com.sandeshkoli.yttrendy.utils.JsonCacheManager;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class VideoPlayerActivity extends AppCompatActivity {

    private YouTubePlayerView youTubePlayerView;
    private RecyclerView rvRelated;
    private ProgressBar relatedLoading;
    private VideoAdapter relatedAdapter;
    private List<VideoItem> relatedList = new ArrayList<>();
    private SwitchCompat switchAutoplay;
    private View contentScroll;

    private String currentRegion = "IN";
    private Gson gson = new Gson();
    private JsonCacheManager cacheManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video_player);

        cacheManager = new JsonCacheManager(this);
        currentRegion = getSharedPreferences("PREFS", MODE_PRIVATE).getString("region", "IN");

        String videoId = getIntent().getStringExtra("VIDEO_ID");
        String title = getIntent().getStringExtra("VIDEO_TITLE");
        String desc = getIntent().getStringExtra("VIDEO_DESC");
        String thumbUrl = getIntent().getStringExtra("VIDEO_THUMB");

        youTubePlayerView = findViewById(R.id.youtube_player_view1);
        TextView titleView = findViewById(R.id.player_title_view);
        TextView descView = findViewById(R.id.player_desc_view);
        rvRelated = findViewById(R.id.rv_related);
        relatedLoading = findViewById(R.id.related_loading);
        contentScroll = findViewById(R.id.scroll_content_layout);
        switchAutoplay = findViewById(R.id.switch_autoplay);

        titleView.setText(title);
        descView.setText(desc);

        getLifecycle().addObserver(youTubePlayerView);

        youTubePlayerView.addYouTubePlayerListener(new AbstractYouTubePlayerListener() {
            @Override
            public void onReady(@NonNull YouTubePlayer youTubePlayer) {
                View customPlayerUi = youTubePlayerView.inflateCustomPlayerUi(R.layout.custom_ui);
                CustomPlayerUiController controller = new CustomPlayerUiController(VideoPlayerActivity.this, customPlayerUi, youTubePlayer, youTubePlayerView);
                youTubePlayer.addListener(controller);

                if (videoId != null) {
                    youTubePlayer.loadVideo(videoId, 0);
                    youTubePlayer.unMute();
                    youTubePlayer.setVolume(100);
                }
            }

            @Override
            public void onStateChange(@NonNull YouTubePlayer youTubePlayer, @NonNull PlayerConstants.PlayerState state) {
                if (state == PlayerConstants.PlayerState.PLAYING) {
                    new android.os.Handler().postDelayed(() -> {
                        youTubePlayer.unMute();
                        youTubePlayer.setVolume(100);
                    }, 500);
                }
                if (state == PlayerConstants.PlayerState.ENDED) {
                    if (switchAutoplay.isChecked() && !relatedList.isEmpty()) {
                        playNextVideo(relatedList.get(0));
                    }
                }
            }
        });

        setupButtons(videoId, title, desc, thumbUrl);
        setupRelatedRecyclerView();

        if (title != null) fetchRelatedVideos(getRelevanceQuery(title));
    }

    private void playNextVideo(VideoItem item) {
        Intent intent = new Intent(this, VideoPlayerActivity.class);
        intent.putExtra("VIDEO_ID", item.getId());
        intent.putExtra("VIDEO_TITLE", item.getSnippet().getTitle());
        intent.putExtra("VIDEO_DESC", item.getSnippet().getChannelTitle());
        intent.putExtra("VIDEO_THUMB", item.getSnippet().getThumbnails().getHigh().getUrl());
        startActivity(intent);
        finish();
    }

    private void fetchRelatedVideos(String query) {
        // 🔥 Region-aware related videos
        String cacheKey = "related_" + query.replaceAll("\\s+", "_").toLowerCase() + "_" + currentRegion;

        String localJson = cacheManager.getCache(cacheKey);
        if (localJson != null) {
            android.util.Log.d("DATA_SOURCE_TRACKER", "📱 SOURCE: [Local Cache] Related: " + query);
            updateRelatedUI(gson.fromJson(localJson, Map.class));
            return;
        }

        FirebaseDataManager.getVideosFromFirebase(query, null, currentRegion)
                .addOnCompleteListener(task -> {
                    if (isFinishing()) return;
                    relatedLoading.setVisibility(View.GONE);
                    if (task.isSuccessful()) {
                        Map<String, Object> result = (Map<String, Object>) task.getResult().getData();
                        String source = (String) result.get("source");
                        android.util.Log.d("DATA_SOURCE_TRACKER", "🌐 SOURCE: [" + source.toUpperCase() + "] Related");
                        cacheManager.saveCache(cacheKey, gson.toJson(result));
                        updateRelatedUI(result);
                    }
                });
    }

    private void updateRelatedUI(Map<String, Object> result) {
        List<Map<String, Object>> items = (List<Map<String, Object>>) result.get("items");
        if (items != null) {
            relatedList.clear();
            for (Map<String, Object> itemMap : items) {
                relatedList.add(gson.fromJson(gson.toJson(itemMap), VideoItem.class));
            }
            relatedAdapter.notifyDataSetChanged();
        }
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
            } else {
                db.videoDao().saveVideo(entity);
                btnSave.setText("❤ Saved");
                btnSave.setBackgroundTintList(android.content.res.ColorStateList.valueOf(Color.RED));
                Toast.makeText(this, "Saved to Library", Toast.LENGTH_SHORT).show();
            }
        });

        findViewById(R.id.btn_share_video).setOnClickListener(v -> {
            Intent shareIntent = new Intent(Intent.ACTION_SEND);
            shareIntent.setType("text/plain");
            // 🔥 FIXED BROKEN STRING
            String msg = "Check out this amazing discovery on ViralStream: \n\n" + title +
                    "\n\nWatch here: https://www.youtube.com/watch?v=" + videoId;
            shareIntent.putExtra(Intent.EXTRA_TEXT, msg);
            startActivity(Intent.createChooser(shareIntent, "Share via"));
        });
    }

    private String getRelevanceQuery(String fullTitle) {
        String[] words = fullTitle.split(" ");
        if (words.length > 4) return words[0] + " " + words[1] + " " + words[2] + " " + words[3];
        return fullTitle;
    }

    private void setupRelatedRecyclerView() {
        rvRelated.setLayoutManager(new LinearLayoutManager(this));
        relatedAdapter = new VideoAdapter(this, relatedList, true, item -> playNextVideo(item));
        rvRelated.setAdapter(relatedAdapter);
    }

    @Override
    public void onPictureInPictureModeChanged(boolean isInPictureInPictureMode, Configuration newConfig) {
        super.onPictureInPictureModeChanged(isInPictureInPictureMode, newConfig);
        contentScroll.setVisibility(isInPictureInPictureMode ? View.GONE : View.VISIBLE);
    }

    @Override
    protected void onUserLeaveHint() {
        super.onUserLeaveHint();
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            enterPictureInPictureMode(new PictureInPictureParams.Builder().setAspectRatio(new Rational(16, 9)).build());
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        youTubePlayerView.release();
    }
}
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
import com.sandeshkoli.yttrendy.utils.QueryOptimizer; // Optimizer import kiya

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
    private TextView titleView, descView;

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
        titleView = findViewById(R.id.player_title_view);
        descView = findViewById(R.id.player_desc_view);
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

        // 🔥 SMART FIX: Purana getRelevanceQuery hata diya, Optimizer direct use kiya
        if (title != null) fetchRelatedVideos(title); // Direct title bhejo, Optimizer sambhaal lega.

        // OnBackPressedDispatcher logic
        getOnBackPressedDispatcher().addCallback(this, new androidx.activity.OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
                    // 1. Agar Landscape hai, toh pehle Portrait mode mein wapas laao
                    setRequestedOrientation(android.content.pm.ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
                } else {
                    // 2. Agar pehle se Portrait hai, toh activity band kar do
                    finish();
                }
            }
        });

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

    private void fetchRelatedVideos(String rawTitle) {
        // 🔥 ROOT KEY LOGIC: "Arijit Singh Kesariya" -> "arijit"
        String optimizedKeyword = QueryOptimizer.getCleanedKey(rawTitle);
        String cacheKey = "related_" + optimizedKeyword + "_" + currentRegion;

        android.util.Log.d("QUOTA_SAVER", "Player Video: [" + rawTitle + "] -> Cache Key: [" + cacheKey + "]");

        // 1. Check Local Cache
        String localJson = cacheManager.getCache(cacheKey);
        if (localJson != null) {
            android.util.Log.d("DATA_SOURCE_TRACKER", "📱 SOURCE: [Local Cache] Related: " + optimizedKeyword);
            updateRelatedUI(gson.fromJson(localJson, Map.class));
            return;
        }

        // 2. Fetch from Firebase
        FirebaseDataManager.getVideosFromFirebase(optimizedKeyword, null, currentRegion)
                .addOnCompleteListener(task -> {
                    if (isFinishing()) return;
                    relatedLoading.setVisibility(View.GONE);
                    if (task.isSuccessful()) {
                        Map<String, Object> result = (Map<String, Object>) task.getResult().getData();
                        String source = (String) result.get("source");
                        android.util.Log.d("DATA_SOURCE_TRACKER", "🌐 SOURCE: [" + source.toUpperCase() + "] Related Key: " + cacheKey);
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
                try {
                    Object idObj = itemMap.get("id");
                    String finalId = (idObj instanceof String) ? (String) idObj :
                            (idObj instanceof Map) ? (String) ((Map) idObj).get("videoId") : "";

                    itemMap.put("id", finalId);
                    relatedList.add(gson.fromJson(gson.toJson(itemMap), VideoItem.class));
                } catch (Exception e) {
                    android.util.Log.e("JSON_ERROR", "Related Videos parse error");
                }
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
            // 🔥 FIXED BROKEN STRING: Ab link sahi jayega
            String msg = title + "\n\nWatch here: https://www.youtube.com/watch?v=" + videoId + "\n\nvia ViralStream App";
            shareIntent.putExtra(Intent.EXTRA_TEXT, msg);
            startActivity(Intent.createChooser(shareIntent, "Share via"));
        });
    }

    private void setupRelatedRecyclerView() {
        rvRelated.setLayoutManager(new LinearLayoutManager(this));
        relatedAdapter = new VideoAdapter(this, relatedList, true, item -> playNextVideo(item));
        rvRelated.setAdapter(relatedAdapter);
    }

    // 🔥 FULLSCREEN LANDSCAPE LOGIC: Phone rotate hote hi extra UI chupa do
    @Override
    public void onConfigurationChanged(@NonNull Configuration newConfig) {
        super.onConfigurationChanged(newConfig);

        View decorView = getWindow().getDecorView();

        if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            // 1. Hide extra UI
            if (contentScroll != null) contentScroll.setVisibility(View.GONE);

            // 2. Fullscreen Mode (Status bar aur Navigation bar hide karo)
            decorView.setSystemUiVisibility(
                    View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                            | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                            | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                            | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                            | View.SYSTEM_UI_FLAG_FULLSCREEN
                            | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);

            // 3. Player ko poori screen par phelao
            youTubePlayerView.setLayoutParams(new android.widget.LinearLayout.LayoutParams(
                    android.widget.LinearLayout.LayoutParams.MATCH_PARENT,
                    android.widget.LinearLayout.LayoutParams.MATCH_PARENT));

        } else {
            // 1. Show extra UI
            if (contentScroll != null) contentScroll.setVisibility(View.VISIBLE);

            // 2. Exit Fullscreen Mode
            decorView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_VISIBLE);

            // 3. Player ka size wapas normal karo (250dp height)
            float heightPx = 250 * getResources().getDisplayMetrics().density;
            youTubePlayerView.setLayoutParams(new android.widget.LinearLayout.LayoutParams(
                    android.widget.LinearLayout.LayoutParams.MATCH_PARENT,
                    (int) heightPx));
        }
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
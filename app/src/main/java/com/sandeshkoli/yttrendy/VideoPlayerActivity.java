package com.sandeshkoli.yttrendy;

import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.PlayerConstants;
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.YouTubePlayer;
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.listeners.AbstractYouTubePlayerListener;
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.views.YouTubePlayerView;
import com.sandeshkoli.yttrendy.adapter.CustomPlayerUiController;
import com.sandeshkoli.yttrendy.models.VideoEntity;
import com.sandeshkoli.yttrendy.network.AppDatabase;

public class VideoPlayerActivity extends AppCompatActivity {

    private YouTubePlayerView youTubePlayerView;
    private View decorView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video_player);

        decorView = getWindow().getDecorView();
        String videoId = getIntent().getStringExtra("VIDEO_ID");
        String title = getIntent().getStringExtra("VIDEO_TITLE");
        String desc = getIntent().getStringExtra("VIDEO_DESC");
        String thumbUrl = getIntent().getStringExtra("VIDEO_THUMB");

        youTubePlayerView = findViewById(R.id.youtube_player_view1);
        TextView titleView = findViewById(R.id.player_title_view);
        TextView descView = findViewById(R.id.player_desc_view);

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
                    // Hum pehle volume set karenge fir load karenge
                    youTubePlayer.setVolume(100);
                    youTubePlayer.unMute();

                    // Shorts ke liye loadVideo ki jagah hum thoda wait karke play karenge
                    youTubePlayer.loadVideo(videoId, 0);
                }
            }

            @Override
            public void onStateChange(@NonNull YouTubePlayer youTubePlayer, @NonNull PlayerConstants.PlayerState state) {
                super.onStateChange(youTubePlayer, state);

                // Jab video PLAYING state mein aaye, tab fir se unmute command bhejo
                // Ye "Ziddi" Shorts ke liye zaroori hai
                if (state == PlayerConstants.PlayerState.PLAYING) {
                    youTubePlayer.unMute();
                    youTubePlayer.setVolume(100);
                }
            }
        });

        // SAVE BUTTON LOGIC
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
                btnSave.setText("❤ Save Video");
                btnSave.setBackgroundTintList(android.content.res.ColorStateList.valueOf(Color.parseColor("#333333")));
                Toast.makeText(this, "Removed from Saved", Toast.LENGTH_SHORT).show();
            } else {
                db.videoDao().saveVideo(entity);
                btnSave.setText("❤ Saved");
                btnSave.setBackgroundTintList(android.content.res.ColorStateList.valueOf(Color.RED));
                Toast.makeText(this, "Video Saved!", Toast.LENGTH_SHORT).show();
            }
        });

        // SHARE BUTTON LOGIC
        Button btnShare = findViewById(R.id.btn_share_video);
        btnShare.setOnClickListener(v -> {
            shareVideo(videoId, title);
        });
    }

    private void shareVideo(String vId, String vTitle) {
        // FIX: URL string sahi kar di gayi hai
        String youtubeUrl = "https://www.youtube.com/watch?v=" + vId;
        String shareMessage = "Check out this amazing video: \n\n" +
                vTitle + "\n" +
                youtubeUrl + "\n\n" +
                "Discovered via ViralVideo App. Download for more!";

        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.setType("text/plain");
        shareIntent.putExtra(Intent.EXTRA_SUBJECT, "ViralVideo Discovery");
        shareIntent.putExtra(Intent.EXTRA_TEXT, shareMessage);

        startActivity(Intent.createChooser(shareIntent, "Share video via"));
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
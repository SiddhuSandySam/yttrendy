package com.sandeshkoli.yttrendy.adapter; // Apna package name check karna

import android.app.Activity;
import android.content.pm.ActivityInfo;
import android.view.View;
import android.widget.ImageButton;
import androidx.annotation.NonNull;
import com.pierfrancescosoffritti.androidyoutubeplayer.core.customui.utils.FadeViewHelper;
import com.pierfrancescosoffritti.androidyoutubeplayer.core.customui.views.YouTubePlayerSeekBar;
import com.pierfrancescosoffritti.androidyoutubeplayer.core.customui.views.YouTubePlayerSeekBarListener;
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.PlayerConstants;
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.YouTubePlayer;
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.listeners.AbstractYouTubePlayerListener;
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.utils.YouTubePlayerTracker;
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.views.YouTubePlayerView;
import com.sandeshkoli.yttrendy.R;

public class CustomPlayerUiController extends AbstractYouTubePlayerListener {
    private final YouTubePlayerTracker playerTracker;
    private final YouTubePlayer youTubePlayer;
    private final YouTubePlayerView youTubePlayerView;
    private final Activity activity;

    public CustomPlayerUiController(Activity activity, View controlsUi, YouTubePlayer youTubePlayer, YouTubePlayerView youTubePlayerView) {
        this.activity = activity;
        this.youTubePlayer = youTubePlayer;
        this.youTubePlayerView = youTubePlayerView;
        this.playerTracker = new YouTubePlayerTracker();

        // Player ka state track karne ke liye
        youTubePlayer.addListener(playerTracker);

        initViews(controlsUi);
    }

    private void initViews(View view) {
        View container = view.findViewById(R.id.container1);
        YouTubePlayerSeekBar seekBar = view.findViewById(R.id.playerSeekbar);
        ImageButton pausePlay = view.findViewById(R.id.pausePlay);
        ImageButton fullScreen = view.findViewById(R.id.toggleFullScreen);

        // --- SEEKBAR LOGIC START ---

        // 1. Player se seekbar ko connect karo (Progress dikhane ke liye)
        youTubePlayer.addListener(seekBar);

        // 2. Seekbar se player ko connect karo (Video jump karane ke liye)
        seekBar.setYoutubePlayerSeekBarListener(new YouTubePlayerSeekBarListener() {
            @Override
            public void seekTo(float time) {
                // Jab user ungli se seekbar chhode tab video jump karegi
                youTubePlayer.seekTo(time);
            }
        });

        // --- SEEKBAR LOGIC END ---

        pausePlay.setOnClickListener(v -> {
            if (playerTracker.getState() == PlayerConstants.PlayerState.PLAYING) {
                pausePlay.setImageResource(android.R.drawable.ic_media_play);
                youTubePlayer.pause();
            } else {
                pausePlay.setImageResource(android.R.drawable.ic_media_pause);
                youTubePlayer.play();
            }
        });

        fullScreen.setOnClickListener(v -> {
            if (activity.getRequestedOrientation() == ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE) {
                activity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
            } else {
                activity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
            }
        });

        // Controls ko auto-hide karne ke liye
        FadeViewHelper fadeViewHelper = new FadeViewHelper(container);
        youTubePlayer.addListener(fadeViewHelper);

        view.findViewById(R.id.root).setOnClickListener(v -> fadeViewHelper.toggleVisibility());
    }
}
package com.sandeshkoli.yttrendy.adapter;

import android.app.Activity;
import android.content.pm.ActivityInfo;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.ImageButton;
import android.widget.Toast;

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
    private final Activity activity;
    private final FadeViewHelper fadeViewHelper;

    // --- MULTI-TAP VARIABLES ---
    private int tapCount = 0;
    private final Handler tapHandler = new Handler(Looper.getMainLooper());
    private Runnable tapRunnable;
    private boolean isMuted = false;

    public CustomPlayerUiController(Activity activity, View controlsUi, YouTubePlayer youTubePlayer, YouTubePlayerView youTubePlayerView) {
        this.activity = activity;
        this.youTubePlayer = youTubePlayer;
        this.playerTracker = new YouTubePlayerTracker();

        youTubePlayer.addListener(playerTracker);

        View container = controlsUi.findViewById(R.id.container1);
        fadeViewHelper = new FadeViewHelper(container);
        youTubePlayer.addListener(fadeViewHelper);

        initViews(controlsUi);
    }

    private void initViews(View view) {
        YouTubePlayerSeekBar seekBar = view.findViewById(R.id.playerSeekbar);
        ImageButton pausePlay = view.findViewById(R.id.pausePlay);
        ImageButton fullScreen = view.findViewById(R.id.toggleFullScreen);
        ImageButton btnVolume = view.findViewById(R.id.btnVolume);

        View viewForward = view.findViewById(R.id.view_forward);
        View viewRewind = view.findViewById(R.id.view_rewind);

        youTubePlayer.addListener(seekBar);
        seekBar.setYoutubePlayerSeekBarListener(new YouTubePlayerSeekBarListener() {
            @Override
            public void seekTo(float time) { youTubePlayer.seekTo(time); }
        });

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

        btnVolume.setOnClickListener(v -> {
            if (isMuted) {
                youTubePlayer.unMute();
                youTubePlayer.setVolume(100);
                btnVolume.setImageResource(android.R.drawable.ic_lock_silent_mode_off);
                isMuted = false;
            } else {
                youTubePlayer.mute();
                btnVolume.setImageResource(android.R.drawable.ic_lock_silent_mode);
                isMuted = true;
            }
        });

        // --- UPDATED MULTI-TAP SEEKING LOGIC ---

        viewForward.setOnClickListener(v -> handleMultiTap(true));
        viewRewind.setOnClickListener(v -> handleMultiTap(false));
    }

    private void handleMultiTap(boolean isForward) {
        tapCount++;

        // Purana timer cancel karo
        tapHandler.removeCallbacks(tapRunnable);

        tapRunnable = () -> {
            if (tapCount == 1) {
                // Single Tap: Controls show/hide karo
                fadeViewHelper.toggleVisibility();
            } else {
                // Multi Tap: Seek karo
                // Formula: (Taps - 1) * 10. (e.g., 2 taps = 10s, 3 taps = 20s)
                int secondsToSeek = (tapCount - 1) * 10;
                float currentTime = playerTracker.getCurrentSecond();

                if (isForward) {
                    youTubePlayer.seekTo(currentTime + secondsToSeek);
                    Toast.makeText(activity, "Forward +" + secondsToSeek + "s", Toast.LENGTH_SHORT).show();
                } else {
                    youTubePlayer.seekTo(Math.max(0, currentTime - secondsToSeek));
                    Toast.makeText(activity, "Rewind -" + secondsToSeek + "s", Toast.LENGTH_SHORT).show();
                }
            }
            // Reset count
            tapCount = 0;
        };

        // 600ms ka window de rahe hain agla tap karne ke liye
        tapHandler.postDelayed(tapRunnable, 600);
    }
}
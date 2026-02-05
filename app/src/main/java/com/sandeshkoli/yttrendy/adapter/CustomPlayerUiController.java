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
    private final YouTubePlayerView youTubePlayerView;
    private final Activity activity;
    private final FadeViewHelper fadeViewHelper;

    // Double Tap Logic Variables
    private int forwardClicks = 0;
    private int rewindClicks = 0;
    private final Handler handler = new Handler(Looper.getMainLooper());
    private boolean isMuted = false;

    public CustomPlayerUiController(Activity activity, View controlsUi, YouTubePlayer youTubePlayer, YouTubePlayerView youTubePlayerView) {
        this.activity = activity;
        this.youTubePlayer = youTubePlayer;
        this.youTubePlayerView = youTubePlayerView;
        this.playerTracker = new YouTubePlayerTracker();

        youTubePlayer.addListener(playerTracker);

        // Auto-hide controls helper
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

        // --- SEEKBAR LOGIC ---
        youTubePlayer.addListener(seekBar);
        seekBar.setYoutubePlayerSeekBarListener(new YouTubePlayerSeekBarListener() {
            @Override
            public void seekTo(float time) {
                youTubePlayer.seekTo(time);
            }
        });

        // --- PLAY / PAUSE ---
        pausePlay.setOnClickListener(v -> {
            if (playerTracker.getState() == PlayerConstants.PlayerState.PLAYING) {
                pausePlay.setImageResource(android.R.drawable.ic_media_play);
                youTubePlayer.pause();
            } else {
                pausePlay.setImageResource(android.R.drawable.ic_media_pause);
                youTubePlayer.play();
            }
        });

        // --- FULL SCREEN ---
        fullScreen.setOnClickListener(v -> {
            if (activity.getRequestedOrientation() == ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE) {
                activity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
            } else {
                activity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
            }
        });

        // --- MUTE / UNMUTE BUTTON ---
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

        // --- DOUBLE TAP LOGIC (FORWARD) ---
        viewForward.setOnClickListener(v -> {
            forwardClicks++;
            if (forwardClicks == 1) {
                // Wait 300ms to see if it's a double tap
                handler.postDelayed(() -> {
                    if (forwardClicks == 1) {
                        // Single Tap: Toggle Controls
                        fadeViewHelper.toggleVisibility();
                    }
                    forwardClicks = 0;
                }, 300);
            } else if (forwardClicks == 2) {
                // Double Tap Detected!
                forwardClicks = 0;
                float current = playerTracker.getCurrentSecond();
                youTubePlayer.seekTo(current + 10);
                Toast.makeText(activity, "+10s", Toast.LENGTH_SHORT).show();
            }
        });

        // --- DOUBLE TAP LOGIC (REWIND) ---
        viewRewind.setOnClickListener(v -> {
            rewindClicks++;
            if (rewindClicks == 1) {
                handler.postDelayed(() -> {
                    if (rewindClicks == 1) {
                        fadeViewHelper.toggleVisibility();
                    }
                    rewindClicks = 0;
                }, 300);
            } else if (rewindClicks == 2) {
                rewindClicks = 0;
                float current = playerTracker.getCurrentSecond();
                youTubePlayer.seekTo(Math.max(0, current - 10));
                Toast.makeText(activity, "-10s", Toast.LENGTH_SHORT).show();
            }
        });

        // Root listener hatado kyunki ab humare custom views click handle kar rahe hain
        // view.findViewById(R.id.root).setOnClickListener(...) -> REMOVED
    }
}
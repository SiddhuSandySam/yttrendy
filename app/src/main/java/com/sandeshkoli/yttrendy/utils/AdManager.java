package com.sandeshkoli.yttrendy.utils;

import android.app.Activity;
import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import androidx.annotation.NonNull;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.FullScreenContentCallback;
import com.google.android.gms.ads.LoadAdError;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.interstitial.InterstitialAd;
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback;

public class AdManager {
    private static InterstitialAd mInterstitialAd;
    private static int clickCount = 0;
    private static long lastAdShowTime = 0; // Pichle ad ka time
    private static final long AD_COOLDOWN_MS = 2 * 60 * 1000; // 2 Minutes ka cooldown

    public static void init(Context context) {
        MobileAds.initialize(context, initializationStatus -> {});
        loadInterstitial(context);
    }

    public static void loadInterstitial(Context context) {
        AdRequest adRequest = new AdRequest.Builder().build();
        InterstitialAd.load(context, "ca-app-pub-3047884361380270/8625683483", adRequest,
                new InterstitialAdLoadCallback() {
                    @Override
                    public void onAdLoaded(@NonNull InterstitialAd interstitialAd) {
                        mInterstitialAd = interstitialAd;
                    }
                    @Override
                    public void onAdFailedToLoad(@NonNull LoadAdError loadAdError) {
                        mInterstitialAd = null;
                        new Handler(Looper.getMainLooper()).postDelayed(() -> loadInterstitial(context), 5000);
                    }
                });
    }

    public static void showInterstitial(Activity activity, AdFinishedListener listener) {
        clickCount++;
        long currentTime = System.currentTimeMillis();

        // Logic: Agar 3 clicks hue hain AUR 2 minute beet chuke hain tabhi ad dikhao
        if (clickCount % 3 == 0 && (currentTime - lastAdShowTime > AD_COOLDOWN_MS) && mInterstitialAd != null) {
            mInterstitialAd.show(activity);
            mInterstitialAd.setFullScreenContentCallback(new FullScreenContentCallback() {
                @Override
                public void onAdDismissedFullScreenContent() {
                    lastAdShowTime = System.currentTimeMillis(); // Time update karo
                    loadInterstitial(activity);
                    if (listener != null) listener.onAdFinished();
                }
                @Override
                public void onAdFailedToShowFullScreenContent(@NonNull com.google.android.gms.ads.AdError adError) {
                    if (listener != null) listener.onAdFinished();
                }
            });
        } else {
            if (listener != null) listener.onAdFinished();
            if (mInterstitialAd == null) loadInterstitial(activity);
        }
    }

    public interface AdFinishedListener {
        void onAdFinished();
    }
}
package com.sandeshkoli.yttrendy.utils;

import android.app.Activity;
import android.content.Context;
import android.os.Handler;
import android.os.Looper; // <--- Ye zaroori hai


import androidx.annotation.NonNull;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.FullScreenContentCallback;
import com.google.android.gms.ads.LoadAdError;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.interstitial.InterstitialAd;
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback;

public class AdManager {
    private static InterstitialAd mInterstitialAd;
    private static int clickCount = 0; // Sabse important variable

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

                        // Naye tarike se Handler likhein (Looper.getMainLooper() ke sath)
                        new Handler(Looper.getMainLooper()).postDelayed(() -> {
                            loadInterstitial(context);
                        }, 5000);
                    }
                });
    }

    public static void showInterstitial(Activity activity, AdFinishedListener listener) {
        clickCount++; // Har baar click count badhao

        // Logic: Agar click count 3 ka multiple hai (3, 6, 9...) aur ad ready hai
        if (clickCount % 3 == 0 && mInterstitialAd != null) {
            mInterstitialAd.show(activity);
            mInterstitialAd.setFullScreenContentCallback(new FullScreenContentCallback() {
                @Override
                public void onAdDismissedFullScreenContent() {
                    loadInterstitial(activity); // Naya load karo
                    if (listener != null) listener.onAdFinished();
                }

                @Override
                public void onAdFailedToShowFullScreenContent(@NonNull com.google.android.gms.ads.AdError adError) {
                    if (listener != null) listener.onAdFinished();
                }
            });
        } else {
            // Agar 3 clicks nahi huye ya ad ready nahi hai, to bina ad ke kaam hone do
            if (listener != null) listener.onAdFinished();

            // Background me ad load karlo agar null hai to
            if (mInterstitialAd == null) {
                loadInterstitial(activity);
            }
        }
    }

    public interface AdFinishedListener {
        void onAdFinished();
    }
}
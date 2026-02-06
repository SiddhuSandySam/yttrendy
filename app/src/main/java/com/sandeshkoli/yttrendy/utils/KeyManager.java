package com.sandeshkoli.yttrendy.utils;

import android.content.Context;
import android.content.SharedPreferences;
import com.sandeshkoli.yttrendy.R;

public class KeyManager {
    private static final String PREF_NAME = "key_manager_prefs";
    private static final String KEY_INDEX = "current_key_index";

    public static String getApiKey(Context context) {
        String[] keys = {
                context.getString(R.string.yt_api_key_1),
                context.getString(R.string.yt_api_key_2),
                context.getString(R.string.yt_api_key_3),
                context.getString(R.string.yt_api_key_4)
        };
        SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        int index = prefs.getInt(KEY_INDEX, 0);
        return keys[index % keys.length];
    }

    public static void rotateKey(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        int currentIndex = prefs.getInt(KEY_INDEX, 0);
        prefs.edit().putInt(KEY_INDEX, currentIndex + 1).apply();
    }

    // Ye batayega ki kya hum saari keys try kar chuke hain? (Retry limit ke liye)
    public static int getKeyCount() {
        return 3; // Hamare paas 3 keys hain
    }
}
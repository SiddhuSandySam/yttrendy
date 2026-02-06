package com.sandeshkoli.yttrendy.utils;

import android.content.Context;
import android.content.SharedPreferences;

public class JsonCacheManager {
    private static final String PREF_NAME = "yt_json_cache";
    private final SharedPreferences prefs;

    // 🔥 Mobile Cache Expiry: 6 Hours (User ko fresh dikhega, par server burden nahi hoga)
    private static final long MOBILE_TTL = 6 * 60 * 60 * 1000;

    public JsonCacheManager(Context context) {
        prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
    }

    public void saveCache(String key, String json) {
        prefs.edit()
                .putString(key, json)
                .putLong(key + "_time", System.currentTimeMillis()) // Time save karo
                .apply();
    }

    public String getCache(String key) {
        long savedTime = prefs.getLong(key + "_time", 0);
        long currentTime = System.currentTimeMillis();

        // Agar data 6 ghante se purana hai, toh use ignore karo
        if (currentTime - savedTime > MOBILE_TTL) {
            return null;
        }

        return prefs.getString(key, null);
    }

    public void clearAll() {
        prefs.edit().clear().apply();
    }
}
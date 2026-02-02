package com.sandeshkoli.yttrendy.utils;

import android.content.Context;
import android.content.SharedPreferences;

public class JsonCacheManager {
    private static final String PREF_NAME = "yt_json_cache";
    private final SharedPreferences prefs;

    public JsonCacheManager(Context context) {
        prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
    }

    public void saveCache(String key, String json) {
        prefs.edit().putString(key, json).apply();
    }

    public String getCache(String key) {
        return prefs.getString(key, null);
    }
}
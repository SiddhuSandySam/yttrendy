package com.sandeshkoli.yttrendy.utils;

import android.content.Context;
import android.content.SharedPreferences;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public class SearchHistoryHelper {
    private static final String PREF_NAME = "search_history_pref";
    private static final String KEY_HISTORY = "history_list";

    public static void saveSearch(Context context, String query) {
        List<String> history = getHistory(context);
        if (history.contains(query)) {
            history.remove(query); // Duplicate hatao
        }
        history.add(0, query); // Top pe add karo

        if (history.size() > 10) { // Sirf last 10 items rakho
            history.remove(history.size() - 1);
        }

        SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        prefs.edit().putString(KEY_HISTORY, new Gson().toJson(history)).apply();
    }

    public static List<String> getHistory(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        String json = prefs.getString(KEY_HISTORY, null);
        if (json == null) return new ArrayList<>();

        Type type = new TypeToken<ArrayList<String>>() {}.getType();
        return new Gson().fromJson(json, type);
    }

    public static void clearHistory(Context context) {
        context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE).edit().clear().apply();
    }
}
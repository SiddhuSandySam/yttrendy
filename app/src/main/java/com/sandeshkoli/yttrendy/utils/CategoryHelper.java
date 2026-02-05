package com.sandeshkoli.yttrendy.utils;

import java.util.LinkedHashMap;
import java.util.Map;

public class CategoryHelper {
    public static Map<String, String> getCategories() {
        LinkedHashMap<String, String> map = new LinkedHashMap<>();

        // Trending
        map.put("🏆 Most Viewed", "MOST_VIEWED"); // Keyword for viewCount
        map.put("👍 Most Liked", "MOST_LIKED");    // Keyword for rating
        map.put("⚡ Shorts", "SHORTS_TREND");
        map.put("🔥 Trending Now", null);

        // Standard Categories (1 Unit Cost)
        map.put("📰 News", "25");
        map.put("⚽ Sports", "17");
        map.put("🎮 Gaming", "20");
        map.put("🎬 Entertainment", "24");
        map.put("🎵 Music", "10");
        map.put("😂 Comedy", "23");

        // FIX: FITNESS aur FOOD ki ID alag ki
        map.put("💪 Fitness", "22"); // People & Blogs (Aksar fitness content yahan hota hai)
        map.put("🍔 Food", "26");    // Howto & Style / Cooking

        // FIX: Jinke Trending list band hain, unhe Search (100 Unit) Keyword se load karo
        map.put("✈️ Travel", "Travel vlogs in India"); // Keyword
        map.put("💻 Technology", "28");
        map.put("🎓 Education", "Coding tutorials for students"); // Keyword

        return map;
    }
}
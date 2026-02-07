package com.sandeshkoli.yttrendy.utils;

import java.util.LinkedHashMap;
import java.util.Map;

public class CategoryHelper {
    public static Map<String, String> getCategories() {
        LinkedHashMap<String, String> map = new LinkedHashMap<>();

        map.put("🔥 Trending Now", null);
        map.put("India Now", "India viral trending");
        map.put("🎵 Music", "MUSIC");
        map.put("📺 TV Shows", "TV_SHOWS");
        map.put("⛩️ Anime", "ANIME");
        map.put("🎮 Games", "GAMES");
        map.put("⚽ Sports", "SPORTS");
        map.put("🎙️ Journalists", "JOURNALISTS");
        map.put("🕌 Scholars", "SCHOLARS");
        map.put("🌍 Leaders", "LEADERS");
        map.put("✨ Motivational", "MOTIVATIONAL");
        map.put("🎓 Courses", "COURSES");
        map.put("💻 Programming", "PROGRAMMING");
        map.put("🧪 Tech", "TECH");
        map.put("🥘 Cooking", "COOKING");
        map.put("😂 Comedy", "COMEDY");
        map.put("🎭 Dramas", "DRAMAS");
        map.put("💪 Fitness", "FITNESS");
        map.put("👗 Lifestyle", "LIFESTYLE");
        map.put("👶 Kids", "KIDS");
        map.put("📰 News", "NEWS");
        map.put("🔍 Leaks", "LEAKS");
        map.put("✈️ Travel", "TRAVEL");

        return map;
    }
}
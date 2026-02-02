package com.sandeshkoli.yttrendy.utils;

import java.util.LinkedHashMap;
import java.util.Map;

public class SubCategoryHelper {

    public static Map<String, String> getSubCategories(String mainCategory) {
        LinkedHashMap<String, String> map = new LinkedHashMap<>();

        switch (mainCategory) {
            case "🎮 Gaming":
                map.put("Minecraft", "Minecraft gameplay hindi");
                map.put("GTA V", "GTA 5 funny moments");
                map.put("BGMI / PUBG", "BGMI india highlights");
                map.put("Valorant", "Valorant best plays");
                map.put("Roblox", "Roblox funny moments");
                map.put("Horror Games", "Horror gaming hindi");
                break;

            case "📰 News":
                map.put("Top Stories", "India news live");
                map.put("Politics", "Indian politics news");
                map.put("World", "World news english");
                map.put("Tech News", "Latest technology news india");
                map.put("Business", "Stock market india live");
                break;

            case "⚽ Sports":
                map.put("Cricket", "Cricket highlights 2026");
                map.put("Football", "Football goals highlights");
                map.put("WWE", "WWE latest raw smackdown");
                map.put("UFC", "UFC knockouts");
                map.put("IPL", "IPL best moments");
                break;

            case "🎬 Entertainment":
                map.put("Movie Trailers", "New movie trailers 2026");
                map.put("Bollywood", "Bollywood gossips hindi");
                map.put("Web Series", "Best web series to watch");
                map.put("Interviews", "Celebrity interviews india");
                map.put("TV Shows", "Indian reality shows highlights");
                break;

            case "😂 Comedy":
                map.put("Standup", "Best indian standup comedy");
                map.put("Roast", "Roast videos hindi");
                map.put("Pranks", "Funny pranks india");
                map.put("Skits", "Funny vines hindi");
                map.put("Memes", "Dank memes compilation");
                break;

            case "💻 Technology":
                map.put("Smartphones", "Best smartphone under 20000");
                map.put("Unboxing", "Gadget unboxing hindi");
                map.put("AI Tools", "ChatGPT AI tutorial");
                map.put("PC Build", "Gaming PC build india");
                map.put("Apps", "Best android apps 2026");
                break;

            case "🎵 Music":
                map.put("Bollywood Hits", "New bollywood songs 2026");
                map.put("Punjabi", "Latest punjabi songs");
                map.put("Lo-Fi", "Lofi study music slow");
                map.put("Rap/Hip-Hop", "Indian hip hop songs");
                map.put("Devotional", "Bhakti songs hindi");
                break;

            case "💪 Fitness":
                map.put("Home Workout", "Full body workout at home");
                map.put("Yoga", "Yoga for beginners");
                map.put("Diet Plans", "Weight loss diet plan india");
                map.put("Bodybuilding", "Gym motivation status");
                break;

            case "🍔 Food":
                map.put("Street Food", "Indian street food");
                map.put("Recipes", "Easy recipes for snacks");
                map.put("Challenges", "Food eating challenge india");
                map.put("Cake", "Cake decoration ideas");
                break;

            case "✈️ Travel":
                map.put("Vlogs", "Travel vlogs india");
                map.put("Budget Trip", "Budget travel india");
                map.put("Mountains", "Manali ladakh trip");
                map.put("International", "Europe tour vlog");
                break;

            case "🎓 Education":
                map.put("Current Affairs", "Current affairs today");
                map.put("Coding", "Learn programming hindi");
                map.put("Motivation", "Study motivation video");
                map.put("Science", "Science experiments for kids");
                break;

            default:
                map.put("Trending", mainCategory + " trending");
                map.put("Popular", "Best " + mainCategory);
                break;
        }
        return map;
    }
}
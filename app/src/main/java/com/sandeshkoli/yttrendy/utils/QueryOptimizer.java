package com.sandeshkoli.yttrendy.utils;

public class QueryOptimizer {
    public static String getCleanedKey(String query) {
        if (query == null || query.isEmpty()) return "trending";

        String targetPart = query;

        // 1. 🔥 YOUTUBE PATTERN LOGIC:
        // Agar title mein '|' ya '-' hai, toh show name/artist name usually baad mein hota hai.
        // Example: "Gori Tera Gav... | Shreya Ghoshal" -> Hum "Shreya Ghoshal" wala part uthayenge.
        if (query.contains("|")) {
            String[] parts = query.split("\\|");
            if (parts.length > 1) targetPart = parts[1].trim(); // Take 2nd part (Show Name)
        } else if (query.contains("-")) {
            String[] parts = query.split("-");
            if (parts.length > 1) targetPart = parts[1].trim(); // Take 2nd part
        }

        // 2. Clean symbols and lowercase
        String cleaned = targetPart.toLowerCase().replaceAll("[^a-z0-9\\s]", "").trim();
        String[] words = cleaned.split("\\s+");

        // 3. STOP WORDS: Inhe ignore karenge
        String stopWords = "the,a,an,top,best,new,latest,official,how,to,my,all,non,stop,episode,full,video,scene,comedy";

        // QueryOptimizer.java ke loop ke andar:
        for (String word : words) {
            // Word stop word na ho, length >= 3 ho AUR wo sirf numbers na ho
            if (!stopWords.contains(word) && word.length() >= 3 && !word.matches("\\d+")) {
                return word;
            }
        }

        // Fallback: Agar kuch na mile toh original query ka pehla word
        return query.toLowerCase().replaceAll("[^a-z0-9\\s]", "").trim().split("\\s+")[0];
    }
}
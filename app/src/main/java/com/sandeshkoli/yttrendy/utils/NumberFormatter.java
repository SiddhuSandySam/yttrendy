package com.sandeshkoli.yttrendy.utils;

import java.text.DecimalFormat;
import java.util.Map;
import java.util.NavigableMap;
import java.util.TreeMap;

public class NumberFormatter {

    private static final NavigableMap<Long, String> suffixes = new TreeMap<>();
    static {
        suffixes.put(1_000L, "K");
        suffixes.put(1_000_000L, "M");
        suffixes.put(1_000_000_000L, "B");
    }

    public static String formatViewCount(long value) {
        if (value < 1000) return String.valueOf(value); // Return as is if less than 1000

        Map.Entry<Long, String> entry = suffixes.floorEntry(value);
        Long divideBy = entry.getKey();
        String suffix = entry.getValue();

        double truncated = (double) value / divideBy;

        // Use DecimalFormat to format to one decimal place if needed
        DecimalFormat df = new DecimalFormat("#.#");
        return df.format(truncated) + suffix;
    }
}
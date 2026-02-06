package com.sandeshkoli.yttrendy.utils;

import java.util.LinkedHashMap;
import java.util.Map;

public class RegionHelper {
    public static Map<String, String> getAvailableRegions() {
        LinkedHashMap<String, String> map = new LinkedHashMap<>();
        map.put("🇮🇳 India", "IN");
        map.put("🇺🇸 United States", "US");
        map.put("🇬🇧 United Kingdom", "GB");
        map.put("🇵🇰 Pakistan", "PK");
        map.put("🇦🇫 Afghanistan", "AF");
        map.put("🇦🇪 United Arab Emirates", "AE");
        map.put("🇨🇦 Canada", "CA");
        map.put("🇦🇺 Australia", "AU");
        map.put("🇧🇷 Brazil", "BR");
        return map;
    }
}
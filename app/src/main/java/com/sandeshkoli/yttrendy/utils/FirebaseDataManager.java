package com.sandeshkoli.yttrendy.utils;

import com.google.android.gms.tasks.Task;
import com.google.firebase.functions.FirebaseFunctions;
import com.google.firebase.functions.HttpsCallableResult;
import java.util.HashMap;
import java.util.Map;

public class FirebaseDataManager {

    public static Task<HttpsCallableResult> getVideosFromFirebase(String query, String categoryId, String regionCode) {
        FirebaseFunctions functions = FirebaseFunctions.getInstance();

        Map<String, Object> data = new HashMap<>();
        if (query != null) data.put("query", query);
        if (categoryId != null) data.put("categoryId", categoryId);

        // Region code bhej rahe hain (e.g., "US", "PK", "IN")
        data.put("region", (regionCode != null) ? regionCode : "IN");

        return functions.getHttpsCallable("getYoutubeContent").call(data);
    }
}
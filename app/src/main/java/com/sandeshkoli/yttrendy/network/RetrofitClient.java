package com.sandeshkoli.yttrendy.network;

import android.content.Context;
import okhttp3.Cache;
import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import java.io.File;
import java.io.IOException;

public class RetrofitClient {

    private static final String BASE_URL = "https://www.googleapis.com/";
    private static Retrofit retrofitInstance = null;

    public static Retrofit getRetrofitInstance(Context context) {
        if (retrofitInstance == null) {

            // 1. Cache setup (10 MB size)
            File cacheDirectory = new File(context.getCacheDir(), "http_cache");
            Cache cache = new Cache(cacheDirectory, 10 * 1024 * 1024);

            // 2. Logging Interceptor (Debugging ke liye)
            // RetrofitClient.java me logging level change karo
            HttpLoggingInterceptor logging = new HttpLoggingInterceptor(message -> {
                // Isse ek special TAG mil jayega Logcat me dhundne ke liye
                android.util.Log.d("API_CACHE_DEBUG", message);
            });
            logging.setLevel(HttpLoggingInterceptor.Level.BODY); // Level change to BODY // Headers dekho taaki pata chale cache hit ho raha hai ya nahi

            // 3. Cache Interceptor (Force cache for 2 hours)
            // YouTube API headers caching allow nahi karte, isliye hum use force karenge
            Interceptor cacheInterceptor = new Interceptor() {
                @Override
                public Response intercept(Chain chain) throws IOException {
                    Response response = chain.proceed(chain.request());
                    return response.newBuilder()
                            .header("Cache-Control", "public, max-age=7200") // 2 hours in seconds
                            .removeHeader("Pragma")
                            .build();
                }
            };

            // 4. OkHttpClient Builder
            OkHttpClient okHttpClient = new OkHttpClient.Builder()
                    .cache(cache)
                    .addInterceptor(logging)
                    .addNetworkInterceptor(cacheInterceptor) // Network interceptor zaroori hai header override ke liye
                    .build();

            retrofitInstance = new Retrofit.Builder()
                    .baseUrl(BASE_URL)
                    .addConverterFactory(GsonConverterFactory.create())
                    .client(okHttpClient)
                    .build();
        }
        return retrofitInstance;
    }
}
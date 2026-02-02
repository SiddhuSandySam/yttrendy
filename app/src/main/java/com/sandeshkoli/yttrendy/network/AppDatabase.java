package com.sandeshkoli.yttrendy.network;

import android.content.Context;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import com.sandeshkoli.yttrendy.models.VideoEntity;

@Database(entities = {VideoEntity.class}, version = 1)
public abstract class AppDatabase extends RoomDatabase {
    public abstract VideoDao videoDao();
    private static AppDatabase instance;

    public static synchronized AppDatabase getInstance(Context context) {
        if (instance == null) {
            instance = Room.databaseBuilder(context.getApplicationContext(),
                            AppDatabase.class, "video_db")
                    .allowMainThreadQueries() // Chhoti app hai, main thread chalta hai
                    .build();
        }
        return instance;
    }
}
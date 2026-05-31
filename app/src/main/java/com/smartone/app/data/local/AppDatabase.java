package com.smartone.app.data.local;

import android.content.Context;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import com.smartone.app.util.Constants;

@Database(entities = {HistoryEntry.class}, version = Constants.DB_VERSION, exportSchema = false)
public abstract class AppDatabase extends RoomDatabase {

    private static volatile AppDatabase INSTANCE;

    public abstract HistoryDao historyDao();

    public static AppDatabase getInstance(Context context) {
        if (INSTANCE == null) {
            synchronized (AppDatabase.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(
                            context.getApplicationContext(),
                            AppDatabase.class,
                            Constants.DB_NAME)
                            .fallbackToDestructiveMigration()
                            .build();
                }
            }
        }
        return INSTANCE;
    }

    public static void destroyInstance() {
        if (INSTANCE != null && INSTANCE.isOpen()) {
            INSTANCE.close();
        }
        INSTANCE = null;
    }
}

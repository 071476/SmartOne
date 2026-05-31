package com.smartone.app.data.local;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;
import java.util.List;

@Dao
public interface HistoryDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long insert(HistoryEntry entry);

    @Update
    void update(HistoryEntry entry);

    @Delete
    void delete(HistoryEntry entry);

    @Query("DELETE FROM history_entries")
    void deleteAll();

    @Query("DELETE FROM history_entries WHERE type = :type")
    void deleteByType(String type);

    @Query("DELETE FROM history_entries WHERE id = :id")
    void deleteById(long id);

    @Query("SELECT * FROM history_entries ORDER BY timestamp DESC")
    LiveData<List<HistoryEntry>> getAll();

    @Query("SELECT * FROM history_entries WHERE type = :type ORDER BY timestamp DESC")
    LiveData<List<HistoryEntry>> getByType(String type);

    @Query("SELECT * FROM history_entries WHERE is_favorite = 1 ORDER BY timestamp DESC")
    LiveData<List<HistoryEntry>> getFavorites();

    @Query("SELECT * FROM history_entries WHERE title LIKE :query OR preview LIKE :query ORDER BY timestamp DESC")
    LiveData<List<HistoryEntry>> search(String query);

    @Query("SELECT * FROM history_entries WHERE id = :id LIMIT 1")
    HistoryEntry getById(long id);

    @Query("UPDATE history_entries SET is_favorite = :favorite WHERE id = :id")
    void setFavorite(long id, boolean favorite);

    @Query("SELECT COUNT(*) FROM history_entries")
    int getTotalCount();

    @Query("SELECT COUNT(*) FROM history_entries WHERE type = :type")
    int getCountByType(String type);

    @Query("SELECT * FROM history_entries ORDER BY timestamp DESC LIMIT :limit")
    LiveData<List<HistoryEntry>> getRecent(int limit);

    @Query("DELETE FROM history_entries WHERE id NOT IN (SELECT id FROM history_entries ORDER BY timestamp DESC LIMIT :maxItems)")
    void trimToMaxItems(int maxItems);
}

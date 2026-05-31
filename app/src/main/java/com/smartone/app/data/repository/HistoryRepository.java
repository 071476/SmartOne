package com.smartone.app.data.repository;

import android.app.Application;
import androidx.lifecycle.LiveData;
import com.smartone.app.data.local.AppDatabase;
import com.smartone.app.data.local.HistoryDao;
import com.smartone.app.data.local.HistoryEntry;
import com.smartone.app.util.Constants;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class HistoryRepository {

    private final HistoryDao dao;
    private final ExecutorService executor;

    public HistoryRepository(Application app) {
        AppDatabase db = AppDatabase.getInstance(app);
        dao            = db.historyDao();
        executor       = Executors.newSingleThreadExecutor();
    }

    public LiveData<List<HistoryEntry>> getAll() {
        return dao.getAll();
    }

    public LiveData<List<HistoryEntry>> getByType(String type) {
        return dao.getByType(type);
    }

    public LiveData<List<HistoryEntry>> getFavorites() {
        return dao.getFavorites();
    }

    public LiveData<List<HistoryEntry>> search(String query) {
        return dao.search("%" + query + "%");
    }

    public LiveData<List<HistoryEntry>> getRecent(int limit) {
        return dao.getRecent(limit);
    }

    public void insert(HistoryEntry entry) {
        executor.execute(() -> {
            dao.insert(entry);
            dao.trimToMaxItems(Constants.HISTORY_MAX_ITEMS);
        });
    }

    public void saveJson(String fileName, String jsonContent) {
        insert(HistoryEntry.fromJson(fileName, jsonContent));
    }

    public void saveChat(String userMessage, String assistantResponse) {
        insert(HistoryEntry.fromChat(userMessage, assistantResponse));
    }

    public void update(HistoryEntry entry) {
        executor.execute(() -> dao.update(entry));
    }

    public void delete(HistoryEntry entry) {
        executor.execute(() -> dao.delete(entry));
    }

    public void deleteById(long id) {
        executor.execute(() -> dao.deleteById(id));
    }

    public void deleteAll() {
        executor.execute(dao::deleteAll);
    }

    public void deleteByType(String type) {
        executor.execute(() -> dao.deleteByType(type));
    }

    public void setFavorite(long id, boolean favorite) {
        executor.execute(() -> dao.setFavorite(id, favorite));
    }

    public void getStats(StatsCallback callback) {
        executor.execute(() -> {
            int total     = dao.getTotalCount();
            int jsonCount = dao.getCountByType(Constants.HISTORY_TYPE_JSON);
            int chatCount = dao.getCountByType(Constants.HISTORY_TYPE_CHAT);
            callback.onStats(total, jsonCount, chatCount);
        });
    }

    public interface StatsCallback {
        void onStats(int total, int jsonCount, int chatCount);
    }
}

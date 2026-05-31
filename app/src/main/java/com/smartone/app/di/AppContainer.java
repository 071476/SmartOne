package com.smartone.app.di;

import android.app.Application;
import com.smartone.app.data.remote.ApiClient;
import com.smartone.app.data.repository.HistoryRepository;
import com.smartone.app.util.PrefsManager;

public class AppContainer {

    public final PrefsManager      prefsManager;
    public final ApiClient         apiClient;
    public final HistoryRepository historyRepository;

    public AppContainer(Application app) {
        prefsManager      = new PrefsManager(app);
        apiClient         = new ApiClient();
        apiClient.setApiKey(prefsManager.getApiKey());
        apiClient.setModel(prefsManager.getModel());
        historyRepository = new HistoryRepository(app);
    }

    public void refreshApiConfig() {
        apiClient.setApiKey(prefsManager.getApiKey());
        apiClient.setModel(prefsManager.getModel());
    }

    public boolean isReady() {
        return prefsManager.hasApiKey();
    }

    public boolean isFirstLaunch() {
        return prefsManager.isFirstLaunch();
    }

    public void reset(Runnable onComplete) {
        prefsManager.clearAllIncludingKey();
        apiClient.setApiKey("");
        apiClient.clearHistory();
        historyRepository.deleteAll();
        if (onComplete != null) onComplete.run();
    }
}

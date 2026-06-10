package com.smartone.app.di;

import android.app.Application;
import android.provider.Settings;
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

        String deviceId = Settings.Secure.getString(
                app.getContentResolver(),
                Settings.Secure.ANDROID_ID);
        apiClient.setDeviceId(deviceId);

        apiClient.setModel(prefsManager.getModel());
        historyRepository = new HistoryRepository(app);
    }

    public void refreshApiConfig() {
        apiClient.setModel(prefsManager.getModel());
    }

    public boolean isReady() {
        return true;
    }

    public boolean isFirstLaunch() {
        return prefsManager.isFirstLaunch();
    }

    public void reset(Runnable onComplete) {
        prefsManager.clearAllIncludingKey();
        apiClient.clearHistory();
        historyRepository.deleteAll();
        if (onComplete != null) onComplete.run();
    }
}

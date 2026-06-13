package com.smartone.app.data.repository;

import android.app.Application;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import com.smartone.app.SmartOneApplication;
import com.smartone.app.data.local.AppDatabase;
import com.smartone.app.data.remote.ApiClient;
import com.smartone.app.util.PrefsManager;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ChatRepository {

    private final ApiClient         apiClient;
    private final HistoryRepository historyRepository;
    private final PrefsManager      prefsManager;

    private final MutableLiveData<List<ChatMessage>> messages
            = new MutableLiveData<>(new ArrayList<>());
    private final MutableLiveData<Boolean> isLoading
            = new MutableLiveData<>(false);
    private final MutableLiveData<String> errorEvent
            = new MutableLiveData<>(null);
    private final MutableLiveData<Integer> remainingMessages
            = new MutableLiveData<>(0);

    public ChatRepository(Application app) {
        PrefsManager prefs;
        ApiClient client;
        HistoryRepository history;

        try {
            SmartOneApplication application = (SmartOneApplication) app;
            prefs   = application.container.prefsManager;
            client  = application.container.apiClient;
            history = application.container.historyRepository;
        } catch (Exception e) {
            android.util.Log.e("SmartOne", "Error obteniendo container: " + e.getMessage());
            prefs   = new PrefsManager(app);
            client  = new ApiClient();
            history = new HistoryRepository(app);
        }

        this.prefsManager      = prefs;
        this.apiClient         = client;
        this.historyRepository = history;

        this.apiClient.setApiKey(this.prefsManager.getApiKey());
        this.apiClient.setModel(this.prefsManager.getModel());
        updateRemainingMessages();
    }

    public LiveData<List<ChatMessage>> getMessages() { return messages; }
    public LiveData<Boolean> getIsLoading()          { return isLoading; }
    public LiveData<String> getErrorEvent()          { return errorEvent; }
    public LiveData<Integer> getRemainingMessages()  { return remainingMessages; }

    public void sendMessage(String text) {
        if (text == null || text.trim().isEmpty()) return;

        if (!apiClient.isConfigured()) {
            postError("API key no configurada. Ve a Ajustes para agregarla.");
            return;
        }

        if (prefsManager.hasReachedFreeLimit()) {
            postError("Alcanzaste el límite diario de mensajes. Vuelve mañana.");
            return;
        }

        addMessage(ChatMessage.fromUser(text));
        isLoading.postValue(true);

        apiClient.sendMessage(text, new ApiClient.Callback() {
            @Override
            public void onSuccess(String reply) {
                addMessage(ChatMessage.fromAssistant(reply));
                isLoading.postValue(false);
                if (prefsManager.isAutoSave()) {
                    historyRepository.saveChat(text, reply);
                }
                prefsManager.incrementMessagesUsed();
                updateRemainingMessages();
            }

            @Override
            public void onError(String error) {
                addMessage(ChatMessage.fromError(error));
                isLoading.postValue(false);
                postError(error);
            }
        });
    }

    public void analyzeJson(String jsonContent, ApiClient.Callback callback) {
        if (!apiClient.isConfigured()) {
            callback.onError("API key no configurada.");
            return;
        }
        if (prefsManager.hasReachedFreeLimit()) {
            callback.onError("Alcanzaste el limite diario de mensajes. Vuelve manana.");
            return;
        }
        String prompt = "Analiza el siguiente JSON. Describe:\n" +
                "1. Su estructura general\n" +
                "2. Los campos principales y sus tipos\n" +
                "3. Si hay algo inusual o que pueda mejorarse\n\n" +
                jsonContent;
        apiClient.sendOneShot(prompt, new ApiClient.Callback() {
            @Override
            public void onSuccess(String reply) {
                prefsManager.incrementMessagesUsed();
                updateRemainingMessages();
                callback.onSuccess(reply);
            }
            @Override
            public void onError(String error) {
                callback.onError(error);
            }
        });
    }

    public void clearMessages() {
        messages.postValue(new ArrayList<>());
        apiClient.clearHistory();
    }

    public void addSystemMessage(ChatMessage message) {
        addMessage(message);
    }

    public void reloadConfig() {
        apiClient.setApiKey(prefsManager.getApiKey());
        apiClient.setModel(prefsManager.getModel());
        updateRemainingMessages();
    }

    private void addMessage(ChatMessage message) {
        List<ChatMessage> current = new ArrayList<>(
                messages.getValue() != null
                        ? messages.getValue()
                        : Collections.emptyList()
        );
        current.add(message);
        messages.postValue(current);
    }

    private void postError(String message) {
        errorEvent.postValue(message);
    }

    private void updateRemainingMessages() {
        remainingMessages.postValue(prefsManager.getRemainingFreeMessages());
    }
}

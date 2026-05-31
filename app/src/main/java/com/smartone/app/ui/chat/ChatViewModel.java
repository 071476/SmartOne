package com.smartone.app.ui.chat;

import android.app.Application;
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import com.smartone.app.data.remote.ApiClient;
import com.smartone.app.data.repository.ChatMessage;
import com.smartone.app.data.repository.ChatRepository;
import com.smartone.app.util.PrefsManager;
import java.util.ArrayList;
import java.util.List;

public class ChatViewModel extends AndroidViewModel {

    private ChatRepository repository;
    private PrefsManager   prefsManager;
    private boolean initialized = false;

    public ChatViewModel(@NonNull Application application) {
        super(application);
        try {
            repository   = new ChatRepository(application);
            prefsManager = new PrefsManager(application);
            initialized  = true;
        } catch (Exception e) {
            android.util.Log.e("SmartOne", "ChatViewModel init error: " + e.getMessage(), e);
        }
    }

    public LiveData<List<ChatMessage>> getMessages() {
        if (!initialized) return new MutableLiveData<>(new ArrayList<>());
        return repository.getMessages();
    }

    public LiveData<Boolean> getIsLoading() {
        if (!initialized) return new MutableLiveData<>(false);
        return repository.getIsLoading();
    }

    public LiveData<String> getErrorEvent() {
        if (!initialized) return new MutableLiveData<>(null);
        return repository.getErrorEvent();
    }

    public LiveData<Integer> getRemainingMessages() {
        if (!initialized) return new MutableLiveData<>(15);
        return repository.getRemainingMessages();
    }

    public void sendMessage(String text) {
        if (!initialized) return;
        repository.sendMessage(text);
    }

    public void addWelcomeMessage() {
        if (!initialized) return;
        List<ChatMessage> current = repository.getMessages().getValue();
        if (current == null || current.isEmpty()) {
            repository.addSystemMessage(ChatMessage.welcome());
        }
    }

    public void clearMessages() {
        if (!initialized) return;
        repository.clearMessages();
    }

    public void analyzeJson(String jsonContent) {
        if (!initialized) return;
        repository.analyzeJson(jsonContent, new ApiClient.Callback() {
            @Override
            public void onSuccess(String reply) {
                repository.addSystemMessage(ChatMessage.fromAssistant(reply));
            }
            @Override
            public void onError(String error) {
                repository.addSystemMessage(ChatMessage.fromError(error));
            }
        });
    }

    public void reloadConfig() {
        if (!initialized) return;
        repository.reloadConfig();
    }

    public boolean isApiKeyConfigured() {
        if (!initialized || prefsManager == null) return false;
        return prefsManager.hasApiKey();
    }

    public int getRemainingFreeCount() {
        if (!initialized || prefsManager == null) return 0;
        return prefsManager.getRemainingFreeMessages();
    }

    public boolean hasReachedLimit() {
        if (!initialized || prefsManager == null) return false;
        return prefsManager.hasReachedFreeLimit();
    }
}

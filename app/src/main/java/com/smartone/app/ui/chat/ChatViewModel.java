package com.smartone.app.ui.chat;

import android.app.Application;
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import com.smartone.app.SmartOneApplication;
import com.smartone.app.data.remote.ApiClient;
import com.smartone.app.data.repository.ChatMessage;
import com.smartone.app.data.repository.ChatRepository;
import com.smartone.app.util.PrefsManager;
import java.util.List;

public class ChatViewModel extends AndroidViewModel {

    private final ChatRepository repository;
    private final PrefsManager   prefsManager;

    public ChatViewModel(@NonNull Application application) {
        super(application);
        repository   = new ChatRepository(application);
        prefsManager = SmartOneApplication
                .from(application)
                .container
                .prefsManager;
    }

    public LiveData<List<ChatMessage>> getMessages() {
        return repository.getMessages();
    }

    public LiveData<Boolean> getIsLoading() {
        return repository.getIsLoading();
    }

    public LiveData<String> getErrorEvent() {
        return repository.getErrorEvent();
    }

    public LiveData<Integer> getRemainingMessages() {
        return repository.getRemainingMessages();
    }

    public void sendMessage(String text) {
        repository.sendMessage(text);
    }

    public void addWelcomeMessage() {
        List<ChatMessage> current = repository.getMessages().getValue();
        if (current == null || current.isEmpty()) {
            repository.addSystemMessage(ChatMessage.welcome());
        }
    }

    public void clearMessages() {
        repository.clearMessages();
    }

    public void analyzeJson(String jsonContent) {
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
        repository.reloadConfig();
    }

    public boolean isApiKeyConfigured() {
        return prefsManager.hasApiKey();
    }

    public int getRemainingFreeCount() {
        return prefsManager.getRemainingFreeMessages();
    }

    public boolean hasReachedLimit() {
        return prefsManager.hasReachedFreeLimit();
    }
}

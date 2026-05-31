package com.smartone.app.ui.chat;

import android.app.Application;
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import com.smartone.app.data.remote.ApiClient;
import com.smartone.app.data.repository.ChatMessage;
import com.smartone.app.util.Constants;
import com.smartone.app.util.PrefsManager;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ChatViewModel extends AndroidViewModel {

    private final ApiClient    apiClient;
    private final PrefsManager prefsManager;

    private final MutableLiveData<List<ChatMessage>> messages
            = new MutableLiveData<>(new ArrayList<>());
    private final MutableLiveData<Boolean> isLoading
            = new MutableLiveData<>(false);
    private final MutableLiveData<String> errorEvent
            = new MutableLiveData<>(null);
    private final MutableLiveData<Integer> remainingMessages
            = new MutableLiveData<>(15);

    public ChatViewModel(@NonNull Application app) {
        super(app);
        prefsManager = new PrefsManager(app);
        apiClient    = new ApiClient();
        apiClient.setApiKey(prefsManager.getApiKey());
        apiClient.setModel(prefsManager.getModel());
        remainingMessages.setValue(prefsManager.getRemainingFreeMessages());
    }

    public LiveData<List<ChatMessage>> getMessages()         { return messages; }
    public LiveData<Boolean>           getIsLoading()        { return isLoading; }
    public LiveData<String>            getErrorEvent()       { return errorEvent; }
    public LiveData<Integer>           getRemainingMessages(){ return remainingMessages; }

    public void addWelcomeMessage() {
        List<ChatMessage> current = messages.getValue();
        if (current == null || current.isEmpty()) {
            addMessage(ChatMessage.welcome());
        }
    }

    public void sendMessage(String text) {
        if (text == null || text.trim().isEmpty()) return;

        if (!apiClient.isConfigured()) {
            errorEvent.postValue("API key no configurada. Ve a Ajustes.");
            return;
        }

        if (prefsManager.hasReachedFreeLimit()) {
            errorEvent.postValue("Límite diario alcanzado. Vuelve mañana.");
            return;
        }

        addMessage(ChatMessage.fromUser(text));
        isLoading.postValue(true);

        apiClient.sendMessage(text, new ApiClient.Callback() {
            @Override
            public void onSuccess(String reply) {
                addMessage(ChatMessage.fromAssistant(reply));
                isLoading.postValue(false);
                prefsManager.incrementMessagesUsed();
                remainingMessages.postValue(
                        prefsManager.getRemainingFreeMessages());
            }

            @Override
            public void onError(String error) {
                addMessage(ChatMessage.fromError(error));
                isLoading.postValue(false);
                errorEvent.postValue(error);
            }
        });
    }

    public void analyzeJson(String json) {
        if (!apiClient.isConfigured()) return;
        String prompt = "Analiza este JSON:\n" + json;
        apiClient.sendOneShot(prompt, new ApiClient.Callback() {
            @Override
            public void onSuccess(String reply) {
                addMessage(ChatMessage.fromAssistant(reply));
            }
            @Override
            public void onError(String error) {
                addMessage(ChatMessage.fromError(error));
            }
        });
    }

    public void clearMessages() {
        messages.postValue(new ArrayList<>());
        apiClient.clearHistory();
    }

    public void reloadConfig() {
        apiClient.setApiKey(prefsManager.getApiKey());
        apiClient.setModel(prefsManager.getModel());
        remainingMessages.postValue(prefsManager.getRemainingFreeMessages());
    }

    public boolean isApiKeyConfigured() {
        return prefsManager.hasApiKey();
    }

    private void addMessage(ChatMessage message) {
        List<ChatMessage> current = new ArrayList<>(
                messages.getValue() != null
                        ? messages.getValue()
                        : Collections.emptyList());
        current.add(message);
        messages.postValue(current);
    }
}

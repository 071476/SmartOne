package com.smartone.app.util;

import android.content.Context;
import android.content.SharedPreferences;
import androidx.security.crypto.EncryptedSharedPreferences;
import androidx.security.crypto.MasterKey;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class PrefsManager {

    private final SharedPreferences prefs;
    private final SharedPreferences securePrefs;

    public PrefsManager(Context context) {
        prefs = context.getSharedPreferences(Constants.PREFS_NAME, Context.MODE_PRIVATE);

        SharedPreferences encrypted;
        try {
            MasterKey masterKey = new MasterKey.Builder(context)
                    .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
                    .build();
            encrypted = EncryptedSharedPreferences.create(
                    context,
                    "smartone_secure_prefs",
                    masterKey,
                    EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                    EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
            );
        } catch (Exception e) {
            encrypted = prefs;
        }
        securePrefs = encrypted;
    }

    public void saveApiKey(String apiKey) {
        securePrefs.edit().putString(Constants.KEY_API_KEY, apiKey).apply();
    }

    public String getApiKey() {
        return securePrefs.getString(Constants.KEY_API_KEY, "");
    }

    public boolean hasApiKey() {
        String key = getApiKey();
        return key != null && !key.trim().isEmpty();
    }

    public void clearApiKey() {
        securePrefs.edit().remove(Constants.KEY_API_KEY).apply();
    }

    public void saveModel(String model) {
        prefs.edit().putString(Constants.KEY_MODEL, model).apply();
    }

    public String getModel() {
        return prefs.getString(Constants.KEY_MODEL, Constants.CLAUDE_MODEL_FAST);
    }

    public void saveTheme(String theme) {
        prefs.edit().putString(Constants.KEY_THEME, theme).apply();
    }

    public String getTheme() {
        return prefs.getString(Constants.KEY_THEME, Constants.THEME_DARK);
    }

    public void saveAutoSave(boolean autoSave) {
        prefs.edit().putBoolean(Constants.KEY_AUTO_SAVE, autoSave).apply();
    }

    public boolean isAutoSave() {
        return prefs.getBoolean(Constants.KEY_AUTO_SAVE, false);
    }

    public void saveFontSize(int size) {
        prefs.edit().putInt(Constants.KEY_FONT_SIZE, size).apply();
    }

    public int getFontSize() {
        return prefs.getInt(Constants.KEY_FONT_SIZE, Constants.FONT_SIZE_MEDIUM);
    }

    public boolean isFirstLaunch() {
        return prefs.getBoolean(Constants.KEY_FIRST_LAUNCH, true);
    }

    public void setFirstLaunchDone() {
        prefs.edit().putBoolean(Constants.KEY_FIRST_LAUNCH, false).apply();
    }

    public int getMessagesUsedToday() {
        String savedDate = prefs.getString(Constants.KEY_MESSAGES_DATE, "");
        String today = LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE);
        if (!today.equals(savedDate)) {
            resetDailyMessages();
            return 0;
        }
        return prefs.getInt(Constants.KEY_MESSAGES_USED, 0);
    }

    public void incrementMessagesUsed() {
        String today = LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE);
        int current = getMessagesUsedToday();
        prefs.edit()
                .putInt(Constants.KEY_MESSAGES_USED, current + 1)
                .putString(Constants.KEY_MESSAGES_DATE, today)
                .apply();
    }

    public boolean hasReachedFreeLimit() {
        return getMessagesUsedToday() >= Constants.FREE_MESSAGES_LIMIT;
    }

    public int getRemainingFreeMessages() {
        return Math.max(0, Constants.FREE_MESSAGES_LIMIT - getMessagesUsedToday());
    }

    private void resetDailyMessages() {
        prefs.edit()
                .putInt(Constants.KEY_MESSAGES_USED, 0)
                .putString(Constants.KEY_MESSAGES_DATE,
                        LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE))
                .apply();
    }

    public void clearAll() {
        prefs.edit().clear().apply();
    }

    public void clearAllIncludingKey() {
        prefs.edit().clear().apply();
        securePrefs.edit().clear().apply();
    }
}

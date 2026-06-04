package com.smartone.app.ui.settings;

import android.os.Bundle;
import android.view.inputmethod.InputMethodManager;
import androidx.appcompat.app.AlertDialog;
import com.google.android.material.snackbar.Snackbar;
import com.smartone.app.R;
import com.smartone.app.SmartOneApplication;
import com.smartone.app.data.repository.HistoryRepository;
import com.smartone.app.databinding.ActivitySettingsBinding;
import com.smartone.app.util.Constants;
import com.smartone.app.util.PrefsManager;

public class SettingsActivity extends com.smartone.app.ui.BaseActivity {

    private ActivitySettingsBinding binding;
    private PrefsManager            prefsManager;
    private HistoryRepository       historyRepository;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivitySettingsBinding.inflate(getLayoutInflater());
        SmartOneApplication app = SmartOneApplication.from(getApplication());
        prefsManager      = app.container.prefsManager;
        historyRepository = app.container.historyRepository;
        setContentView(binding.getRoot());

        loadCurrentSettings();
        setupBehaviorSection();
        setupFontSelector();
        setupDataSection();
        setupBottomNav(R.id.navSettings);
    }

    private void loadCurrentSettings() {
        binding.switchAutoSave.setChecked(prefsManager.isAutoSave());
        updateFontUI(prefsManager.getFontSize());
        loadStats();
    }

    private void setupBehaviorSection() {
        binding.switchAutoSave.setOnCheckedChangeListener((btn, checked) -> {
            prefsManager.saveAutoSave(checked);
            showSnackbar(checked ? "Autoguardado activado." : "Autoguardado desactivado.");
        });
    }

    private void setupFontSelector() {
        binding.fontSmall.setOnClickListener(v ->
                selectFont(Constants.FONT_SIZE_SMALL));
        binding.fontMedium.setOnClickListener(v ->
                selectFont(Constants.FONT_SIZE_MEDIUM));
        binding.fontLarge.setOnClickListener(v ->
                selectFont(Constants.FONT_SIZE_LARGE));
    }

    private void selectFont(int size) {
        prefsManager.saveFontSize(size);
        updateFontUI(size);
        showSnackbar("Tamaño de fuente actualizado.");
    }

    private void updateFontUI(int size) {
        setOptionActive(binding.fontSmall,   size == Constants.FONT_SIZE_SMALL);
        setOptionActive(binding.fontMedium,  size == Constants.FONT_SIZE_MEDIUM);
        setOptionActive(binding.fontLarge,   size == Constants.FONT_SIZE_LARGE);
    }

    private void setupDataSection() {
        binding.rowClearCache.setOnClickListener(v -> confirmClearHistory());
    }

    private void loadStats() {
        historyRepository.getStats((total, jsonCount, chatCount) ->
                runOnUiThread(() ->
                        binding.tvStats.setText(jsonCount + " JSON · " + chatCount + " chats")));
    }

    private void confirmClearHistory() {
        new AlertDialog.Builder(this)
                .setTitle("Limpiar historial")
                .setMessage("Se eliminarán todas las entradas guardadas. Esta acción no se puede deshacer.")
                .setPositiveButton("Eliminar todo", (d, w) -> {
                    historyRepository.deleteAll();
                    loadStats();
                    showSnackbar("Historial limpiado.");
                })
                .setNegativeButton("Cancelar", null)
                .show();
    }

    private void setOptionActive(android.widget.TextView view, boolean active) {
        view.setBackgroundResource(active
                ? R.drawable.bg_model_active
                : android.R.color.transparent);
        view.setTextColor(getColor(active ? R.color.accent : R.color.text_secondary));
    }

    private void showSnackbar(String message) {
        Snackbar.make(binding.getRoot(), message, Snackbar.LENGTH_SHORT).show();
    }
}

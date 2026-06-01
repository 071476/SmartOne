package com.smartone.app.ui.settings;

import android.os.Bundle;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
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
        setupApiKeySection();
        setupModelSelector();
        setupBehaviorSection();
        setupFontSelector();
        setupDataSection();
        setupBottomNav(R.id.navSettings);
    }

    private void loadCurrentSettings() {
        boolean hasKey = prefsManager.hasApiKey();
        binding.tvApiKeyStatus.setText(hasKey ? "Configurada ✓" : "No configurada");
        binding.tvApiKeyStatus.setTextColor(
                getColor(hasKey ? R.color.success : R.color.error));
        binding.switchAutoSave.setChecked(prefsManager.isAutoSave());
        updateModelUI(prefsManager.getModel());
        updateFontUI(prefsManager.getFontSize());
        loadStats();
    }

    private void setupApiKeySection() {
        binding.btnEditApiKey.setOnClickListener(v -> {
            if (binding.etApiKey.getVisibility() == View.VISIBLE) {
                hideApiKeyField();
            } else {
                showApiKeyField();
            }
        });
        binding.btnSaveApiKey.setOnClickListener(v -> saveApiKey());
    }

    private void showApiKeyField() {
        binding.etApiKey.setVisibility(View.VISIBLE);
        binding.btnSaveApiKey.setVisibility(View.VISIBLE);
        binding.etApiKey.requestFocus();
        InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
        imm.showSoftInput(binding.etApiKey, InputMethodManager.SHOW_IMPLICIT);
    }

    private void hideApiKeyField() {
        binding.etApiKey.setVisibility(View.GONE);
        binding.btnSaveApiKey.setVisibility(View.GONE);
        binding.etApiKey.setText("");
        hideKeyboard();
    }

    private void saveApiKey() {
        String key = binding.etApiKey.getText() != null
                ? binding.etApiKey.getText().toString().trim() : "";
        if (key.isEmpty()) {
            showSnackbar("Ingresa una API key válida.");
            return;
        }
        if (!key.startsWith("sk-ant-")) {
            new AlertDialog.Builder(this)
                    .setTitle("API Key inusual")
                    .setMessage("La key no tiene el formato esperado (sk-ant-...). ¿Guardarla de todas formas?")
                    .setPositiveButton("Guardar", (d, w) -> persistApiKey(key))
                    .setNegativeButton("Cancelar", null)
                    .show();
            return;
        }
        persistApiKey(key);
    }

    private void persistApiKey(String key) {
        prefsManager.saveApiKey(key);
        SmartOneApplication.from(getApplication()).container.refreshApiConfig();
        binding.tvApiKeyStatus.setText("Configurada ✓");
        binding.tvApiKeyStatus.setTextColor(getColor(R.color.success));
        hideApiKeyField();
        showSnackbar("API key guardada correctamente.");
    }

    private void setupModelSelector() {
        binding.modelHaiku.setOnClickListener(v ->
                selectModel(Constants.CLAUDE_MODEL_FAST));
        binding.modelSonnet.setOnClickListener(v ->
                selectModel(Constants.CLAUDE_MODEL_PRO));
    }

    private void selectModel(String model) {
        prefsManager.saveModel(model);
        SmartOneApplication.from(getApplication()).container.refreshApiConfig();
        updateModelUI(model);
        String name = model.contains("haiku") ? "Haiku" : "Sonnet";
        showSnackbar("Modelo cambiado a " + name + ".");
    }

    private void updateModelUI(String model) {
        boolean isHaiku = model.equals(Constants.CLAUDE_MODEL_FAST);
        setOptionActive(binding.modelHaiku,  isHaiku);
        setOptionActive(binding.modelSonnet, !isHaiku);
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

    private void hideKeyboard() {
        InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(binding.getRoot().getWindowToken(), 0);
    }
}

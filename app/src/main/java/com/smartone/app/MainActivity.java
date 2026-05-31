package com.smartone.app;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import com.google.android.material.snackbar.Snackbar;
import com.smartone.app.databinding.ActivityMainBinding;
import com.smartone.app.ui.chat.AiChatActivity;
import com.smartone.app.ui.history.HistoryActivity;
import com.smartone.app.ui.settings.SettingsActivity;
import com.smartone.app.ui.viewer.JsonViewerActivity;
import com.smartone.app.util.Constants;
import com.smartone.app.util.PrefsManager;

public class MainActivity extends AppCompatActivity {

    private ActivityMainBinding binding;
    private PrefsManager        prefsManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding      = ActivityMainBinding.inflate(getLayoutInflater());
        prefsManager = SmartOneApplication
                .from(getApplication())
                .container
                .prefsManager;

        applyTheme();
        setContentView(binding.getRoot());

        handleFirstLaunch();
        setupBottomNavigation();
        setupQuickActions();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadQuickStats();
    }

    private void handleFirstLaunch() {
        if (prefsManager.isFirstLaunch()) {
            prefsManager.setFirstLaunchDone();
            showWelcomeBanner();
        } else if (!prefsManager.hasApiKey()) {
            showNoApiKeyBanner();
        }
    }

    private void setupBottomNavigation() {
        binding.bottomNav.setOnItemSelectedListener(item -> {
            openSection(item.getItemId());
            return true;
        });
    }

    private void openSection(int itemId) {
        Intent intent = null;
        if (itemId == R.id.navViewer) {
            intent = new Intent(this, JsonViewerActivity.class);
        } else if (itemId == R.id.navHistory) {
            intent = new Intent(this, HistoryActivity.class);
        } else if (itemId == R.id.navChat) {
            intent = new Intent(this, AiChatActivity.class);
        } else if (itemId == R.id.navSettings) {
            intent = new Intent(this, SettingsActivity.class);
        }
        if (intent != null) {
            intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
            startActivity(intent);
        }
    }

    private void setupQuickActions() {
        binding.cardViewer.setOnClickListener(v ->
                binding.bottomNav.setSelectedItemId(R.id.navViewer));
        binding.cardHistory.setOnClickListener(v ->
                binding.bottomNav.setSelectedItemId(R.id.navHistory));
        binding.cardChat.setOnClickListener(v ->
                binding.bottomNav.setSelectedItemId(R.id.navChat));
        binding.cardSettings.setOnClickListener(v ->
                binding.bottomNav.setSelectedItemId(R.id.navSettings));
    }

    private void applyTheme() {
        String theme = prefsManager.getTheme();
        int mode = theme.equals(Constants.THEME_LIGHT)
                ? AppCompatDelegate.MODE_NIGHT_NO
                : AppCompatDelegate.MODE_NIGHT_YES;
        AppCompatDelegate.setDefaultNightMode(mode);
    }

    private void showWelcomeBanner() {
        binding.bannerWelcome.setVisibility(View.VISIBLE);
        binding.tvBannerMessage.setText(
                "Bienvenido a SmartOne. Configura tu API key en Ajustes para comenzar.");
        binding.btnBannerAction.setText("Configurar");
        binding.btnBannerAction.setOnClickListener(v -> {
            hideBanner();
            binding.bottomNav.setSelectedItemId(R.id.navSettings);
        });
        binding.btnBannerClose.setOnClickListener(v -> hideBanner());
    }

    private void showNoApiKeyBanner() {
        binding.bannerWelcome.setVisibility(View.VISIBLE);
        binding.tvBannerMessage.setText(
                "Sin API key. El chat no funcionará hasta que la configures.");
        binding.btnBannerAction.setText("Ir a Ajustes");
        binding.btnBannerAction.setOnClickListener(v -> {
            hideBanner();
            binding.bottomNav.setSelectedItemId(R.id.navSettings);
        });
        binding.btnBannerClose.setOnClickListener(v -> hideBanner());
    }

    private void hideBanner() {
        binding.bannerWelcome.setVisibility(View.GONE);
    }

    private void loadQuickStats() {
        SmartOneApplication
                .from(getApplication())
                .container
                .historyRepository
                .getStats((total, jsonCount, chatCount) ->
                        runOnUiThread(() -> {
                            binding.tvStatJson.setText(jsonCount + "\nJSON");
                            binding.tvStatChat.setText(chatCount + "\nChats");
                            binding.tvStatTotal.setText(total + "\nTotal");
                        })
                );
    }
}

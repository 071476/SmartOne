package com.smartone.app.ui;

import android.content.Intent;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.smartone.app.R;
import com.smartone.app.ui.chat.AiChatActivity;
import com.smartone.app.ui.history.HistoryActivity;
import com.smartone.app.ui.settings.SettingsActivity;
import com.smartone.app.ui.viewer.JsonViewerActivity;

public abstract class BaseActivity extends AppCompatActivity {

    protected BottomNavigationView bottomNav;
    private int currentNavId = -1;

    protected void setupBottomNav(int selectedItemId) {
        this.currentNavId = selectedItemId;
        bottomNav = findViewById(R.id.bottomNav);
        if (bottomNav == null) return;

        // Marcar el item actual sin disparar el listener
        bottomNav.setOnItemSelectedListener(null);
        bottomNav.setSelectedItemId(selectedItemId);

        bottomNav.setOnItemSelectedListener(item -> {
            int id = item.getItemId();

            if (id == currentNavId) return true;

            Intent intent = null;

            if (id == R.id.navViewer) {
                intent = new Intent(this, JsonViewerActivity.class);
            } else if (id == R.id.navHistory) {
                intent = new Intent(this, HistoryActivity.class);
            } else if (id == R.id.navChat) {
                intent = new Intent(this, AiChatActivity.class);
            } else if (id == R.id.navSettings) {
                intent = new Intent(this, SettingsActivity.class);
            }

            if (intent != null) {
                intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                startActivity(intent);
                overridePendingTransition(0, 0);
            }

            return true;
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Al volver a esta pantalla, re-marcar su boton correcto
        if (bottomNav != null && currentNavId != -1) {
            bottomNav.setOnItemSelectedListener(null);
            bottomNav.setSelectedItemId(currentNavId);
            setupListener();
        }
    }

    private void setupListener() {
        bottomNav.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == currentNavId) return true;

            Intent intent = null;
            if (id == R.id.navViewer) {
                intent = new Intent(this, JsonViewerActivity.class);
            } else if (id == R.id.navHistory) {
                intent = new Intent(this, HistoryActivity.class);
            } else if (id == R.id.navChat) {
                intent = new Intent(this, AiChatActivity.class);
            } else if (id == R.id.navSettings) {
                intent = new Intent(this, SettingsActivity.class);
            }

            if (intent != null) {
                intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                startActivity(intent);
                overridePendingTransition(0, 0);
            }
            return true;
        });
    }
}

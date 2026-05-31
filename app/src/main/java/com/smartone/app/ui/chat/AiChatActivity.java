package com.smartone.app.ui.chat;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import com.google.android.material.snackbar.Snackbar;
import com.smartone.app.databinding.ActivityAiChatBinding;
import com.smartone.app.ui.settings.SettingsActivity;
import com.smartone.app.util.Constants;

public class AiChatActivity extends AppCompatActivity {

    private ActivityAiChatBinding binding;
    private ChatViewModel          viewModel;
    private ChatMessageAdapter     adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        try {
            binding   = ActivityAiChatBinding.inflate(getLayoutInflater());
            viewModel = new ViewModelProvider(this).get(ChatViewModel.class);
            setContentView(binding.getRoot());

            setupRecyclerView();
            setupClickListeners();
            observeViewModel();
            checkApiKey();
            handleIncomingJson();

        } catch (Exception e) {
            android.util.Log.e("SmartOne", "Error en AiChatActivity: " + e.getMessage(), e);
            if (binding != null) {
                setContentView(binding.getRoot());
                Snackbar.make(binding.getRoot(),
                        "Error al cargar el chat: " + e.getMessage(),
                        Snackbar.LENGTH_LONG).show();
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        try {
            if (viewModel != null) {
                viewModel.reloadConfig();
                updateStatusIndicator();
            }
        } catch (Exception e) {
            android.util.Log.e("SmartOne", "Error en onResume: " + e.getMessage());
        }
    }

    private void setupRecyclerView() {
        adapter = new ChatMessageAdapter();
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setStackFromEnd(true);
        binding.rvMessages.setLayoutManager(layoutManager);
        binding.rvMessages.setAdapter(adapter);
        binding.rvMessages.setItemAnimator(null);
        viewModel.addWelcomeMessage();
    }

    private void setupClickListeners() {
        binding.btnSend.setOnClickListener(v -> sendMessage());
        binding.etInput.setOnEditorActionListener((v, actionId, event) -> {
            sendMessage();
            return true;
        });
        binding.btnNewChat.setOnClickListener(v -> confirmNewChat());
    }

    private void sendMessage() {
        String text = binding.etInput.getText() != null
                ? binding.etInput.getText().toString().trim()
                : "";
        if (text.isEmpty()) return;
        binding.etInput.setText("");
        viewModel.sendMessage(text);
    }

    private void observeViewModel() {
        viewModel.getMessages().observe(this, messages -> {
            adapter.submitList(messages);
            if (!messages.isEmpty()) {
                binding.rvMessages.smoothScrollToPosition(messages.size() - 1);
            }
        });

        viewModel.getIsLoading().observe(this, loading -> {
            if (loading != null) {
                if (loading) showLoadingIndicator();
                else hideLoadingIndicator();
            }
        });

        viewModel.getErrorEvent().observe(this, error -> {
            if (error != null && !error.isEmpty()) {
                showError(error);
            }
        });

        viewModel.getRemainingMessages().observe(this, remaining -> {
            if (remaining != null) {
                binding.tvRemainingMessages.setText(remaining + " left");
                binding.tvRemainingMessages.setVisibility(
                        remaining < Constants.FREE_MESSAGES_LIMIT
                                ? View.VISIBLE : View.GONE);
            }
        });
    }

    private void showLoadingIndicator() {
        binding.loadingIndicator.setVisibility(View.VISIBLE);
        animateDot(binding.dot1, 0);
        animateDot(binding.dot2, 150);
        animateDot(binding.dot3, 300);
    }

    private void hideLoadingIndicator() {
        binding.loadingIndicator.setVisibility(View.GONE);
        binding.dot1.clearAnimation();
        binding.dot2.clearAnimation();
        binding.dot3.clearAnimation();
    }

    private void animateDot(View dot, long delay) {
        TranslateAnimation move = new TranslateAnimation(0, 0, 0, -8);
        move.setDuration(400);
        move.setRepeatMode(Animation.REVERSE);
        move.setRepeatCount(Animation.INFINITE);
        move.setStartOffset(delay);
        dot.startAnimation(move);
    }

    private void checkApiKey() {
        if (viewModel != null && !viewModel.isApiKeyConfigured()) {
            showApiKeyWarning();
        }
    }

    private void showApiKeyWarning() {
        Snackbar.make(
                binding.getRoot(),
                "Configura tu API key en Ajustes para usar el chat.",
                Snackbar.LENGTH_INDEFINITE
        ).setAction("Ajustes", v -> openSettings()).show();
    }

    private void updateStatusIndicator() {
        if (viewModel == null || binding == null) return;
        boolean configured = viewModel.isApiKeyConfigured();
        binding.tvStatus.setText(configured ? "Conectado" : "Sin configurar");
        binding.statusDot.getBackground().setTint(
                getColor(configured
                        ? android.R.color.holo_green_dark
                        : android.R.color.holo_red_dark));
    }

    private void handleIncomingJson() {
        String json = getIntent().getStringExtra(Constants.EXTRA_JSON_CONTENT);
        if (json != null && !json.isEmpty() && viewModel != null) {
            viewModel.analyzeJson(json);
        }
    }

    private void confirmNewChat() {
        new AlertDialog.Builder(this)
                .setTitle("Nueva conversación")
                .setMessage("Se borrará el historial de esta sesión. ¿Continuar?")
                .setPositiveButton("Sí, borrar", (d, w) -> {
                    viewModel.clearMessages();
                    viewModel.addWelcomeMessage();
                })
                .setNegativeButton("Cancelar", null)
                .show();
    }

    private void openSettings() {
        startActivity(new Intent(this, SettingsActivity.class));
    }

    private void showError(String message) {
        if (binding == null) return;
        Snackbar snackbar = Snackbar.make(
                binding.getRoot(), message, Snackbar.LENGTH_LONG);
        if (message.contains("API key") || message.contains("Ajustes")) {
            snackbar.setAction("Ajustes", v -> openSettings());
        }
        snackbar.show();
    }
}

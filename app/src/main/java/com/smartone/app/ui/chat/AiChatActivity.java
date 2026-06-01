package com.smartone.app.ui.chat;

import android.os.Bundle;
import android.view.View;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import com.google.android.material.snackbar.Snackbar;
import com.smartone.app.databinding.ActivityAiChatBinding;
import com.smartone.app.util.Constants;

public class AiChatActivity extends com.smartone.app.ui.BaseActivity {

    private ActivityAiChatBinding binding;
    private ChatViewModel viewModel;
    private ChatMessageAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityAiChatBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        viewModel = new ViewModelProvider(this).get(ChatViewModel.class);
        setupRecyclerView();
        setupClicks();
        observeViewModel();
        setupBottomNav(R.id.navChat);
    }

    private void setupRecyclerView() {
        adapter = new ChatMessageAdapter();
        LinearLayoutManager lm = new LinearLayoutManager(this);
        lm.setStackFromEnd(true);
        binding.rvMessages.setLayoutManager(lm);
        binding.rvMessages.setAdapter(adapter);
        binding.rvMessages.setItemAnimator(null);
        viewModel.addWelcomeMessage();
    }

    private void setupClicks() {
        binding.btnSend.setOnClickListener(v -> {
            String text = binding.etInput.getText() != null
                    ? binding.etInput.getText().toString().trim() : "";
            if (!text.isEmpty()) {
                binding.etInput.setText("");
                viewModel.sendMessage(text);
            }
        });

        binding.btnNewChat.setOnClickListener(v -> {
            viewModel.clearMessages();
            viewModel.addWelcomeMessage();
        });
    }

    private void observeViewModel() {
        viewModel.getMessages().observe(this, messages -> {
            if (messages != null) {
                adapter.submitList(messages);
                if (!messages.isEmpty()) {
                    binding.rvMessages.smoothScrollToPosition(
                            messages.size() - 1);
                }
            }
        });

        viewModel.getIsLoading().observe(this, loading -> {
            if (loading != null) {
                binding.loadingIndicator.setVisibility(
                        loading ? View.VISIBLE : View.GONE);
            }
        });

        viewModel.getErrorEvent().observe(this, error -> {
            if (error != null && !error.isEmpty()) {
                Snackbar.make(binding.getRoot(), error,
                        Snackbar.LENGTH_LONG).show();
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
}

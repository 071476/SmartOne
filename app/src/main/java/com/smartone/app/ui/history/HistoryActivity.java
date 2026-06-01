package com.smartone.app.ui.history;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.LiveData;
import androidx.recyclerview.widget.LinearLayoutManager;
import com.google.android.material.snackbar.Snackbar;
import com.smartone.app.R;
import com.smartone.app.SmartOneApplication;
import com.smartone.app.data.local.HistoryEntry;
import com.smartone.app.data.repository.HistoryRepository;
import com.smartone.app.databinding.ActivityHistoryBinding;
import com.smartone.app.ui.chat.AiChatActivity;
import com.smartone.app.ui.viewer.JsonViewerActivity;
import com.smartone.app.util.Constants;
import java.util.List;

public class HistoryActivity extends com.smartone.app.ui.BaseActivity {

    private ActivityHistoryBinding binding;
    private HistoryRepository      repository;
    private HistoryAdapter         adapter;

    private enum Filter { ALL, JSON, CHAT, FAVORITES }
    private Filter currentFilter = Filter.ALL;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding    = ActivityHistoryBinding.inflate(getLayoutInflater());
        repository = SmartOneApplication
                .from(getApplication())
                .container
                .historyRepository;
        setContentView(binding.getRoot());

        setupRecyclerView();
        setupSearch();
        setupFilters();
        setupClickListeners();
        observeEntries(Filter.ALL);
        setupBottomNav(R.id.navHistory);
    }

    private void setupRecyclerView() {
        adapter = new HistoryAdapter(
                this::onEntryClick,
                this::onEntryLongClick,
                this::onFavoriteClick
        );
        binding.rvHistory.setLayoutManager(new LinearLayoutManager(this));
        binding.rvHistory.setAdapter(adapter);
        binding.rvHistory.setItemAnimator(null);
    }

    private void setupSearch() {
        binding.etSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String query = s.toString().trim();
                if (query.isEmpty()) {
                    observeEntries(currentFilter);
                } else {
                    observeSearch(query);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });
    }

    private void setupFilters() {
        binding.filterAll.setOnClickListener(v -> applyFilter(Filter.ALL));
        binding.filterJson.setOnClickListener(v -> applyFilter(Filter.JSON));
        binding.filterChat.setOnClickListener(v -> applyFilter(Filter.CHAT));
        binding.filterFavorites.setOnClickListener(v -> applyFilter(Filter.FAVORITES));
    }

    private void applyFilter(Filter filter) {
        currentFilter = filter;
        binding.etSearch.setText("");
        updateFilterUI(filter);
        observeEntries(filter);
    }

    private void updateFilterUI(Filter active) {
        setFilterStyle(binding.filterAll,       active == Filter.ALL);
        setFilterStyle(binding.filterJson,      active == Filter.JSON);
        setFilterStyle(binding.filterChat,      active == Filter.CHAT);
        setFilterStyle(binding.filterFavorites, active == Filter.FAVORITES);
    }

    private void setFilterStyle(android.widget.TextView pill, boolean active) {
        pill.setBackgroundResource(active
                ? R.drawable.bg_filter_active
                : R.drawable.bg_filter_inactive);
        pill.setTextColor(getColor(active
                ? R.color.accent
                : R.color.text_secondary));
    }

    private void observeEntries(Filter filter) {
        LiveData<List<HistoryEntry>> liveData;
        switch (filter) {
            case JSON:      liveData = repository.getByType(Constants.HISTORY_TYPE_JSON); break;
            case CHAT:      liveData = repository.getByType(Constants.HISTORY_TYPE_CHAT); break;
            case FAVORITES: liveData = repository.getFavorites(); break;
            default:        liveData = repository.getAll(); break;
        }
        liveData.observe(this, this::updateList);
    }

    private void observeSearch(String query) {
        repository.search(query).observe(this, this::updateList);
    }

    private void updateList(List<HistoryEntry> entries) {
        boolean empty = entries == null || entries.isEmpty();
        binding.emptyState.setVisibility(empty ? View.VISIBLE : View.GONE);
        binding.rvHistory.setVisibility(empty ? View.GONE : View.VISIBLE);
        int count = entries != null ? entries.size() : 0;
        binding.tvEntryCount.setText(
                count == 1 ? "1 entrada" : count + " entradas");
        if (!empty) adapter.submitList(entries);
    }

    private void setupClickListeners() {
        binding.btnDeleteAll.setOnClickListener(v -> confirmDeleteAll());
    }

    private void onEntryClick(HistoryEntry entry) {
        if (entry.isJson()) {
            Intent intent = new Intent(this, JsonViewerActivity.class);
            intent.putExtra(Constants.EXTRA_JSON_CONTENT, entry.content);
            startActivity(intent);
        } else {
            Intent intent = new Intent(this, AiChatActivity.class);
            intent.putExtra(Constants.EXTRA_JSON_CONTENT, entry.content);
            startActivity(intent);
        }
    }

    private void onEntryLongClick(HistoryEntry entry) {
        String[] options = {"Eliminar", "Copiar contenido"};
        new AlertDialog.Builder(this)
                .setTitle(entry.title)
                .setItems(options, (dialog, which) -> {
                    if (which == 0) confirmDelete(entry);
                    else copyEntryContent(entry);
                })
                .show();
    }

    private void onFavoriteClick(HistoryEntry entry) {
        boolean newState = !entry.isFavorite;
        repository.setFavorite(entry.id, newState);
        showSnackbar(newState ? "Marcado como favorito" : "Eliminado de favoritos");
    }

    private void confirmDelete(HistoryEntry entry) {
        new AlertDialog.Builder(this)
                .setTitle("Eliminar entrada")
                .setMessage("¿Eliminar \"" + entry.title + "\"?")
                .setPositiveButton("Eliminar", (d, w) -> {
                    repository.delete(entry);
                    showSnackbar("Entrada eliminada.");
                })
                .setNegativeButton("Cancelar", null)
                .show();
    }

    private void confirmDeleteAll() {
        new AlertDialog.Builder(this)
                .setTitle("Limpiar historial")
                .setMessage("Se eliminarán todas las entradas. Esta acción no se puede deshacer.")
                .setPositiveButton("Eliminar todo", (d, w) -> {
                    repository.deleteAll();
                    showSnackbar("Historial limpiado.");
                })
                .setNegativeButton("Cancelar", null)
                .show();
    }

    private void copyEntryContent(HistoryEntry entry) {
        android.content.ClipboardManager clipboard =
                (android.content.ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
        clipboard.setPrimaryClip(
                android.content.ClipData.newPlainText(entry.title, entry.content));
        showSnackbar("Contenido copiado.");
    }

    private void showSnackbar(String message) {
        Snackbar.make(binding.getRoot(), message, Snackbar.LENGTH_SHORT).show();
    }
}

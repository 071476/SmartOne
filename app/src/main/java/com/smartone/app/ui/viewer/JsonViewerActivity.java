package com.smartone.app.ui.viewer;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import com.google.android.material.snackbar.Snackbar;
import com.smartone.app.R;
import com.smartone.app.SmartOneApplication;
import com.smartone.app.data.repository.HistoryRepository;
import com.smartone.app.databinding.ActivityJsonViewerBinding;
import com.smartone.app.R;
import com.smartone.app.parser.JsonParser;
import com.smartone.app.ui.chat.AiChatActivity;
import com.smartone.app.util.Constants;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

public class JsonViewerActivity extends com.smartone.app.ui.BaseActivity {

    private ActivityJsonViewerBinding binding;
    private JsonLineAdapter            adapter;
    private HistoryRepository          historyRepository;
    private ActivityResultLauncher<String[]> openFileLauncher;

    private String  currentJson     = "";
    private String  currentFileName = "sin_archivo.json";
    private boolean isPreviewMode   = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityJsonViewerBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        historyRepository = SmartOneApplication
                .from(getApplication())
                .container
                .historyRepository;

        setupRecyclerView();
        setupFilePicker();
        setupClickListeners();
        setupTabs();
        setupBottomNav(R.id.navViewer);

        String incomingJson = getIntent().getStringExtra(Constants.EXTRA_JSON_CONTENT);
        if (incomingJson != null && !incomingJson.isEmpty()) {
            currentJson = incomingJson;
            validateCurrentJson();
        }
    }

    private void setupRecyclerView() {
        adapter = new JsonLineAdapter();
        binding.rvJsonLines.setLayoutManager(new LinearLayoutManager(this));
        binding.rvJsonLines.setAdapter(adapter);
        binding.rvJsonLines.setItemAnimator(null);
    }

    private void setupFilePicker() {
        openFileLauncher = registerForActivityResult(
                new ActivityResultContracts.OpenDocument(),
                uri -> {
                    if (uri == null) return;
                    loadFileFromUri(uri);
                }
        );
    }

    private void setupClickListeners() {
        binding.btnLoadFile.setOnClickListener(v ->
                openFileLauncher.launch(new String[]{
                        "application/json", "text/plain", "*/*"}));
        binding.btnInfo.setOnClickListener(v -> showInfoDialog());
        binding.btnValidate.setOnClickListener(v -> validateCurrentJson());
        binding.btnAnalyzeAI.setOnClickListener(v -> analyzeWithAI());
        binding.btnCopy.setOnClickListener(v -> copyToClipboard());
        binding.btnClear.setOnClickListener(v -> clearAll());
        binding.btnPerformance.setOnClickListener(v -> analyzePerformance());
    }

    private void setupTabs() {
        binding.tabEditor.setOnClickListener(v -> switchToEditor());
        binding.tabPreview.setOnClickListener(v -> switchToPreview());
    }

    private void switchToEditor() {
        isPreviewMode = false;
        binding.editorContainer.setVisibility(View.VISIBLE);
        binding.scrollPreview.setVisibility(View.GONE);
        binding.tabEditor.setBackgroundResource(R.drawable.bg_tab_active);
        binding.tabEditor.setTextColor(getColor(R.color.accent));
        binding.tabPreview.setBackgroundResource(R.drawable.bg_tab_inactive);
        binding.tabPreview.setTextColor(getColor(R.color.text_secondary));
    }

    private void switchToPreview() {
        if (currentJson.isEmpty()) {
            showSnackbar("Carga o pega un JSON primero.");
            return;
        }
        isPreviewMode = true;
        binding.editorContainer.setVisibility(View.GONE);
        binding.scrollPreview.setVisibility(View.VISIBLE);
        binding.tabEditor.setBackgroundResource(R.drawable.bg_tab_inactive);
        binding.tabEditor.setTextColor(getColor(R.color.text_secondary));
        binding.tabPreview.setBackgroundResource(R.drawable.bg_tab_active);
        binding.tabPreview.setTextColor(getColor(R.color.accent));
    }

    private void loadFileFromUri(Uri uri) {
        try (InputStream stream = getContentResolver().openInputStream(uri)) {
            if (stream == null) {
                showSnackbar("No se pudo abrir el archivo.");
                return;
            }
            String content  = readStream(stream);
            currentFileName = resolveFileName(uri);
            currentJson     = content;
            binding.tvFileName.setText(currentFileName);
            binding.tvFileInfo.setText(currentFileName
                    + " · " + formatSize(content.length()));
            validateCurrentJson();
        } catch (IOException e) {
            showSnackbar("Error al leer el archivo: " + e.getMessage());
        }
    }

    private String readStream(InputStream stream) throws IOException {
        BufferedReader reader = new BufferedReader(new InputStreamReader(stream));
        StringBuilder sb = new StringBuilder();
        String line;
        while ((line = reader.readLine()) != null) {
            sb.append(line).append("\n");
        }
        return sb.toString();
    }

    private String resolveFileName(Uri uri) {
        String path = uri.getLastPathSegment();
        if (path != null && path.contains("/")) {
            path = path.substring(path.lastIndexOf("/") + 1);
        }
        return path != null ? path : "archivo.json";
    }

    private void validateCurrentJson() {
        if (binding.etJsonInput.getText() != null && !binding.etJsonInput.getText().toString().trim().isEmpty()) {
            currentJson = binding.etJsonInput.getText().toString();
        }
        if (currentJson.trim().isEmpty()) {
            showSnackbar("Carga o pega un JSON para validar.");
            return;
        }
        JsonParser.ParseResult result = JsonParser.parse(currentJson);
        if (result.isValid) {
            onValidJson(result);
        } else {
            onInvalidJson(result);
        }
    }

    private void onValidJson(JsonParser.ParseResult result) {
        showStatus("✓ JSON válido", getColor(R.color.success));
        binding.tvJsonInfo.setText(result.info.getSummary());
        binding.tvJsonInfo.setVisibility(View.VISIBLE);
        adapter.submitList(JsonParser.buildLines(currentJson, -1));
        binding.tvPreview.setText(result.formatted);
        updateLineNumbers(result.formatted);
        historyRepository.saveJson(currentFileName, currentJson);
        binding.btnAnalyzeAI.setEnabled(true);
    }

    private void onInvalidJson(JsonParser.ParseResult result) {
        String msg = "✗ Error en línea " + result.errorLine
                + ", columna " + result.errorColumn
                + ": " + result.errorMessage;
        showStatus(msg, getColor(R.color.error));
        binding.tvJsonInfo.setVisibility(View.GONE);
        adapter.submitList(JsonParser.buildLines(currentJson, result.errorLine));
        binding.tvPreview.setText("");
        binding.btnAnalyzeAI.setEnabled(false);
    }

    private void updateLineNumbers(String text) {
        if (text == null || text.isEmpty()) {
            binding.tvLineNumbers.setText("");
            return;
        }
        String[] lines = text.split("\\n", -1);
        StringBuilder numbers = new StringBuilder();
        for (int i = 1; i <= lines.length; i++) {
            numbers.append(i).append("\n");
        }
        binding.tvLineNumbers.setText(numbers.toString());
    }

    private void openChatWithJson() {
        if (currentJson.isEmpty()) {
            showSnackbar("No hay JSON cargado para analizar.");
            return;
        }
        Intent intent = new Intent(this, AiChatActivity.class);
        intent.putExtra(Constants.EXTRA_JSON_CONTENT, currentJson);
        startActivity(intent);
    }

    private void copyToClipboard() {
        if (currentJson.isEmpty()) {
            showSnackbar("No hay JSON para copiar.");
            return;
        }
        ClipboardManager clipboard =
                (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
        clipboard.setPrimaryClip(
                ClipData.newPlainText("json", currentJson));
        showSnackbar("JSON copiado al portapapeles.");
    }

    private void clearAll() {
        currentJson = "";
        currentFileName = "sin_archivo.json";
        binding.etJsonInput.setText("");
        binding.tvFileName.setText("sin_archivo.json");
        binding.tvFileInfo.setText("Sin archivo cargado");
        binding.tvStatus.setVisibility(android.view.View.GONE);
        binding.tvJsonInfo.setVisibility(android.view.View.GONE);
        binding.tvPreview.setText("");
        adapter.submitList(new java.util.ArrayList<>());
        binding.btnAnalyzeAI.setEnabled(false);
        showSnackbar("Limpiado.");
    }

    private void analyzeWithAI() {
        if (currentJson.isEmpty()) {
            showSnackbar("Carga o pega un JSON primero.");
            return;
        }
        showSnackbar("Analizando con IA...");
        SmartOneApplication.from(getApplication())
                .container
                .apiClient
                .sendOneShot(
                        "Analiza este JSON. Describe su estructura, los campos principales y sus tipos, y detecta cualquier inconsistencia o mejora posible. Responde en espanol:\n\n" + currentJson,
                        new com.smartone.app.data.remote.ApiClient.Callback() {
                            @Override
                            public void onSuccess(String reply) {
                                runOnUiThread(() -> showAnalysisDialog(reply));
                            }
                            @Override
                            public void onError(String error) {
                                runOnUiThread(() -> showSnackbar("Error: " + error));
                            }
                        }
                );
    }

    private void showAnalysisDialog(String report) {
        new androidx.appcompat.app.AlertDialog.Builder(this)
                .setTitle("\uD83E\uDD16 Analisis con IA")
                .setMessage(report)
                .setPositiveButton("Cerrar", null)
                .setNeutralButton("Copiar", (d, w) -> {
                    android.content.ClipboardManager clipboard =
                            (android.content.ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
                    clipboard.setPrimaryClip(
                            android.content.ClipData.newPlainText("analisis", report));
                    showSnackbar("Analisis copiado.");
                })
                .show();
    }

    private void analyzePerformance() {
        if (currentJson.isEmpty()) {
            showSnackbar("Carga o pega un JSON primero.");
            return;
        }
        showSnackbar("Analizando rendimiento...");
        SmartOneApplication.from(getApplication())
                .container
                .apiClient
                .sendOneShot(
                        Constants.PERFORMANCE_PROMPT + "\n\nJSON a analizar:\n" + currentJson,
                        new com.smartone.app.data.remote.ApiClient.Callback() {
                            @Override
                            public void onSuccess(String reply) {
                                runOnUiThread(() -> showPerformanceDialog(reply));
                            }
                            @Override
                            public void onError(String error) {
                                runOnUiThread(() -> showSnackbar("Error: " + error));
                            }
                        }
                );
    }

    private void showPerformanceDialog(String report) {
        new androidx.appcompat.app.AlertDialog.Builder(this)
                .setTitle("⚡ Reporte de Rendimiento")
                .setMessage(report)
                .setPositiveButton("Cerrar", null)
                .setNeutralButton("Copiar", (d, w) -> {
                    android.content.ClipboardManager clipboard =
                            (android.content.ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
                    clipboard.setPrimaryClip(
                            android.content.ClipData.newPlainText("reporte", report));
                    showSnackbar("Reporte copiado.");
                })
                .show();
    }

    private void showInfoDialog() {
        new AlertDialog.Builder(this)
                .setTitle("Archivos compatibles")
                .setMessage("SmartOne puede abrir:\n\n" +
                        "• Archivos .json\n" +
                        "• Archivos .txt con contenido JSON\n\n" +
                        "Si el JSON contiene errores, se resaltará " +
                        "la línea exacta donde falla.")
                .setPositiveButton("Entendido", null)
                .show();
    }

    private void showStatus(String message, int color) {
        binding.tvStatus.setText(message);
        binding.tvStatus.setTextColor(color);
        binding.tvStatus.setVisibility(View.VISIBLE);
    }

    private void showSnackbar(String message) {
        Snackbar.make(binding.getRoot(), message,
                Snackbar.LENGTH_SHORT).show();
    }

    private String formatSize(int chars) {
        if (chars < 1024) return chars + " B";
        return (chars / 1024) + " KB";
    }
}

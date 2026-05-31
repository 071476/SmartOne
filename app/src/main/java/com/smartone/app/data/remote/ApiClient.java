package com.smartone.app.data.remote;

import com.smartone.app.util.Constants;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class ApiClient {

    private static final MediaType JSON_TYPE =
            MediaType.get("application/json; charset=utf-8");

    private String apiKey;
    private String model;
    private final OkHttpClient httpClient;
    private final ExecutorService executor;
    private final List<JSONObject> conversationHistory;

    public interface Callback {
        void onSuccess(String reply);
        void onError(String errorMessage);
    }

    public ApiClient() {
        this.apiKey = "";
        this.model  = Constants.CLAUDE_MODEL_FAST;
        this.httpClient = new OkHttpClient.Builder()
                .connectTimeout(15, TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.SECONDS)
                .writeTimeout(15, TimeUnit.SECONDS)
                .build();
        this.executor = Executors.newSingleThreadExecutor();
        this.conversationHistory = new ArrayList<>();
    }

    public void setApiKey(String apiKey) {
        this.apiKey = apiKey != null ? apiKey.trim() : "";
    }

    public void setModel(String model) {
        this.model = model != null ? model : Constants.CLAUDE_MODEL_FAST;
    }

    public boolean isConfigured() {
        return apiKey != null && !apiKey.isEmpty();
    }

    public void sendMessage(String userMessage, Callback callback) {
        if (!isConfigured()) {
            callback.onError("API key no configurada. Ve a Ajustes para agregarla.");
            return;
        }
        executor.execute(() -> {
            try {
                addToHistory("user", userMessage);
                String body     = buildRequestBody(conversationHistory);
                String response = executeRequest(body);
                String reply    = parseReply(response);
                addToHistory("assistant", reply);
                callback.onSuccess(reply);
            } catch (IOException e) {
                removeLastMessage();
                callback.onError("Sin conexión. Verifica tu red e intenta de nuevo.");
            } catch (JSONException e) {
                removeLastMessage();
                callback.onError("Error al procesar la respuesta de Claude.");
            } catch (ApiException e) {
                removeLastMessage();
                callback.onError(e.getMessage());
            }
        });
    }

    public void sendOneShot(String userMessage, Callback callback) {
        if (!isConfigured()) {
            callback.onError("API key no configurada. Ve a Ajustes para agregarla.");
            return;
        }
        executor.execute(() -> {
            try {
                List<JSONObject> single = new ArrayList<>();
                single.add(buildMessage("user", userMessage));
                String body     = buildRequestBody(single);
                String response = executeRequest(body);
                callback.onSuccess(parseReply(response));
            } catch (IOException e) {
                callback.onError("Sin conexión. Verifica tu red e intenta de nuevo.");
            } catch (JSONException e) {
                callback.onError("Error al procesar la respuesta de Claude.");
            } catch (ApiException e) {
                callback.onError(e.getMessage());
            }
        });
    }

    public void clearHistory() {
        conversationHistory.clear();
    }

    public int getHistorySize() {
        return conversationHistory.size();
    }

    private String buildRequestBody(List<JSONObject> messages) throws JSONException {
        JSONArray arr = new JSONArray();
        for (JSONObject msg : messages) arr.put(msg);
        JSONObject body = new JSONObject();
        body.put("model",      model);
        body.put("max_tokens", Constants.CLAUDE_MAX_TOKENS);
        body.put("system",     Constants.SYSTEM_PROMPT);
        body.put("messages",   arr);
        return body.toString();
    }

    private String executeRequest(String jsonBody) throws IOException, ApiException {
        Request request = new Request.Builder()
                .url(Constants.CLAUDE_BASE_URL)
                .post(RequestBody.create(jsonBody, JSON_TYPE))
                .addHeader("x-api-key",         apiKey)
                .addHeader("anthropic-version", Constants.CLAUDE_API_VERSION)
                .addHeader("content-type",      "application/json")
                .build();
        try (Response response = httpClient.newCall(request).execute()) {
            String responseBody = response.body() != null
                    ? response.body().string() : "";
            if (!response.isSuccessful()) {
                throw new ApiException(parseApiError(responseBody, response.code()));
            }
            return responseBody;
        }
    }

    private String parseReply(String responseBody) throws JSONException {
        JSONObject json = new JSONObject(responseBody);
        return json.getJSONArray("content")
                   .getJSONObject(0)
                   .getString("text");
    }

    private String parseApiError(String errorBody, int httpCode) {
        try {
            JSONObject json = new JSONObject(errorBody);
            if (json.has("error")) {
                String message = json.getJSONObject("error")
                                     .optString("message", "Error desconocido.");
                return buildUserFriendlyError(httpCode, message);
            }
        } catch (JSONException ignored) {}
        return buildUserFriendlyError(httpCode, errorBody);
    }

    private String buildUserFriendlyError(int httpCode, String raw) {
        switch (httpCode) {
            case 401: return "API key inválida. Verifica en Ajustes.";
            case 403: return "Sin acceso. Revisa los permisos de tu API key.";
            case 429: return "Límite de solicitudes alcanzado. Espera un momento.";
            case 500:
            case 529: return "El servidor de Claude no está disponible.";
            default:  return "Error " + httpCode + ": " + raw;
        }
    }

    private void addToHistory(String role, String content) throws JSONException {
        conversationHistory.add(buildMessage(role, content));
    }

    private JSONObject buildMessage(String role, String content) throws JSONException {
        JSONObject msg = new JSONObject();
        msg.put("role",    role);
        msg.put("content", content);
        return msg;
    }

    private void removeLastMessage() {
        if (!conversationHistory.isEmpty()) {
            conversationHistory.remove(conversationHistory.size() - 1);
        }
    }

    public static class ApiException extends Exception {
        public ApiException(String message) {
            super(message);
        }
    }
}

package com.smartone.app.data.remote;

import com.smartone.app.util.Constants;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.io.IOException;
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

    // URL del backend de SmartOne en Railway
    private static final String BACKEND_URL =
            "https://smartone-backend-production.up.railway.app/chat";

    private String model;
    private String deviceId = "";
    private final OkHttpClient httpClient;
    private final ExecutorService executor;

    public interface Callback {
        void onSuccess(String reply);
        void onError(String errorMessage);
    }

    public ApiClient() {
        this.model = "haiku";
        this.httpClient = new OkHttpClient.Builder()
                .connectTimeout(15, TimeUnit.SECONDS)
                .readTimeout(60, TimeUnit.SECONDS)
                .writeTimeout(30, TimeUnit.SECONDS)
                .build();
        this.executor = Executors.newCachedThreadPool();
    }

    // Ya no necesita API key porque el backend la tiene
    public void setApiKey(String apiKey) {
        // No-op: la key vive en el backend
    }

    public void setModel(String model) {
        if (model != null && model.toLowerCase().contains("sonnet")) {
            this.model = "sonnet";
        } else {
            this.model = "haiku";
        }
    }

    public void setDeviceId(String id) {
        this.deviceId = id != null ? id : "";
    }

    // Siempre configurado porque usa el backend
    public boolean isConfigured() {
        return true;
    }

    public void sendMessage(String userMessage, Callback callback) {
        sendToBackend(userMessage, callback);
    }

    public void sendOneShot(String userMessage, Callback callback) {
        sendToBackend(userMessage, callback);
    }

    private void sendToBackend(String userMessage, Callback callback) {
        executor.execute(() -> {
            try {
                JSONObject payload = new JSONObject();
                payload.put("message", userMessage);
                payload.put("model", model);
                payload.put("deviceId", deviceId);

                RequestBody body = RequestBody.create(payload.toString(), JSON_TYPE);

                Request request = new Request.Builder()
                        .url(BACKEND_URL)
                        .post(body)
                        .build();

                try (Response response = httpClient.newCall(request).execute()) {
                    String responseBody = response.body() != null
                            ? response.body().string() : "";

                    JSONObject json = new JSONObject(responseBody);

                    if (json.has("error")) {
                        callback.onError(json.getString("error"));
                        return;
                    }

                    String reply = json.optString("reply", "");
                    if (reply.isEmpty()) {
                        callback.onError("Respuesta vacía del servidor.");
                    } else {
                        callback.onSuccess(reply);
                    }
                }

            } catch (IOException e) {
                callback.onError("Sin conexión. Verifica tu internet.");
            } catch (JSONException e) {
                callback.onError("Error procesando la respuesta.");
            } catch (Exception e) {
                callback.onError("Error: " + e.getMessage());
            }
        });
    }

    public void clearHistory() {
        // historial manejado por el backend
    }



    public static class ApiException extends Exception {
        public ApiException(String message) {
            super(message);
        }
    }
}

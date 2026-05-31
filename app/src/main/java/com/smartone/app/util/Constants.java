package com.smartone.app.util;

public final class Constants {

    private Constants() {}

    public static final String CLAUDE_BASE_URL    = "https://api.anthropic.com/v1/messages";
    public static final String CLAUDE_API_VERSION = "2023-06-01";
    public static final String CLAUDE_MODEL_FAST  = "claude-haiku-4-5-20251001";
    public static final String CLAUDE_MODEL_PRO   = "claude-sonnet-4-6";
    public static final int    CLAUDE_MAX_TOKENS  = 1024;

    public static final String PREFS_NAME         = "smartone_prefs";
    public static final String KEY_API_KEY        = "api_key";
    public static final String KEY_MODEL          = "selected_model";
    public static final String KEY_THEME          = "theme";
    public static final String KEY_AUTO_SAVE      = "auto_save";
    public static final String KEY_FONT_SIZE      = "font_size";
    public static final String KEY_FIRST_LAUNCH   = "first_launch";
    public static final String KEY_MESSAGES_USED  = "messages_used";
    public static final String KEY_MESSAGES_DATE  = "messages_date";

    public static final String THEME_DARK         = "dark";
    public static final String THEME_LIGHT        = "light";
    public static final String THEME_SYSTEM       = "system";

    public static final int    FONT_SIZE_SMALL    = 11;
    public static final int    FONT_SIZE_MEDIUM   = 13;
    public static final int    FONT_SIZE_LARGE    = 15;

    public static final String HISTORY_TYPE_JSON  = "json";
    public static final String HISTORY_TYPE_CHAT  = "chat";
    public static final int    HISTORY_MAX_ITEMS  = 200;

    public static final int    FREE_MESSAGES_LIMIT = 15;

    public static final String EXTRA_JSON_CONTENT = "extra_json_content";
    public static final String EXTRA_HISTORY_ID   = "extra_history_id";
    public static final String EXTRA_OPEN_CHAT    = "extra_open_chat";

    public static final String DB_NAME            = "smartone.db";
    public static final int    DB_VERSION         = 1;

    public static final String SYSTEM_PROMPT =
        "Eres un asistente técnico integrado en SmartOne, una app Android para " +
        "desarrolladores. Puedes analizar JSON, detectar errores de sintaxis, " +
        "describir estructuras de datos, responder preguntas técnicas sobre APIs " +
        "y ayudar a depurar código. Responde siempre en español, de forma clara " +
        "y concisa. Si el usuario comparte JSON inválido, explica exactamente " +
        "qué línea falla y cómo corregirla.";
}
// ok

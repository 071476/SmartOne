package com.smartone.app.parser;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class JsonParser {

    public static class ParseResult {
        public final boolean  isValid;
        public final String   formatted;
        public final String   errorMessage;
        public final int      errorLine;
        public final int      errorColumn;
        public final JsonInfo info;

        private ParseResult(String formatted, JsonInfo info) {
            this.isValid      = true;
            this.formatted    = formatted;
            this.errorMessage = null;
            this.errorLine    = -1;
            this.errorColumn  = -1;
            this.info         = info;
        }

        private ParseResult(String errorMessage, int errorLine, int errorColumn) {
            this.isValid      = false;
            this.formatted    = null;
            this.errorMessage = errorMessage;
            this.errorLine    = errorLine;
            this.errorColumn  = errorColumn;
            this.info         = null;
        }
    }

    public static class JsonInfo {
        public final String type;
        public final int    fieldCount;
        public final int    depth;
        public final int    arraySize;
        public final int    charCount;

        public JsonInfo(String type, int fieldCount,
                        int depth, int arraySize, int charCount) {
            this.type       = type;
            this.fieldCount = fieldCount;
            this.depth      = depth;
            this.arraySize  = arraySize;
            this.charCount  = charCount;
        }

        public String getSummary() {
            if ("array".equals(type)) {
                return "Array · " + arraySize + " elementos · profundidad " + depth;
            }
            return "Objeto · " + fieldCount + " campos · profundidad " + depth;
        }
    }

    public static ParseResult parse(String input) {
        if (input == null || input.trim().isEmpty()) {
            return new ParseResult("El contenido está vacío.", 1, 1);
        }
        String trimmed = input.trim();
        try {
            if (trimmed.startsWith("{")) {
                JSONObject obj = new JSONObject(trimmed);
                String formatted = obj.toString(2);
                JsonInfo info = extractObjectInfo(obj, trimmed.length());
                return new ParseResult(formatted, info);
            } else if (trimmed.startsWith("[")) {
                JSONArray arr = new JSONArray(trimmed);
                String formatted = arr.toString(2);
                JsonInfo info = extractArrayInfo(arr, trimmed.length());
                return new ParseResult(formatted, info);
            } else {
                return new ParseResult(
                        "El contenido no comienza con { ni [.",
                        1, 1);
            }
        } catch (JSONException e) {
            return buildErrorResult(e.getMessage(), input);
        }
    }

    public static String format(String input) {
        try {
            String trimmed = input.trim();
            if (trimmed.startsWith("{")) return new JSONObject(trimmed).toString(2);
            if (trimmed.startsWith("[")) return new JSONArray(trimmed).toString(2);
        } catch (JSONException ignored) {}
        return input;
    }

    public static String minify(String input) {
        try {
            String trimmed = input.trim();
            if (trimmed.startsWith("{")) return new JSONObject(trimmed).toString();
            if (trimmed.startsWith("[")) return new JSONArray(trimmed).toString();
        } catch (JSONException ignored) {}
        return input;
    }

    public static List<JsonLine> buildLines(String rawJson, int errorLine) {
        String[] parts = rawJson.split("\\r?\\n", -1);
        List<JsonLine> lines = new ArrayList<>(parts.length);
        for (int i = 0; i < parts.length; i++) {
            int lineNumber = i + 1;
            boolean hasError = lineNumber == errorLine;
            lines.add(new JsonLine(lineNumber, parts[i], hasError));
        }
        return lines;
    }

    public static class JsonLine {
        public final int     lineNumber;
        public final String  content;
        public final boolean hasError;

        public JsonLine(int lineNumber, String content, boolean hasError) {
            this.lineNumber = lineNumber;
            this.content    = content;
            this.hasError   = hasError;
        }
    }

    private static JsonInfo extractObjectInfo(JSONObject obj, int charCount) {
        int fieldCount = obj.length();
        int depth      = calculateDepth(obj);
        return new JsonInfo("object", fieldCount, depth, 0, charCount);
    }

    private static JsonInfo extractArrayInfo(JSONArray arr, int charCount) {
        int arraySize = arr.length();
        int depth     = 1 + calculateArrayDepth(arr);
        return new JsonInfo("array", 0, depth, arraySize, charCount);
    }

    private static int calculateDepth(JSONObject obj) {
        int maxDepth = 1;
        Iterator<String> keys = obj.keys();
        while (keys.hasNext()) {
            try {
                Object value = obj.get(keys.next());
                if (value instanceof JSONObject) {
                    maxDepth = Math.max(maxDepth, 1 + calculateDepth((JSONObject) value));
                } else if (value instanceof JSONArray) {
                    maxDepth = Math.max(maxDepth, 1 + calculateArrayDepth((JSONArray) value));
                }
            } catch (JSONException ignored) {}
        }
        return maxDepth;
    }

    private static int calculateArrayDepth(JSONArray arr) {
        int maxDepth = 1;
        for (int i = 0; i < arr.length(); i++) {
            try {
                Object value = arr.get(i);
                if (value instanceof JSONObject) {
                    maxDepth = Math.max(maxDepth, 1 + calculateDepth((JSONObject) value));
                } else if (value instanceof JSONArray) {
                    maxDepth = Math.max(maxDepth, 1 + calculateArrayDepth((JSONArray) value));
                }
            } catch (JSONException ignored) {}
        }
        return maxDepth;
    }

    private static ParseResult buildErrorResult(String rawMessage, String rawJson) {
        int line   = 1;
        int column = 1;
        if (rawMessage != null) {
            Matcher matcher = Pattern
                    .compile("line\\s+(\\d+)\\s+column\\s+(\\d+)")
                    .matcher(rawMessage);
            if (matcher.find()) {
                line   = safeInt(matcher.group(1), 1);
                column = safeInt(matcher.group(2), 1);
            }
        }
        return new ParseResult(cleanErrorMessage(rawMessage), line, column);
    }

    private static String cleanErrorMessage(String raw) {
        if (raw == null || raw.trim().isEmpty()) return "Error de sintaxis.";
        return raw.replace("com.google.gson.JsonSyntaxException:", "")
                  .replace("java.lang.IllegalStateException:", "")
                  .trim();
    }

    private static int safeInt(String value, int fallback) {
        try {
            return Integer.parseInt(value);
        } catch (Exception e) {
            return fallback;
        }
    }
}

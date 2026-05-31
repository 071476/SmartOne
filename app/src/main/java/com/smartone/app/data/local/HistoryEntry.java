package com.smartone.app.data.local;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;
import com.smartone.app.util.Constants;

@Entity(tableName = "history_entries")
public class HistoryEntry {

    @PrimaryKey(autoGenerate = true)
    public long id;

    @ColumnInfo(name = "type")
    public String type;

    @ColumnInfo(name = "title")
    public String title;

    @ColumnInfo(name = "content")
    public String content;

    @ColumnInfo(name = "preview")
    public String preview;

    @ColumnInfo(name = "timestamp")
    public long timestamp;

    @ColumnInfo(name = "is_favorite")
    public boolean isFavorite;

    @ColumnInfo(name = "file_name")
    public String fileName;

    @ColumnInfo(name = "char_count")
    public int charCount;

    public HistoryEntry(String type, String title, String content) {
        this.type       = type;
        this.title      = title;
        this.content    = content;
        this.preview    = buildPreview(content);
        this.timestamp  = System.currentTimeMillis();
        this.isFavorite = false;
        this.fileName   = "";
        this.charCount  = content != null ? content.length() : 0;
    }

    public static HistoryEntry fromJson(String fileName, String jsonContent) {
        HistoryEntry entry = new HistoryEntry(
                Constants.HISTORY_TYPE_JSON,
                fileName,
                jsonContent
        );
        entry.fileName = fileName;
        return entry;
    }

    public static HistoryEntry fromChat(String userMessage, String assistantResponse) {
        String title = userMessage.length() > 50
                ? userMessage.substring(0, 47) + "..."
                : userMessage;
        return new HistoryEntry(
                Constants.HISTORY_TYPE_CHAT,
                title,
                assistantResponse
        );
    }

    public boolean isJson() {
        return Constants.HISTORY_TYPE_JSON.equals(type);
    }

    public boolean isChat() {
        return Constants.HISTORY_TYPE_CHAT.equals(type);
    }

    private static String buildPreview(String content) {
        if (content == null || content.isEmpty()) return "";
        String flat = content.replaceAll("\\s+", " ").trim();
        return flat.length() > 80 ? flat.substring(0, 77) + "..." : flat;
    }

    public String getRelativeTime() {
        long diff    = System.currentTimeMillis() - timestamp;
        long minutes = diff / 60_000;
        long hours   = diff / 3_600_000;
        long days    = diff / 86_400_000;

        if (minutes < 1)  return "ahora";
        if (minutes < 60) return "hace " + minutes + " min";
        if (hours < 24)   return "hace " + hours + " h";
        if (days == 1)    return "ayer";
        return "hace " + days + " días";
    }
}

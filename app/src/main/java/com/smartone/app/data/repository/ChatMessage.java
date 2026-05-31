package com.smartone.app.data.repository;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.UUID;

public class ChatMessage {

    public enum Role {
        USER, ASSISTANT, ERROR, SYSTEM
    }

    public final String id;
    public final String content;
    public final Role   role;
    public final long   timestamp;

    private ChatMessage(String content, Role role) {
        this.id        = UUID.randomUUID().toString();
        this.content   = content;
        this.role      = role;
        this.timestamp = System.currentTimeMillis();
    }

    public static ChatMessage fromUser(String content) {
        return new ChatMessage(content, Role.USER);
    }

    public static ChatMessage fromAssistant(String content) {
        return new ChatMessage(content, Role.ASSISTANT);
    }

    public static ChatMessage fromError(String content) {
        return new ChatMessage(content, Role.ERROR);
    }

    public static ChatMessage system(String content) {
        return new ChatMessage(content, Role.SYSTEM);
    }

    public boolean isUser() {
        return role == Role.USER;
    }

    public boolean isAssistant() {
        return role == Role.ASSISTANT;
    }

    public boolean isError() {
        return role == Role.ERROR;
    }

    public boolean isSystem() {
        return role == Role.SYSTEM;
    }

    public boolean isAlignedRight() {
        return role == Role.USER;
    }

    public String getFormattedTime() {
        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm", Locale.getDefault());
        return sdf.format(new Date(timestamp));
    }

    public String getPreview(int maxChars) {
        if (content == null || content.isEmpty()) return "";
        return content.length() > maxChars
                ? content.substring(0, maxChars - 3) + "..."
                : content;
    }

    public static ChatMessage welcome() {
        return system(
            "Hola. Soy tu asistente en SmartOne.\n\n" +
            "Puedo ayudarte a:\n" +
            "• Analizar y corregir archivos JSON\n" +
            "• Responder preguntas técnicas sobre APIs\n" +
            "• Revisar código y estructuras de datos\n\n" +
            "¿En qué trabajamos hoy?"
        );
    }
}

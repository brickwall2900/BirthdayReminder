package com.github.brickwall2900.birthdays;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.stream.JsonReader;

import java.io.*;
import java.util.*;

public class TranslatableText {
    public static final Map<String, JsonElement> TEXT;

    static {
        Locale locale = Locale.getDefault();
        String tag = locale.toLanguageTag();
        try (JsonReader reader = new JsonReader(new InputStreamReader(
                Objects.requireNonNull(
                        TranslatableText.class.getResourceAsStream("/lang/" + tag + ".json"),
                        "Cannot find language file!")))) {
            JsonElement element = JsonParser.parseReader(reader);
            JsonObject object = element.getAsJsonObject();
            TEXT = object.asMap();
        } catch (IOException e) {
            throw new IllegalStateException("Language file can't be read: " + tag);
        }
    }

    public static String text(String text) {
        JsonElement element = TEXT.get(text);
        if (element == null) {
            return text;
        }

        if (element.isJsonNull()) {
            return text;
        }

        return element.getAsString();
    }

    public static String text(String text, Object... formatting) {
        return text(text).formatted(formatting);
    }

    public static String[] getArray(String text) {
        JsonElement element = TEXT.get(text);
        if (element == null) {
            return null;
        }

        if (element.isJsonNull() || !element.isJsonArray()) {
            return null;
        }

        JsonArray array = element.getAsJsonArray();
        return array.asList().stream().map(JsonElement::getAsString).toList().toArray(new String[0]);
    }

    public static String friendlyException(Throwable t) {
        Queue<Throwable> causeChain = new ArrayDeque<>();
        Throwable tmpCause = t.getCause();
        while (tmpCause != null) {
            causeChain.offer(tmpCause);
            tmpCause = tmpCause.getCause();
        }
        causeChain.offer(t);
        while (true) {
            tmpCause = causeChain.poll();
            if (tmpCause != null) {
                String key = "exceptions." + tmpCause.getClass().getName();
                String translated = text(key);
                if (!translated.equals(key)) {
                    String[] cases = translated.split(":::");
                    for (String c : cases) {
                        String sub = c.substring(1, c.length() - 1);
                        String[] split = sub.split("\\{:\\}");
                        String regex = null, newMessage;
                        if (split.length == 2) {
                            regex = split[0];
                            newMessage = split[1];
                        } else {
                            newMessage = split[0];
                        }
                        String message = tmpCause.getMessage();
                        if (message != null && regex != null && message.matches(regex)) {
                            return newMessage;
                        } else if (regex == null && split.length == 1) { // generic message
                            return newMessage;
                        }
                    }
                }
            } else {
                break;
            }
        }
        return t.toString();
    }
}
